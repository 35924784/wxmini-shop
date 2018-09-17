package com.mingyue.restapi.message;

import static com.mongodb.client.model.Filters.eq;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import com.alibaba.fastjson.JSONObject;
import com.mingyue.common.AccessTokenUtil;
import com.mingyue.common.DateHelper;
import com.mingyue.database.MongoDBUtil;
import com.mingyue.restapi.order.Order;
import com.mingyue.restapi.order.OrderFee;
import com.mingyue.restapi.user.User;
import com.mingyue.restapi.user.UserService;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class TemplateMessageService {

    private static Logger LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    private static final String templateMessageUrl = "https://api.weixin.qq.com/cgi-bin/message/wxopen/template/send?access_token=ACCESS_TOKEN";
    
    //发送订单支付成功通知 --- 暂未使用  B0HDrv-P81GgqnecvYSQOyLKepvJKNwLdGLesRoeqbU
    public static void sendPaySuccessMessage(Order Order){
        //不写了。。。
    }


    //发送订单发货提醒  6QSQvNI77qCCyWDbK1HJYHC_Jfot14O-XCmGp3_OaxQ
    public static void sendDeliveryMessage(String orderid){
        //根据订单号查询到订单
        MongoDatabase database = MongoDBUtil.getDataBase();
        final MongoCollection<Order> collection = database.getCollection("order", Order.class);
        final Order findOrder =  collection.find(eq("orderid", orderid)).first();

        //查询到用户信息
        String userid = findOrder.getUserid();
        User findUser = UserService.getUserInfoByUserid(userid);

        //查询到费用信息
        final MongoCollection<OrderFee> feecollection = database.getCollection("orderfee", OrderFee.class);
        OrderFee orderfee = feecollection.find(eq("orderid", orderid)).first();

        if(StringUtils.isEmpty(orderfee.getPrepayid())){
            LOG.error("订单未支付，没有prepayid，无法下发模板消息");
            return;
        }

        Map<String,Object> messageMap = new HashMap<String,Object>();
        messageMap.put("touser",findUser.getOpenid());
        messageMap.put("template_id","6QSQvNI77qCCyWDbK1HJYHC_Jfot14O-XCmGp3_OaxQ");
        messageMap.put("form_id",orderfee.getPrepayid());
        
        Map<String,Map<String,String>> paraMap = new HashMap<String,Map<String,String>>();
        paraMap.put("keyword1", new HashMap<String,String>(){{put("value",findOrder.getOrderid());}});
        paraMap.put("keyword2", new HashMap<String,String>(){{put("value",findOrder.getLogisticCompany());}});
        paraMap.put("keyword3", new HashMap<String,String>(){{put("value",findOrder.getLogisticNumber());}});
        paraMap.put("keyword4", new HashMap<String,String>(){{put("value",findOrder.getAddrinfo().getAddrDetail());}});

        LOG.info(JSONObject.toJSONString(paraMap));
        messageMap.put("data",paraMap);

        String access_token = AccessTokenUtil.getAccessToken();
        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget restTarget = client.target(templateMessageUrl.replaceFirst("ACCESS_TOKEN", access_token));

        Response response = restTarget.request().post(Entity.json(JSONObject.toJSONString(messageMap)));;
        String value = response.readEntity(String.class);
        LOG.info(value);
        response.close();
    }

    //发送订单完成通知  Mas1BfRIkTiz6xZpLkgO6Rkb29tB7L9jaLUEWtzO22o
    public static void sendOrderCompleteMessage(String orderid){
        //根据订单号查询到订单
        MongoDatabase database = MongoDBUtil.getDataBase();
        final MongoCollection<Order> collection = database.getCollection("order", Order.class);
        final Order findOrder =  collection.find(eq("orderid", orderid)).first();

        //查询到用户信息
        String userid = findOrder.getUserid();
        User findUser = UserService.getUserInfoByUserid(userid);

        //查询到费用信息
        final MongoCollection<OrderFee> feecollection = database.getCollection("orderfee", OrderFee.class);
        OrderFee orderfee = feecollection.find(eq("orderid", orderid)).first();

        if(StringUtils.isEmpty(orderfee.getPrepayid())){
            LOG.error("订单未支付，没有prepayid，无法下发模板消息");
            return;
        }

        Map<String,Object> messageMap = new HashMap<String,Object>();
        messageMap.put("touser",findUser.getOpenid());
        messageMap.put("template_id","Mas1BfRIkTiz6xZpLkgO6Rkb29tB7L9jaLUEWtzO22o");
        messageMap.put("form_id",orderfee.getPrepayid());
        
        Map<String,Map<String,String>> paraMap = new HashMap<String,Map<String,String>>();
        paraMap.put("keyword1", new HashMap<String,String>(){{put("value",findOrder.getOrderid());}});
        paraMap.put("keyword2", new HashMap<String,String>(){{put("value",DateHelper.getDateString("yyyy-MM-dd hh:mm:ss"));}});
        
        paraMap.put("keyword3", new HashMap<String, String>() {
            {
                put("value",
                        findOrder.getOrderItemList().size() > 1
                                ? findOrder.getOrderItemList().get(0).getGoodsname() + "等"
                                : findOrder.getOrderItemList().get(0).getGoodsname());
            }
        });
        paraMap.put("keyword4", new HashMap<String,String>(){{put("value","您的订单已完成，欢迎再次光临。");}});

        LOG.info(JSONObject.toJSONString(paraMap));
        messageMap.put("data",paraMap);

        String access_token = AccessTokenUtil.getAccessToken();
        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget restTarget = client.target(templateMessageUrl.replaceFirst("ACCESS_TOKEN", access_token));

        Response response = restTarget.request().post(Entity.json(JSONObject.toJSONString(messageMap)));;
        String value = response.readEntity(String.class);
        LOG.info(value);
        response.close();
    }


}