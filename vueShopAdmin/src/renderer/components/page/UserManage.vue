<template>
    <div>
        <div class="crumbs" style="margin-bottom: 8px">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item>
                    <i class="el-icon-menu"></i> 用户</el-breadcrumb-item>
            </el-breadcrumb>
        </div>

        <div>
            <li>
                <div class="block" style="width：100%">
                    <span class="demonstration" style="width:150px;margin-right:8px">活跃时间:</span>
                    <el-date-picker v-model="timerange" type="datetimerange" :picker-options="pickerOptions2" range-separator="至" start-placeholder="开始日期"
                        end-placeholder="结束日期" align="right">
                    </el-date-picker>
                </div>
            </li>
            <li>
                <div style="width:100%">
                    <span style="width:150px;margin-right:8px">省份:&nbsp;&nbsp;&nbsp; </span>
                    <el-input v-model="province" clearable placeholder="" style="width:150px;margin-right:8px"> </el-input>
                </div>
            </li>
            <li>
                <div>
                    <span class="demonstration" style="width:150px;margin-right:8px">用户:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
                    <el-input v-model="nickName" placeholder="请输入用户昵称" style="width:150px;margin-right:8px"></el-input>
                </div>
            </li>
            <li>
                <el-button type="info" icon="el-icon-search" @click="getUsers(15,1)">查询</el-button>
            </li>
            <br>
            <span style="border:0.2px solid #C0C0C0; width:100%;float:left"></span>
        </div>

        <el-table :data="userList" stripe border style="width: 100%">
            <el-table-column prop="nickName" label="昵称" min-width="10px"> </el-table-column>
            <el-table-column label="性别" min-width="8px">
                <template slot-scope="scope">
                    <p>{{ genderMap[scope.row.gender] }}</p>
                </template>
            </el-table-column>
            <el-table-column label="头像" min-width="10px">
                <template slot-scope="scope">
                    <img v-bind:src='scope.row.avatarUrl' style="width:60px;height:60px"></img>
                </template>
            </el-table-column>
            <el-table-column label="地址" min-width="18px" show-overflow-tooltip>
                <template slot-scope="scope">
                    <p>{{ scope.row.country + " " + scope.row.province + " " + scope.row.city }}</p>
                </template>
            </el-table-column>
            <el-table-column prop="sessiondate" label="最近活跃时间" min-width="15px">
                <template slot-scope="scope">
                    <p>{{ new Date(scope.row.sessiondate).toLocaleString() }}</p>
                </template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" min-width="15px">
                <template slot-scope="scope">
                    <el-button @click="ChangeUserSetting(scope.row)" size="mini">修改信息</el-button>
                    <el-button @click="ChangeUserDiscount(scope.row)" size="mini">设置折扣</el-button>
                </template>
            </el-table-column>
        </el-table>
        <div class="block">
            <el-pagination @current-change="handleCurrentChange" :current-page.sync="curPage" :page-size="10" layout="total, prev, pager, next"
                :total="totalNum">
            </el-pagination>
        </div>
        <el-dialog title="用户设置" :visible.sync="editDialogVisible" width="70%">
            <div class="form-box">
                <el-form ref="form" :model="form" label-width="80px">
                    <el-form-item label="用户等级" class="myitem">
                        <el-rate v-model="form.level" show-text :texts="['九五折','九折','八五折','八折','七五折']"> </el-rate>
                    </el-form-item>
                    <el-form-item label="联系电话" prop="telphone" class="myitem">
                        <el-input v-model="form.telphone"></el-input>
                    </el-form-item>
                    <el-form-item label="联系电话2" prop="telphone2" class="myitem">
                        <el-input v-model="form.telphone2"></el-input>
                    </el-form-item>
                    <el-form-item label="真实姓名" prop="realName" class="myitem">
                        <el-input v-model="form.realName"></el-input>
                    </el-form-item>
                    <el-form-item label="积分" prop="score" class="myitem">
                        <el-input v-model="form.score"></el-input>
                    </el-form-item>
                    <el-form-item label="备注" prop="remark" class="myitem">
                        <el-input v-model="form.remark"></el-input>
                    </el-form-item>
                    <p class="clear"></p>

                    <el-form-item>
                        <el-button @click="editDialogVisible = false">取 消</el-button>
                        <el-button type="primary" @click="SubmitUserSetting()">确 定</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </el-dialog>
        <el-dialog title="折扣设置" :visible.sync="discountDialogVisible" width="80%" height="300px">
            <el-container>
                <el-main style="margin-top:0px">
                  <div style="margin-bottom: 5px">所有商品
                    <el-button @click="allDiscount(80)" size="mini">8折</el-button>
                    <el-button @click="allDiscount(85)" size="mini">85折</el-button>
                    <el-button @click="allDiscount(90)" size="mini">9折</el-button>
                    <el-button @click="allDiscount(95)" size="mini">95折</el-button>
                  </div>
                    <el-table :data="allGoodsList" stripe border style="width: 100%">
                        <el-table-column prop="code" label="商品编码" min-width="10px"> </el-table-column>
                        <el-table-column prop="name" label="商品名称" min-width="10px"> </el-table-column>
                        <el-table-column prop="remark" label="商品描述" min-width="35px" show-overflow-tooltip> </el-table-column>
                        <el-table-column prop="price" label="价格" min-width="8px">
                            <template slot-scope="scope">{{scope.row.price/100.0}}</template>
                        </el-table-column>
                        <el-table-column fixed="right" label="折扣" min-width="15px">
                            <template slot-scope="scope">
                              <div  @click="{{chgThisDisdocunt(scope.row.code)}}">
                              <el-input v-model="discountMap[scope.row.code]" style="width: 90px" maxlength="2" v-if="scope.row.code == editCode" @change="valueChange(scope.row.code)" @blur="editCode=null"></el-input>
                              <span v-else>{{discountMap[scope.row.code] || 100}}</span>
                              </div>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-main>
                <el-footer >
              <div style="float:right;" >
                <el-button @click="discountDialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="SubmitUserDiscount()">确 定</el-button>
              </div>
              </el-footer>
            </el-container>
        </el-dialog>

    </div>
