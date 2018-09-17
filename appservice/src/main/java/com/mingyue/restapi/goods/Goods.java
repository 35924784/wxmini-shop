package com.mingyue.restapi.goods;

import java.util.Date;
import java.util.List;

public class Goods{

    private String code;     //商品编码

    private String name;      //商品名称

    private String remark;    //商品描述

    private List<String> detailImgs; //商品详情图片

    private int price;     //价格

    private String classify;  //分类

    private Date createDate;  //创建时间

    private String status;       //状态
    
    private List<String> images; //商品轮播图片

    private List<List<String>> extParams; //商品参数 key,value,sort 参数名称：参数值：排序值组成的List

    private List<List<String>> specs;
    
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

    public List<String> getDetailImgs(){
        return detailImgs;
    }
    
    public void setDetailImgs(List<String> detailImgs){
        this.detailImgs = detailImgs;
    }

    public int getPrice(){
        return price;
    }
    
    public void setPrice(int price){
        this.price = price;
    }

    public String getClassify(){
        return classify;
    }
    
    public void setClassify(String classify){
        this.classify = classify;
    }

    public Date getCreateDate(){
        return createDate;
    }
    
    public void setCreateDate(Date createDate){
        this.createDate = createDate;
    }

    public String getStatus(){
        return status;
    }
    
    public void setStatus(String status){
        this.status = status;
    }

    public List<String> getImages(){
        return images;
    }
    
    public void setImages(List<String> images){
        this.images = images;
    }

    public List<List<String>> getExtParams(){
        return extParams;
    }
    
    public void setExtParams(List<List<String>> extParams){
        this.extParams = extParams;
    }

    public List<List<String>> getSpecs(){
        return specs;
    }
    
    public void setSpecs(List<List<String>> specs){
        this.specs = specs;
    }
}