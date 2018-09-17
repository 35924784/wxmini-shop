package com.mingyue.task;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.in;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.mingyue.common.ValidateUtils;
import com.mingyue.database.MongoDBUtil;
import com.mingyue.restapi.goods.Goods;
import com.mingyue.restapi.order.OrderItem;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.util.DateUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

//定时任务清理一些长时间未提交的购物车 以及商品下架后购物车中仍然存在的订单
public class ClearCartJob implements Job {

    private static Logger LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
	
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOG.info(Thread.currentThread().getName() + " test job begin " + DateUtil.formatDate(new Date()));
        LOG.info("Begin to clear cart.");
        MongoDatabase database = MongoDBUtil.getDataBase();
        final MongoCollection<OrderItem> collection = database.getCollection("orderItem", OrderItem.class);
        final MongoCollection<OrderItem> expCollection = database.getCollection("orderItemexp", OrderItem.class);

        //清理超过一个月未提交的
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.MONTH, -1);
        Date monthbefore = c.getTime();   //一个月之前

        FindIterable<OrderItem> findIterable = collection.find(lt("createDate", monthbefore)).limit(30);
        MongoCursor<OrderItem> cursor = findIterable.iterator();

        
        List<OrderItem> orderItemList = new ArrayList<OrderItem>();

        try {
            while (cursor.hasNext()) {
                OrderItem item = cursor.next();
                orderItemList.add(item); 
                collection.deleteOne(eq("itemid",item.getItemid()));
                LOG.info("delete otderitem:"+ item.getItemid());
            }
        } finally {
            cursor.close();
        }

        if(orderItemList.size()>0){
            expCollection.insertMany(orderItemList);
        }

        //清除下架的
        final MongoCollection<Goods> goodscollection = database.getCollection("goods", Goods.class);
        FindIterable<Goods> goodsIterable = goodscollection.find(eq("status", "D"));

        MongoCursor<Goods> goodscursor = goodsIterable.iterator();

        List<String> goodscode = new ArrayList<String>();
        try {
            while (goodscursor.hasNext()) {
                Goods goods = goodscursor.next();
                goodscode.add(goods.getCode());
            }
        } finally {
            goodscursor.close();
        }
        
        List<OrderItem> invalidItemList = new ArrayList<OrderItem>();

        if (ValidateUtils.isNotEmptyCollection(goodscode)) {
            FindIterable<OrderItem> invaIterable = collection.find(in("goodscode", goodscode)).limit(30);
            MongoCursor<OrderItem> invalid_cursor = invaIterable.iterator();
            try {
                while (invalid_cursor.hasNext()) {
                    OrderItem item = invalid_cursor.next();
                    invalidItemList.add(item); 
                    collection.deleteOne(eq("itemid",item.getItemid()));
                    LOG.info("delete otderitem:"+ item.getItemid());
                }
            } finally {
                invalid_cursor.close();
            }

            if(invalidItemList.size() > 0){
                expCollection.insertMany(invalidItemList);
            }
        }

    }
}