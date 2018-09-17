const App = getApp()

Page({
    data: {
        inputVal: '',
        prompt: {
            hidden: !0,
        },
        goods: {},
    },
    clearInput() {
        this.setData({
            inputVal: ''
        })
    },
    inputTyping(e) {
        this.setData({
            inputVal: e.detail.value
        })
        this.search()
    },
    search() {
    	if (!this.data.inputVal) return
        
        const goods = this.data.goods

    	App.HttpService.search({
            keyword: this.data.inputVal
        })
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
                    'prompt.hidden': data.retParams.goodsList.length,
                })
            }
        })
    },
    navigateTo(e) {
        console.log(e)
        App.WxService.navigateTo('/pages/goods/detail/index', {
            id: e.currentTarget.dataset.id
        })
    },
})