import Vue from 'vue'
import axios from 'axios'

import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import '../lib/emotion.css'
import chat from './chat.vue'
import CustService from '../components/page/CustService.vue'

if (!process.env.IS_WEB) Vue.use(require('vue-electron'))
Vue.config.productionTip = false

Vue.use(ElementUI)
axios.defaults.baseURL = '/restapi'
axios.defaults.withCredentials = true
//Vue聊天
/**
 * 转换成图片表情
 */

var qqWechatEmotionParser = require('../lib/index');


Vue.directive('emotion', {
    bind: function (el, binding) {
        el.innerHTML = qqWechatEmotionParser(binding.value)
    }
});
//Vue聊天 表情转换end

/* eslint-disable no-new */
new Vue({
  components: { chat },
  template: '<chat/>'
}).$mount('#chat')
