package  com.mingyue.restapi.pay;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

import java.io.UnsupportedEncodingException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.alibaba.fastjson.JSONObject;
import com.mingyue.common.ConfigUtil;
import com.mingyue.common.ValidateUtils;
import com.mingyue.database.MongoDBUtil;
import com.mingyue.restapi.ReturnMessage;
import com.mingyue.restapi.message.ContactMessageService;
import com.mingyue.restapi.order.Order;
import com.mingyue.restapi.order.OrderFee;
import com.mingyue.restapi.order.OrderItem;
import com.mingyue.restapi.order.OrderService;
import com.mingyue.restapi.user.User;
import com.mingyue.restapi.user.UserService;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

@Path("/restapi/wxpay")
public class WXPay {

	private static Logger LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
	
	private static final String appid = ConfigUtil.getProperty("appid");

	private static final String mch_id = ConfigUtil.getProperty("mch_id");

	//微信支付的商户密钥  
    public static final String key = ConfigUtil.getProperty("mch_key");
	//微信支付结果回调通知接口
	public static final String notify_url = "https://www.example.com/restapi/wxpay/wxNotify";
	//微信统一下单接口地址  
	public static final String pay_url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
	//微信查询订单接口地址  
	public static final String orderquery_url = "https://api.mch.weixin.qq.com/pay/orderquery";
	//微信关闭订单接口地址  
	public static final String close_url = "https://api.mch.weixin.qq.com/pay/closeorder";
	//微信申请退款接口地址  
	public static final String refund_url = "https://api.mch.weixin.qq.com/secapi/pay/refund";
	//微信退款结果回调通知接口
	public static final String refundNotify_url = "https://www.example.com/restapi/wxpay/wxRefundNotify";
	//微信查询退款接口地址  
	public static final String refundquery_url = "https://api.mch.weixin.qq.com/pay/orderquery";
	//签名方式，固定值  
    public static final String SIGNTYPE = "MD5";  
    //交易类型，小程序支付的固定值为JSAPI  
	public static final String TRADETYPE = "JSAPI";
	
