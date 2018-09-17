<template>
  <div style="border-radius: 5px;background-clip: padding-box;margin: 180px auto;width: 350px;padding: 35px 35px 15px 35px;background: #fff;border: 1px solid #eaeaea;box-shadow: 0 0 25px #cac6c6;">
  <el-form :model="ruleForm2" :rules="rules2" ref="ruleForm2" label-position="left" label-width="0px" >
    <h3 style="margin: 0px auto 40px auto; text-align: center; color: #505458;">系统登录</h3>
    <el-form-item prop="account">
      <el-input type="text" v-model="ruleForm2.account" auto-complete="off" placeholder="账号"></el-input>
    </el-form-item>
    <el-form-item prop="checkPass">
      <el-input type="password" v-model="ruleForm2.checkPass" auto-complete="off" placeholder="密码"></el-input>
    </el-form-item>
    <el-checkbox v-model="checked" checked style="margin: 0px 0px 35px 0px;">记住密码</el-checkbox>
    <el-form-item style="width:100%;">
      <el-button type="primary" style="width:100%;" @click.native.prevent="handleSubmit2" :loading="logining">登录</el-button>
    </el-form-item>
  </el-form>
</div>
</template>

<script>

import axios from 'axios'

export default {
    data () {
      return {
        logining: false,
        ruleForm2: {
          account: 'admin',
          checkPass: ''
        },
        rules2: {
          account: [
            { required: true, message: '请输入账号', trigger: 'blur' }
            // { validator: validaePass }
          ],
          checkPass: [
            { required: true, message: '请输入密码', trigger: 'blur' }
            // { validator: validaePass2 }
          ]
        },
        checked: true
      }
  },
    methods: {
      handleReset2 () {
        this.$refs.ruleForm2.resetFields()
      },
      handleSubmit2 (ev) {
        var _this = this
        this.$refs.ruleForm2.validate((valid) => {
          if (valid) {
            //将原cookie如果有也置失效，相当于清除cookie 其实不清也没事
            document.cookie = 'admin_session="";expires=Sun, 15 Jul 2018 11:50:10 GMT;path=/';
            this.logining = true

            var loginParams = { username: this.ruleForm2.account, password: this.ruleForm2.checkPass }
            axios.post('/admin/login', loginParams).then(result => {
              this.logining = false
              console.log(result)
              var data = result.data

              if (data.retCode !== '0') {
                this.$message({
                  message: data.retMessage,
                  type: 'error'
                })
              } else {
                //document.domain = 'www.example.com';
                document.cookie = 'admin_session=' + data.retParams.admin_session + '; path=/'
                this.$router.push({ path: '/admin' })
              }
            }).catch(err => {
              this.logining = false
              console.log(err)
              this.$message({ message: err, type: 'error' })
            })

          } else {
            console.log('error submit!!')
            return false
          }
        })
      }
    }
  }
</script>