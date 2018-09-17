<template>
    <el-container style="height:100%" >
        <el-aside :style="{width: widthData}" style="background-color: #F8F8F8;">
            
            <div class="list" v-if="showList">
                <ul>
                    
                    <li v-for="item in chats" :key="item.openid" :class="{active: item.openid === currentOpenId}" @click="selectChat(item)" >
                        <el-badge :is-dot="item.new > 0" class="item">
                        <img class="avatar" width="40" height="40" :alt="userInfoMap[item.openid].nickName" :src="userInfoMap[item.openid].avatarUrl" v-if="userInfoMap[item.openid] != null">
                        <p class="name"  v-if="userInfoMap[item.openid] != null">{{userInfoMap[item.openid].nickName}}</p>
                        </el-badge>
                    </li>
                
                </ul>
            </div>
        </el-aside>
        <el-main style="padding: 0">
            <div class="wxchat-container" style="backgroundColor: #efefef;">
                <div class="window" id="window-view-container" style="height:100%;width:100%">

                    <div class="title">
                      <el-button @click="ChangeWidth()" type='default' style="float:left">{{showList?'隐藏':'显示'}}</el-button>
                        <span v-text="contactNickname"></span>
                    </div>
                    <!-- main -->
                    <div class="container-main">
                        <div class="loading" v-if="topLoading">
                            <div class="loader">加载中...</div>
                        </div>
                        <div class="loading" v-if="currentOpenId==null">
                            <div style="margin-top: 300px;text-align:center; font-size: 16px;">
                                <span>未选择聊天记录</span>
                            </div>
                        </div>

                        <div style="height:90%">
                            <div class="message" style="height:90%">
                                <div class="loading" v-if="dataArray && dataArray.length==0">
                                    <div style="margin-top: 300px;text-align:center; font-size: 16px;">
                                        <span>未查找到聊天记录</span>
                                    </div>
                                </div>
                                <ul>
                                    <li v-for="(message, index) in dataArray" :key="message.id" :class="message.direction==2?'an-move-right':'an-move-left'">
                                        <p class="time">
                                            <span v-text="new Date(message.createTime).toLocaleString() "></span>
                                        </p>
                                        <p class="time system" v-if="message.type==10000">
                                            <span v-html="message.content"></span>
                                        </p>
                                        <div :class="'main' + (message.direction==2?' self':'')" v-else>
                                            <img class="avatar" width="45" height="45" :src="message.direction==2? ownerAvatarUrl: contactAvatarUrl">
                                            <!-- 文本 -->
                                            <div class="text" v-emotion="message.content" v-if="message.msgType=='text'"></div>

                                            <!-- 图片 -->
                                            <div class="text" v-else-if="message.msgType=='image'">
                                                <img :src="message.picUrl" class="image" alt="聊天图片" @click="onshowImage(message.picUrl)">
                                            </div>

                                            <!-- 其他 -->
                                            <div class="text" v-else-if="message.msgType!=10000" v-text="'[暂未支持的消息类型:'+ message.type +']\n\r' + message.content">

                                            </div>
                                        </div>
                                    </li>

                                </ul>
                            </div>
                            
                        </div>
                        
                    </div>
                    <div class="editortext" style="max-height: 20%"  v-if="currentOpenId">
                                
                                <el-input type="textarea" :autosize="{ minRows: 2, maxRows: 6}" resize="none" placeholder="" class="textarea" v-model="content" maxlength="180">
                                </el-input>
                                <el-button @click="send" type='success' class="send">发送</el-button>
                    </div>
                </div>

            </div>
        </el-main>
        <el-dialog  title="图片详情" :visible.sync="showImage" width="85%" @close="showImage=false">
        <img width="100%" :src="showImageUrl" alt="">     
        </el-dialog>
    </el-container>
    

</template>

