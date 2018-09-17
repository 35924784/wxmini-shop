package com.mingyue.restapi.order;

import static com.mongodb.client.model.Filters.eq;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.mingyue.common.CacheHelper;
import com.mingyue.common.DateHelper;
import com.mingyue.common.ValidateUtils;
import com.mingyue.database.MongoDBUtil;
import com.mingyue.restapi.ReturnMessage;
import com.mingyue.restapi.goods.Goods;
import com.mingyue.restapi.goods.GoodsService;
import com.mingyue.restapi.user.User;
import com.mingyue.restapi.user.UserExtInfo;
import com.mingyue.restapi.user.UserService;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;

@Path("/restapi/orderfee")
public class OrderFeeService{

    private static Logger LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    private static int default_shipping_fee = 500;   //单位分

    private static volatile int seq = 0; 

    //算钱 加起来多少钱
    public static GroovyScriptEngine engine ;

    static {
        try {
            engine = new GroovyScriptEngine("groovy/");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    //计算订单费用  如使用积分 满减 打折 特定商品减免 满多少减邮费  之类的
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage calcOrderFee(@CookieParam("my_session") String sessionid,Order request){
        User userinfo = UserService.getUserInfoBySession(sessionid);
        MongoDatabase database = MongoDBUtil.getDataBase();
        final MongoCollection<UserExtInfo> collection = database.getCollection("userext", UserExtInfo.class);
        UserExtInfo userext = collection.find(eq("userid", userinfo.getUserid())).first(); //待上缓存

        String time = DateHelper.getDateString("yyMMddhhmmss");
        String orderid = time + String.format("%03d", seq);
        seq++;
        OrderFee orderfee =  new OrderFee(orderid);

        int originPrice = calcTotalPrice(request.getOrderItemList()); //原始价格
        orderfee.setOriginPrice(originPrice);
        orderfee.setTotalPrice(originPrice + default_shipping_fee);
        orderfee.setShippingFee(default_shipping_fee);

        Binding binding = new Binding();
        binding.setVariable("order", request);
        binding.setVariable("userinfo", userinfo);
        binding.setVariable("userext", userext);
        binding.setVariable("orderfee", orderfee);

        try {
			engine.run("OrderFee.groovy", binding);
		} catch (Exception e) {
            LOG.error("执行脚本算费失败，执行基本算费模式");
            orderfee.setOriginPrice(originPrice);
            orderfee.setTotalPrice(originPrice + default_shipping_fee);
            orderfee.setShippingFee(default_shipping_fee);
			e.printStackTrace();
        }

        //将算费信息 及算费对应的订单请求 存入缓存 提交订单时 以订单号为准，从缓存中取订单信息
        org.ehcache.Cache<String,OrderFee> orderFeeCache = CacheHelper.getOrderFeeCache();
        orderFeeCache.put(orderid, orderfee);
        org.ehcache.Cache<String,Order> orderRequestCache = CacheHelper.getOrderRequestCache();
        orderRequestCache.put(orderid, request);

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("orderfee", orderfee);
        result.retParams.put("userScore", userext.getScore());  //可用积分也返回给客户端

        return result;
    }

    //基础算费  计算原始费用
    private int calcTotalPrice(List<OrderItem> orderItemList){
        int fee = 0;
        if(!ValidateUtils.isNotEmptyCollection(orderItemList)){
            return fee;
        }

        GoodsService gService = new GoodsService();

        for(OrderItem item : orderItemList){
            Goods thegoods = gService.getGoods(item.getGoodscode());

            //如果是默认的就取商品默认价格
            if (item.getSpec().equals("default")) {
                item.setPrice(thegoods.getPrice());
            } else {
                // 查找spec，计算规格的价格
                for (List<String> spec : thegoods.getSpecs()) {
                    if (item.getSpec().equals(spec.get(0))) {
                        item.setPrice(Integer.valueOf(spec.get(2))); // 订单项价格更新为规格的价格
                        break;
                    }
                }
            }

            fee = fee +  item.getAmount() * item.getPrice();
            
        }
        return fee;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage queryOrderFee(@QueryParam("orderid") String orderid){
        MongoDatabase database = MongoDBUtil.getDataBase();
        //查询OrderFee
        final MongoCollection<OrderFee> feecollection = database.getCollection("orderfee", OrderFee.class);
        OrderFee orderfee = feecollection.find(eq("orderid", orderid)).first();

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("orderfee", orderfee);
        return result;
    }

}