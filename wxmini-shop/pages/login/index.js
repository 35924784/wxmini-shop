const App = getApp()

Page({
	data: {
		logged: !1
	},
    onLoad() {
    	const token = App.WxService.getStorageSync('token')
    	if(!token){
    		this.setData({
    			logged: false
    		})

    		return;
    	} else {
    		this.setData({
    			logged: true
    		})
    	}

    	App.HttpService.checkSession(token)
    	.then(res => {
			console.log(res)
			token && setTimeout(this.goIndex, 500)
			
		}).catch((err) => {
    		console.log(err) //error 1

    		this.setData({
    			logged: false
    		})

    		if(err.statusCode == 456){
    			App.WxService.removeStorageSync('token')
			} else {
				this.showLoginError()
			}
    		
  		})
    },
    onShow() {},
    login(e) {
    	console.log(e.detail.userInfo)
    	this.wechatSignIn(this.goIndex,e.detail.userInfo)
    },
    goIndex() {
    	App.WxService.switchTab('/pages/index/index')
    },
	showModal() {
		App.WxService.showModal({
            title: '友情提示', 
            content: '获取用户登录状态失败，请重新登录', 
            showCancel: !1, 
        })
	},
	showLoginError() {
		App.WxService.showModal({
            title: '友情提示', 
            content: '登录小程序失败，请稍后重试', 
            showCancel: !1, 
        })
	},
	wechatSignIn(cb,userInfo) {
		
		var requestData = {}

		App.WxService.login()
		.then(data => {
			console.log('wechatSignIn', data.code)
			if (!data.code){
				throw new Error("获取用户登录状态失败。")
			}

			requestData.code = data.code;
			console.log(userInfo)
			requestData.userInfo = userInfo;
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
				cb()
			} else if(data.retCode == '40029') {
				this.showModal()
			} else {
				this.showLoginError()
			}
		}).catch((err) => {
    		console.log(err) //error 1
    		this.showLoginError()
  		})
	},
})