<script>
    
    
    export default {
        name:'CustService',
        data() {
            return {
                showImage: false,
                showImageUrl:null,
                widthData: '24%',
                websocket: null,
                showList: true,
                contactNickname: '',
                ownerAvatarUrl: 'https://wx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTJib9E057SrYNWAtuAX7Hg11Rdbib71n4eSXrLCDXbQMiaeogL7wgLrOtn9q4tkp8RJxv6CcQiaFH9aoQ/132',
                contactAvatarUrl: './src/renderer/assets/avatar2.png',
                topLoading: false,
                dataArray: [],
                chats:[],          //会话列表
                userInfoMap:{},   //用户信息本地缓存 主要用于显示头像 昵称等
                currentOpenId: '',
                content: '',
                errreason:{
                    '-1':'系统繁忙',
                    '40001':'access_token 有误',
                    '40002':'不合法的凭证类型',
                    '40003':'不合法的 OpenID',
                    '45015':'回复时间超过限制',
                    '45047':'客服接口下行条数超过上限',
                    '48001':'api功能未授权'
                }
            }
        },
        created: function () {
            var height = window.screen.availHeight;
            if (height > 1000) {
                height = 730;
            }

            console.log(height)

            //如果浏览器支持WebSocket
            if(window.WebSocket){  
                var websocket = new WebSocket("wss://www.example.com/websocket");  //获得WebSocket对象

                const self = this
                
                //当有消息过来的时候触发
                websocket.onmessage = function(event){ 
                    console.log("onmessage")

                    var data = JSON.parse(event.data);
                    console.log(data)
                    if(data.type=='chats'){
                      var chats =  data.content
                      console.log(chats)
                      self.chats = chats
                      var openids = []
                      for (var i = chats.length - 1; i >= 0; i--) {
                          openids.push(chats[i].openid)
                          
                          if(chats[i].messages.slice(-1).direction == 1){
                            chats[i].new = 1   //new > 0 会话列表会显示个红点
                          }
                      }
                      console.log(JSON.stringify({'type':'getUser','openids': openids}))
                      //发送获取用户信息请求
                      websocket.send(JSON.stringify({'type':'getUser','openids': openids}))
                    }

                    if(data.type=='users'){
                      var users =  data.content

                      console.log(users)
                      for (var i = users.length - 1; i >= 0; i--) {
                          //转换为map方便渲染页面
                          self.$set(self.userInfoMap, users[i].openid, users[i])
                      }
                      
                      if(!self.currentOpenId){
                        self.selectChat(self.chats[0])
                      }
                    }

                    if(data.type=='message'){
                      var message = data.content
                      var find = false; //已有的会话
                      for(var i=0; i< self.chats.length; i++){
                        if(self.chats[i].openid == message.openid){
                          self.chats[i].messages.push(message)   //如果找到就将这条信息加入已有信息列表
                          if(self.currentOpenId != message.openid){
                            self.chats[i].new = (self.chats[i].new?self.chats[i].new:0)+1
                          }
                          
                          find = true;
                        }
                      }

                      if(!find){
                        self.chats.push({"openid":message.openid,'lastTime':message.createTime,'messages':[message],'new':1})
                        websocket.send(JSON.stringify({'type':'getUser','openids': [message.openid]}))
                      }

                      if(self.currentOpenId != message.openid){
                            self.$message({ message: '其他用户发来新消息！',  type: 'success' });
                      }
                      
                    }

                    if(data.type=='sendfail'){
                        var errcode = data.content

                        self.$message({ message: '发送失败：'+self.errreason[errcode],  type: 'warning',duration:2000 });
                    }  

                }
                
                //连接关闭的时候触发
                websocket.onclose = function(event){

                    console.log("onclose")
                    self.$message({ duration: 0, message: '连接关闭，请退出重进', type: 'error' });
                }
                
                //连接打开的时候触发
                websocket.onopen = function(event){
                    console.log("onopen")
                }

                this.websocket =  websocket;
            }else{
                alert("浏览器不支持WebSocket");
            }
        },
        methods: {
            ChangeWidth() {
                if (this.showList) {
                    this.showList = false;
                    this.widthData = '0%'
                } else {
                    this.showList = true;
                    this.widthData = '24%'
                }
            },
            selectChat(chat) {

                this.currentOpenId = chat.openid
                this.dataArray = chat.messages
                this.contactAvatarUrl =  this.userInfoMap[this.currentOpenId]?this.userInfoMap[this.currentOpenId].avatarUrl:''
                this.contactNickname = this.userInfoMap[this.currentOpenId]?this.userInfoMap[this.currentOpenId].nickName:''
                chat.new = 0    //选择后将新消息标记为0 会话列表不显示红点
            },
            onshowImage(url) {
                this.showImageUrl = url
                this.showImage = true;
            },
            //点击向上加载数据
            getUpperData() {
                console.log("need add")
            },
            send() {
              console.log("send")
              console.log(this.content.length)
                if (this.content.length <= 0) {
                    return;
                }

                //使用websocket 发送通知
                if(this.websocket.readyState == WebSocket.OPEN) { //如果WebSocket是打开状态
                    this.websocket.send(JSON.stringify({'type':'replyMsg','msgContent':this.content,'toOpenid':this.currentOpenId})); //send()发送消息
                }
                
                
                this.content = '';
            }

        }
    }
</script>

