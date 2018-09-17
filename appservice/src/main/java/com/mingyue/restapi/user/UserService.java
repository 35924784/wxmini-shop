package com.mingyue.restapi.user;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.regex;
import static com.mongodb.client.model.Projections.exclude;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.mingyue.common.CacheHelper;
import com.mingyue.common.ConfigUtil;
import com.mingyue.common.DateHelper;
import com.mingyue.common.ValidateUtils;
import com.mingyue.database.MongoDBUtil;
import com.mingyue.restapi.ReturnMessage;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.conversions.Bson;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ehcache.Cache;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

@Path("/restapi/user")
public class UserService{

    private static Logger LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    private static final String target = "https://api.weixin.qq.com/sns/jscode2session?appid=APPID&secret=SECRET&js_code=JSCODE&grant_type=authorization_code";

    private static final String appid = ConfigUtil.getProperty("appid");

    private static final String secret = ConfigUtil.getProperty("secret");

    private static MongoCollection<User> collection = MongoDBUtil.getDataBase().getCollection("user", User.class);

    //一周
    private static long expire_duration = 1000 * 60 * 60 * 24 * 7;

    @SuppressWarnings("unchecked")
    @POST
    @Path("/wechatSignIn")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage wechatSignIn(WeChatSignRequest signRequest) throws JsonParseException, JsonMappingException, IOException
    {
        //先根据code 获取openid session_key等信息
        String jsCode = signRequest.getCode();
        String targetURL = target.replaceFirst("APPID", appid)
            .replaceFirst("SECRET", secret)
            .replaceFirst("JSCODE", jsCode);

        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget restTarget = client.target(targetURL);

        Response response = restTarget.request().get();
        String value = response.readEntity(String.class);
        response.close();

        ObjectMapper mapper = new ObjectMapper(); 
        Map<String,String> valueMap = mapper.readValue(value, Map.class);

        LOG.error(valueMap.get("openid"));
        LOG.error(valueMap.get("session_key"));

        //如果没获取到用户登录状态，则返回错误信息
        if (valueMap.get("errcode") != null)
        {
            LOG.error("return error:" + value);
            ReturnMessage result = new ReturnMessage(valueMap.get("errcode"),valueMap.get("errmsg"));
            return result;
        }

        //根据openid查询用户信息 如果未记录过则记录到数据库
        String openid = valueMap.get("openid");
        String sessionkey = valueMap.get("session_key");
        User userinfo = signRequest.getUserInfo();
        userinfo.setOpenid(openid);
        userinfo.setSessionkey(sessionkey);

        //生成uuid作为自己的session 需要维护用户的 session 15天有效,微信小程序里一开始检查是否有有效session
        String session = UUID.randomUUID().toString().replaceAll("-", "");
        userinfo.setSessionid(session);
        userinfo.setSessiondate(new Date());

        User findUser = collection.find(eq("openid", openid)).first();

        //更新用户信息
        if(findUser != null){
            userinfo.setUserid(findUser.getUserid());
            userinfo.setShareid(findUser.getShareid()); //shareid标识最初用户是否通过分享页面进入的，不更新
            updateUserInfo(userinfo);
        }
        else{
            String userid = openid.substring(0,4) + "_" + DateHelper.getDateString("yyMMddhhmmssSSS");
            userinfo.setUserid(userid);
            addUserInfo(userinfo);
        }

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("my_session", session);
        return result;
    }

    private void addUserInfo(User userInfo){

        if(StringUtils.isNotEmpty(userInfo.getShareid())){
            UserExtService.changeScore(userInfo.getShareid(), Integer.valueOf(ConfigUtil.getProperty("scoreForNewUser"))); //默认有新用户注册，给分享者加积分，加多少可配置
        }
        collection.insertOne(userInfo);
    }

    private void updateUserInfo(User userInfo){

        collection.findOneAndReplace(eq("userid", userInfo.getUserid()), userInfo);
    }

