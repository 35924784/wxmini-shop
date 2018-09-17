const App = getApp()

Page({
    data: {
        indicatorDots: !0,
        vertical: !1,
        interval: 3000,
        duration: 1000,
        current: 0,
        curIndex: 0,
        goods: {},
        num: 1,
        minusStatus: 'disabled',
        showShare:true,
        showModal:false,
        selected:-1
    },
    swiperchange(e) {
        this.setData({
            current: e.detail.current, 
        })
    },
    onLoad(option) {
        console.log(option)
        this.goods = App.HttpResource('/goods/:id', {id: '@id'})
        this.setData({
            id: option.id
        })

        if(option.shareid != null ){
            App.WxService.setStorageSync('shareid', option.shareid)
        }

        const token = App.WxService.getStorageSync('token')
        if(!token){
            console.log("login")
            this.setData({'showModal': true})
        }

    },
    onShow() {
        this.getDetail(this.data.id)

        const token = App.WxService.getStorageSync('token')
        if(!token){
            wx.hideShareMenu()   //如果没有token隐藏分享转发按钮
            this.setData({'showShare': false})
            return;
        }

        App.HttpService.checkSession(token)
        .then(res => {
            console.log(res) //session有效则不隐藏转发按钮
            const data = res.data
            if (data.retCode == '0') {
                this.setData({
                    userid: data.retParams.userid
                })
            } else {
                wx.hideShareMenu()   //如果没有token隐藏分享转发按钮
                this.setData({'showShare': false})
                return;
            }             
        }).catch((err) => {
            console.log(err)
            wx.hideShareMenu() //如果token校验失败也隐藏分享转发按钮
            this.setData({'showShare': false})

            if(err.statusCode == 456){
                App.WxService.removeStorageSync('token')
            }
            
        })
    },
    addCart(e) {
        const token = App.WxService.getStorageSync('token')
        if(!token){
            console.log("login")
            this.setData({'showModal': true})
            return;
        } 

        const goodscode = this.data.goods.code
        const num = this.data.num
        const selected = this.data.selected
        const spec = selected >= 0 ? this.data.goods.specs[selected][0] : 'default'  //所选规格编码
        
        App.HttpService.addCartByUser(goodscode, num, spec)
        .then(res => {
            const data = res.data
            console.log(data)
            if (data.retCode == '0') {
                App.WxService.showToast({
                    title   : "加入成功", 
                    icon    : 'success', 
                    duration: 1500
                }).then(res => {
                    console.log(res)
                    App.WxService.switchTab('/pages/cart/index')
                })
               
            } else{
                App.WxService.showToast({
                    title   : "加入购物车失败，请稍后重试", 
                    icon    : 'none', 
                    duration: 1500, 
                })
            }
        })
    },
    previewImage(e) {
        //console.log(this.data.goods)
        const urls = this.data.goods.frontImages   //不判空，顶多报错看不了呗
        const index = e.currentTarget.dataset.index
        const current = urls[Number(index)]
        
        App.WxService.previewImage({
            current: current, 
            urls: urls, 
        })
    },
    askKefu(e){
        console.log(e)
        App.WxService.navigateTo('/pages/publicAcct/index')
    },
    showToast(message) {
        App.WxService.showToast({
            title   : message, 
            icon    : 'success', 
            duration: 1500, 
        })
    },
    showLoginError() {
        App.WxService.showModal({
            title: '友情提示', 
            content: '登录小程序失败，请稍后重试', 
            showCancel: !1, 
        })
    },
    getDetail(id) {
    	// App.HttpService.getDetail(id)
        this.goods.getAsync({id: id})
        .then(res => {
            const data = res.data
            console.log(data)
        	//if (data.retCode == '0') {
                data.frontImages = []
                data.images = data.images || []  //避免空
                data.images.forEach(n => {
                    n = App.renderImage("/thumbnail/" + n + ".jpg") 
                    console.log(n)
                    data.frontImages.push(n)
                })

                data.detailImages = []
                data.detailImgs = data.detailImgs || []
                data.detailImgs.forEach(n => {
                    n = App.renderImage("/thumbnail/" + n + ".jpg") 
                    console.log(n)
                    data.detailImages.push(n)
                })

        		this.setData({
                    'goods': data, 
                    total: data.images.length, 
                })
        	//}
        })
    },
    bindTap(e) {
        const index = parseInt(e.currentTarget.dataset.index);
        this.setData({
            curIndex: index
        })
    },
    /* 点击减号 */
    bindMinus: function() { 
      var num = this.data.num; 
      // 如果大于1时，才可以减 
      if (num > 1) { 
        num --; 
      } 
      // 只有大于一件的时候，才能normal状态，否则disable状态 
      var minusStatus = num <= 1 ? 'disabled' : 'normal'; 
      // 将数值与状态写回 
      this.setData({ 
        num: num, 
        minusStatus: minusStatus 
      }); 
    }, 
    /* 点击加号 */
    bindPlus: function() { 
      var num = this.data.num; 
      num ++; 
      // 只有大于一件的时候，才能normal状态，否则disable状态 
      var minusStatus = num < 1 ? 'disabled' : 'normal'; 
      this.setData({ 
        num: num, 
        minusStatus: minusStatus 
      }); 
    }, 
    /* 输入框事件 */
    bindManual: function(e) { 
      var num = e.detail.value; 
      this.setData({ 
        num: num 
      }); 
    },
    chooseSpec: function(e){
        this.setData({'showSpecModal': true})
    },
    choose: function(e){
        const index = e.currentTarget.dataset.index
        console.log(index)
        if(index >= 0){
            this.setData({'selected': index})
        } else {
            this.setData({'selected': -1})
        }
        
    },
    closeChooseSpec: function(e){
        this.setData({'showSpecModal': false})
    },
    onCancel: function(e){
        this.setData({'showModal': false})
    },
    wechatSignIn(e) {

        console.log(e.detail.userInfo)
        
        var requestData = {}

        App.WxService.login()
        .then(data => {
            console.log('wechatSignIn', data.code)
            if (!data.code){
                throw new Error("获取用户登录状态失败。")
            }

            requestData.code = data.code;
            requestData.userInfo = e.detail.userInfo;
            const shareid = App.WxService.getStorageSync('shareid')
            if(!shareid){
                requestData.userInfo.shareid = shareid
            }

        })
        .then(res2 => {
            console.log(requestData);
            //将requestData传给 自己的后台服务器
            return App.HttpService.wechatSignIn(requestData)
        })
        .then(res => {
            console.log(res)

            const data = res.data
            console.log('wechatSignIn', data)

            if (data.retCode == '0') {
                App.WxService.setStorageSync('token', data.retParams.my_session)
                this.setData({'showModal': false})
            } else if(data.retCode == '40029') {
                this.showLoginError()
            } else {
                this.showLoginError()
            }
        }).catch((err) => {
            console.log(err) //error 1
            this.showLoginError()
        })

        this.setData({'showModal': false})
    },
    onShareAppMessage: function (res) {
      if (res.from === 'button') {
        // 来自页面内转发按钮
        console.log(res.target)
      }

      return {
        title: '文玉茶叶',
        path: '/pages/index/index?goGoodsDetail=true&id=' + this.data.id + '&shareid=' + this.data.userid ,
        success: function(res) {
          console.log(res)
        },
        fail: function(res) {
          console.log(res)
        }
      }
    }
})