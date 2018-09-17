package com.mingyue.restapi.qrcode;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import com.mingyue.common.AccessTokenUtil;
import com.mingyue.common.CacheHelper;
import com.mingyue.restapi.user.User;
import com.mingyue.restapi.user.UserService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import net.coobird.thumbnailator.Thumbnails;

@Path("/restapi/salesqrcode")
public class QRCodeService{

    private static Logger LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    private static final String qrcode_url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=ACCESS_TOKEN";

    //获取用户专属二维码  用于转发朋友圈 其他人通过二维码计入商城后购买商品后给分享者返现
    @GET
    @Produces("image/jpeg,image/png")
    public byte[] getSalesQRCode(@QueryParam("token") String sessionid) throws IOException
    {
        User userinfo = UserService.getUserInfoBySession(sessionid);
        //先从缓存中取
        org.ehcache.Cache<String,byte[]> qrCache = CacheHelper.getQrcodeCache();

        if (qrCache.get(userinfo.getUserid()) != null)
        {
            LOG.info("return from cache.");
            return qrCache.get(userinfo.getUserid());
        }
        
        Map<String,Object> request = new HashMap<String,Object>();
        request.put("scene", "userid=" + userinfo.getUserid());
        request.put("width", "400");
        request.put("auto_color", true);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        LOG.debug("统一微信获取二维码接口 请求json：" + json);
        String targeturl = qrcode_url.replaceFirst("ACCESS_TOKEN", AccessTokenUtil.getAccessToken());
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget restTarget = client.target(targeturl);
        Response wxresponse = restTarget.request().post(Entity.json(json));
        
		byte[] qrbytes = wxresponse.readEntity(byte[].class);   //二维码
        wxresponse.close();
        
        /*
        File file = new File("QRcode.jpg");
        file.createNewFile();
        FileUtils.writeByteArrayToFile(file, qrbytes);
        */
        //获取头像       
        targeturl = userinfo.getAvatarUrl();
		restTarget = client.target(targeturl);
        Response logosponse = restTarget.request().get();
		byte[] logobytes = logosponse.readEntity(byte[].class);   //头像
        logosponse.close();

        ByteArrayInputStream in = new ByteArrayInputStream(qrbytes);   
        BufferedImage image = ImageIO.read(in);    //二维码

        ByteArrayInputStream logoin = new ByteArrayInputStream(logobytes);
        BufferedImage logoimage = ImageIO.read(logoin);      //头像
        logoimage = Thumbnails.of(logoimage).size(100, 100).outputFormat("jpg").asBufferedImage();//头像缩小为100*100
        
        //开始画大图
        int width = 430;
		int height = 620;
        
        BufferedImage bufferedImage = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
        
        //得到画笔对象
        Graphics g = bufferedImage.getGraphics();
        g.setColor(Color.WHITE); //设置白色
        g.fillRect(0,0,width,height);//填充整个屏幕，将背景色改为白色
        //将小图片绘到大图片上
        g.drawImage(logoimage, 5, 5, null);
        g.drawImage(image, 0, 160, null);
        //设置颜色。
        g.setColor(Color.BLACK); //设置黑色来写字
        g.setFont(new Font("宋体", Font.PLAIN, 15));
        g.drawString("我是" + userinfo.getNickName(), 120, 35);
        g.drawString("我为文玉茶叶代言", 120, 65);
        g.drawString("分享轻松得积分", width/2-100, 140);
        g.drawString("长按图片识别图中的小程序码", width/2-100, 600);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", baos);
        byte[] bytes = baos.toByteArray();
        //ImageIO.write(bufferedImage, "JPG",new File("QRcode2.jpg"));
        //将生成的图片保存到缓存中，不保存到数据库了。
        qrCache.put(userinfo.getUserid(), bytes);
        return bytes;
    }

}