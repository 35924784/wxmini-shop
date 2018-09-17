package com.mingyue.restapi.image;

import java.util.Date;
import java.util.UUID;

public class Image{
    //由于轮播Banner也是图片 因此直接加个字段标识是否是轮播图片
    private String fileid;     //图片唯一id 用于商品查询图片等
    private Date createDate;  //创建时间
    private byte[] fileData;     //图片文件信息
    private String fileName;   //jpg, png, gif
    private Boolean isbanner;  //是否轮播图片

    public Image(){
        fileid = UUID.randomUUID().toString().replaceAll("-", "");
        createDate = new Date(System.currentTimeMillis());
    }

    public Image(String fileName,byte[] fileData){
        fileid = UUID.randomUUID().toString().replaceAll("-", "");
        createDate = new Date(System.currentTimeMillis());
        this.fileName = fileName;
        this.fileData = fileData;
    }

    public String getFileid(){
        return fileid;
    }
    
    public void setFileid(String fileid){
        this.fileid = fileid;
    }

    public String getFileName(){
        return fileName;
    }
    
    public void setFileName(String fileName){
        this.fileName = fileName;
    }

    public Date getCreateDate(){
        return createDate;
    }
    
    public void setCreateDate(Date createDate){
        this.createDate = createDate;
    }

    public byte[] getFileData(){
        return fileData;
    }
    
    public void setFileData(byte[] fileData){
        this.fileData = fileData;
    }

    public Boolean getIsbanner(){
        return isbanner;
    }
    
    public void setIsbanner(Boolean isbanner){
        this.isbanner = isbanner;
    }

}