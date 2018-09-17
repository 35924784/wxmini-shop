const App = getApp()

Page({
    data: {
        addrList: [],
        prompt: {
            hidden: !0,
            icon: '../../../assets/images/iconfont-addr-empty.png',
            title: '还没有收货地址呢',
            text: '暂时没有相关数据',
        },
    },
    onLoad() {
        this.address = App.HttpResource('/address/:id', {id: '@id'})
    },
    onShow() {
        this.onPullDownRefresh()
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
                        this.onPullDownRefresh()
                    }
                  })

            } else {
                return;
            }
        })

    },
    toAddressAdd(e) {
        console.log(e)
        if(this.data.addrList && this.data.addrList.length >= 5){
            App.WxService.showModal({
                title: '友情提示', 
                content: '每个用户最多保存5个地址', 
                showCancel: false
            })

            return;
        }
        App.WxService.navigateTo('/pages/address/add/index')
    },
    setDefalutAddress(e) {
        const id = e.currentTarget.dataset.id
        App.HttpService.setDefalutAddress(id)
        .then(res => {
            const data = res.data
            console.log(data)
            if (data.retCode == '0') {
                this.onPullDownRefresh()
            }
        })
    },
    getList() {

        // App.HttpService.getAddressList(params)
        this.address.queryAsync()
        .then(res => {
            const data = res.data
            console.log(data)
            if (data.retCode == '0' && data.retParams.addrList) {

                this.setData({
                    "addrList": data.retParams.addrList
                })
            }
        })
    },
    onPullDownRefresh() {
        console.info('onPullDownRefresh')
        this.getList()
    },
    onReachBottom() {
        console.info('onReachBottom')
    },
})