    @POST 
    @Path("/create_payorder")      //创建支付订单
    @Produces("application/json;charset=UTF-8")
	public ReturnMessage payForOrder(@Context HttpHeaders headers,
		@CookieParam("my_session") String sessionid, String orderid)
    {
		LOG.info("headers:" + headers.getRequestHeaders().toString());
		LOG.info(JSONObject.toJSONString(headers));
		LOG.info(JSONObject.toJSONString(headers.getRequestHeaders()));
		User userinfo = UserService.getUserInfoBySession(sessionid);
		MongoDatabase database = MongoDBUtil.getDataBase();
        final MongoCollection<Order> collection = database.getCollection("order", Order.class);
        Order findOrder = 
                    collection.find(and(eq("userid", userinfo.getUserid()),eq("orderid", orderid))).first();

		String openid = userinfo.getOpenid();

        try{
			//生成的随机字符串
			String nonce_str = getRandomStringByLength(32);
			//商品名称
			String body = "";
			List<OrderItem> items = findOrder.getOrderItemList();
			for (OrderItem item : items) {
				body = body + item.getGoodsname() + " ";
			}
			
			//获取客户端的ip地址
			String spbill_create_ip = getIpAddr(headers);
			
			//组装参数，用户生成统一下单接口的签名
			Map<String, String> packageParams = new HashMap<String, String>();
			packageParams.put("appid", appid);
			packageParams.put("mch_id", mch_id);
			packageParams.put("nonce_str", nonce_str);
			packageParams.put("body", body);
			packageParams.put("out_trade_no", findOrder.getOrderid());//商户订单号
			packageParams.put("total_fee", Integer.toString(findOrder.getTotalPrice()));//支付金额，这边需要转成字符串类型，否则后面的签名会失败
			packageParams.put("spbill_create_ip", spbill_create_ip);
			packageParams.put("notify_url", notify_url); 	//支付成功后的回调地址
			packageParams.put("trade_type", TRADETYPE);		//支付方式
			packageParams.put("openid", openid);
			   
	        String prestr = createLinkString(packageParams); // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串 
	        
	        //MD5运算生成签名，这里是第一次签名，用于调用统一下单接口
	        String mysign = sign(prestr, key, "utf-8").toUpperCase();
	        
	        //拼接统一下单接口使用的xml数据，要将上一步生成的签名一起拼接进去
	        String xml = "<xml>" + "<appid>" + appid + "</appid>" 
                    + "<body><![CDATA[" + body + "]]></body>" 
                    + "<mch_id>" + mch_id + "</mch_id>" 
                    + "<nonce_str>" + nonce_str + "</nonce_str>" 
                    + "<notify_url>" + notify_url + "</notify_url>" 
                    + "<openid>" + userinfo.getOpenid() + "</openid>" 
                    + "<out_trade_no>" + findOrder.getOrderid() + "</out_trade_no>" 
                    + "<spbill_create_ip>" + spbill_create_ip + "</spbill_create_ip>" 
                    + "<total_fee>" + findOrder.getTotalPrice() + "</total_fee>"
                    + "<trade_type>" + TRADETYPE + "</trade_type>" 
                    + "<sign>" + mysign + "</sign>"
                    + "</xml>";
			
			String wxresult = postToWx(xml,pay_url);
	        
	        // 将解析结果存储在HashMap中   
	        Map<String,String> map = doXMLParse(wxresult);
	        
			String return_code = (String) map.get("return_code");//返回状态码
	        
		    Map<String, Object> payParam = new HashMap<String, Object>(); //返回给小程序端需要的参数
	        if("SUCCESS".equals(return_code) && "SUCCESS".equals((String) map.get("result_code"))){   
				String prepay_id = (String) map.get("prepay_id");//返回的预付单信息 

				org.bson.Document update = new org.bson.Document();
				update.put("prepayid", prepay_id);
        		final MongoCollection<OrderFee> feecollection = database.getCollection("orderfee", OrderFee.class); 
        		feecollection.updateOne(eq("orderid", orderid),new org.bson.Document("$set", update));  //更新微信支付信息到费用信息表

	            payParam.put("nonceStr", nonce_str);
	            payParam.put("package", "prepay_id=" + prepay_id);
	            Long timeStamp = System.currentTimeMillis() / 1000;   
	            payParam.put("timeStamp", timeStamp + "");//这边要将返回的时间戳转化成字符串，不然小程序端调用wx.requestPayment方法会报签名错误
	            //拼接签名需要的参数
	            String stringSignTemp = "appId=" + appid + "&nonceStr=" + nonce_str + "&package=prepay_id=" + prepay_id+ "&signType=MD5&timeStamp=" + timeStamp;   
	            //再次签名，这个签名用于小程序端调用wx.requesetPayment方法
	            String paySign = sign(stringSignTemp, key, "utf-8").toUpperCase();
	            
				payParam.put("paySign", paySign);
				payParam.put("appid", appid);
	        } else {
				return new ReturnMessage("999","微信支付下单失败");
			}
			
	        ReturnMessage result = new ReturnMessage();
        	result.retParams.put("payParam", payParam);
        	return result;
		}catch(Exception e){
			e.printStackTrace();
		}
		return new ReturnMessage("999","微信支付下单失败");
    }

