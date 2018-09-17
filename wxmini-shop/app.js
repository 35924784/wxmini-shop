import WxValidate from './assets/plugins/wx-validate/WxValidate'
import WxService from './assets/plugins/wx-service/WxService'
import HttpResource from './helpers/HttpResource'
import HttpService from './helpers/HttpService'
import __config from './etc/config'

App({
	onLaunch(options) {
		console.log('onLaunch')
		console.log(options)
		if(options.scene == '1035'){
			wx.setStorageSync('publicAcct', true)  //设置为已关注公众号
		}
	},
	onShow() {
		console.log('onShow')
	},
	onHide() {
		console.log('onHide')
	},
	globalData: {
	},
	renderImage(path) {
        if (!path) return ''
        if (path.indexOf('http') !== -1) return path
        return `${this.__config.domain}${path}`
    },
	WxValidate: (rules, messages) => new WxValidate(rules, messages), 
	HttpResource: (url, paramDefaults, actions, options) => new HttpResource(url, paramDefaults, actions, options).init(), 
	HttpService: new HttpService({
		baseURL: __config.basePath,
	}), 
	WxService: new WxService, 
	__config, 
})