package com.mingyue.restapi.order;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Sorts.descending;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.alibaba.fastjson.JSONObject;
import com.mingyue.common.CacheHelper;
import com.mingyue.common.ValidateUtils;
import com.mingyue.database.MongoDBUtil;
import com.mingyue.restapi.ReturnMessage;
import com.mingyue.restapi.address.Address;
import com.mingyue.restapi.message.TemplateMessageService;
import com.mingyue.restapi.user.User;
import com.mingyue.restapi.user.UserExtService;
import com.mingyue.restapi.user.UserService;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

@Path("/restapi/order")
public class OrderService {

    private static MongoCollection<Order> collection = MongoDBUtil.getDataBase().getCollection("order", Order.class);

    private static Logger LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    // 提交订单
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage submitOrder(@CookieParam("my_session") String sessionid, Order request) {
        LOG.debug(sessionid);
        LOG.info(JSONObject.toJSONString(request));
        User userinfo = UserService.getUserInfoBySession(sessionid);

        if (request.getOrderid() == null) {
            return new ReturnMessage("999", "请求无效。");
        }

        String orderid = request.getOrderid();

        // 增加逻辑，从缓存中查询不到算费时缓存订单信息时，提示 请从购物车界面重新进入结算页面并提交
        org.ehcache.Cache<String, OrderFee> orderFeeCache = CacheHelper.getOrderFeeCache();
        org.ehcache.Cache<String, Order> orderRequestCache = CacheHelper.getOrderRequestCache();
        OrderFee orderfee = orderFeeCache.get(orderid);
        Order orderCache = orderRequestCache.get(orderid);

        if (orderfee == null || orderCache == null) {
            return new ReturnMessage("999", "查询不到订单信息，请从购物车界面重新进入结算界面提交。");
        }

        request.setOrderItemList(orderCache.getOrderItemList());
        // 针对物流费 前台后台都增加判断 是信阳市的如果有物流费就免除物流费
        if (request.getAddrinfo().getAddrDetail().contains("信阳") && orderfee.getShippingFee() != 0) {
            orderfee.setShippingFee(0);
        }

        orderfee.setTotalPrice(orderfee.getOriginPrice() - orderfee.getDiscountAmount() + orderfee.getShippingFee()
                - orderfee.getScore() * 100);
        // 算费逻辑待继续补充
        request.setTotalPrice(orderfee.getTotalPrice());

        // request 需要对request补充一些信息
        request.setUserid(userinfo.getUserid());
        request.setEvaluated(false);
        request.setStatus("0"); // 初始提交状态
        request.setStatusDate(new Date());
        request.setCreateDate(new Date()); // 订单创建时间
        request.setHasPaid(false);
        request.setRefundStatus(null);

        collection.insertOne(request);
        orderRequestCache.remove(orderid); // 清除订单请求的缓存

        // 插入OrderFee
        final MongoCollection<OrderFee> feecollection = MongoDBUtil.getDataBase().getCollection("orderfee",
                OrderFee.class);
        feecollection.insertOne(orderfee);

        if (orderfee.getScore() > 0) { // 记录积分使用情况
            UserExtService.changeScore(userinfo.getUserid(), -orderfee.getScore());
        }

        // 清空购物车
        final MongoCollection<OrderItem> cartcollection = MongoDBUtil.getDataBase().getCollection("orderItem",
                OrderItem.class);
        cartcollection.deleteMany(eq("userid", userinfo.getUserid()));

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("orderid", request.getOrderid());
        return result;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage getUserOrderList(@CookieParam("my_session") String sessionid,
            @QueryParam("pageSize") int pageSize, @QueryParam("curPage") int curPage,
            @QueryParam("status") String status) {
        User userinfo = UserService.getUserInfoBySession(sessionid);

        return getOrderListByUserid(userinfo.getUserid(), pageSize, curPage, status);
    }

    // 供管理后台使用
    @GET
    @Path("/admin/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage getOrderList(@PathParam("userid") String userid, @QueryParam("pageSize") int pageSize,
            @QueryParam("curPage") int curPage, @QueryParam("status") String status) {
        ReturnMessage result = getOrderListByUserid(userid, pageSize, curPage, status);

        return result;
    }

    private ReturnMessage getOrderListByUserid(String userid, int pageSize, int curPage, String status) {

        long total = 0;
        List<Order> orderList = new ArrayList<Order>();
        FindIterable<Order> findIterable;

        if (!"all".equals(status) && status != null) {
            total = collection.count(and(eq("userid", userid), eq("status", status)));
            findIterable = collection.find(and(eq("userid", userid), eq("status", status)))
                    .skip((curPage - 1) * pageSize).limit(pageSize).sort(descending("createDate"));
        } else {
            total = collection.count(eq("userid", userid));
            findIterable = collection.find(eq("userid", userid)).skip((curPage - 1) * pageSize).limit(pageSize)
                    .sort(descending("createDate"));
        }

        if (total > 0) {
            orderList = MongoDBUtil.getListFromFindIterable(findIterable);
        }

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("orderList", orderList);
        result.retParams.put("total", total);
        return result;
    }

    // 供管理后台使用,根据查询条件查询所有订单 查询条件 状态 用户+状态 时间
    @POST
    @Path("/admin/all")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage getOrderList(Map<String, String> request) {
        Bson filters = exists("orderid");

        if (ValidateUtils.isNotEmptyString(request.get("status"))) {
            filters = and(filters, eq("status", request.get("status")));
        }
        if (ValidateUtils.isNotEmptyString(request.get("starttime"))) {
            Long starttime = Long.valueOf(request.get("starttime"));
            filters = and(filters, gt("createDate", new Date(starttime)));
        }
        if (ValidateUtils.isNotEmptyString(request.get("endtime"))) {
            Long endtime = Long.valueOf(request.get("endtime"));
            filters = and(filters, lt("createDate", new Date(endtime)));
        }
        if (ValidateUtils.isNotEmptyString(request.get("nickName"))) {
            List<User> userlist = UserService.getUserInfoByUsername(request.get("nickName"));
            List<String> userids = new ArrayList<String>();

            for (User user : userlist) {
                userids.add(user.getUserid());
            }
            filters = and(filters, in("userid", userids));
        }

        int curPage = Integer.valueOf(request.get("curPage") == null ? "1" : request.get("curPage"));
        int pageSize = Integer.valueOf(request.get("pageSize") == null ? "10" : request.get("pageSize"));

        FindIterable<Order> findIterable = collection.find(filters).skip((curPage - 1) * pageSize)
                .sort(descending("createDate")).limit(pageSize);
        ;
        Long total = collection.count(filters);
        List<Order> orderList = MongoDBUtil.getListFromFindIterable(findIterable);

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("orderList", orderList);
        result.retParams.put("total", total);
        return result;
    }

    @GET
    @Path("/{orderid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage getOneOrderDetail(@CookieParam("my_session") String sessionid,
            @PathParam("orderid") String orderid) {
        User userinfo = UserService.getUserInfoBySession(sessionid);

        Order findOrder = collection.find(and(eq("userid", userinfo.getUserid()), eq("orderid", orderid))).first();

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("orderinfo", findOrder);
        result.retParams.put("userinfo", userinfo);
        return result;
    }

    // 暂不提供删除方法 所有订单数据都要保留 备份
    @DELETE
    @Path("/{orderid}")
    public ReturnMessage deleteOrder(@CookieParam("my_session") String sessionid,
            @PathParam("orderid") String orderid) {

        return new ReturnMessage();
    }

    // 取消订单 状态更新为9
    @POST
    @Path("/cancel/{orderid}")
    public ReturnMessage cancelOrder(@CookieParam("my_session") String sessionid,
            @PathParam("orderid") String orderid) {

        if (orderid == null) {
            return new ReturnMessage("901", "订单号为空");
        }

        User userinfo = UserService.getUserInfoBySession(sessionid);

        final MongoCollection<OrderFee> feecollection = MongoDBUtil.getDataBase().getCollection("orderfee",
                OrderFee.class);
        OrderFee orderfee = feecollection.find(eq("orderid", orderid)).first();

        Document update = new Document();
        update.put("status", "9");
        update.put("statusDate", new Date());

        collection.updateOne(eq("orderid", orderid), new Document("$set", update));

        if (orderfee.getScore() > 0) {
            UserExtService.changeScore(userinfo.getUserid(), orderfee.getScore());
        }
        return new ReturnMessage();
    }

    // 更新订单信息 支持更新状态 物流信息 地址信息 备注信息
    public static void updateOrder(Map<String, Object> request) throws Exception {
        String orderid = (String) request.get("orderid");
        if (orderid == null) {
            throw new Exception("订单号不能为空");
        }

        Document update = new Document();
        if (request.get("status") != null) {
            update.put("status", (String) request.get("status"));
            update.put("statusDate", new Date());
        }
        if (request.get("remark") != null) {
            update.put("remark", (String) request.get("remark"));
        }
        if (request.get("hasPaid") != null) {
            update.put("hasPaid", (Boolean) request.get("hasPaid"));
        }
        if (request.get("addrinfo") != null) {
            update.put("addrinfo", (Address) request.get("addrinfo"));
        }
        if (request.get("logisticCompany") != null) {
            update.put("logisticCompany", (String) request.get("logisticCompany"));
        }
        if (request.get("logisticNumber") != null) {
            update.put("logisticNumber", (String) request.get("logisticNumber"));
        }
        if (request.get("refundStatus") != null) {
            update.put("refundStatus", (String) request.get("refundStatus"));
        }

        if (update.isEmpty()) {
            return;
        }

        collection.updateOne(eq("orderid", orderid), new Document("$set", update));
        // 订单状态更新为已发货时，发送模板消息
        if ("2".equals(update.get("status"))) {
            TemplateMessageService.sendDeliveryMessage(orderid);
        }

        if ("3".equals(update.get("status"))) {
            TemplateMessageService.sendOrderCompleteMessage(orderid);
        }

    }

    @POST
    @Path("/admin/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage adminUpdateOrder(Map<String, Object> request) throws Exception {
        String orderid = (String) request.get("orderid");
        if (orderid == null) {
            return new ReturnMessage("999", "请求数据异常");
        }

        updateOrder(request);

        return new ReturnMessage();
    }
}