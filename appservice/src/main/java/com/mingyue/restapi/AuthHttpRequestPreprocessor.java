package com.mingyue.restapi;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ws.rs.core.Cookie;

import com.mingyue.InvalidSessionException;
import com.mingyue.restapi.admin.AdminService;
import com.mingyue.restapi.user.UserService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.client.exception.ResteasyHttpException;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpRequestPreprocessor;

//鉴权，校验sessionid是否有效
//也可以采用 ContainerRequestFilter  ContainerRequestContext.abortWith 方法
public class AuthHttpRequestPreprocessor implements HttpRequestPreprocessor {

    private static Logger LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    private static final Set<String> notAuthPaths = new HashSet<String>();

    private static final Set<String> onlyAdminPaths = new HashSet<String>();  //只有后台管理才能调用的路径
    
    private static Pattern needAuth = 
        Pattern.compile("/restapi/(admin|user|order|cart|wxpay|address|userext|orderfee)(([/|\\?].*)|$)");
    
    static {
        notAuthPaths.add("/restapi/user/wechatSignIn");
        notAuthPaths.add("/restapi/admin/login");
        notAuthPaths.add("/restapi/wxpay/wxNotify");
        notAuthPaths.add("/restapi/wxpay/wxRefundNotify");

        onlyAdminPaths.add("/restapi/wxpay/WXRefund");  //退款只能从后台管理页面由管理员发起
    }

    public void preProcess(HttpRequest request) throws InvalidSessionException{
        LOG.info("AuthHttpRequestPreprocessor effected.");
        LOG.info(request.getUri().getPath());

        Map<String,Cookie> cookies = request.getHttpHeaders().getCookies();

        String path = request.getUri().getPath();
        if (notAuthPaths.contains(path)) {
            return; //如果是登录 则不处理
        }

        boolean isMatch = needAuth.matcher(path).matches();
        if(!isMatch){
            return; //如果不在需要鉴别session的里面也直接return
        }

        if (cookies.isEmpty()){
            throw new ResteasyHttpException("Invalid Request.");
        }

        //LOG.info(JSONObject.toJSONString(cookies));
        String sessionId = "";

        //管理后台的session,区分微信用户和管理员
        if (cookies.get("admin_session") != null){
            sessionId = cookies.get("admin_session").getValue();
            if (AdminService.authAdminSession(sessionId)){
                return;
            }
        }

        //用户的session校验
        if (cookies.get("my_session") != null){
            sessionId = cookies.get("my_session").getValue();

            //对SessionId进行鉴权
            if (UserService.authUserSession(sessionId)) {

                if(path.contains("/admin") || onlyAdminPaths.contains(path)){ //有人试图使用从小程序端获取到的session来修改后台配置。。
                    LOG.error("Illegal Request from user of openid:" + UserService.getUserInfoBySession(sessionId).getOpenid());
                    throw new InvalidSessionException("Invalid Request.");
                }
                return;
            } else {
                throw new InvalidSessionException("Invalid Session.");
            }
        }
        
        //最终校验不通过
        throw new InvalidSessionException("Invalid Request.");    
    }

}