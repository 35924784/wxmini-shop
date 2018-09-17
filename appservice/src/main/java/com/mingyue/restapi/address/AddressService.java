package com.mingyue.restapi.address;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.ne;

import java.util.ArrayList;
import java.util.List;

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
import com.mingyue.database.MongoDBUtil;
import com.mingyue.restapi.ReturnMessage;
import com.mingyue.restapi.user.User;
import com.mingyue.restapi.user.UserService;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

@Path("/restapi/address")
public class AddressService{

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage newAddress(@CookieParam("my_session") String sessionid, Address address)
    {   
        User userinfo = UserService.getUserInfoBySession(sessionid);

        MongoDatabase database = MongoDBUtil.getDataBase();

        String addrid = DateHelper.getDateString("yyyyMMddhhmmssSSS");
        address.setAddrId(addrid);   //取时间字符串
        address.setUserid(userinfo.getUserid());

        final MongoCollection<Address> collection = database.getCollection("address", Address.class);

        if(address.getIsDefault()){
            //如果新插入的地址是默认地址，则需要更新其他地址为非默认
            Document update = new Document();
            update.put("isDefault", false);
            collection.updateMany(eq("userid", userinfo.getUserid()),new Document("$set", update));
        }

        collection.insertOne(address);

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("addrid", addrid);
        return result;
    }

    //供管理后台使用
    @GET
    @Path("/admin/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage getAddrList(@PathParam("userid") String userid) {
        
        List<Address> addrList = getAddrListByUserid(userid);

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("addrList", addrList);
        return result;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage getUserAddrList(@CookieParam("my_session") String sessionid) {
        User userinfo = UserService.getUserInfoBySession(sessionid);

        List<Address> addrList = getAddrListByUserid(userinfo.getUserid());

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("addrList", addrList);
        return result;
    }

    private List<Address> getAddrListByUserid(String userid){
        MongoDatabase database = MongoDBUtil.getDataBase();
        final MongoCollection<Address> collection = database.getCollection("address", Address.class);
        FindIterable<Address> findIterable = collection.find(eq("userid", userid));

        MongoCursor<Address> cursor = findIterable.iterator();

        List<Address> addrList = new ArrayList<Address>();

        try {
            while (cursor.hasNext()) {
                addrList.add(cursor.next());
            }
        } finally {
            cursor.close();
        }

        return addrList;
    }

    @GET
    @Path("/default")
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage getDefaultAddr(@CookieParam("my_session") String sessionid) {

        User userinfo = UserService.getUserInfoBySession(sessionid);
        
        MongoDatabase database = MongoDBUtil.getDataBase();
        final MongoCollection<Address> collection = database.getCollection("address", Address.class);
        Address defaultAddr = collection.find(and(eq("userid", userinfo.getUserid()),eq("isDefault", true))).first();
        //如果默认地址没有，则取第一个为默认
        if(defaultAddr == null){
            defaultAddr = collection.find(eq("userid", userinfo.getUserid())).first();
        }

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("defaultAddr", defaultAddr);
        return result;
    }

    @DELETE
    @Path("/{addrId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage delGoods(@CookieParam("my_session") String sessionid, @PathParam("addrId") String addrId)
    {
        User userinfo = UserService.getUserInfoBySession(sessionid);

        MongoDatabase database = MongoDBUtil.getDataBase();
        final MongoCollection<Document> collection = database.getCollection("address");
        //算了 不校验是否成功删除了，简单粗暴点
        collection.deleteOne(and(eq("addrId", addrId),eq("userid", userinfo.getUserid())));
        
        return new ReturnMessage();
    }

    //设为默认地址
    @POST
    @Path("/default/{addrId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage setDefaultFlag(@CookieParam("my_session") String sessionid,@PathParam("addrId") String addrId)
    { 
        User userinfo = UserService.getUserInfoBySession(sessionid);

        MongoDatabase database = MongoDBUtil.getDataBase();

        final MongoCollection<Address> collection = database.getCollection("address", Address.class);

        //需要更新其他地址为非默认
        Document update = new Document();
        update.put("isDefault", false);
        collection.updateMany(and(eq("userid", userinfo.getUserid()),ne("addrId", addrId)),new Document("$set", update));
        //更新这一条为默认
        update.put("isDefault", true);
        collection.updateOne(and(eq("userid", userinfo.getUserid()),eq("addrId", addrId)),new Document("$set", update));

        return new ReturnMessage();
    }

}