<style>
    h1,
    h2 {
        font-weight: normal;
    }

    ul {
        list-style-type: none;
        padding: 0;
    }

    .list li {
        padding: 8px 10px;
        border-bottom: 1px solid #E8E8E8;
        cursor: pointer;
        transition: background-color .1s;
    }

    .list .name {
        vertical-align: middle;
    }

    .list .avatar {
        border-radius: 2px;
    }

    .list .name {
        display: inline-block;
        margin: 0 0 0 15px;
    }

    li:hover {
        color: #D0D0D0;
        background-color: #F0F0F0;
        cursor: pointer;
        font-weight: bold;
    }

    .switch {
        background: #fff;
        display: inline-block;
        padding: 8px 24px;
        color: green;
    }

    .wxchat-container {
        width: 100%;
        height: 100%;
    }
    .container-main {
        width: 100%;
        height: 100%;
        overflow:hidden
    }

    .window {
        box-shadow: 1px 1px 20px -5px #000;
        background: #F5F5F5;
        margin: 0 auto;
        overflow: hidden;
        padding: 0;
        height: 100%;
        position: relative;
        z-index: 101;
    }

    .title {
        background: #ccc;
        text-align: center;
        color: #2e3138;
        width: 100%;
        height: 40px;
        line-height: 40px;
        font-size: 14px;
    }

    .loading {
        text-align: center;
        color: #b0b0b0;
        line-height: 100px;
    }

    .message {
        height:90%;
        -webkit-overflow-scrolling:touch;
        padding: 10px 15px;
        overflow-y: auto;
        background-color: #F5F5F5;
        overflow-x: hidden;
    }

    .message li {
        margin-bottom: 15px;
        left: 0;
        position: relative;
        display: block;
    }

    .message .time {
        margin: 10px 0;
        text-align: center;
    }

    .message .text {
        display: inline-block;
        position: relative;
        padding: 0 10px;
        max-width: calc(100% - 75px);
        min-height: 35px;
        line-height: 2.1;
        font-size: 15px;
        padding: 6px 10px;
        text-align: left;
        word-break: break-all;
        background-color: #fff;
        color: #000;
        border-radius: 4px;
        box-shadow: 0px 1px 7px -5px #000;
    }

    .message .avatar {
        float: left;
        margin: 0 10px 0 0;
        border-radius: 3px;
        background: #fff;
    }

    .message .time>span {
        display: inline-block;
        padding: 0 5px;
        font-size: 12px;
        color: #fff;
        border-radius: 2px;
        background-color: #DADADA;
    }

    .message .system>span {
        padding: 4px 9px;
        text-align: left;
    }

    .message .text:before {
        content: " ";
        position: absolute;
        top: 9px;
        right: 100%;
        border: 6px solid transparent;
        border-right-color: #fff;
    }

    .message .self {
        text-align: right;
    }

    .message .self .avatar {
        float: right;
        margin: 0 0 0 10px;
    }

    .message .self .text {
        background-color: #9EEA6A;
    }

    .message .self .text:before {
        right: inherit;
        left: 100%;
        border-right-color: transparent;
        border-left-color: #9EEA6A;
    }

    .message .image {
        max-width: 200px;
        max-height: 200px;
    }

    img.static-emotion-gif,
    img.static-emotion {
        vertical-align: middle !important;
    }

    .an-move-left {
        left: 0;
        animation: moveLeft .7s ease;
        -webkit-animation: moveLeft .7s ease;
    }

    .an-move-right {
        left: 0;
        animation: moveRight .7s ease;
        -webkit-animation: moveRight .7s ease;
    }

    @keyframes moveRight {
        0% {
            left: -20px;
            opacity: 0
        }
        ;
        100% {
            left: 0;
            opacity: 1
        }
    }

    @-webkit-keyframes moveRight {
        0% {
            left: -20px;
            opacity: 0
        }
        ;
        100% {
            left: 0px;
            opacity: 1
        }
    }

    @keyframes moveLeft {
        0% {
            left: 20px;
            opacity: 0
        }
        ;
        100% {
            left: 0px;
            opacity: 1
        }
    }

    @-webkit-keyframes moveLeft {
        0% {
            left: 20px;
            opacity: 0
        }
        ;
        100% {
            left: 0px;
            opacity: 1
        }
    }

    .editortext {
        border-top: solid 1px #ddd;
        border-bottom: solid 1px #ddd;
        position: fixed;
        bottom: 0;
        width: 100%;
    }

    .send {
        position: fixed;
        bottom: 1px;
        right: 0;
        padding: 5px 5px;
        margin: 1px;
        font-size: 16px;
        background-color: #15b708;
        border: solid 1px #bbb;
        border-radius: 10px;
        cursor: pointer;
        opacity: 0.4;
    }

    .textarea {
        padding: 1px;
        font-size: 15px;
        height: 100%;
        width: 100%;
        border: none;
        outline: none;
        background-color: #f5f7fa;
    }
</style>