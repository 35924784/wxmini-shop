package com.mingyue.restapi.category;

import java.util.List;

public class Category{
    private String name; //目录名称
    private String code; //目录编码
    private List<String> goodsList; //目录中包含的商品

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

    public List<String> getGoodsList(){
        return goodsList;
    }
    
    public void setGoodsList(List<String> goodsList){
        this.goodsList = goodsList;
    }
}