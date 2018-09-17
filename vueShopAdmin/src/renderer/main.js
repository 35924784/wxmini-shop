import Vue from 'vue'
import axios from 'axios'

import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import App from './App'
import router from './router'
import store from './store'

if (!process.env.IS_WEB) Vue.use(require('vue-electron'))
Vue.http = Vue.prototype.$http = axios
Vue.config.productionTip = false

Vue.use(ElementUI)

axios.defaults.baseURL = '/restapi'
//axios.defaults.baseURL = 'https://www.example.com/restapi'
// http响应拦截器
axios.interceptors.response.use(data => {
 	return data
}, error => {
	console.log(error.response)
	if(error.response && error.response.status == 456){
		router.push({ path: '/login' })
		return
	}
 	return Promise.reject(error)
})

//Vue聊天
/**
 * 转换成图片表情
 */
var qqWechatEmotionParser = require('./lib/index');


Vue.directive('emotion', {
    bind: function (el, binding) {
        el.innerHTML = qqWechatEmotionParser(binding.value)
    }
});
//Vue聊天 表情转换end

/* eslint-disable no-new */
new Vue({
  components: { App },
  router,
  store,
  template: '<App/>'
}).$mount('#app')
