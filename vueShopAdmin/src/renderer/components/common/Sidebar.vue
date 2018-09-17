<template>
    <div class="sidebar">
        <el-menu :default-active="onRoutes" class="el-menu-vertical" background-color="#545c64" text-color="#fff" active-text-color="#ffd04b" unique-opened router>
            <el-submenu :index="item.index" v-if="item.subs" v-for="item in items" >
                <template slot="title"><i :class="item.icon"></i>{{ item.title }}</template>
                  <el-menu-item v-for="(subItem,i) in item.subs" :key="i" :index="subItem.index" style="min-width:80px">{{ subItem.title }}
                  </el-menu-item>
            </el-submenu>
            <el-menu-item v-else :index="item.index">
                <i :class="item.icon"></i>{{ item.title }}
            </el-menu-item>
        </el-menu>
    </div>
</template>

<script>
    export default {
      data () {
        return {
          items: [
            {
              icon: 'el-icon-setting',
              index: 'admin',
              title: '说明'
            },
            {
              icon: 'el-icon-setting',
              index: 'classify',
              title: '分类'
            },
            {
              icon: 'el-icon-setting',
              index: 'goods',
              title: '商品'
            },
            {
              icon: 'el-icon-menu',
              index: 'image',
              title: '图片'
            },
            {
              icon: 'el-icon-menu',
              index: '2',
              title: '订单中心',
              subs: [
                {
                  index: 'orders',
                  title: '订单管理'
                },
                {
                  index: 'orderReport',
                  title: '订单统计'
                }
              ]
            },
            {
              icon: 'el-icon-menu',
              index: 'category',
              title: '首页目录'
            },
            {
              icon: 'el-icon-date',
              index: '4',
              title: '用户中心',
              subs: [
                {
                  index: 'usermanage',
                  title: '用户列表'
                }
              ]
            },
            {
              icon: 'el-icon-menu',
              index: 'CustService',
              title: '客服中心'
            }
          ]
        }
      },
      computed: {
        onRoutes () {
          return this.$route.path.replace('/', '')
        }
      }
    }
</script>

<style scoped>
    .sidebar{
        display: block;
        position: absolute;
        left: 0;
        width: 14%;
        top: 70px;
        bottom:0;
        background: #2E363F;
    }
    .sidebar > ul {
        height:100%;
    }
</style>
