package com.mingyue.restapi.user;

import java.util.Map;

public class UserExtInfo {
    private String userid;
    private int level;           //等级
    private String telphone;     //联系电话
    private String telphone2;    //联系电话2
    private String realName;     //真实姓名
    private int    score;        //积分 
    private String remark;       //备注
    private Map<String,Integer>    discountMap;      //折扣信息 商品code与折扣的map表

    public String getUserid(){
        return userid;
    }
    
    public void setUserid(String userid){
        this.userid = userid;
    }

    public int getLevel(){
        return level;
    }
    
    public void setLevel(int level){
        this.level = level;
    }

    public String getTelphone(){
        return telphone;
    }
    
    public void setTelphone(String telphone){
        this.telphone = telphone;
    }

    public String getTelphone2(){
        return telphone2;
    }
    
    public void setTelphone2(String telphone2){
        this.telphone2 = telphone2;
    }

    public String getRealName(){
        return realName;
    }
    
    public void setRealName(String realName){
        this.realName = realName;
    }

    public int getScore(){
        return score;
    }
    
    public void setScore(int score){
        this.score = score;
    }

    public String getRemark(){
        return remark;
    }
    
    public void setRemark(String remark){
        this.remark = remark;
    }

    public Map<String,Integer> getDiscountMap(){
        return discountMap;
    }
    
    public void setDiscountMap(Map<String,Integer> discountMap){
        this.discountMap = discountMap;
    }
}