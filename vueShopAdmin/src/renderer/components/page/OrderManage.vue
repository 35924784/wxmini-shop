<template>
    <div>
        <div class="crumbs" style="margin-bottom: 8px">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item>
                    <i class="el-icon-menu"></i> 订单管理</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div >
            <li>
                <div class="block" style="width：100%">
                    <span class="demonstration" style="width:150px;margin-right:8px">起始时间:</span>
                    <el-date-picker v-model="timerange" type="datetimerange" :picker-options="pickerOptions2" range-separator="至" start-placeholder="开始日期"
                        end-placeholder="结束日期" align="right">
                    </el-date-picker>
                </div>
            </li>
            <li>
                <div style="width:100%">
                    <span style="width:150px;margin-right:8px">状态:&nbsp;&nbsp;&nbsp; </span>
                    <el-select v-model="status" clearable placeholder="">
                        <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value">
                        </el-option>
                    </el-select>
                </div>
            </li>
            <li>
                <div>
                    <span class="demonstration" style="width:150px;margin-right:8px">用户:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
                    <el-input v-model="nickName" placeholder="请输入用户昵称" style="width:150px;margin-right:8px"></el-input>
                </div>
            </li>
            <li>
                <el-button type="info" icon="el-icon-search" @click="getOrders(10,1)">查询</el-button>
            </li>
            <br>
            <span style="border:0.2px solid #C0C0C0; width:100%;float:left"></span>
        </div>
        

        <el-table :data="tableData" stripe style="width: 100%;" header-row-class-name="tableheader">
            <el-table-column type="expand">
                <template slot-scope="props">
                    <el-form label-position="left" inline class="demo-table-expand">
                        <el-form-item label="订单号：" style="width: 30%;margin-bottom:0">
                            <span>{{ props.row.orderid }}</span>
                        </el-form-item>
                        <el-form-item label="状态：" style="width: 30%;margin-bottom:0">
                            <span> {{statusMap[props.row.status]}}</span>
                        </el-form-item>
                        <el-form-item label="状态时间：" style="width: 30%;margin-bottom:0">
                            <span> {{props.row.statusDate == null?"":new Date(props.row.statusDate).toLocaleString()}}</span>
                        </el-form-item>
                        <el-form-item label="收货地址:" style="width:100%;margin-bottom:0">
                            <span> {{props.row.addrinfo.contactName + ' &nbsp;&nbsp;&nbsp;' + props.row.addrinfo.contactPhone +
                                ' &nbsp;&nbsp;&nbsp;' + props.row.addrinfo.addrDetail }}</span>
                        </el-form-item>
                        <el-form-item label="付费信息:" style="width:100%;margin-bottom:0">
                            <span style="color:blue;margin-right: 20px">{{props.row.hasPaid ? "已付费":"未付费"}}</span>
                            <el-button @click="checkFeeDetail(props.row.orderid)" type="primary" size="mini">费用详情</el-button>
                            
                        </el-form-item>
                        <el-form-item label="用户信息:" style="width:100%;margin-bottom:0">
                            <a href="#" @click="checkUserInfo(props.row.userid)"> {{props.row.userid}}</a>
                        </el-form-item>
                        <el-form-item label="物流信息:" style="width:100%;margin-bottom:0">
                            <span> {{props.row.logisticCompany}}</span>
                            <span> {{props.row.logisticNumber}}</span>
                        </el-form-item>
                        <el-form-item label="子订单项:" style="width:100%;margin-bottom:0">
                            <el-table :data="props.row.orderItemList" stripe style="min-width:600px">
                                <el-table-column prop="goodscode" label="商品编号" min-width="10px"> </el-table-column>
                                <el-table-column prop="goodsname" label="商品名称" min-width="10px"> </el-table-column>
                                <el-table-column label="单价" min-width="10px">
                                    <template slot-scope="scope"> ¥ {{scope.row.price/100.0}}</template>
                                </el-table-column>
                                <el-table-column prop="amount" label="数量" min-width="10px"> </el-table-column>
                            </el-table>
                        </el-form-item>
                    </el-form>
                </template>
            </el-table-column>
            <el-table-column prop="orderid" label="订单号" min-width="10px"> </el-table-column>
            <el-table-column label="总价" min-width="10px">
                <template slot-scope="scope"> ¥ {{scope.row.totalPrice/100.0}}</template>
            </el-table-column>
            <el-table-column label="状态" min-width="10px">
                <template slot-scope="scope"> {{statusMap[scope.row.status]}}</template>
            </el-table-column>
            <el-table-column label="创建时间" min-width="10px">
                <template slot-scope="scope"> {{scope.row.createDate == null?"":new Date(scope.row.createDate).toLocaleString()}}</template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" min-width="10px">
                <template slot-scope="scope">
                    <el-button @click="tryHandleOrder(scope.row)" size="mini">处理</el-button>
                    
                </template>
            </el-table-column>
        </el-table>
        <div class="block">
      <el-pagination
        @current-change="handleCurrentChange"
        :current-page.sync="curPage"
        :page-size="10"
        layout="total, prev, pager, next"
        :total="totalNum">
      </el-pagination>
    </div>
        <el-dialog title="处理订单" :visible.sync="dialogVisible" width="60%">
            <span style="color:blue;">请小心操作。</span>
            <div style="width:100%">
                    <span style="width:90px;margin-right:8px">新状态:&nbsp;&nbsp;&nbsp; </span>
                    <el-select v-model="newstatus" placeholder="">
                        <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value">
                        </el-option>
                    </el-select>
                    <el-input v-model="logisticCompany" placeholder="物流公司" v-if="newstatus=='2'" style="width:160px"></el-input>
                    <el-input v-model="logisticNumber" placeholder="请输入单号" v-if="newstatus=='2'" style="width:240px"></el-input>
                    <el-input v-model="cancelRemark" placeholder="取消原因" v-if="newstatus=='9'" style="width:360px"></el-input>
                </div>
            <span slot="footer" class="dialog-footer">
                <el-button @click="dialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="handleOrder()">确 定</el-button>
            </span>
        </el-dialog>

        <el-dialog title="用户信息" :visible.sync="userdialogVisible" width="60%">
          <span style="color:blue;">微信公开信息，仅供参考，未必是用户真实信息</span>
            <div style="width:100%">
              <img v-bind:src='userInfo.avatarUrl' style="width:60px;height:60px;margin:5px 20px 10px 10px;float:left"></img>
              <el-row :gutter="20" style="margin:20px 20px 10px 10px;">
                  <el-col :span="8">
                      <div class="grid-content"> <span>昵称：{{userInfo.nickName}}</span> </div>
                  </el-col>
                  <el-col :span="8">
                      <div class="grid-content"> <span>性别：{{ genderMap[userInfo.gender] }}</span> </div>
                  </el-col>
                  <el-col :span="8" style="margin:20px 20px 10px 0px;">
                      <div class="grid-content"> <span>地址：{{userInfo.country + " " + userInfo.province + " " + userInfo.city}}</span> </div>
                  </el-col>
              </el-row>
            </div>
            <span slot="footer" class="dialog-footer">
                <el-button @click="userdialogVisible = false">关闭</el-button>
            </span>
        </el-dialog>

        <el-dialog title="费用详情" :visible.sync="feedialogVisible" width="75%">
          <span style="color:blue;">请注意</span>
            <div style="width:100%">
              <el-row :gutter="20" >
                  <el-col :span="6">
                      <div class="grid-content"> <span>订单号：{{orderFee.orderid}}</span> </div>
                  </el-col>
                  <el-col :span="6">
                      <div class="grid-content"> <span>实际费用： ¥ {{ orderFee.totalPrice/100.0 }}</span> </div>
                  </el-col>
                  <el-col :span="6">
                      <div class="grid-content"> <span>原始总价： ¥ {{ orderFee.originPrice/100.0 }}</span> </div>
                  </el-col>
                  <el-col :span="6">
                      <div class="grid-content"> <span>折扣优惠： ¥ {{ orderFee.discountAmount/100.0 }}</span> </div>
                  </el-col>
              </el-row>
              <el-row :gutter="20" >
                  <el-col :span="6">
                      <div class="grid-content"> <span>减免费用： ¥ {{ orderFee.waiveFee/100.0 }}</span> </div>
                  </el-col>
                  <el-col :span="6">
                      <div class="grid-content"> <span>物流费： ¥ {{ orderFee.shippingFee/100.0  }}</span> </div>
                  </el-col>
                  <el-col :span="6">
                      <div class="grid-content"> <span>使用积分：{{ orderFee.score }}</span> </div>
                  </el-col>
                  <el-col :span="6">
                      <div class="grid-content"> <span>退款金额： ¥ {{ orderFee.refundfee/100.0 }}</span> </div>
                  </el-col>
              </el-row>
            </div>
            <span slot="footer" class="dialog-footer">
                <el-button @click="tryRefundOrder(orderFee)" style="float:left">退款</el-button>
                <el-button @click="feedialogVisible = false">关闭</el-button>
            </span>
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
      tableData: [],
      form: {},
      dialogVisible:false,
      userdialogVisible:false,
      feedialogVisible:false,
      genderMap:{"1":"男","2":"女"},
      statusMap:{
        "0":"待付款",
        "1":"待发货",
        "2":"已发货",
        "3":"已完成",
        "9":"已取消"
      },
      cancelRemark:"",
      newstatus:"",
      logisticCompany:"",
      logisticNumber:"",
      status:"",
      timerange:['',new Date()],
      nickName:"",
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
      options: [{
          value: '0',
          label: '待付款'
        }, {
          value: '1',
          label: '待发货'
        }, {
          value: '2',
          label: '已发货'
        }, {
          value: '3',
          label: '已完成'
        }, {
          value: '9',
          label: '已取消'
        }],
      
      userInfo: {},
      orderFee: {}
    }    
  },
  created: function () {
    this.getOrders(10,1)
  },
  methods: {
    handleCurrentChange(val) {
        console.log(`当前页: ${val}`);
        this.curPage = val
        this.getOrders(10,this.curPage)
    },
    getOrders (pageSize,curPage) {
      pageSize = pageSize == null ? 10: pageSize
      curPage = curPage == null ? 1: curPage

      var starttime = Date.parse(this.timerange[0])
      var endtime = Date.parse(this.timerange[1])

      var self = this
      axios.post('/order/admin/all',{ "pageSize": pageSize.toString(),"curPage":curPage.toString(),"starttime":starttime,"endtime":endtime,"nickName":self.nickName,"status":self.status })
        .then(function (response) {
          console.log(response)
          var data = response.data
          self.tableData = data.retParams.orderList
          self.curPage = curPage
          self.totalNum = data.retParams.total

        }).catch(function (error) {
          self.$message({ message: error, type: 'error' })
        })

    },
    tryHandleOrder(order){
      console.log(order)
      this.form =  order
      this.dialogVisible = true;
      this.newstatus = "";
      this.logisticCompany = "";
      this.logisticNumber = "";
    },
    tryRefundOrder(orderfee){
      console.log(orderfee)
      var self = this
      axios.post('/wxpay/WXRefund', orderfee.orderid )
        .then(function (response) {
          console.log(response)
          var data = response.data
          if(data.retCode == '0'){
            self.$message({ message: '发起退款成功', type: 'warning' })
          } else {
            self.$message({ message: data.retMessage, type: 'error' })
          }

        }).catch(function (error) {
          self.$message({ message: error, type: 'error' })
        })
    },
    handleOrder(){
      console.log(this.logisticCompany)
      if(this.newstatus =='2' && (!this.logisticCompany || this.logisticCompany.lenght <=0) )
      {
        this.$message({ message: "必须输入物流信息", type: 'error' })
        return;
      }

      var self = this
      axios.post('/order/admin/update',{ "orderid": this.form.orderid,"status":self.newstatus,"logisticCompany":this.logisticCompany,"logisticNumber":this.logisticNumber })
        .then(function (response) {
          console.log(response)
          self.dialogVisible = false;
          self.getOrders(10,self.curPage)

        }).catch(function (error) {
          self.$message({ message: error, type: 'error' })
        })

    },
    checkUserInfo(userid){
      console.log(userid)
      var self = this
      axios.get('/user/info?userid=' + userid)
        .then(function (response) {
          console.log(response)
          var data = response.data
          self.userInfo = data.retParams.userInfo

          self.userdialogVisible = true;
        
        }).catch(function (error) {
          self.$message({ message: error, type: 'error' })
        })
      
    },
    checkFeeDetail(orderid){
      console.log(orderid)
      this.orderFee = {}
      var self = this
      axios.get('/orderfee?orderid=' + orderid)
        .then(function (response) {
          console.log(response)
          var data = response.data
          self.orderFee = data.retParams.orderfee

          self.feedialogVisible = true;
        
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
li{ float:left; list-style:none;width: 48%;margin: 5px} 
.tableheader{border-bottom:1px dashed #000;}
</style>