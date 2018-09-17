package com.mingyue.restapi.qrcode;

import java.util.Date;

public class QRCode{
    String userid;
    private Date createDate;  //创建时间
    private byte[] fileData;     //二维码图片信息

    public String getUserid(){
        return userid;
    }
    
    public void setUserid(String userid){
        this.userid = userid;
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
}