    /**
	 * StringUtils工具类方法
	 * 获取一定长度的随机字符串，范围0-9，a-z
	 * @param length：指定字符串长度
	 * @return 一定长度的随机字符串
	 */
	public static String getRandomStringByLength(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }
	/**
	 * 获取真实的ip地址
	 */
	public static String getIpAddr(HttpHeaders headers) {
		String clientip = "";
		String forward = headers.getHeaderString("X-Forwarded-For");
		LOG.info(forward);
	    if(ValidateUtils.isNotEmptyString(forward) && !"unKnown".equalsIgnoreCase(forward)){
	         //多次反向代理后会有多个ip值，第一个ip才是真实ip
	    	int index = forward.indexOf(",");
	        if(index != -1){
	            clientip = forward.substring(0,index);
			}else{
				clientip = forward;
			}
			
			if(ValidateUtils.isNotEmptyString(clientip) && !"unKnown".equalsIgnoreCase(clientip)){
				return clientip;
			}
		}

		clientip = headers.getHeaderString("Proxy-Client-IP");
		if(ValidateUtils.isNotEmptyString(clientip) && !"unKnown".equalsIgnoreCase(clientip)){
			return clientip;
		}

		clientip = headers.getHeaderString("WL-Proxy-Client-IP");
		if(ValidateUtils.isNotEmptyString(clientip) && !"unKnown".equalsIgnoreCase(clientip)){
			return clientip;
		}

		clientip = headers.getHeaderString("HTTP_CLIENT_IP");
		if(ValidateUtils.isNotEmptyString(clientip) && !"unKnown".equalsIgnoreCase(clientip)){
			return clientip;
		}

		clientip = headers.getHeaderString("HTTP_X_FORWARDED_FOR");
		if(ValidateUtils.isNotEmptyString(clientip) && !"unKnown".equalsIgnoreCase(clientip)){
			return clientip;
		}

		String XRealIP = headers.getHeaderString("X-Real-IP");
	    if(ValidateUtils.isNotEmptyString(XRealIP) && !"unKnown".equalsIgnoreCase(XRealIP)){
	       return XRealIP;
		}
	
		return clientip;
	}


