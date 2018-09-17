package com.mingyue.websocket;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.mingyue.common.ConfigUtil;
import com.mingyue.restapi.message.ChatRecord;
import com.mingyue.restapi.message.ContactMessageService;
import com.mingyue.restapi.message.WxMessage;
import com.mingyue.restapi.user.User;
import com.mingyue.restapi.user.UserService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

/**
 * websocket 消息处理
 */
@Sharable
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    private static volatile ChannelHandlerContext activeCtx ;  //当前活动中的channel

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        // ping and pong frames already handled

        if (frame instanceof TextWebSocketFrame) {
            // Send the uppercase string back.
            String request = ((TextWebSocketFrame) frame).text();
            LOG.info("{} received {}", ctx.channel(), request);

            JSONObject jsonObject = JSONObject.parseObject(request);
            if("replyMsg".equals(jsonObject.getString("type"))){
                //转发客服消息给用户
                String toOpenid = jsonObject.getString("toOpenid");
                String msgContent = jsonObject.getString("msgContent");
                int errcode = ContactMessageService.transReplyMsg(toOpenid,msgContent);
                if(errcode != 0){
                    sendFailPromptToClient(errcode);
                    return;  //发送消息失败则直接返回
                }
                
                WxMessage message = new WxMessage();
                message.setOpenid(toOpenid);
                message.setAdminOpenid(ConfigUtil.getProperty("remind_openid"));
		        message.setCreateTime(new Date());
		        message.setMsgType("text");
		        message.setContent(msgContent);
		        message.setPicUrl("");
		        message.setMsgId("");
                message.setDirection("2");
                //记录到数据库 并重新推送给聊天客户端，客户端收到后知道发送成功。。
                ContactMessageService.messageToDB(toOpenid, message);
        
            } else if("getUser".equals(jsonObject.getString("type"))){
                sendUsersToClient(jsonObject.getJSONArray("openids").toJavaList(String.class));
            }

            
        } else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            //待添加记录当前ctx 然后在触发打开新的客服消息界面时关闭之前的连接，修改活动连接为新连接
            LOG.info("HandshakeComplete"); 
            activeCtx = ctx;
            //初始化发送消息到客户端
            sendChatsToClient(ContactMessageService.initChats()); 
        }
        super.userEventTriggered(ctx, evt);
    }

    public static boolean isHaveActiveConnect(){
        if(activeCtx != null && activeCtx.channel().isActive()){
            return true;
        }

        return false;
    }

    public static void sendMessageToClient(WxMessage message){
        //发送一条消息内容到客户端
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("type","message");
        map.put("content", message);

        if(activeCtx != null){
            activeCtx.channel().writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(map)));
        } 
    }

    public static void sendFailPromptToClient(int errcode){
        //发送 发送失败的提示到客户端
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("type","sendfail");
        map.put("content", errcode);

        if(activeCtx != null){
            activeCtx.channel().writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(map)));
        } 
    }

    public static void sendChatsToClient(List<ChatRecord> chatsessions){
        //发送初始化聊天内容返回给客户端
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("type","chats");
        map.put("content", chatsessions);

        if(activeCtx != null){
            activeCtx.channel().writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(map)));
        } 
    }

    public static void sendUsersToClient(List<String> openids){
        List<User> users = UserService.getUserByOpenid(openids);
        //发送初始化聊天内容返回给客户端
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("type","users");
        map.put("content", users);

        if(activeCtx != null){
            activeCtx.channel().writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(map)));
        } 
    }
}
