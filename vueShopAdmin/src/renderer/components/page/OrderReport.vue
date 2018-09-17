<template>
    <div>
        <div class="crumbs" style="margin-bottom: 8px">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item>
                    <i class="el-icon-menu"></i> 订单统计</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div>
            <li>
                <div class="block" style="width：100%">
                    <span class="demonstration" style="width:150px;margin-right:8px">起始时间:</span>
                    <el-date-picker v-model="timerange" type="datetimerange" :picker-options="pickerOptions2" range-separator="至" start-placeholder="开始日期"
                        end-placeholder="结束日期" align="right">
                    </el-date-picker>
                </div>
            </li>
            <li>
                <el-button type="primary" plain @click="getOrders(10,1)">确定</el-button>
            </li>
            <br>
            <span style="border:0.2px solid #C0C0C0; width:100%;float:left"></span>
        </div>
        <div style="clear: both;"> </div>
        <div>
            <p style="margin:8px"></p>
        </div>
        <el-row :gutter="20">
            <el-col :span="8">
                <div class="grid-content"> <span>总销量：{{totalNum}}</span> </div>
            </el-col>
            <el-col :span="8">
                <div class="grid-content"> <span>总金额：¥ {{10768}}</span> </div>
            </el-col>
            <el-col :span="8">
                <div class="grid-content"> <span>购买人数：{{200}}</span> </div>
            </el-col>
        </el-row>
        <el-row :gutter="20">
            <el-col :span="8">
                <div class="grid-content bg-purple">

                </div>
            </el-col>
            <el-col :span="8">
                <div class="grid-content bg-purple"></div>
            </el-col>
            <el-col :span="4">
                <div class="grid-content bg-purple"></div>
            </el-col>
            <el-col :span="4">
                <div class="grid-content bg-purple"></div>
            </el-col>
        </el-row>
        <el-row :gutter="20">
            <el-col :span="4">
                <div class="grid-content bg-purple"></div>
            </el-col>
            <el-col :span="16">
                <div class="grid-content bg-purple"></div>
            </el-col>
            <el-col :span="4">
                <div class="grid-content bg-purple"></div>
            </el-col>
        </el-row>

    </div>
</template>

<script>

import axios from 'axios'

export default {
  data () {
    return {
      tableData: [],
      dialogVisible:false,
      statusMap:{
        "0":"待付款",
        "1":"待发货",
        "2":"已发货",
        "3":"已完成",
        "9":"已取消"
      },
      timerange:['',new Date()],
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
        }]
    }
  },
  created: function () {
    this.getOrders(10,1)
  },
  methods: {
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

.el-row {
    margin-bottom: 20px;
    &:last-child {
      margin-bottom: 0;
    }
  }
  .el-col {
    border-radius: 4px;
  }
  .bg-purple-dark {
    background: #99a9bf;
  }
  .bg-purple {
    background: #d3dce6;
  }
  .bg-purple-light {
    background: #e5e9f2;
  }
  .grid-content {
    border-radius: 4px;
    min-height: 36px;
  }
  .row-bg {
    padding: 10px 0;
    background-color: #f9fafc;
  }
</style>