</template>

<script>

import axios from 'axios'

export default {
  data () {
    return {
      curPage:1,
      totalNum:0,
      genderMap:{"1":"男","2":"女"},
      userList: [
        {
          userid: 'xinyanghong',
          nickName: '李石头',
          gender: '1',   // 1:男 2:女
          province: 'HuBei',
          country: "China",
          city: 'Wuhan',
          avatarUrl: 'https://wx.qlogo.cn/mmopen/vi_32/DYAIOgq83epPgp6SxfpHmcquWIiceicXfvXGINibKMDkVAU6dqUhUicRRAvZQsgbvnWg9ibeqptvUs5oibCotribHPrPQ/0',
          sessiondate: '2018-04-15T17:42:55.267Z'
        }
      ],
      editDialogVisible: false,
      form: {},
      timerange:['',new Date()],
      nickName:"",
      province:"",
      pickerOptions2: {
          shortcuts: [{
            text: '最近一周',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: '最近一个月',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: '最近三个月',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
              picker.$emit('pick', [start, end]);
            }
          }]
      },
      discountDialogVisible: false,
      selectUserid:null,
      allGoodsList:[],      //商品列表
      discountMap:{},
      editCode:null    //可编辑的商品编码
    }
  },
  created: function () {
      this.getUsers(15,1)
      this.getAllGoods()
  },
  methods: {
    handleCurrentChange(val) {
        console.log(`当前页: ${val}`);
        this.curPage = val
        this.getAllUsers(15,val)
    },
    ChangeUserSetting(user){
      console.log(user)
      this.form = {}
      this.form.userid = user.userid;
      var self = this
      axios.get('/userext',{ params: { "userid": user.userid } })
        .then(function (response) {
          console.log(response)
          var data = response.data
          if(data.retParams.userext != null){
              self.form = data.retParams.userext

          }
          self.editDialogVisible = true;
        }).catch(function (error) {
          console.log(error)
          self.$message({ message: error, type: 'error' })
        })
    },
    ChangeUserDiscount(user){
      this.selectUserid = user.userid
      this.discountMap = {}
      this.discountDialogVisible = true;
      this.editCode = null
      var self = this
      axios.get('/userext',{ params: { "userid": user.userid } })
        .then(function (response) {
          console.log(response)
          var data = response.data
          if(data.retParams.userext != null){
              self.discountMap = data.retParams.userext.discountMap
          }
        }).catch(function (error) {
          console.log(error)
          self.$message({ message: error, type: 'error' })
        })
    },
    chgThisDisdocunt(code){
      this.editCode = code;
    },
    valueChange(code){
      console.log(this.discountMap[code])
      console.log(code)
      this.discountMap[code] = this.discountMap[code].replace(/[^\d]/g,'');
      if(this.discountMap[code] < 60){
        this.discountMap[code] = 100
      }
    },
    allDiscount(dis){
      console.log(dis)
      for(var i=0;i<this.allGoodsList.length;i++){
        console.log(this.allGoodsList[i].code)
        this.$set(this.discountMap,this.allGoodsList[i].code,dis)
      }
      console.log(this.discountMap)
    },
    SubmitUserDiscount(){
      var self = this
      axios.post('/userext/admin/discount/' + this.selectUserid,  this.discountMap )
        .then(function (response) {
          console.log(response)
          var data = response.data
          self.discountDialogVisible = false;
          self.selectUserid = null
          self.discountMap = {}
        
        }).catch(function (error) {
          self.$message({ message: error, type: 'error' })
        })
    },
    getUsers(pageSize,curPage) {   //根据查询条件搜索用户
      pageSize = pageSize == null ? 10: pageSize
      curPage = curPage == null ? 1: curPage

      if(this.timerange == null){
        this.timerange = ['','']
      }

      var starttime = Date.parse(this.timerange[0])
      var endtime = Date.parse(this.timerange[1])

      //根据条件查询
      var self = this
      axios.post('/user/admin/all', { "pageSize": pageSize,"curPage":curPage,"starttime":starttime,"endtime":endtime,"nickName":self.nickName,"province":self.province  } )
        .then(function (response) {
          console.log(response)
          var data = response.data
          self.userList = data.retParams.userList
          self.totalNum = data.retParams.totalNum
        
        }).catch(function (error) {
          self.$message({ message: error, type: 'error' })
        })
    },
    SubmitUserSetting(){   //提交用户修改请求
      console.log(this.form)

      this.form.level = this.form.level?this.form.level:0
      this.form.score = parseInt(this.form.score)

      var self = this
      axios.post('/userext/admin', this.form )
        .then(function (response) {
          console.log(response)
          var data = response.data
          if(data.retCode == '0'){
            self.editDialogVisible = false;
          } else {
            self.$message({ message: data.retMessage, type: 'error' })
          }
          
        }).catch(function (error) {
          console.log(error)
          self.$message({ message: error, type: 'error' })
        })
    },
    getAllGoods(){
        if(this.allGoodsList && this.allGoodsList.length > 0){
          return;
        }
        self = this
        axios.get('/goods',{ params: { "pageSize": 100,"curPage":1,"release":"Y" } })
        .then(function (response) {
          console.log(response)
          var data = response.data
          self.allGoodsList = data.retParams.goodsList                  

        }).catch(function (error) {
          self.$message({ message: error, type: 'error' })
        })
    }
  },
  computed: {
    // 计算属性的 getter
    formatCreateDate: function () {
      var date = new Date(this.form.createDate)

      return date.toLocaleDateString() + '  ' + date.toTimeString().substr(0, 5)
    }
  }
}
</script>

<style scoped>
  .myitem {
    width:40%; padding-left:1rem; padding-right:1rem; padding-top:1rem;float: left;
  }
  .clear{clear: both;}
  li{ float:left; list-style:none;width: 48%;margin: 5px} 
.tableheader{border-bottom:1px dashed #000;}
</style>