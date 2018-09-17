<template>
    <div class="header">
        <div class="logo">后台管理系统</div>
        <div class="user-info">
            <el-dropdown trigger="click" @command="handleCommand">
                <span class="el-dropdown-link">
                    <img class="user-logo" src="../../../static/img/logo.jpg">
                    {{username}}
                </span>
                <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item command="loginout">退出</el-dropdown-item>
                </el-dropdown-menu>
            </el-dropdown>
        </div>
    </div>
</template>
<script>
    import axios from 'axios'

    export default {
      data () {
        return {
          name: 'admin'
        }
      },
      computed: {
        username () {
          let username = localStorage.getItem('ms_username')
          return username || this.name
    }
      },
      methods: {
        handleCommand (command) {
          var self = this
          if (command == 'loginout') {
            axios.post('/admin/logout','')
            .then(function (response) {
                    console.log(response)
                    self.$router.push('/login')
            }).catch(function (error) {
                self.$router.push('/login')
            })
            
          }
        }
      }
    }
</script>
<style scoped>
    .header {
        background-color:#e4e7ed;
        position: relative;
        box-sizing: border-box;
        width: 100%;
        height: 60px;
        font-size: 22px;
        line-height: 60px;
        color: #303133;
    }
    .header .logo{
        float: left;
        width:250px;
        text-align: center;
    }
    .user-info {
        float: right;
        padding-right: 50px;
        font-size: 16px;
        color: #303133;
    }
    .user-info .el-dropdown-link{
        position: relative;
        display: inline-block;
        padding-left: 50px;
        color: #303133;
        cursor: pointer;
        vertical-align: middle;
    }
    .user-info .user-logo{
        position: absolute;
        left:0;
        top:15px;
        width:40px;
        height:40px;
        border-radius: 50%;
    }
    .el-dropdown-menu__item{
        text-align: center;
    }
</style>
