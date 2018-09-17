const App = getApp()

Page({
    data: {
        activeIndex: 0,
        navList: [{"name":"热销商品","_id":"hot"},{"name":"新品上市","_id":"new"}],
        indicatorDots: !0,
        current: 0,
        interval: 3000,
        duration: 1000,
        circular: !0,
        goods: {},
        prompt: {
            hidden: !0,
        },
        logged: false
    },
    swiperchange(e) {
        // console.log(e.detail.current)
    },
    previewImage: function (e) {
        wx.previewImage({
            urls: [e.currentTarget.dataset.src]
        })
    },
    onLoad(option) {
        this.banner = App.HttpResource('/banner/:id', {id: '@id'})

        this.initData()
        this.getBanners()
        this.getList()

        console.log('shareid:' + option.shareid)
        if(option.shareid != null){
            App.WxService.setStorageSync('shareid', option.shareid)
        }

        if(option.goGoodsDetail != null && option.goGoodsDetail=="true" && option.id){
            wx.showLoading({ title: 'loading', })
            setTimeout(function () {
                wx.hideLoading()
                wx.navigateTo({
                    url: '/pages/goods/detail/index?id=' + option.id,
                })
            }, 500)
        }
        
    },
    onShow() {
        const token = App.WxService.getStorageSync('token')
        if(!token){
            wx.hideShareMenu()   //如果没有token也隐藏分享转发按钮
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
                return;
            }            
        }).catch((err) => {
            console.log(err) 
            wx.hideShareMenu() //如果token校验失败没获取到userid也隐藏分享转发按钮

            if(err.statusCode == 456){
                App.WxService.removeStorageSync('token')
            }
            
        })
    },
    initData() {
        const type = ''
        const goods = {
            items: [],
            params: {     //查询时的请求参数
                code : "hot"
            },
            totalNum: 0
        }

        this.setData({
            goods: goods
        })
    },
    navigateTo(e) {
        console.log(e)
        App.WxService.navigateTo('/pages/goods/detail/index', {
            id: e.currentTarget.dataset.id
        })
    },
    search() {
        App.WxService.navigateTo('/pages/search/index')
    },
    getBanners() {
    	// App.HttpService.getBanners({is_show: !0})
        this.banner.queryAsync({is_show: !0})
        .then(res => {
            const data = res.data
        	console.log(data)
        	if (data.retCode == '0') {
                data.retParams.bannerList.forEach(n => n.path = App.renderImage("/thumbnail/" + n.fileid))
        		this.setData({
                    images: data.retParams.bannerList
                })
        	}
        })
    },
    getList() {
        const goods = this.data.goods
        const params = goods.params
        console.log(params)

        App.HttpService.getGoodsByCategory(params)
        .then(res => {
            const data = res.data
            console.log(data)
            if (data.retCode == '0') {
                data.retParams.goodsList.forEach(n => {
                    if(n.images && n.images.length > 0){
                        n.thumb_url = App.renderImage("/thumbnail/") + n.images[0]
                    } else {
                        n.thumb_url = App.renderImage("/thumbnail/3e9c8211dd394b6ea5b0cf0d9c781c94")
                    }
                    
                })
                goods.items = data.retParams.goodsList
                goods.totalNum = data.retParams.totalNum

                this.setData({
                    goods: goods,
                    'prompt.hidden': goods.items.length,
                })
            }
        })
    },
    onPullDownRefresh() {
        console.info('onPullDownRefresh')
        this.initData()
        this.getList()
    },
    onReachBottom() {
        console.info('onReachBottom')
    },
    onTapTag(e) {
        const type = e.currentTarget.dataset.type  //与order公用的nav.wxml
        const index = e.currentTarget.dataset.index
        this.setData({
            activeIndex: index
        })

        const goods = {
            items: [],
            params: {     //查询时的请求参数
                code : type
            },
            totalNum: 0
        }

        this.setData({
            goods: goods
        })
        this.getList()
    },
    onShareAppMessage: function (res) {

      return {
        title: '文玉茶叶',
        path: '/pages/index/index?shareid=' + this.data.userid,
        success: function(res) {
            console.log(res)
        },
        fail: function(res) {
          console.log(res)
        }
      }
    }
})
