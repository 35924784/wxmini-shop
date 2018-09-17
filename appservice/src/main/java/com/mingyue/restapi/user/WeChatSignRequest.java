package com.mingyue.restapi.user;

//微信登录请求
public class WeChatSignRequest{
    private String code;   //微信 wx.login 接口获取到的登录凭证
    private User userInfo;

    public String getCode(){
        return code;
    }
    
    public void setCode(String code){
        this.code = code;
    }

    public User getUserInfo(){
        return userInfo;
    }
    
    public void setUserInfo(User userInfo){
        this.userInfo = userInfo;
    }
}