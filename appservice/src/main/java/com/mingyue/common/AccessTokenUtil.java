package com.mingyue.common;

import java.util.Date;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class AccessTokenUtil {

    private static Logger LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    // 请求获取access_token的微信接口URL
    public static final String req_toekn_url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=SECRET";
    public static final String appid = ConfigUtil.getProperty("appid");
	public static final String secret = ConfigUtil.getProperty("secret");

    public volatile static String access_token = ""; // 统一的access_token
    public volatile static Date token_expdate = new Date(); // token失效时间

    public static synchronized String getAccessToken() {
        if (token_expdate.before(new Date()) || ValidateUtils.isEmptyString(access_token)) { // 虽然判断，但是定时刷新的情况下应该用不到。
            getTokenFromWX();
        }

        return access_token;
    }

    @SuppressWarnings("unchecked")
    private static void getTokenFromWX() {
        try {
            LOG.info("getTokenFromWX begin");

            ResteasyClient client = new ResteasyClientBuilder().build();
            ResteasyWebTarget restTarget = client.target(req_toekn_url.replaceFirst("APPID", appid).replaceFirst("SECRET", secret));

            Response response = restTarget.request().get();

            Map<String, Object> valueMap = response.readEntity(Map.class);
            response.close();
            LOG.info(valueMap);

            access_token = (String)valueMap.get("access_token");

            Long newdate = new Date().getTime() + (Integer)valueMap.get("expires_in") * 1000;
            token_expdate = new Date(newdate - 240000);//提前4分钟刷新token
            LOG.info(token_expdate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 使用 Quartz 定时任务刷新 access_token
}