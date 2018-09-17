const App = getApp()

Page({
    data: {
        activeIndex: 0,
        navList: [],
        order: {},
        prompt: {
            hidden: !0,
            icon: '../../../assets/images/iconfont-order-default.png',
            title: '您还没有相关的订单',
            text: '可以去看看有哪些想买的',
        },
        statusMap:{
        "0":"待付款",
        "1":"待发货",
        "2":"已发货",
        "3":"已完成",
        "9":"已取消"
      }
    },
    onLoad() {
        this.order = App.HttpResource('/order/:id', {id: '@id'})
        this.setData({
            navList: [
                {
                    name: '全部',
                    _id: 'all',
                },
                {
                    name: '待付款',
                    _id: '0',
                },
                {
                    name: '待发货',
                    _id: '1',
                },

                {
                    name: '已发货',
                    _id: '2',
                },
                {
                    name: '已完成',
                    _id: '3',
                },
            ]
        })
        this.onPullDownRefresh()
    },
    initData() {

        this.setData({
            order: {
                orderList: [],
                params:{ 
                    curPage:1,
                    pageSize:10,
                    status:"all"
                },
                total:0  
            },

        })
    },
    getList() {
        const order = this.data.order
        const params = order.params

        // App.HttpService.getOrderList(params)
        this.order.queryAsync(params)
        .then(res => {
            const data = res.data
            console.log(data)
            if (data.retCode == '0') {
                order.orderList = data.retParams.orderList
                order.total = data.retParams.total;
                this.setData({
                    order: order,
                    'prompt.hidden': order.total,
                })
            }
        })
    },
    onPullDownRefresh() {
        console.info('onPullDownRefresh')
        this.initData()
        this.getList()
    },
    onReachBottom() { //待后续添加下拉显示更多的功能
        console.info('onReachBottom')
        var curPage = this.data.order.params.curPage;
        var pageSize = this.data.order.params.pageSize;
        var total = this.data.order.total;
        if (curPage * pageSize >= total){
            return
        } else {
            this.setData({
                'order.params.curPage': curPage+1
            })

            this.getList()
        }

    },
    onTapTag(e) {
        const status = e.currentTarget.dataset.type
        const index = e.currentTarget.dataset.index
        this.initData()
        this.setData({
            activeIndex: index,
            'order.params.status': status,
        })
        this.getList()
    },
    toWxPay(e) {
        const orderid = e.currentTarget.dataset.orderid
        App.HttpService.postWxPayOrder(orderid)
        .then(res => {
            const data = res.data
            console.log(data)
            if (data.retCode == '0') {
                //获取签名数据 调用requestPayment 
                var payParam = data.retParams.payParam

                var self = this
                //小程序发起微信支付
                wx.requestPayment({
                  timeStamp: payParam.timeStamp,//记住，这边的timeStamp一定要是字符串类型的，不然会报错，我这边在java后端包装成了字符串类型了
                  nonceStr: payParam.nonceStr,
                  package: payParam.package,
                  signType: 'MD5',
                  paySign: payParam.paySign,
                  success: function (event) {
                    // success   
                    console.log(event);
                    
                    App.WxService.showToast({
                      title: '付款成功',
                      icon: 'success',
                      duration: 1000
                    })
                  },
                  fail: function (error) {
                    // fail   
                    App.WxService.showToast({
                      title: '付款失败，请重试',
                      icon: 'none',
                      duration: 1000
                    })
                  },
                  complete: function () {
                    //complete   
                    console.log("pay complete")
                    self.getList()
                  }
                });

            } else {
                App.WxService.showModal({
                    title: '提示', 
                    content: '创建微信支付失败，请稍后重试', 
                    showCancel: !1, 
                }).then(res => {
                    this.getList()
                })
            }
        }).catch((err) => {
            console.log(err) //error 1
            App.WxService.showModal({
                title: '提示', 
                content: '创建微信支付失败，请稍后重试', 
                showCancel: !1, 
            }).then(res => {
                console.log(res)
                this.getList()
            })
        })
    }
})