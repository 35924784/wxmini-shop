<template>
    <div id="chat" style="height:100%">
        <CustService v-if="loginSucc"></CustService>
        <el-dialog title="登录" :visible.sync="dialogVisible" width="60%" v-else>
            <el-form label-position="left" label-width="0px">
                <el-form-item>
                    <el-input type="text" v-model="account" auto-complete="off" placeholder="账号"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-input type="password" v-model="checkPass" auto-complete="off" placeholder="密码"></el-input>
                </el-form-item>
                <el-form-item style="width:100%;">
                    <el-button type="primary" style="width:100%;" @click.native.prevent="Login" :loading="logining">登录</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>
    </div>
</template>

<script>
    import CustService from '../components/page/CustService.vue'
    import axios from 'axios'

    export default {
        name: 'chat',
        components: { CustService },
        data() {
            return {
                dialogVisible: true,
                loginSucc: false,
                logining: false,
                account: 'admin',
                checkPass: ''
            };
        },
        created: function () {
            var store_pwd = window.localStorage.getItem("adminpass")
            if(store_pwd){
                this.checkPass = store_pwd
                this.Login()
            }
        },
        methods: {
            Login() {
                //将原cookie如果有也置失效，相当于清除cookie 其实不清也没事
                document.cookie = 'admin_session="";expires=Sun, 15 Jul 2018 11:50:10 GMT;path=/';
                this.logining = true

                var loginParams = { username: this.account, password: this.checkPass }
                axios.post('/admin/login', loginParams).then(result => {
                    this.logining = false
                    console.log(result)
                    var data = result.data

                    if (data.retCode !== '0') {
                        this.$message({ message: data.retMessage, type: 'error' })
                    } else {
                        document.cookie = 'admin_session=' + data.retParams.admin_session + ';path=/'
                        this.loginSucc = true;
                        window.localStorage.setItem("adminpass",this.checkPass)
                    }
                }).catch(err => {
                    this.logining = false
                    this.loginSucc = false;
                    console.log(err)
                    this.$message({ message: err, type: 'error' })
                })

            }

        }
    }
</script>