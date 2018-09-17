package com.mingyue.restapi.message;

import java.util.Date;
import java.util.List;

public class ChatRecord{

    String openid;    //用户id

    Date lastTime;    //会话的最后更新时间

    List<WxMessage> messages; //对话消息列表

    public String getOpenid(){
        return openid;
    }
    
    public void setOpenid(String openid){
        this.openid = openid;
    }

    public Date getLastTime(){
        return lastTime;
    }
    
    public void setLastTime(Date lastTime){
        this.lastTime = lastTime;
    }

    public List<WxMessage> getMessages(){
        return messages;
    }
    
    public void setMessages(List<WxMessage> messages){
        this.messages = messages;
    }
}