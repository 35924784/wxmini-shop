# wxmini-shop
微信小程序商城

# 简介
    wxmini-shop包含3部分，微信小程序，前台管理界面，商城后台服务。
    前后端分离，通过Rest接口交互。

    微信小程序： wxmini-program  下载安装 微信web开发者工具 导入即可。

    前台管理界面：vueShopAdmin 技术框架 vue、 element-ui、electron-vue。使用 electron-vue 既可以打包成 electron客户端，也可以编译作为web资源文件。
    electron-vue文档:https://simulatedgreg.gitbooks.io/electron-vue/content/cn/ 

    商城后台服务：appservice  采用Java，主要技术框架：Netty、RestEasy、MongoDB、ehcache缓存、quartz定时任务。

    老婆说要在微信上卖点东西，所以做了这个，个人独立完成，没想过要做多强大多完善，都是直接怼，后台连Spring也都没用，完全是怎么简单怎么来的。。  安全性也只有简单的session校验，估计漏洞不少。。   

    系统运行截图，请查看 截图 目录。

## 1、微信小程序
没什么可说的，下载安装 微信web开发者工具 导入即可。


## 2、前台管理界面

推荐使用yarn
```
1、 yarn run build:web   生成web资源文件上传到后台appservice目录的web目录下 

```

管理界面个人觉得稍微原创一点的特性：
客服消息会话界面，目前微信小程序有网页版客服工具可以使用，但是只能电脑登陆，太不方便，所以做了个简单的微信客服消息回复界面。支持微信表情。  
参考 https://github.com/ANBUZHIDAO/qq-wechat-emotion-parser  fork自https://github.com/buddys/qq-wechat-emotion-parser  
所做的修改：修复了一个小bug，改造了一下支持微信表情


## 3、后台服务
appservice，使用Netty+RestEasy+MongoDB  
ehcache做缓存。  

```
1、 开发状态下，执行 mvn compile exec:java -Dexec.mainClass="com.mingyue.App" 可启动 

2、 mvn package   打成一个可执行jar包。 java -jar appservice-1.0-SNAPSHOT.jar可运行  
   执行前需配置好 conf下的相关配置参数

打包完成后，可执行jar包运行时以下目录：
conf ----参数配置
logs ----日志
groovy ---groovy算费脚本
appservice-1.0-SNAPSHOT.jar  ----可执行jar包
manage.sh  ----启停脚本
web   ---- html,js,css前台管理界面资源文件
MP_verify_2xxxxxxxxN.txt  ---微信小程序相关校验文件

3、生产环境真正对接微信，微信有校验证书，当前证书是我用 keytool -genkey -keysize 2048 -validity 1700 -keyalg RSA -keypass passwd_123 -storepass passwd_123 -keystore local.jks  无法通过微信校验。  
生产可用的证书可以到阿里云申请免费的，自行百度。
```
