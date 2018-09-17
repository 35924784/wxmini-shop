package com.mingyue.restapi.address;

public class Address{
    public String addrId;   //地址编号
    public String userid;   //地址所属用户编号
    public String contactName;  //收货人姓名
    public String contactPhone; //收货人联系电话
    public String addrDetail;   //收货人地址详情
    public Boolean isDefault;   //是否默认 

    public String getAddrId(){
        return addrId;
    }
    
    public void setAddrId(String addrId){
        this.addrId = addrId;
    }

    public String getUserid(){
        return userid;
    }
    
    public void setUserid(String userid){
        this.userid = userid;
    }

    public String getContactName(){
        return contactName;
    }
    
    public void setContactName(String contactName){
        this.contactName = contactName;
    }

    public String getContactPhone(){
        return contactPhone;
    }
    
    public void setContactPhone(String contactPhone){
        this.contactPhone = contactPhone;
    }

    public String getAddrDetail(){
        return addrDetail;
    }
    
    public void setAddrDetail(String addrDetail){
        this.addrDetail = addrDetail;
    }

    public Boolean getIsDefault(){
        return isDefault;
    }
    
    public void setIsDefault(Boolean isDefault){
        this.isDefault = isDefault;
    }
}