const App = getApp()

Page({
	data: {
		userInfo: {},
		userscore: 0,
		items: [
			{
				icon: '../../assets/images/iconfont-order.png',
				text: '我的订单',
				path: '/pages/order/list/index'
			}, 
			{
				icon: '../../assets/images/iconfont-addr.png',
				text: '收货地址',
				path: '/pages/address/list/index'
			}, 
			{
				icon: '../../assets/images/iconfont-kefu.png',
				text: '分享到朋友圈',
				path: '/pages/qrcode/index'
			}, 
			{
				icon: '../../assets/images/iconfont-help.png',
				text: '常见问题',
				path: '/pages/help/index'
			}
		],
		settings: [
			{
				icon: '../../assets/images/iconfont-clear.png',
				text: '清除缓存',
				path: '0.0KB'
			}, 
			{
				icon: '../../assets/images/iconfont-about.png',
				text: '关于我们',
				path: '/pages/about/index'
			}, 
		]
	},
	onLoad() {
		this.getUserScore()
		this.getStorageInfo()

	},
	navigateTo(e) {
		const index = e.currentTarget.dataset.index
		const path = e.currentTarget.dataset.path

		switch(index) {
			default:
				App.WxService.navigateTo(path)
		}
    },
    getUserScore() {
        App.HttpService.getUserScore()
        .then(res => {
            const data = res.data
            console.log(data)
            if (data.retCode == '0' && data.retParams.userscore) {
                this.setData({
                    userscore:data.retParams.userscore
                })
            } 
        }).catch((err) => {
            console.log(err)             
        })
    },
    getStorageInfo() {
    	App.WxService.getStorageInfo()
    	.then(data => {
    		console.log(data)
    		this.setData({
    			'settings[0].path': `${data.currentSize}KB`
    		})
    	})
    },
    bindtap(e) {
    	const index = e.currentTarget.dataset.index
		const path = e.currentTarget.dataset.path

		switch(index) {
			case 0:
				App.WxService.showModal({
		            title: '友情提示', 
		            content: '确定要清除缓存吗？', 
		        })
		        .then(data => data.confirm == 1 && App.WxService.clearStorage())
				break
			default:
				App.WxService.navigateTo(path)
		}
    }
})