import WxRequest from '../assets/plugins/wx-request/lib/index'

class HttpService extends WxRequest {
	constructor(options) {
		super(options)
		this.$$prefix = ''
		this.$$path = {
			wechatSignUp: '/user/wechatSignUp',
			wechatSignIn: '/user/wechatSignIn',
			checkSession: '/user/checkSession',
			decryptData : '/user/wechat/decrypt/data',
			signIn      : '/user/sign/in',
			signOut     : '/user/sign/out',
			banner      : '/banner', 
			classify    : '/classify', 
			goods       : '/goods', 
			goodsbyclassify : '/goods/byclassify',
			search      : '/goods/search', 
			cart        : '/cart', 
			address     : '/address',
			userscore     : '/userext/score', 
			order       : '/order',
			orderfee    : '/orderfee',
			wxpay       : '/wxpay'
        }
        this.interceptors.use({
            request(request) {
            	request.header = request.header || {}
            	request.header['content-type'] = 'application/json'
                if (request.url.indexOf('/restapi') !== -1 && wx.getStorageSync('token')) {
                    request.header.Cookie = 'my_session=' + wx.getStorageSync('token')
                }
                wx.showLoading({
                    title: '加载中', 
                })
                return request
            },
            requestError(requestError) {
            	wx.hideLoading()
                return Promise.reject(requestError)
            },
            response(response) {
            	wx.hideLoading()
            	if(response.statusCode === 401) {
                    wx.removeStorageSync('token')
                    wx.redirectTo({
                        url: '/pages/login/index'
                    })
                }
                return response
            },
            responseError(responseError) {
            	wx.hideLoading()
                return Promise.reject(responseError)
            },
        })
	}

	wechatSignUp(params) {
		return this.postRequest(this.$$path.wechatSignUp, {
			data: params,
		})
	}

	wechatSignIn(params) {
		return this.postRequest(this.$$path.wechatSignIn, {
			data: params,
		})
	}

	checkSession(params) {
		return this.postRequest(this.$$path.checkSession, {
			data: params,
		})
	}

	getSalesQRCode() {
		return this.getRequest(this.$$path.getSalesQRCode, {})
	}

	wechatDecryptData(params) {
		return this.postRequest(this.$$path.decryptData, {
			data: params,
		})
	}
	
	signIn(params) {
		return this.postRequest(this.$$path.signIn, {
			data: params,
		}) 
	}

	signOut() {
		return this.postRequest(this.$$path.signOut) 
	}

	getBanners(params) {
		return this.getRequest(this.$$path.banner, {
			data: params,
		})
	}

	search(params) {
		return this.getRequest(this.$$path.search, {
			data: params,
		})
	}

	getGoods(params) {
		return this.getRequest(this.$$path.goods, {
			data: params,
		})
	}

	getGoodsByCategory(params) {
		return this.getRequest("/category/goods", {
			data: params,
		})
	}

	getGoodsByClassify(params) {
		return this.getRequest("/goods/byclassify", {
			data: params,
		})
	}

	getClassify(params) {
		return this.getRequest(this.$$path.classify, {
			data: params,
		})
	}

	getDetail(id) {
		return this.getRequest(`${this.$$path.goods}/${id}`)
	}

	getCartByUser() {
		return this.getRequest(this.$$path.cart)
	}

	addCartByUser(goodscode,num,spec) {
		return this.postRequest(this.$$path.cart, {
			data: {
				"goodscode":goodscode,
				"num": num,
				"spec": spec
			},
		})
	}

	updateCartByUser(params) {
		return this.postRequest(`${this.$$path.cart}/update`, {
			data: params,
		})
	}

	delCartByUser(id) {
		return this.deleteRequest(`${this.$$path.cart}/${id}`)
	}

	clearCartByUser() {
		return this.postRequest(`${this.$$path.cart}/clear`)
	}

	getAddressList(params) {
		return this.getRequest(this.$$path.address, {
			data: params,
		})
	}

	getAddressDetail(id) {
		return this.getRequest(`${this.$$path.address}/${id}`)
	}

	getUserScore() {
		return this.getRequest(`${this.$$path.userscore}`)
	}

	postAddress(params) {
		return this.postRequest(this.$$path.address, {
			data: params,
		})
	}

	putAddress(id, params) {
		return this.putRequest(`${this.$$path.address}/${id}`, {
			data: params,
		})
	}

	deleteAddress(id, params) {
		return this.deleteRequest(`${this.$$path.address}/${id}`)
	}

	getDefalutAddress() {
		return this.getRequest(`${this.$$path.address}/default`)
	}

	setDefalutAddress(id) {
		return this.postRequest(`${this.$$path.address}/default/${id}`)
	}

	getOrderList(params) {
		return this.getRequest(this.$$path.order, {
			data: params,
		})
	}

	getOrderDetail(id) {
		return this.getRequest(`${this.$$path.order}/${id}`)
	}

	calcOrderFee(params) {
		return this.postRequest(this.$$path.orderfee, {
			data: params,
		})
	}

	postOrder(params) {
		return this.postRequest(this.$$path.order, {
			data: params,
		})
	}
	//创建微信支付订单
	postWxPayOrder(orderid) {
		return this.postRequest(this.$$path.wxpay + '/create_payorder', {
			data: orderid,
		})
	}

	putOrder(id, params) {
		return this.putRequest(`${this.$$path.order}/${id}`, {
			data: params,
		})
	}

	deleteOrder(id, params) {
		return this.deleteRequest(`${this.$$path.order}/${id}`)
	}
}

export default HttpService