    //根据昵称查询用户,最多10个
    public static List<User> getUserInfoByUsername(String nickName){

        FindIterable<User> findIterable = collection.find(regex("nickName", nickName)).limit(10);
        List<User> userlist = MongoDBUtil.getListFromFindIterable(findIterable);

        return userlist;
    }

    //checkSession 每次微信小程序进入时先检查一下自己设置的 session是否有效，无效则重新登录。
    //实际校验在 AuthHttpRequestPreprocessor 做了 返回userid为分享使用
    @POST
    @Path("/checkSession")
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage checkUserSession(String sessionid){
        org.ehcache.Cache<String,User> userCache = CacheHelper.getUserSessionCache();
        User userinfo = userCache.get(sessionid);

        if(userinfo == null){   
            return  new ReturnMessage("456","session无效");
        }

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("userid", userinfo.getUserid());
        return result;
    }

    public static boolean authUserSession(String sessionid){
        if(ValidateUtils.isEmptyString(sessionid)){
            return false;
        }
        Cache<String,User> userCache = CacheHelper.getUserSessionCache();
        User userinfo = userCache.get(sessionid);
        if(userinfo != null &&  (new Date().getTime() - userinfo.getSessiondate().getTime()) < expire_duration){
            userinfo.setSessiondate(new Date());
            userCache.put(sessionid,userinfo);    //放到缓存中，由缓存的Write-behind策略决定何时更新到数据库，避免频繁更新
            return true;
        }

        return false;
    }

    public static User getUserInfoBySession(String sessionid){
        Cache<String,User> userCache = CacheHelper.getUserSessionCache();
        
        return userCache.get(sessionid);
    }

    //供管理后台使用,根据查询条件查询用户
    @POST
    @Path("/admin/all")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage getAllUsers(Map<String,String> request)
    {
        Bson filters = exists("userid");

        if(ValidateUtils.isNotEmptyString(request.get("province"))){
            filters = and(filters,eq("province", request.get("province")));
        }
        if(ValidateUtils.isNotEmptyString(request.get("starttime"))){
            Long starttime = Long.valueOf(request.get("starttime"));
            filters = and(filters,gt("sessiondate", new Date(starttime)));
        }
        if(ValidateUtils.isNotEmptyString(request.get("endtime"))){
            Long endtime = Long.valueOf(request.get("endtime"));
            filters = and(filters,lt("sessiondate", new Date(endtime)));
        }
        if(ValidateUtils.isNotEmptyString(request.get("nickName"))){
            
            filters = and(filters,regex("nickName", request.get("nickName")));
        }

        int curPage = Integer.valueOf(request.get("curPage")== null?"1":request.get("curPage"));
        int pageSize = Integer.valueOf(request.get("pageSize")== null?"10":request.get("pageSize"));

        FindIterable<User>  findIterable = collection.find(filters).projection(exclude("sessionid","sessionkey"))
                            .skip((curPage-1) * pageSize).limit(pageSize);

        List<User> userList = MongoDBUtil.getListFromFindIterable(findIterable);
        Long totalNum = collection.count(filters);
        
        ReturnMessage result = new ReturnMessage();
        result.retParams.put("userList", userList);
        result.retParams.put("totalNum", totalNum);
        return result; 
    }

    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage getUserInfo(@QueryParam("userid") String userid)
    {
        User findUser = getUserInfoByUserid(userid);
        
        ReturnMessage result = new ReturnMessage();
        result.retParams.put("userInfo", findUser);
        return result; 
    }

    public static User getUserInfoByUserid(String userid)
    {
        Cache<String,User> userCache = CacheHelper.getUserInfoCache();
        if(userCache.get(userid) != null){
            return userCache.get(userid);
        }

        User userinfo = collection.find(eq("userid", userid)).first();
        if(userinfo != null){
            userCache.put(userid, userinfo);
        }
        return userinfo; 
    }

    //根据openid查询用户信息
    public static List<User> getUserByOpenid(List<String> openids)
    {
        FindIterable<User>  findIterable = collection.find(in("openid",openids.toArray())).projection(exclude("sessionid","sessionkey"));
        
        return MongoDBUtil.getListFromFindIterable(findIterable);
    }
    
}