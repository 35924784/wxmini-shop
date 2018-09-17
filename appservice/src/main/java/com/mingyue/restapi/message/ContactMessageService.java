package com.mingyue.restapi.message;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Projections.slice;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.push;
import static com.mongodb.client.model.Updates.set;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.alibaba.fastjson.JSONObject;
import com.mingyue.common.AccessTokenUtil;
import com.mingyue.common.ConfigUtil;
import com.mingyue.database.MongoDBUtil;
import com.mingyue.restapi.order.Order;
import com.mingyue.restapi.user.User;
import com.mingyue.restapi.user.UserService;
import com.mingyue.websocket.WebSocketFrameHandler;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.qq.weixin.mp.aes.AesException;
import com.qq.weixin.mp.aes.WXBizMsgCrypt;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

/*
    https://developers.weixin.qq.com/miniprogram/dev/api/custommsg/callback_help.html
    接入指引 
    https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419318479&token=&lang=zh_CN
    下载的样例代码直接使用的
*/
@Path("/restapi/contactmessage")
public class ContactMessageService {

	private static Logger LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

	// 需要加密的明文
	public static final String encodingAesKey = ConfigUtil.getProperty("encodingAesKey");
	public static final String token = ConfigUtil.getProperty("messagetoken");
	public static final String appId = ConfigUtil.getProperty("appid");

	private static final String msg_url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=";
	//回复消息模板
	private static final String replyMsgTemp = "{\"touser\":\"{{OPENID}}\",\"msgtype\":\"text\",\"text\":{\"content\":\"{{CONTENT}}\"}}";

	//转发消息时的回复模板
	private static final String trans_reply = "<xml> <ToUserName><![CDATA[touser]]></ToUserName> <FromUserName><! [CDATA[fromuser]]></FromUserName> <CreateTime>nowtime</CreateTime> <MsgType><![CDATA[transfer_customer_service]]></MsgType> </xml>";

	private static volatile String isTransCustService =  "0";    //默认为0 不转发到网页版客服系统

	//新的客服消息接入时，发送提醒
	private static final String remind_content = "文玉茶叶有客户发来客服消息啦，请点击 https://www.example.com/web/chat.html 进行回复。";

	private static volatile LocalDateTime lastRemindTime =  LocalDateTime.of(2009, 12, 12, 12, 12);

	public static final Map<String,String> auto_contents = new HashMap<String,String>();

	private static MongoCollection<ChatRecord> collection = MongoDBUtil.getDataBase().getCollection("chatrecord", ChatRecord.class);

	static {
		auto_contents.put("1","明月&&梁勇");
		auto_contents.put("2","今天运营情况：共{{count}}笔订单。");
		auto_contents.put("3","自动回复，系统运行正常。");
	}

