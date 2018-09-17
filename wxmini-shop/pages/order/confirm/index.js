const App = getApp()

Page({
    data: {
        showaddrs: false,    //是否展示地址列表供选择
        carts: {},
        address: null,
        addrList:[]
    },
    onLoad(option) {
        console.log("onLoad") 
        const carts = {
            items: App.WxService.getStorageSync('confirmOrder')
        }

        this.setData({
            carts: carts
        })

        if(option.back){
            this.setData({
                showaddrs: true
            })
        }

        this.calcOrderFee()
        this.getDefalutAddress()

        console.log(this.data.carts)
    },
    onShow() {
        console.log("onshow") 
        if(this.data.showaddrs){
            this.showAddrList()
        }       
    },
    getDefalutAddress() {
        App.HttpService.getDefalutAddress()
        .then(res => {
            const data = res.data
            console.log(data)
            if (data.retCode == '0' && data.retParams.defaultAddr) {
                this.setData({
                    address:data.retParams.defaultAddr
                })

                this.checkAddr()

            } else {
                this.showModal()
            }
        })
    },
    checkAddr(){
        var orderfee = this.data.orderFee

        if(!this.data.address){
            return
        }

        if(this.data.address.addrDetail.indexOf("信阳") != -1 && orderfee && orderfee.shippingFee != 0){
            const shippingfee = orderfee.shippingFee;
            orderfee.shippingFee = 0;
            //orderfee.totalPrice = orderfee.totalPrice + orderfee.shippingFee;
        }

        if(this.data.address.addrDetail.indexOf("信阳") == -1 && orderfee && orderfee.shippingFee == 0){
            orderfee.shippingFee = 0;
            //orderfee.totalPrice = orderfee.totalPrice + orderfee.shippingFee;
        }

        this.setData({
            orderFee:orderfee
        })
    },
    calcOrderFee() {
        const params = {
            orderItemList: this.data.carts.items
        }

        App.HttpService.calcOrderFee(params)
        .then(res => {
            const data = res.data
            console.log(data)
            if (data.retCode == '0' && data.retParams.orderfee) {
                this.setData({
                    orderFee:data.retParams.orderfee,
                    userScore:data.retParams.userScore
                })
            } else {
                App.WxService.showModal({
                    title: '算费失败', 
                    content: '请稍后再试', 
                    showCancel: false
                })
            }
        })
    },
    showAddrList() {
        App.HttpService.getAddressList()
        .then(res => {
            const data = res.data
            console.log(data)
            if (data.retCode == '0' && data.retParams.addrList) {
                this.setData({
                    addrList:data.retParams.addrList
                })
            }

            this.setData({
                showaddrs:true
            })
        })
    },
    selectAddr(e){
        var addr = e.currentTarget.dataset.addr
        console.log(addr)
        this.setData({
            address:addr,
            showaddrs:false
        })

        this.checkAddr()
    },
    deleteAddr(e) {
        var addr = e.currentTarget.dataset.addr
        console.log(addr)
        //添加确认
        App.WxService.showModal({
            title: '提示', 
            content: '确认删除此地址信息吗？', 
        })
        .then(data => {
            console.log(data)
            if (data.confirm == 1) {
                 App.HttpService.deleteAddress(addr.addrId)
                 .then(res => {
                    const data = res.data
                    console.log(data)
                    if (data.retCode == '0') {
                        this.showAddrList()
                    }
                  })

            } else {
                return;
            }
        })
    },
    toAddressAdd(){
        if(this.data.addrList && this.data.addrList.length >= 5){
            App.WxService.showModal({
                title: '友情提示', 
                content: '每个用户最多保存5个地址', 
                showCancel: false
            })

            return;
        }
        this.setData({
            showaddrs:true
        })
        App.WxService.navigateTo('/pages/address/add/index')
    },
    showModal() {
        App.WxService.showModal({
            title: '友情提示', 
            content: '没有默认收货地址，请先设置', 
        })
        .then(data => {
            console.log(data)
            if (data.confirm == 1) {
                this.setData({
                    showaddrs:true
                })
                App.WxService.navigateTo('/pages/address/add/index')
            } else {
                App.WxService.navigateBack()
            }
        })
    },
    addOrder() {
        console.log(this.data.address)

        if(!this.data.address){
            this.showModal()
        }

        const params = {
            addrinfo: this.data.address,
            orderid: this.data.orderFee.orderid
        }

        console.log(params)
        App.HttpService.postOrder(params)
        .then(res => {
            const data = res.data
            console.log(data)
            if (data.retCode == '0') {
                //应该去支付 
                const orderid = data.retParams.orderid
                this.toWxPay(orderid)
            }else {
                App.WxService.showModal({
                    title: '提示', 
                    content: '创建订单失败，请稍后重试', 
                    showCancel: !1, 
                })
            }
        })
    },
    toWxPay(orderid) {
        App.HttpService.postWxPayOrder(orderid)
        .then(res => {
            const data = res.data
            console.log(data)
            if (data.retCode == '0') {
                //获取签名数据 调用requestPayment 
                var payParam = data.retParams.payParam
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
                      title: '支付成功',
                      icon: 'success',
                      duration: 2000
                    }).then(res => {
                        App.WxService.redirectTo('/pages/order/list/index')
                    });
                  },
                  fail: function (error) {
                    // fail   
                    App.WxService.showToast({
                      title: '支付失败，请重试',
                      icon: 'none',
                      duration: 2000
                    }).then(res => {
                        App.WxService.redirectTo('/pages/order/list/index')
                    });
                  },
                  complete: function () {
                    //complete   
                    console.log("pay complete")
                  }
                });

            } else {
                App.WxService.showModal({
                    title: '提示', 
                    content: '创建微信支付失败，请稍后重试', 
                    showCancel: !1, 
                }).then(res => {
                    //console.log(res)
                    App.WxService.redirectTo('/pages/order/list/index')
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
                App.WxService.redirectTo('/pages/order/list/index')
            })
        })
    }
})