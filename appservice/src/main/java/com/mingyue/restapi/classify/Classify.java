package com.mingyue.restapi.classify;


public class Classify{

    private String code;     //分类编码

    private String name;      //分类名称

    private String remark;    //分类描述

    public String getCode(){
        return code;
    }
    
    public void setCode(String code){
        this.code = code;
    }

    public String getName(){
        return name;
    }
    
    public void setName(String name){
        this.name = name;
    }

    public String getRemark(){
        return remark;
    }
    
    public void setRemark(String remark){
        this.remark = remark;
    }
}