	/**  
     * 签名字符串  
     * @param text需要签名的字符串  
     * @param key 密钥  
     * @param input_charset编码格式  
     * @return 签名结果  
     */   
    public static String sign(String text, String key, String input_charset) {   
        text = text + "&key=" + key;   
        return DigestUtils.md5Hex(getContentBytes(text, input_charset));   
    }   
    /**  
     * 校验签名字符串  
     *  @param text需要签名的字符串  
     * @param sign 签名结果  
     * @param key密钥  
     * @param input_charset 编码格式  
     * @return 签名结果  
     */   
    public static boolean verify(String text, String sign, String key, String input_charset) {   
		text = text+ "&key=" + key;
		LOG.info("mysigntext:" + text);  
		String mysign = DigestUtils.md5Hex(getContentBytes(text, input_charset)).toUpperCase();
		LOG.info("mysign:" + mysign);
        if (mysign.equals(sign)) {   
            return true;   
        } else {   
            return false;   
        }   
    }   
    /**  
     * @param content  
     * @param charset  
     * @return  
     * @throws SignatureException  
     * @throws UnsupportedEncodingException  
     */   
    public static byte[] getContentBytes(String content, String charset) {   
        if (charset == null || "".equals(charset)) {   
            return content.getBytes();   
        }   
        try {   
            return content.getBytes(charset);   
        } catch (UnsupportedEncodingException e) {   
            throw new RuntimeException("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);   
        }   
    }   
	
	/*
    private static boolean isValidChar(char ch) {   
        if ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z'))   
            return true;   
        if ((ch >= 0x4e00 && ch <= 0x7fff) || (ch >= 0x8000 && ch <= 0x952f))   
            return true;// 简体中文汉字编码   
        return false;   
    }  */ 
    /**  
     * 除去数组中的空值和签名参数  
     * @param sArray 签名参数组  
     * @return 去掉空值与签名参数后的新签名参数组  
     */   
    public static Map<String, String> paraFilter(Map<String, String> sArray) {   
        Map<String, String> result = new HashMap<String, String>();   
        if (sArray == null || sArray.size() <= 0) {   
            return result;   
        }   
        for (String key : sArray.keySet()) {   
            String value = sArray.get(key);   
            if (value == null || value.equals("") || key.equalsIgnoreCase("sign")   
                    || key.equalsIgnoreCase("sign_type")) {   
                continue;   
            }   
            result.put(key, value);   
        }   
        return result;   
    }   
    /**  
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串  
     * @param params 需要排序并参与字符拼接的参数组  
     * @return 拼接后字符串  
     */   
    public static String createLinkString(Map<String, String> params) {   
        List<String> keys = new ArrayList<String>(params.keySet());   
        Collections.sort(keys);   
        String prestr = "";   
        for (int i = 0; i < keys.size(); i++) {   
            String key = keys.get(i);   
            String value = params.get(key);   
            if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符   
                prestr = prestr + key + "=" + value;   
            } else {   
                prestr = prestr + key + "=" + value + "&";   
            }   
        }   
        return prestr;   
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

	/**
	 * @Description:微信支付结果通知	
	 * @return
	 */
	@POST
	@Path("/wxNotify")
	@Produces(MediaType.APPLICATION_XML)
	public String wxNotify(String notityXml) throws Exception{

		String resXml = "";
		LOG.info("接收到的报文：" + notityXml);

		if(ValidateUtils.isEmptyString(notityXml)){
			resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
			+ "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
			return resXml;
		}
	
		Map<String,String> map = doXMLParse(notityXml);
	    
		String returnCode = (String) map.get("return_code");
		if("SUCCESS".equals(returnCode)){
			//验证签名是否正确
			String wx_sign = (String)map.get("sign");
			map.remove("sign");//微信通知接口，sign不参与签名算法
			if(verify(createLinkString(map), wx_sign, key, "utf-8")){
				//首先查询订单，看订单状态是否已处理，已处理则直接返回成功。
				MongoDatabase database = MongoDBUtil.getDataBase();
        		final MongoCollection<Order> collection = database.getCollection("order", Order.class);
        		Order findOrder = 
                    collection.find(eq("orderid", map.get("out_trade_no"))).first();
				if(findOrder.getHasPaid()){
					resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
						+ "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
					
						return resXml;
				}

				//修改订单状态为已支付
				if("SUCCESS".equals(map.get("result_code"))){
					Map<String,Object> request = new HashMap<String,Object>();
					request.put("orderid", map.get("out_trade_no"));
					request.put("status", "1");
					request.put("hasPaid",true);
					OrderService.updateOrder(request);
				}
				
				//记录支付结果到结果表
				org.bson.Document doc = new org.bson.Document("orderid", map.get("out_trade_no"));
				for (Map.Entry<String, String> entry : map.entrySet()) {
					doc.append(entry.getKey(), entry.getValue());
				}
				MongoCollection<org.bson.Document> payResuCollection = database.getCollection("wxpayresult");
				payResuCollection.insertOne(doc);

				//发送提醒消息
				ContactMessageService.sendOrderremind(findOrder);
				//通知微信成功
				resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
				+ "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
			} else {
				resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
				+ "<return_msg><![CDATA[验证签名失败]]></return_msg>" + "</xml> ";
			}
		}else{
			//失败 也要给微信返回SUCCESS
			resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
			+ "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";

			LOG.error(notityXml);
		}

		LOG.info("微信支付通知处理结果:" + resXml);

		return resXml;
	}

	//是只返回交易状态还是返回所有参数的一个map？
	public Map<String,String> queryWxPayOrder(String orderid) throws DocumentException {
		//生成的随机字符串
		String nonce_str = getRandomStringByLength(32);
		//组装参数，用户生成统一下单接口的签名
		Map<String, String> packageParams = new HashMap<String, String>();
		packageParams.put("appid", appid);
		packageParams.put("mch_id", mch_id);
		packageParams.put("nonce_str", nonce_str);
		packageParams.put("out_trade_no", orderid);//商户订单号
		   
		String prestr = createLinkString(packageParams); // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串 
		//MD5运算生成签名，用于调用查询接口
		String mysign = sign(prestr, key, "utf-8").toUpperCase();

		//拼接查询接口使用的xml数据，要将上一步生成的签名一起拼接进去
		String xml = "<xml>" + "<appid>" + appid + "</appid>" 
		+ "<mch_id>" + mch_id + "</mch_id>" 
		+ "<nonce_str>" + nonce_str + "</nonce_str>" 
		+ "<out_trade_no>" + orderid + "</out_trade_no>" 
		+ "<sign>" + mysign + "</sign>"
		+ "</xml>";

		String wxresult = postToWx(xml,orderquery_url);

		// 将解析结果存储在HashMap中   
		Map<String,String> map = doXMLParse(wxresult);
		
		return map;

	}

	@POST 
    @Path("/WXRefund")      //退款
    @Produces("application/json;charset=UTF-8")
	public ReturnMessage refundForOrder(String orderid)
    {
		MongoDatabase database = MongoDBUtil.getDataBase();
        final MongoCollection<Order> collection = database.getCollection("order", Order.class);
        Order findOrder =  collection.find(eq("orderid", orderid)).first();

        try{
			//生成的随机字符串
			String nonce_str = getRandomStringByLength(32);
			
			//组装参数，生成申请退款接口的签名
			Map<String, String> packageParams = new HashMap<String, String>();
			packageParams.put("appid", appid);
			packageParams.put("mch_id", mch_id);
			packageParams.put("nonce_str", nonce_str);
			packageParams.put("out_trade_no", findOrder.getOrderid());//商户订单号
			packageParams.put("out_refund_no", "refund" + findOrder.getOrderid());//商户退款单号
			packageParams.put("total_fee", Integer.toString(findOrder.getTotalPrice())); //支付金额，这边需要转成字符串类型，否则后面的签名会失败
			packageParams.put("refund_fee", Integer.toString(findOrder.getTotalPrice())); //退款金额
			packageParams.put("notify_url", refundNotify_url); 	//退款通知的回调地址
			packageParams.put("refund_desc", "退款");		//支付方式
			   
	        String prestr = createLinkString(packageParams); // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串 
	        
	        //MD5运算生成签名，用于申请退款接口
	        String mysign = sign(prestr, key, "utf-8").toUpperCase();
	        
	        //拼接申请退款接口使用的xml数据，要将上一步生成的签名一起拼接进去
	        String xml = "<xml>" + "<appid>" + appid + "</appid>" 
                    + "<mch_id>" + mch_id + "</mch_id>" 
					+ "<nonce_str>" + nonce_str + "</nonce_str>" 
					+ "<out_trade_no>" + findOrder.getOrderid() + "</out_trade_no>" 
					+ "<out_refund_no>" + "refund" + findOrder.getOrderid() + "</out_trade_no>" 
					+ "<total_fee>" + findOrder.getTotalPrice() + "</total_fee>"
					+ "<refund_fee>" + findOrder.getTotalPrice() + "</refund_fee>"
                    + "<notify_url>" + refundNotify_url + "</notify_url>"                   
                    + "<refund_desc>" + "退款" + "</refund_desc>" 
                    + "<sign>" + mysign + "</sign>"
					+ "</xml>";
					
			String wxresult = postToWx(xml,refund_url);
	        
	        // 将解析结果存储在HashMap中   
	        Map<String,String> map = doXMLParse(wxresult);
	        String return_code = (String) map.get("return_code");//返回状态码

	        if("SUCCESS".equals(return_code) && "SUCCESS".equals((String) map.get("result_code"))){   
	            String refund_id = (String) map.get("refund_id");//返回的预付单信息   
	            ReturnMessage result = new ReturnMessage();
        		result.retParams.put("refund_id", refund_id);
        		return result;
	        } else {
				LOG.error(wxresult);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return new ReturnMessage("999","申请退款失败");
	}
	
	//退款通知接口
	@POST
	@Path("/wxRefundNotify")
	@Produces(MediaType.APPLICATION_XML)
	public String wxRefundNotify(String notityXml) throws Exception{

		String resXml = "";
		LOG.info("接收到的报文：" + notityXml);

		if(ValidateUtils.isEmptyString(notityXml)){
			resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
			+ "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
			return resXml;
		}

		Map<String,String> map = doXMLParse(notityXml);
	    
		String returnCode = (String) map.get("return_code");
		if("SUCCESS".equals(returnCode)){
			//验证签名是否正确
			String wx_sign = (String)map.get("sign");
			map.remove("sign");//微信通知接口，sign不参与签名算法
			if(verify(createLinkString(map), wx_sign, key, "utf-8")){
				//首先查询订单，看订单是否状态是否已处理，已处理则直接返回成功。
				MongoDatabase database = MongoDBUtil.getDataBase();
        		final MongoCollection<Order> collection = database.getCollection("wxrefundresult",Order.class);
        		Order findOrder = collection.find(eq("orderid", map.get("out_trade_no"))).first();
				if("3".equals(findOrder.getRefundStatus())){
					resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
						+ "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
					
						return resXml;
				}

				//修改订单状态为已退款
				Map<String,Object> request = new HashMap<String,Object>();
				request.put("orderid", map.get("out_trade_no"));
				request.put("refundStatus","3");
				OrderService.updateOrder(request);
				//记录退款结果到结果表
				org.bson.Document doc = new org.bson.Document("orderid", map.get("out_trade_no"));
				for (Map.Entry<String, String> entry : map.entrySet()) {
					doc.append(entry.getKey(), entry.getValue());
				}
				MongoCollection<org.bson.Document> payResuCollection = database.getCollection("wxrefundresult");
				payResuCollection.insertOne(doc);

				//使用小程序模板消息功能 给 客户发送已退款消息 最终退款结果是成功还是失败。

				//通知微信服务器接收退款通知成功
				resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
				+ "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
			} else {
				LOG.info("验证签名失败");
				resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
				+ "<return_msg><![CDATA[验证签名失败]]></return_msg>" + "</xml> ";
			}
		}else{
			//失败 也要给微信返回SUCCESS
			resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
			+ "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";

			LOG.error(notityXml);
		}

		LOG.info("微信退款通知处理结果:" + resXml);

		return resXml;
	}

	//查询退款接口 返回所有返回参数的Map
	public Map<String,String> queryWxRefund(String orderid) throws DocumentException {
		//生成的随机字符串
		String nonce_str = getRandomStringByLength(32);
		//组装参数，用户生成统一下单接口的签名
		Map<String, String> packageParams = new HashMap<String, String>();
		packageParams.put("appid", appid);
		packageParams.put("mch_id", mch_id);
		packageParams.put("nonce_str", nonce_str);
		packageParams.put("out_trade_no", orderid);//商户订单号
		   
		String prestr = createLinkString(packageParams); // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串 
		//MD5运算生成签名，用于调用查询接口
		String mysign = sign(prestr, key, "utf-8").toUpperCase();

		//拼接查询接口使用的xml数据，要将上一步生成的签名一起拼接进去
		String xml = "<xml>" + "<appid>" + appid + "</appid>" 
		+ "<mch_id>" + mch_id + "</mch_id>" 
		+ "<nonce_str>" + nonce_str + "</nonce_str>" 
		+ "<out_trade_no>" + orderid + "</out_trade_no>" 
		+ "<sign>" + mysign + "</sign>"
		+ "</xml>";

		String wxresult = postToWx(xml,refundquery_url);

		LOG.info("统一查询接口 返回XML数据：" + wxresult);

		// 将解析结果存储在HashMap中   
		Map<String,String> map = doXMLParse(wxresult);
	        
		return map;
	}

	//微信支付关闭订单接口
	public Map<String,String> closeWxOrder(String orderid) throws DocumentException {
		//生成的随机字符串
		String nonce_str = getRandomStringByLength(32);
		//组装参数，用户生成统一下单接口的签名
		Map<String, String> packageParams = new HashMap<String, String>();
		packageParams.put("appid", appid);
		packageParams.put("mch_id", mch_id);
		packageParams.put("nonce_str", nonce_str);
		packageParams.put("out_trade_no", orderid);//商户订单号
		   
		String prestr = createLinkString(packageParams); // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串 
		//MD5运算生成签名，用于调用查询接口
		String mysign = sign(prestr, key, "utf-8").toUpperCase();

		//拼接查询接口使用的xml数据，要将上一步生成的签名一起拼接进去
		String xml = "<xml>" + "<appid>" + appid + "</appid>" 
		+ "<mch_id>" + mch_id + "</mch_id>" 
		+ "<nonce_str>" + nonce_str + "</nonce_str>" 
		+ "<out_trade_no>" + orderid + "</out_trade_no>" 
		+ "<sign>" + mysign + "</sign>"
		+ "</xml>";

		String wxresult = postToWx(xml,close_url);
		// 将解析结果存储在HashMap中   
		Map<String,String> map = doXMLParse(wxresult);
	        
		return map;
	}

	private String postToWx(String xml,String req_url){
		LOG.info("调试模式统一微信接口 请求XML数据：" + xml);
		//调用查询接口，并接受返回的结果
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget restTarget = client.target(req_url);
		Response wxresponse = restTarget.request().post(Entity.xml(xml));
		String wxresult = wxresponse.readEntity(String.class);
		wxresponse.close();
		
		LOG.info("调试模式统一微信接口 返回XML数据：" + wxresult);

		return wxresult;
	}
}