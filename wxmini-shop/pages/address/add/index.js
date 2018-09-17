const App = getApp()

Page({
    data: {
    	show: !0,
    	form: {
			contactName   : '', 
			contactPhone    : '', 
			addrDetail: '', 
			isDefault : !1, 
        }
    },
    onLoad() {
    	this.WxValidate = App.WxValidate({
			contactName: {
				required: true, 
				minlength: 2, 
				maxlength: 10, 
			},
			contactPhone: {
				required: true, 
				contactPhone: true,
				maxlength: 20,  
			},
			addrDetail: {
				required: true, 
				minlength: 2, 
				maxlength: 100, 
			},
		}, {
			contactName: {
				required: '请输入收货人姓名', 
				minlength:'收货人姓名最少输入两个字',
				maxlength:'收货人姓名最多10个字'
			},
			contactPhone: {
				required: '请输入收货人电话',
				contactPhone:'收货人电话号码不正确',
				maxlength:'收货人电话号码不正确'
			},
			addrDetail: {
				required: '请输入收货地址', 
				minlength:'收货地址最少输入两个字',
				maxlength:'收货人姓名最多100个字'
			},
		})

    },
	submitForm(e) {
		const params = e.detail.value

		console.log(params)

		if (!this.WxValidate.checkForm(e)) {
			const error = this.WxValidate.errorList[0]
			App.WxService.showModal({
				title: '友情提示', 
					content: `${error.msg}`, 
					showCancel: !1, 
			})
			return false
		}

		App.HttpService.postAddress(params)
		.then(res => {
            const data = res.data
            console.log(data)
			if (data.retCode == '0') {
				this.showToast(data.retMessage)
			}
		})
	},
	showToast(message) {
		App.WxService.showToast({
			title   : message, 
			icon    : 'success', 
			duration: 1500, 
		})
		.then(() => App.WxService.navigateBack({
     		success: function() {
     		    beforePage.onLoad({back:true}); // 执行前一个页面的onLoad方法
     		}
 		}))
	},
	chooseLocation() {
		App.WxService.chooseLocation()
	    .then(data => {
	        console.log(data)
	        this.setData({
	        	'form.addrDetail': data.address
	        })
	    })
	},
})