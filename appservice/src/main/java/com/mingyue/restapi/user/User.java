package com.mingyue.restapi.user;

import java.util.Date;

//用户信息
public class User{
    private String userid;
    private String nickName;    //昵称
    private String gender;      //性别
    private String language;    //语言
    private String city;        //城市
    private String province;   //省份
    private String country;   //国家
    private String avatarUrl; //头像
    private String openid;    //用户的openId
    private String sessionkey; //微信接口返回的session_key
    private String sessionid; //自己生成的sessionid
    private Date sessiondate; //自己生成sessionid的时间 用于简单判断session是否过期
    private String shareid;   //如果是通过分享的小程序进入登录的，记录分享者的userid 只在首次时记录，不更新

    public String getUserid(){
        return userid;
    }
    
    public void setUserid(String userid){
        this.userid = userid;
    }

    public String getNickName(){
        return nickName;
    }
    
    public void setNickName(String nickName){
        this.nickName = nickName;
    }

    public String getGender(){
        return gender;
    }
    
    public void setGender(String gender){
        this.gender = gender;
    }

    public String getLanguage(){
        return language;
    }
    
    public void setLanguage(String language){
        this.language = language;
    }

    public String getCity(){
        return city;
    }
    
    public void setCity(String city){
        this.city = city;
    }

    public String getProvince(){
        return province;
    }
    
    public void setProvince(String province){
        this.province = province;
    }

    public String getCountry(){
        return country;
    }
    
    public void setCountry(String country){
        this.country = country;
    }

    public String getAvatarUrl(){
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl){
        this.avatarUrl = avatarUrl;
    }

    public String getOpenid(){
        return openid;
    }
    
    public void setOpenid(String openid){
        this.openid = openid;
    }

    public String getSessionkey(){
        return sessionkey;
    }
    
    public void setSessionkey(String sessionkey){
        this.sessionkey = sessionkey;
    }

    public String getSessionid(){
        return sessionid;
    }
    
    public void setSessionid(String sessionid){
        this.sessionid = sessionid;
    }

    public Date getSessiondate(){
        return sessiondate;
    }
    
    public void setSessiondate(Date sessiondate){
        this.sessiondate = sessiondate;
    }

    public String getShareid(){
        return shareid;
    }
    
    public void setShareid(String shareid){
        this.shareid = shareid;
    }
}