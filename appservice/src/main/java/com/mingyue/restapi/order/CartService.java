package com.mingyue.restapi.order;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mingyue.common.DateHelper;
import com.mingyue.common.ValidateUtils;
import com.mingyue.database.MongoDBUtil;
import com.mingyue.restapi.ReturnMessage;
import com.mingyue.restapi.goods.Goods;
import com.mingyue.restapi.goods.GoodsService;
import com.mingyue.restapi.user.User;
import com.mingyue.restapi.user.UserService;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("/restapi/cart")
public class CartService {

    private static final GoodsService goodsService = new GoodsService();

    private static Logger LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    private static volatile int seq = 0;

    private static MongoCollection<OrderItem> collection = MongoDBUtil.getDataBase().getCollection("orderItem", OrderItem.class);

    //获取用户购物车信息
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage getUserCart(@CookieParam("my_session") String sessionid) {

        User userinfo = UserService.getUserInfoBySession(sessionid);

        Long totalNum = collection.count(eq("userid", userinfo.getUserid()));
        FindIterable<OrderItem> findIterable = collection.find(eq("userid", userinfo.getUserid())).limit(5);

        MongoCursor<OrderItem> cursor = findIterable.iterator();
        Set<String> codeset =  new HashSet<String>();
        Map<String,Goods> goodsInfoMap = new HashMap<String,Goods>();

        List<OrderItem> orderItemList = new ArrayList<OrderItem>();
        try {
            while (cursor.hasNext()) {
                OrderItem item = cursor.next();
                orderItemList.add(item);
                codeset.add(item.getGoodscode());
            }
        } finally {
            cursor.close();
        }

        if (ValidateUtils.isNotEmptyCollection(codeset)) {
            goodsInfoMap = GoodsService.getAllGoods(codeset);
        }


        //对购物车做处理 原购物车中已下架的商品不返回 原来选中的规格不存在了的改为default，用户愿意提交继续购买就提交不愿意就删
        Iterator<OrderItem> it = orderItemList.iterator();
        while(it.hasNext()){
            OrderItem item = it.next();
            Goods thegoods = goodsInfoMap.get(item.getGoodscode());
            if(thegoods == null || thegoods.getStatus().equals("D")){  //如果不存在或是下架状态，则从列表中删除不返回
                it.remove();
            }

            //找到对应的规格看还在不在，不在就更新为default
            boolean findSpec = false;
            if(ValidateUtils.isNotEmptyCollection(thegoods.getSpecs())){
                
                for(List<String> spec : thegoods.getSpecs()){
                    if(ValidateUtils.isNotEmptyCollection(spec)){
                        if(item.getSpec().equals(spec.get(0))){
                            findSpec = true;
                        }
                    }
                }
            }

            if(findSpec){
                continue;
            }else{
                item.setSpec("default");
                collection.findOneAndReplace(eq("itemid",item.getItemid()), item);
            }
            //更新购物车规格完毕。
        }

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("orderItemList", orderItemList);
        result.retParams.put("goodsInfoMap", goodsInfoMap);
        result.retParams.put("totalNum", totalNum);

        return result;
    }

    //传入3个参数 goodscode、数量、规格编码，剩下的需要补充 价格之类的可能变动，不保存了，查询购物车时根据当前商品信息返回
    //因商品价格可能会调整，因此购物车里的价格随着变
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage addCart(@CookieParam("my_session") String sessionid,Map<String,String> request)
    {
        LOG.debug(sessionid);
        User userinfo = UserService.getUserInfoBySession(sessionid);

        String amount = request.get("num");
        String goodscode = request.get("goodscode");
        String speccode = request.get("spec");   //规格编码
        Goods goods = goodsService.getGoods(goodscode);

        if(goods == null || goods.getStatus().equals("D")){
            return new ReturnMessage("999","无效的商品编码。");
        }

        if(Integer.parseInt(amount) <= 0){
            return new ReturnMessage("997","无效的商品数量。");
        }

        FindIterable<OrderItem> findIterable = collection.find(eq("userid", userinfo.getUserid())).limit(5);
        List<OrderItem> existCartList = MongoDBUtil.getListFromFindIterable(findIterable);

        int totalNum = 0;   //购物车里的商品数量

        for(OrderItem item : existCartList){            
            if(StringUtils.equals(item.getSpec(), speccode) && StringUtils.equals(goodscode, item.getGoodscode())){
                //加入进去
                item.setAmount(item.getAmount() + Integer.parseInt(amount));
                if(item.getAmount() > 20){
                    item.setAmount(20);
                }

                collection.findOneAndReplace(eq("itemid",item.getItemid()), item);
                ReturnMessage result = new ReturnMessage();
                result.retParams.put("itemid", item.getItemid());
                return result;
            } 
            
            totalNum = totalNum + 1;   
        }

        if(totalNum >= 5){
            return new ReturnMessage("998","购物车已满5件商品，不能继续添加。");
        }    

        OrderItem item = new OrderItem();

        String time = DateHelper.getDateString("yyMMddhhmmssSSS");
        String itemid = time + String.format("%05d", seq);
        seq++;

        item.setItemid(itemid);
        item.setUserid(userinfo.getUserid());
        item.setAmount(Integer.parseInt(amount));
        item.setSpec(speccode);
        item.setCreateDate(new Date());
        item.setGoodscode(goodscode);
        item.setGoodsname(goods.getName());
        item.setStatus("0");

        collection.insertOne(item);

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("itemid", itemid);
        return result;
    }


    //需要校验此订单项是否是本人的,非本人的订单项无法修改
    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage updateCart(@CookieParam("my_session") String sessionid,Map<String,String> request)
    {
        User userinfo = UserService.getUserInfoBySession(sessionid);
        //目前只能修改数量
        String itemid = request.get("itemid");
        int amount = new Integer(request.get("num"));

        if(amount <= 0){
            return new ReturnMessage("997","无效的商品数量。");
        }

        OrderItem findItem = 
            collection.find(and(eq("userid", userinfo.getUserid()),eq("itemid", itemid))).first();

        if(findItem == null){
            LOG.info(userinfo.getUserid() + ":" + itemid + " Invalid request");
            return new ReturnMessage("499","非法请求。");
        }

        UpdateResult updateResult = collection.updateOne(and(eq("userid", userinfo.getUserid()),eq("itemid", itemid)),set("amount", amount));
        if(updateResult.getModifiedCount() > 1){
            LOG.error("Something Exception. Need check Manual");
        }

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("itemid", itemid);
        return result;
    }


    @DELETE
    @Path("/{itemid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage delCart(@CookieParam("my_session") String sessionid,@PathParam("itemid") String itemid)
    {
        User userinfo = UserService.getUserInfoBySession(sessionid);

        OrderItem findItem = 
            collection.find(and(eq("userid", userinfo.getUserid()),eq("itemid", itemid))).first();

        if(findItem == null){
            LOG.error(userinfo.getUserid() + ":" + itemid + " Invalid request");
            return new ReturnMessage("499","非法请求。");
        }

        collection.deleteOne(and(eq("userid", userinfo.getUserid()),eq("itemid", itemid)));
        return new ReturnMessage();
    }

    //清空购物车
    @POST
    @Path("/clear")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage clearCart(@CookieParam("my_session") String sessionid)
    {
        User userinfo = UserService.getUserInfoBySession(sessionid);
       
        DeleteResult delresult =  collection.deleteMany(eq("userid", userinfo.getUserid()));
        ReturnMessage result = new ReturnMessage();
        result.retParams.put("delresult", delresult);
        return result;
    }
}