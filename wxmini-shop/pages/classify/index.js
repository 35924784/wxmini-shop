const App = getApp()

Page({
    data: {
        activeIndex: 0, 
        goods: {},
        classifyList: {},
        prompt: {
            hidden: !0,
        },
    },
    onLoad() {
        this.classify = App.HttpResource('/classify/:id', {id: '@id'})
        this.goods = App.HttpResource('/goods/:id', {id: '@id'})
        this.getSystemInfo()
        this.onRefresh()
    },
    initData() {
        this.setData({
            classifyList: {}
        })
    },
    navigateTo(e) {
        console.log(e)
        App.WxService.navigateTo('/pages/goods/detail/index', {
            id: e.currentTarget.dataset.id
        })
    },
    getList() {
        // App.HttpService.getClassify()
        this.classify.queryAsync()
        .then(res => {
            const data = res.data
            console.log(data)
            if (data.retCode == '0') {
                const classifyList = data.retParams.classifyList
                this.setData({
                    classifyList: classifyList, 
                    'prompt.hidden': classifyList.length, 
                    activeIndex: 0, 
                    'goods.params.classify': classifyList[0].code, 
                })

                this.getGoods()
            }
        })
    },
    onRefresh() {
        this.initData()
        this.initGoods()
        this.getList()
    },
    getMore() {
        //暂无用
    },
    changeTab(e) {
        const dataset = e.currentTarget.dataset
        const index = dataset.index
        const classifycode = dataset.id

        this.setData({
            activeIndex: index
        })

        this.initGoods(classifycode)
        this.getGoods()
    },
    initGoods(classifycode) {
        const goods = {
            items: [],
            params: {
                curPage:   1,
                pageSize:  30,
                classify : classifycode,
                release: "Y"
            },
            paginate: {}
        }

        this.setData({
            goods: goods
        })
    },
    getGoods() {
        const goods = this.data.goods
        const params = goods.params

        App.HttpService.getGoodsByClassify(params)
        //this.goodsbyclassify.queryAsync(params)
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
    onRefreshGoods() {
        const classifycode = this.goods.params.classify

        this.initGoods()
        this.getGoods()
    },
    getMoreGoods() {
        //if (!this.data.goods.paginate.hasNext) return
        //this.getGoods()
    },
    getSystemInfo() {
        App.WxService.getSystemInfo()
        .then(data => {
            console.log(data)
            this.setData({
                deviceWidth: data.windowWidth, 
                deviceHeight: data.windowHeight, 
            })
        })
    },
})