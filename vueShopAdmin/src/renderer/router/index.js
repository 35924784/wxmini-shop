import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'login-page',
      component: require('@/components/Login').default
    },
    {
      path: '/admin',
      component: resolve => require(['../components/common/Home.vue'], resolve),
      children: [
        {
          path: '/',
          component: resolve => require(['../components/page/Readme.vue'], resolve)
        },
        {
          path: '/usermanage',
          component: resolve => require(['../components/page/UserManage.vue'], resolve) // 拖拽列表组件
        },
        {
          path: '/classify',
          component: resolve => require(['../components/page/Classify.vue'], resolve) // 分类
        },
        {
          path: '/goods',
          component: resolve => require(['../components/page/GoodsManage.vue'], resolve) // 商品管理
        },
        {
          path: '/image',
          component: resolve => require(['../components/page/ImageManage.vue'], resolve) // 图片管理
        },
        {
          path: '/category',
          component: resolve => require(['../components/page/Category.vue'], resolve) // 配置小商城首页的新品首发和推荐
        },
        {
          path: '/CustService',
          component: resolve => require(['../components/page/CustService.vue'], resolve) // 客服中心  处理客服消息
        },
        {
          path: '/orders',
          component: resolve => require(['../components/page/OrderManage.vue'], resolve)
        },
        {
          path: '/orderReport',
          component: resolve => require(['../components/page/orderReport.vue'], resolve)
        }
      ]
    },
    {
      path: '*',
      redirect: '/'
    }
  ]
})
