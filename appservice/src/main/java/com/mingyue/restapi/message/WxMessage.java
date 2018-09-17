package com.mingyue.restapi.message;

import java.util.Date;

public class WxMessage {
    String openid;      //  会话所属openid
    String adminOpenid; //  后台客服人员的openid
    Date   createTime;  //	消息创建时间(整型）
    String msgType;     //	text;
    String content;	    //  文本消息内容
    String picUrl;      //  图片消息的url
    String mediaId;     //  图片消息媒体id 用于调用获取临时素材接口拉取数据
    String msgId;	    //  消息id，64位整型
    String direction;   //  为2表示管理员发出的消息，1表示客户发来的客服消息

    public String getOpenid(){
        return openid;
    }
    
    public void setOpenid(String openid){
        this.openid = openid;
    }

    public String getAdminOpenid(){
        return adminOpenid;
    }
    
    public void setAdminOpenid(String adminOpenid){
        this.adminOpenid = adminOpenid;
    }

    public Date getCreateTime(){
        return createTime;
    }
    
    public void setCreateTime(Date createTime){
        this.createTime = createTime;
    }

    public String getMsgType(){
        return msgType;
    }
    
    public void setMsgType(String msgType){
        this.msgType = msgType;
    }

    public String getContent(){
        return content;
    }
    
    public void setContent(String content){
        this.content = content;
    }

    public String getPicUrl(){
        return picUrl;
    }
    
    public void setPicUrl(String picUrl){
        this.picUrl = picUrl;
    }

    public String getMediaId(){
        return mediaId;
    }
    
    public void setMediaId(String mediaId){
        this.mediaId = mediaId;
    }

    public String getMsgId(){
        return msgId;
    }
    
    public void setMsgId(String msgId){
        this.msgId = msgId;
    }

    public String getDirection(){
        return direction;
    }
    
    public void setDirection(String direction){
        this.direction = direction;
    }
}