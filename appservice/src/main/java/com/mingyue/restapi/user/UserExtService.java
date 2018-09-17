package com.mingyue.restapi.user;

import static com.mongodb.client.model.Filters.eq;

import java.util.Date;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.mingyue.database.MongoDBUtil;
import com.mingyue.restapi.ReturnMessage;
import com.mongodb.client.MongoCollection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

@Path("/restapi/userext")
public class UserExtService{

    private static Logger LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    private static MongoCollection<UserExtInfo> collection = MongoDBUtil.getDataBase().getCollection("userext", UserExtInfo.class);

    //如果没找到就新增
    @POST
    @Path("/admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage updateUserInfo(Map<String,Object> request){

        if(request.get("userid")==null){
            return new ReturnMessage("999","参数有误");
        }

        Document update = new Document();
        
        if(request.get("level") != null){
            update.put("level", (Integer)request.get("level")); 
        }
        if(request.get("telphone") != null){
            update.put("telphone", (String)request.get("telphone")); 
        }
        if(request.get("telphone2") != null){
            update.put("telphone2", (String)request.get("telphone2")); 
        }
        if(request.get("realName") != null){
            update.put("realName", (String)request.get("realName")); 
        }
        if(request.get("score") != null){
            update.put("score", ((Integer)request.get("score"))); 
        }
        if(request.get("remark") != null){
            update.put("remark", (String)request.get("remark")); 
        }

        if(update.isEmpty()){
            return new ReturnMessage();
        }

        UserExtInfo userext = collection.find(eq("userid", request.get("userid"))).first();

        if(userext == null){  //没找到 需要新增
            UserExtInfo newext = new UserExtInfo();
            newext.setUserid((String)request.get("userid"));
            newext.setLevel((Integer)request.get("level"));
            newext.setTelphone((String)request.get("telphone"));
            newext.setTelphone2((String)request.get("telphone2"));
            newext.setRealName((String)request.get("realName"));
            newext.setScore((request.get("score") == null? 0 :(Integer)request.get("score")));
            newext.setRemark((String)request.get("remark"));

            collection.insertOne(newext);

        } else {  //找到了 更新
            collection.updateOne(eq("userid", request.get("userid")),new Document("$set", update));
        }

        return new ReturnMessage();
    }

    //查询用户扩展信息
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage getUserExtInfoByUserId(@QueryParam("userid") String userid){

        UserExtInfo userext = collection.find(eq("userid", userid)).first();

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("userext", userext);
        return result;
    }

    //查询用户扩展信息
    @GET
    @Path("/score")
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage getUserScore(@CookieParam("my_session") String sessionid){

        User userinfo = UserService.getUserInfoBySession(sessionid);
        UserExtInfo userext = collection.find(eq("userid", userinfo.getUserid())).first();

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("userscore", userext.getScore());
        return result;
    }

    @POST
    @Path("/admin/discount/{userid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage updateUserDiscount(@PathParam("userid") String userid,Map<String,Integer> discountMap){

        if(userid == null){
            return new ReturnMessage("999","参数有误");
        }

        LOG.info("discountMap:" + discountMap);

        UserExtInfo userext = collection.find(eq("userid", userid)).first();
        if(userext == null){
            UserExtInfo newext = new UserExtInfo(); 
            newext.setUserid(userid);
            newext.setDiscountMap(discountMap);
            collection.insertOne(newext);
        } else {
            userext.setDiscountMap(discountMap);
            collection.findOneAndReplace(eq("userid", userid), userext);
        }

        return new ReturnMessage();
    }

    //积分变化 chgScore为负值表示扣减
    public static boolean changeScore(String userid,int chgScore){
        UserExtInfo userext = collection.find(eq("userid", userid)).first();
        if(userext.getScore() + chgScore < 0){
            LOG.error("积分不够：" + userid);
            return false;
        }
        userext.setScore(userext.getScore() + chgScore);

        collection.findOneAndReplace(eq("userid", userid), userext);

        //记录积分使用结果
		org.bson.Document doc = new org.bson.Document("userid", userid);
        doc.append("changeDate", new Date());
        doc.append("changeScore", chgScore);
        doc.append("newUserScore", userext.getScore());

		MongoCollection<org.bson.Document> scorelogCollection = MongoDBUtil.getDataBase().getCollection("scorelog");
        scorelogCollection.insertOne(doc);
        
        return true;
    }

}