	//接收微信客服消息 并自动回复  记得按照 https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419318479&token=&lang=zh_CN
	//中所说下载 JCE文件并覆盖原来的
	@POST
	@Produces(MediaType.TEXT_PLAIN)
    public String reply(@QueryParam("msg_signature") String msg_signature, @QueryParam("timestamp") String timestamp,
	@QueryParam("nonce") String nonce, String postdata) throws AesException, DocumentException
    {
		LOG.info("解密前: " + postdata);
		WXBizMsgCrypt pc = new WXBizMsgCrypt(token, encodingAesKey, appId);
		String mingwen = pc.decryptMsg(msg_signature, timestamp, nonce, postdata);
		LOG.info("解密后: " + mingwen);

		// 将解析结果存储在HashMap中   
		Map<String,String> msg_map = doXMLParse(mingwen);

		//后续可以针对消息的具体内容加判断来回复
		String fromopenid = msg_map.get("FromUserName");
		String message_content = msg_map.get("Content");

		//如果在是微信小程序的管理员，且消息是1,2,3 则管理员实用客服功能时将根据输入自动回复
		String admins = ConfigUtil.getProperty("adminopenid"); //小程序管理员的openid
		List<String> adminOpenids = null;
		if(StringUtils.isNotEmpty(admins)){
			adminOpenids = Arrays.asList(admins.split("\\|"));
		}
		if(adminOpenids != null && adminOpenids.contains(fromopenid)){
			//如果管理员发送的在自动回复范围内，则自动回复
			if(auto_contents.get(message_content) != null){
				String content = auto_contents.get(message_content);

				if("2".equals(message_content)){
					content = content.replaceFirst("\\{\\{count\\}\\}", getOrderToday());
				}
				String replyMsg = replyMsgTemp.replaceFirst("\\{\\{OPENID\\}\\}", fromopenid).replaceFirst("\\{\\{CONTENT\\}\\}", content) ;

				postToWx(replyMsg,msg_url + AccessTokenUtil.getAccessToken());
			
			}

			return "success"; //管理员主动发送的消息直接回复success，都不再转发
		}

		//如果是事件类型，则不做处理
		String msgType = msg_map.get("MsgType");
		if("event".equals(msgType)){
			return "success";
		}

		// 其他普通用户的消息，如果当前没有已打开的后台客服会话界面且5分钟内没有发送过，则给管理员发送消息，提醒管理员有客服消息。
		if (!WebSocketFrameHandler.isHaveActiveConnect() && LocalDateTime.now().isAfter(lastRemindTime.plusMinutes(5))) {
			String remind_openid = ConfigUtil.getProperty("remind_openid"); // 有客服消息时，提醒管理员的openid
			String remind_Msg = replyMsgTemp.replaceFirst("\\{\\{OPENID\\}\\}", remind_openid)
					.replaceFirst("\\{\\{CONTENT\\}\\}", remind_content);
			postToWx(remind_Msg, msg_url + AccessTokenUtil.getAccessToken());

			lastRemindTime = LocalDateTime.now();
		}else {
			lastRemindTime = LocalDateTime.now().minusMinutes(10); 
			//如果有活动的连接，则更新时间，以便后面关闭客服会话又来了新的消息可以立即提醒
			//如果不加这个，则打开客服页面回复之后，关闭了，5分钟内的另一个客服消息将无法发送提醒
		}

		WxMessage message = new WxMessage();
		message.setOpenid(fromopenid);
		message.setCreateTime(new Date());
		message.setMsgType(msg_map.get("MsgType"));
		message.setContent(msg_map.get("Content"));
		message.setPicUrl(msg_map.get("PicUrl"));
		message.setMsgId(msg_map.get("MsgId"));
		message.setDirection("1");
		//记录消息到数据库并发送到我自己的客户端
		messageToDB(fromopenid,message);

		//如果需要转发到客服系统则转发到客服系统
		isTransCustService = ConfigUtil.getProperty("istranscustservice");
		if("true".equals(isTransCustService)){
			String wx_acct = ConfigUtil.getProperty("wx_acct"); //开发者微信号
			String reply = trans_reply.replaceFirst("touser", fromopenid)
							.replaceFirst("nowtime", String.valueOf(new Date().getTime() / 1000))
							.replaceFirst("fromuser", wx_acct); // 转发到微信客服系统
		
			return reply;
		}

		return "success";
		
	}


	//根据接入指南 第二步 验证消息来自微信
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String verify(@QueryParam("signature") String signature, @QueryParam("timestamp") String timestamp,
			@QueryParam("nonce") String nonce, @QueryParam("echostr") String echostr) throws AesException 
	{
		WXBizMsgCrypt pc = new WXBizMsgCrypt(token, encodingAesKey, appId);

		LOG.error("signature:" + signature + ";" + "timestamp:" + timestamp + ";" + "nonce:" + nonce + ";" + "echostr:" + echostr + ".");
		String decrptyechostr = pc.verifyUrl(signature, timestamp, nonce, echostr);
		LOG.error("decrptyechostr:" + decrptyechostr);
		return decrptyechostr;
	}

