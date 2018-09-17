const App = getApp()

Page({
    data: {
        canEdit: !1,
        carts: {
            items: []
        },
        prompt: {
            hidden: !0,
            icon: '../../assets/images/iconfont-cart-empty.png',
            title: '购物车空空如也',
            text: '来挑几件好货吧',
            buttons: [
                {
                    text: '随便逛',
                    bindtap: 'bindtap',
                },
            ],
        },
    },
    bindtap(e) {
        const index = e.currentTarget.dataset.index
        
        switch(index) {
            case 0:
                App.WxService.switchTab('/pages/index/index')
                break
            default:
                break
        }
    },
    onLoad() {
    },
    onShow() {
        this.setData({
            'carts.items': [],
            'prompt.hidden': 0,
        })
        this.getCarts()
    },
    getCarts() {
        App.HttpService.getCartByUser()
        .then(res => {
            const data = res.data
            console.log(data)
            if (data.retCode == "0") {
                var goodsInfoMap = data.retParams.goodsInfoMap
                data.retParams.orderItemList.forEach(n => {
                    let thegoods = goodsInfoMap[n.goodscode]
                    if(thegoods){
                        n.goods = thegoods
                        n.price = thegoods.price
                        if(thegoods.images && thegoods.images.length > 0){
                            n.thumb_url = App.renderImage("/thumbnail/" + thegoods.images[0])  
                        }

                        if(n.spec == "default"){
                            n.specDetail = ["500g","默认一斤", n.price]
                            return;
                        }

                        //商品中每个spec是个3个元素的数组，依次是 规格编码 规格名称 规格价格 购物车orderitem中只是编码
                        if(thegoods.specs && thegoods.specs.length > 0){
                            for (var i = 0; i < thegoods.specs.length; i++) {
                                var tempspec = thegoods.specs[i]
                                console.log(tempspec)
                                if(tempspec[0] && tempspec[0] == n.spec){
                                    n.specDetail = tempspec
                                }
                            }
                        }
                    }
                })
                this.setData({
                    'carts.items': data.retParams.orderItemList,
                    'prompt.hidden': data.retParams.orderItemList.length,
                })
            }
        })
    },
    onPullDownRefresh() {
        this.getCarts()
    },
    navigateTo(e) {
        console.log(e)
        App.WxService.navigateTo('/pages/goods/detail/index', {
            id: e.currentTarget.dataset.id
        })
    },
    confirmOrder(e) {
        console.log(e)
        App.WxService.setStorageSync('confirmOrder', this.data.carts.items)
        App.WxService.navigateTo('/pages/order/confirm/index')
    },
    del(e) {
        const id = e.currentTarget.dataset.id

        App.WxService.showModal({
            title: '友情提示', 
            content: '确定要删除这个宝贝吗？', 
        })
        .then(data => {
            if (data.confirm == 1) {
                App.HttpService.delCartByUser(id)
                .then(res => {
                    const data = res.data
                    console.log(data)
                    if (data.retCode == '0') {
                        this.getCarts()
                    }
                })
            }
        })
    },
    clear() {
        App.WxService.showModal({
            title: '友情提示', 
            content: '确定要清空购物车吗？', 
        })
        .then(data => {
            if (data.confirm == 1) {
                App.HttpService.clearCartByUser()
                .then(res => {
                    const data = res.data
                    console.log(data)
                    if (data.retCode == '0') {
                        this.getCarts()
                    }
                })
            }
        })
    },
    onTapEdit(e) {
        const id = e.currentTarget.dataset.id
        const value = e.currentTarget.dataset.value
        console.log(e)

        for (var i = 0; i < this.data.carts.items.length; i++) {

            if(id == this.data.carts.items[i].itemid){
                var item_canEdit = "carts.items["+i+"].canEdit"

                if(value == "ok"){
                    this.updateCartByUser(this.data.carts.items[i])
                }
                else{
                    this.setData({
                        [item_canEdit] : true
                    })
                }

                break;
            }
        }

        console.log(this.data.carts)
    },
    bindKeyInput(e) {
        const id = e.currentTarget.dataset.id
        const total = Math.abs(e.detail.value)
        if (total < 0 || total > 100) return

        for (var i = 0; i < this.data.carts.items.length; i++) {

            if(id == this.data.carts.items[i].itemid){
                var item_amount = "carts.items["+i+"].amount"
                this.setData({
                    [item_amount] : total
                })
                break;
            }
        }

        console.log(this.data.carts)
    },
    updateCartByUser(item) {
        console.log(item)
        App.HttpService.updateCartByUser({"itemid":item.itemid,"num":item.amount})
        .then(res => {
            const data = res.data
            console.log(data)
            if (data.retCode == '0') {
                this.getCarts()
            }
        })
    },
    decrease(e) {
        const id = e.currentTarget.dataset.id
        const total = Math.abs(e.currentTarget.dataset.total)
        if (total == 1) return

        for (var i = 0; i < this.data.carts.items.length; i++) {

            if(id == this.data.carts.items[i].itemid){
                var item_amount = "carts.items["+i+"].amount"
                this.setData({
                    [item_amount] : total-1
                })
                break;
            }
        }

        console.log(this.data.carts)
        
    },
    increase(e) {
        const id = e.currentTarget.dataset.id
        const total = Math.abs(e.currentTarget.dataset.total)

        if (total == 20) return

        for (var i = 0; i < this.data.carts.items.length; i++) {

            if(id == this.data.carts.items[i].itemid){
                var item_amount = "carts.items["+i+"].amount"
                this.setData({
                    [item_amount] : total+1
                })
                break;
            }
        }

        console.log(this.data.carts)
        
    },
})