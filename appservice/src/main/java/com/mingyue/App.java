package com.mingyue;

import java.util.ArrayList;
import java.util.List;

import com.mingyue.restapi.AuthHttpRequestPreprocessor;
import com.mingyue.restapi.CharsetHttpRequestPreprocessor;
import com.mingyue.restapi.address.AddressService;
import com.mingyue.restapi.admin.AdminService;
import com.mingyue.restapi.banner.BannerService;
import com.mingyue.restapi.category.CategoryService;
import com.mingyue.restapi.classify.ClassifyService;
import com.mingyue.restapi.goods.GoodsService;
import com.mingyue.restapi.hellodemo.HelloWorld;
import com.mingyue.restapi.image.ImageService;
import com.mingyue.restapi.image.ThumbnailService;
import com.mingyue.restapi.message.ContactMessageService;
import com.mingyue.restapi.order.CartService;
import com.mingyue.restapi.order.OrderFeeService;
import com.mingyue.restapi.order.OrderService;
import com.mingyue.restapi.pay.WXPay;
import com.mingyue.restapi.qrcode.QRCodeService;
import com.mingyue.restapi.user.UserExtService;
import com.mingyue.restapi.user.UserService;
import com.mingyue.task.AccessTokenJob;
import com.mingyue.task.ClearCartJob;
import com.mingyue.task.ReloadConfigJob;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.plugins.server.netty.NettyJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import io.netty.channel.ChannelHandler;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final int port = 443 ;

    private static Logger LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    private static int maxRequestSize = 10 * 1024 * 1024;

    private static NettyJaxrsServer nettyServer;

    private static ResteasyDeployment deployment;

    static final boolean SSL = true;
    static final int WEBSOCKET_PORT = 8443;

    public static void main( String[] args )
    {
        LOG.info( "Hello World!" );
        try {
			start();
		} catch (Exception e) {

			e.printStackTrace();
		}
    }

    public static void start() throws Exception
    {
        deployment = new ResteasyDeployment();
        //deployment.getProviderClasses().add("com.mingyue.restapi.ReadCharsetInterceptor");      

        List<Object> instances = new ArrayList<Object>();
        instances.add(new HelloWorld());
        instances.add(new GoodsService());
        instances.add(new CategoryService());
        instances.add(new ImageService());
        instances.add(new ThumbnailService());
        instances.add(new ClassifyService());
        instances.add(new AddressService());
        instances.add(new UserService());
        instances.add(new AdminService());
        instances.add(new BannerService());
        instances.add(new CartService());
        instances.add(new OrderService());
        instances.add(new OrderFeeService());
        instances.add(new WXPay());
        instances.add(new QRCodeService());
        instances.add(new UserExtService());
        instances.add(new ContactMessageService());
        deployment.getResources().addAll(instances);


        nettyServer = new NettyJaxrsServer();
        nettyServer.setDeployment(deployment);
        nettyServer.setPort(port);
        nettyServer.setRootResourcePath("/");
        nettyServer.setMaxRequestSize(maxRequestSize);  //最大10M 估计能传几M的图片。。
        nettyServer.setSecurityDomain(null);
        nettyServer.setSSLContext(SSLContextFactory.getSslContext());

        final List<ChannelHandler> httpChannelHandlers = new ArrayList<ChannelHandler>();
        httpChannelHandlers.add(new HttpStaticFileServerHandler());
        //httpChannelHandlers.add(new WebSocketServerCompressionHandler());
        //httpChannelHandlers.add(new WebSocketServerProtocolHandler("/websocket", null, true));
        //httpChannelHandlers.add(new WebSocketFrameHandler());
        
        nettyServer.setHttpChannelHandlers(httpChannelHandlers);
        nettyServer.start();

        startJobScheduler();  //启动定时任务

        //启动之后获取Dispatcher 添加http拦截器
        deployment.getDispatcher().addHttpPreprocessor(new CharsetHttpRequestPreprocessor());
        deployment.getDispatcher().addHttpPreprocessor(new AuthHttpRequestPreprocessor());
        //添加个异常session处理器
        deployment.getDispatcher().getProviderFactory()
                .getExceptionMappers().put(InvalidSessionException.class, new InvalidSessionExceptionHandler());
        
        LOG.error("Netty rest server started on port: " + port);
        LOG.info("Netty rest server started on port: " + port);

        //startWebSocketServer();   //启动WebSocket服务  --- 不用另外启动了，直接用443端口
    }

    public static void startJobScheduler() throws InterruptedException, SchedulerException {
        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        // 开始
        scheduler.start();
        //获取access_token的定时任务
        JobDetail job = JobBuilder.newJob(AccessTokenJob.class).withIdentity("accesstoken_job", "wxgroup").build();
        //3分钟执行一次
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("accesstoken_trigger", "wxgroup").startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(180).repeatForever()).build();

        //加载配置的定时任务 --- 虽然没用到过，改了配置目前还是重启吧
        JobDetail configjob = JobBuilder.newJob(ReloadConfigJob.class).withIdentity("cofiguration_job", "sysgroup").build();
        //60秒执行一次
        Trigger configtrigger = TriggerBuilder.newTrigger().withIdentity("config_trigger", "sysgroup").startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(60).repeatForever()).build();
        
        //清理购物车的定时任务
        JobDetail clearcartjob = JobBuilder.newJob(ClearCartJob.class).withIdentity("clearcart_job", "sysgroup").build();
        //2小时执行一次
        Trigger clearcarttrigger = TriggerBuilder.newTrigger().withIdentity("clearcart_trigger", "sysgroup").startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(2).repeatForever()).build();
        
        // Tell quartz to schedule the job using our trigger
        scheduler.scheduleJob(job, trigger);
        scheduler.scheduleJob(configjob, configtrigger);
        scheduler.scheduleJob(clearcartjob, clearcarttrigger);

    }
    
}