	/**
	 * 解析xml,返回第一级元素键值对。 使用dom4j解析
	 * @param strxml
	 * @throws JDOMException
	 */
	public static Map<String,String> doXMLParse(String strxml) throws DocumentException {
		if(null == strxml || "".equals(strxml)) {
			return null;
		}
		
		Map<String,String> m = new HashMap<String,String>();
		Document document = DocumentHelper.parseText(strxml);
        Element root = document.getRootElement();
        for (Iterator<Element> it = root.elementIterator(); it.hasNext();) {
            Element element = it.next();
			LOG.debug(element.getName() + ":"+ element.getStringValue());
			m.put(element.getName(), element.getStringValue());
        }
		
		return m;
	}

	private static int postToWx(String json,String msg_url){
		LOG.info("发送客服消息：" + json);
		//调用发送客服消息接口，并接受返回的结果
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget restTarget = client.target(msg_url);
		Response wxresponse = restTarget.request().post(Entity.json(json));
		String wxresult = wxresponse.readEntity(String.class);
		wxresponse.close();

		int errcode = 0;
		try {
			JSONObject jsonObject = JSONObject.parseObject(wxresult);
			errcode = jsonObject.getInteger("errcode");
		} catch (Exception e) {
			
		}
		
		LOG.info("发送客服消息结果：" + wxresult);

		return errcode;
	}

	public static void sendOrderremind(Order order){
		//使用消息管理功能 给管理员 发消息通知有订单。
		User findUser = UserService.getUserInfoByUserid(order.getUserid());
		String remind_openid =  ConfigUtil.getProperty("remind_openid"); //有客服消息时，提醒管理员的openid
		String remindContent = "客户"+ findUser.getNickName() + "支付下单啦，请尽快处理发货啊。订单号：" + order.getOrderid();
		String remind_Msg = replyMsgTemp.replaceFirst("\\{\\{OPENID\\}\\}", remind_openid).replaceFirst("\\{\\{CONTENT\\}\\}", remindContent);
		postToWx(remind_Msg,msg_url + AccessTokenUtil.getAccessToken());
	}

	public static String getOrderToday(){
		final MongoCollection<Order> collection = MongoDBUtil.getDataBase().getCollection("order", Order.class);

		List<String> statuslist =  new ArrayList<String>();
		statuslist.add("1");
		statuslist.add("2");

		LocalDateTime today_start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
		ZonedDateTime zdt = today_start.atZone(ZoneId.systemDefault());
        Date startdate = Date.from(zdt.toInstant());

		Long count = collection.count(and(gt("createDate", startdate),in("status",statuslist )));
		return count.toString();
	}

	//初始化客服消息会话
	public static List<ChatRecord> initChats(){
		FindIterable<ChatRecord> findIterable = collection.find().sort(descending("lastTime")).limit(8)
			.projection(slice("messages", -25));
		
		List<ChatRecord> chatlist = MongoDBUtil.getListFromFindIterable(findIterable);

		return chatlist;
	}

	//客服聊天客户端的消息转发给用户
	public static int transReplyMsg(String toOpenid,String msgContent){
		String replyMsg = replyMsgTemp.replaceFirst("\\{\\{OPENID\\}\\}", toOpenid).replaceFirst("\\{\\{CONTENT\\}\\}", msgContent) ;

		return postToWx(replyMsg,msg_url + AccessTokenUtil.getAccessToken());
	}

	//记录消息到数据库 并推送到后台客户聊天会话
	public static void messageToDB(String openid, WxMessage message){
		ChatRecord record = collection.find(eq("openid",openid)).first();

		if(record == null){
			record = new ChatRecord();
			record.setOpenid(openid);;
			record.setLastTime(new Date());

			record.setMessages(Arrays.asList(message));
			collection.insertOne(record);  //插入新的会话
			
		} else {
			record.setLastTime(new Date());
			//更新消息记录
			collection.updateOne(eq("openid",openid), combine(set("lastTime", new Date()), push("messages", message)));	
		}

		//判断是否后台客服会话是否打开，打开的情况下推送消息到websocket
		if(WebSocketFrameHandler.isHaveActiveConnect()){
			WebSocketFrameHandler.sendMessageToClient(message);
		}
	}

}