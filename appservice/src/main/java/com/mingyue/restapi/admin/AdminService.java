package com.mingyue.restapi.admin;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mingyue.common.ConfigUtil;
import com.mingyue.common.ValidateUtils;
import com.mingyue.restapi.ReturnMessage;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("/restapi/admin")
public class AdminService{
    private static Logger LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    //实际我只弄了一个操作员而已。。。
    private volatile static Map<String,Date> adminSessionMap = new HashMap<String,Date>();
    //session有效期30分钟
    private static long expire_duration = 1000 * 60 * 30;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage login(Map<String,String> loginReq)
    {
        ReturnMessage result;

        LOG.debug(loginReq.get("username"));
        LOG.debug(loginReq.get("password"));

        String username = loginReq.get("username");
        String passwd = loginReq.get("password");

        if(StringUtils.isEmpty(username) || StringUtils.isEmpty(passwd)){
            return new ReturnMessage("999","用户名或密码错误");
        }

        byte[] hash = DigestUtils.sha256(passwd);
        String hashStr = Hex.encodeHexString(hash);
        String usrConfigpwd = ConfigUtil.getProperty(username + "_passwd");

        if (hashStr.equals(usrConfigpwd))
        {
             //生成uuid作为自己的session
            String session = UUID.randomUUID().toString().replaceAll("-", "");
            adminSessionMap.put(session, new Date());
            result = new ReturnMessage();
            result.retParams.put("admin_session", session);
        }
        else
        {
            result = new ReturnMessage("999","用户名或密码错误");
        }

        return result;    
    }

    @POST
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage logout(@HeaderParam("admin_session") String session)
    {
        ReturnMessage result;
        //无论是否失效都直接删除
        if (adminSessionMap.containsKey(session))
        {
            adminSessionMap.remove(session);
            result = new ReturnMessage();
        }
        else
        {
            result = new ReturnMessage("999","已退出");
        }

        return result;    
    }

    public static boolean authAdminSession(String sessionid)
    {
        if(ValidateUtils.isNotEmptyString(sessionid) 
            && adminSessionMap.containsKey(sessionid)
            &&  (new Date().getTime() - adminSessionMap.get(sessionid).getTime()) < expire_duration)
        {
            //更新session最后生效时间
            adminSessionMap.put(sessionid, new Date());
            return true;
        }
        else
        {
            return false;
        }
    }
}