<template>
  <div>
    <div class="crumbs" style="margin-bottom: 8px">
          <el-breadcrumb separator="/">
              <el-breadcrumb-item><i class="el-icon-menu"></i> 商品</el-breadcrumb-item>
          </el-breadcrumb>
    </div>
    <div >
            <li>
                <div style="width:100%">
                    <span style="width:150px;margin-right:8px">分类:&nbsp;&nbsp;&nbsp; </span>
                    <el-select v-model="s_classify" clearable placeholder="">
                        <el-option v-for="item in classifyList" :key="item.code" :label="item.name" :value="item.code">
                        </el-option>
                    </el-select>
                </div>
            </li>
            <li>
                <div>
                    <span class="demonstration" style="width:150px;margin-right:8px">名称:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
                    <el-input v-model="s_name" placeholder="请输入商品名称" style="width:150px;margin-right:8px"></el-input>
                </div>
            </li>
            <li>
                <el-button type="info" icon="el-icon-search" @click="searchGoods()">查询</el-button>
            </li>
            <br>
            <span style="border:0.2px solid #C0C0C0; width:100%;float:left;margin-bottom: 10px"></span>
        </div>
        <p >
    <el-button type="primary" icon="el-icon-circle-plus" style="margin-bottom: 8px" @click="newGoods()">新增商品</el-button>
    <el-table :data="tableData" stripe border style="width: 100%">
      <el-table-column prop="name" label="商品名称" min-width="10px" > </el-table-column>
      <el-table-column label="分类" min-width="10px" > 
        <template slot-scope="scope">
          <p>{{ getClassifyName(scope.row.classify) }}</p>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="商品描述" min-width="35px" 
      show-overflow-tooltip > </el-table-column>
      <el-table-column prop="price" label="价格" min-width="10px" >
        <template slot-scope="scope">{{scope.row.price/100.0}}</template>
         </el-table-column>
      <el-table-column label="状态" min-width="10px" > 
        <template slot-scope="scope"><a v-bind:class=" (scope.row.status =='R') ? 'release' : 'draft' " ></a>{{scope.row.status == 'R' ? "发布" : "草稿"}}</template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" min-width="25px" >
        <template slot-scope="scope">
          <el-button @click="changeGoodsStatus(scope.row)" type="primary" size="mini" >{{scope.row.status=='R' ? "下架" : "上架"}}</el-button>
          <el-button @click="tryDeleteGoods(scope.row,scope.$index)" type="danger" size="mini">删除</el-button>
          <el-button @click="editGoods(scope.row)" size="mini">编辑</el-button>
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
    <el-dialog title="提示" :visible.sync="dialogVisible" width="30%" >
        <span>确定删除此商品吗？</span>
        <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitDeleteGoods()">确 定</el-button>
        </span>
    </el-dialog>
    <el-dialog title="编辑商品" :visible.sync="editDialogVisible" width="70%" >
        <div class="form-box">
            <el-form ref="form" :model="form" :rules="rules" label-width="80px">
                <el-form-item label="商品名称"  prop="name">
                    <el-input v-model="form.name"></el-input>
                </el-form-item>
                <el-form-item label="选择分类"  prop="classify">
                    <el-select v-model="form.classify" placeholder="请选择">
                        <el-option :key="classify.code" :label="classify.name" :value="classify.code" v-for="classify in classifyList"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="创建时间">
                    <i class="el-icon-time"></i>
                    <span style="margin-left: 10px">{{ formatCreateDate }}</span>
                </el-form-item>
                <el-form-item label="商品描述"  prop="remark">
                    <el-input type="textarea" v-model="form.remark"></el-input>
                </el-form-item>
                <el-form-item label="商品详图" >
                    <el-upload class="upload-demo" style="width: 50%" ref="upload"
                        action="/restapi/image/uploadfile"
                        :on-preview="handlePictureCardPreview"
                        :on-remove="handleDetailImgRemove"
                        :on-success="handleDetailImgSuccess"
                        :beforeUpload="beforeUpload"
                        :file-list="detailimgList"
                        :auto-upload="true"
                        :data="extendData"
                        list-type="picture"
                        :limit="5" >
                        <el-button slot="trigger" size="small" type="primary">上传文件</el-button>
                        <div class="el-upload__tip" slot="tip">只能上传jpg/png文件，且不超过3M</div>
                    </el-upload>
                </el-form-item>

                <el-form-item label="商品价格"  prop="price">
                    <el-input v-model.number="form.price" :maxlength="7" style="width:100px" ></el-input>分
                </el-form-item>
                <el-form-item label="商品规格">
                    <el-button type="primary" @click="addSpecdialogVisible = true" size="mini">新增规格</el-button>
                    <div style="width:100%" v-for="(spec,index) in specs">
                      规格编码：
                      <el-input :value="spec[0]" style="width:70px" :disabled="true" ></el-input>
                      规格名称：
                      <el-input :value="spec[1]" style="width:100px" :disabled="true"></el-input>
                      价格(元)：
                      <el-input :value="spec[2]/100.0" style="width:80px" :disabled="true"></el-input>
                      <el-button @click="delextSpec(index)" type="warning" size="mini" style="margin-left:10px">删除</el-button>
                    </div>
                </el-form-item>
                <el-form-item label="轮播图">
                    <el-upload class="upload-demo" ref="upload"
                        action="/restapi/image/uploadfile"
                        :on-preview="handlePictureCardPreview"
                        :on-remove="handleRemove"
                        :on-success="handleSuccess"
                        :beforeUpload="beforeUpload"
                        :file-list="fileList"
                        :auto-upload="true"
                        :data="extendData"
                        list-type="picture-card"
                        :limit="9" >
                        <el-button slot="trigger" size="small" type="primary">上传文件</el-button>
                        <div class="el-upload__tip" slot="tip">只能上传jpg/png文件，且不超过3M</div>
                    </el-upload>
                    <el-dialog :visible.sync="previewdialogVisible" append-to-body>
                      <img width="100%" :src="dialogImageUrl" alt="">
                    </el-dialog>
                </el-form-item>
                <el-form-item label="商品参数">
                  <div style=" float:left; width:45%;margin-right:20px;font-family: kaiti;" v-for="(param,index) in extParams">
                    <span style="width:45%">{{param[0] + ' : '}}</span> <span style="width:45%">{{param[1]}}</span>
                    <el-button @click.prevent="delextParam(index)"  plain size="mini" style="margin-left:18px">删除</el-button>
                  </div>
                  </p>
                <el-button @click="addParamdialogVisible=true" type="primary" size="mini">新增参数</el-button>
                <el-dialog :visible.sync="addParamdialogVisible" append-to-body>
                    <p style="margin-left:20px">提示：输入参数名称及参数值，如包装 15g X 20</p>
                    参数：<el-input v-model="addKey" style="width:150px"></el-input>
                     值：<el-input v-model="addValue" style="width:150px"></el-input>
                     <!-- 
                     排序：<el-input v-model="sortValue" style="width:150px"></el-input> -->
                    <el-button type="primary" @click="addextParams">确 定</el-button>
                </el-dialog>
                <el-dialog :visible.sync="addSpecdialogVisible" append-to-body>
                    <p style="margin-left:20px">提示：选择规格并输入价格</p>
                    <span style="width:90px;margin-right:8px">规格:&nbsp;&nbsp;&nbsp; </span>
                    <el-select v-model="newSpec" placeholder="" >
                        <el-option v-for="item in options" :key="item.value" :label="item.label+'('+ item.value + ')'" :value="item.value + '|' + item.label">
                        </el-option>
                    </el-select>
                     价格（分）：<el-input v-model="newSpecPrice" style="width:150px"></el-input>
                    <el-button type="primary" @click="addNewSpec">确 定</el-button>
                </el-dialog>
               </el-form-item> 

                </p>

                <el-form-item>
                    <el-button @click="editDialogVisible = false">取 消</el-button>
                    <el-button type="primary" @click="submitGoods">确 定</el-button>
                </el-form-item>
            </el-form>
        </div>    
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
      classifyList:[],
      s_classify:"",
      s_name:"",
      tableData: [
        {
          code: 'xinyanghong',
          name: '信阳红',
          classify: '红茶',
          remark: '信阳红茶',
          price: '100',
          status: 'R',
          createDate: '2018-02-26 23:40:00'
        }
      ],
      rules: {
          name: [
            { required: true, message: '请输入商品名称', trigger: 'blur' },
            { min: 1, max: 80, message: '长度在 1 到 80 个字符', trigger: 'blur' }
          ],
          classify: [
            { required: true, message: '请选择分类', trigger: 'change' }
          ],
          price: [
            { required: true, message: '请填写价格', trigger: 'change' }
          ],
          remark: [
            { required: true, message: '请填写活动形式', trigger: 'blur' }
          ]
        },
      dialogImageUrl: '',
      previewdialogVisible: false,
      dialogVisible: false,
      editDialogVisible: false,
      addParamdialogVisible: false,
      addSpecdialogVisible:false,
      form: {},
      fileList: [],
      detailimgList:[],
      extendData: {},
      extParams:[],
      addKey:"",
      addValue:"",
      sortValue:"",
      delIndex:-1,
      options: [{
          value: '50g',
          label: '一两'
        }, {
          value: '100g',
          label: '二两'
        }, {
          value: '125g',
          label: '二两半'
        }, {
          value: '250g',
          label: '半斤'
        }, {
          value: '500g',
          label: '一斤'
        }],
      newSpec:"",        //新规格编码
      newSpecPrice:'',   //新规格价格
      specs:[]
    }
  },
  created: function () {
    this.getClassify();
    this.getGoods(10,1)
  },
  methods: {
    handleCurrentChange(val) {
        console.log(`当前页: ${val}`);
        this.curPage = val
        this.getGoods(10,val)
    },
    getClassifyName: function (classify) {
      for (var i = this.classifyList.length - 1; i >= 0; i--) {
        if(this.classifyList[i].code == classify){
          return this.classifyList[i].name
        }
      }
    },
    getClassify() {
      //获取所有分类
      var self = this
      axios.get('/classify')
        .then(function (response) {
          console.log(response)
          var data = response.data
          self.classifyList = data.retParams.classifyList
        
        }).catch(function (error) {
          self.$message({ message: error, type: 'error' })
        })
    },
    getGoods (pageSize,curPage) {
      pageSize = pageSize == null ? 10: pageSize
      curPage = curPage == null ? 1: curPage

      var self = this
      axios.get('/goods',{ params: { "pageSize": pageSize,"curPage":curPage } })
        .then(function (response) {
          console.log(response)
          var data = response.data
          self.tableData = data.retParams.goodsList
          self.curPage = curPage
          self.totalNum = data.retParams.totalNum

        }).catch(function (error) {
          self.$message({ message: error, type: 'error' })
        })

    },
    searchGoods (pageSize,curPage) {
      if(!this.s_name && !this.s_classify){
        this.getGoods(pageSize,curPage)
        return;
      }

      pageSize = pageSize == null ? 10: pageSize
      curPage = curPage == null ? 1: curPage

      var self = this
      var path = "/goods/search"
      if(this.s_classify)
      {
        path = "/goods/byclassify"
      }
      axios.get(path,{ params: { "pageSize": pageSize,"curPage":curPage,"classify":this.s_classify,"keyword":this.s_name } })
        .then(function (response) {
          console.log(response)
          var data = response.data
          self.tableData = data.retParams.goodsList
          self.curPage = curPage
          self.totalNum = data.retParams.totalNum

        }).catch(function (error) {
          self.$message({ message: error, type: 'error' })
        })

    },
    changeGoodsStatus (goods) {
      if (goods.status == 'R') {
        goods.status = 'D'
      } else {
        goods.status = 'R'
      }

      axios.post('/goods/admin/'+goods.code + '/status', goods.status).then(function (response) {
            console.log(response)
            self.editDialogVisible = false
          }).catch(function (error) {
            console.log(error)
          })
    },
    tryDeleteGoods (goods,index) {
      this.form = goods
      this.delIndex = index
      this.dialogVisible = true
    },
    cancelDeleteGoods () {
      this.form = {}
      this.dialogVisible = false
    },
    submitDeleteGoods () {

      this.dialogVisible = false
      var self = this
      axios.delete('/goods/admin/' + self.form.code).then(function (response) {
        console.log(response)
        var data = response.data
        if (data.retCode !== '0') {
                self.$message({ message: data.retMessage, type: 'error' })
        } else {
            self.$message('成功');
            self.dialogVisible = false;
            //self.tableData.splice(self.delIndex,1)
            self.getGoods(10,self.curPage)
        }
        
      }).catch(function (error) {
          self.$message({ message: error, type: 'error' })
      })

      this.form = {}
    },
    newGoods () {
      this.form = {createDate: new Date().getTime(), status: 'D'}
      this.fileList = []
      this.detailimgList = []
      this.extParams = []
      this.editDialogVisible = true
    },
    submitGoods () {

      this.$refs["form"].validate((valid) => {
          if (valid) {
            this.form.extParams = this.extParams
            this.form.specs = this.specs;
            var self = this

            if(this.form.code == null){
                axios.post('/goods/admin', this.form).then(function (response) {
                  console.log(response)
                  self.editDialogVisible = false
                  self.getGoods(10,self.curPage)
                }).catch(function (error) {
                  console.log(error)
                })
            } else
            {
                axios.post('/goods/admin/update', this.form).then(function (response) {
                  console.log(response)
                  self.editDialogVisible = false
                }).catch(function (error) {
                  console.log(error)
                })
            }
          } else {
            console.log('error submit!!');
            return false;
          }
        });

    },
    editGoods (goods) {
      this.form = goods
      this.fileList = []
      this.detailimgList = []
      this.extParams = goods.extParams || {}
      this.specs = goods.specs || []

      goods.images = goods.images || []
      goods.detailImgs = goods.detailImgs || []

      for (var i =0; i < goods.images.length; i++) {
        var url = axios.defaults.baseURL + '/thumbnail/' + goods.images[i]
        this.fileList.push({"url": url,"fileid":goods.images[i]})
      }

      for (var i =0; i < goods.detailImgs.length; i++) {
        var url = axios.defaults.baseURL + '/thumbnail/' + goods.detailImgs[i]
        this.detailimgList.push({"url": url,"fileid":goods.detailImgs[i]})
      }
      this.editDialogVisible = true
    },
    beforeUpload (file) {
      console.log('before')
      const isInLimit = file.size / 1024 / 1024 < 3;
      if (!isInLimit) {
          this.$message.error('上传图片大小不能超过 3MB!');
          return isInLimit;
      }

      this.extendData.encodefilename = encodeURI(file.name)
      console.log(this.extendData)
    },
    handleRemove (file, fileList) {
      console.log(file, fileList)

      var fileid = file.fileid
      var i = this.form.images.indexOf(fileid) 
      if(i != -1){
        this.form.images.splice(i,1)
      }

      var self = this

      if(this.form.code != null){   //先更新商品的图片信息 再去删除图片
        axios.post('/goods/admin/'+ this.form.code + '/images', this.form.images).then(function (response) {
              console.log(response)
              var data = response.data
              if(data.retCode !== '0'){
                self.$message({ message: data.retMessage, type: 'error' })
                return;
              }
              
              axios.delete('/image/admin/' + fileid).then(function (response) {
                  console.log(response)

              }).catch(function (error) {
                  console.log(error)
              })

          }).catch(function (error) {
              console.log(error)
          })
      
      } else {   //直接删除
          axios.delete('/image/admin/' + fileid).then(function (response) {
              console.log(response)
          }).catch(function (error) {
              console.log(error)
          })
      }  
      
    },
    handleSuccess (response, file, fileList) {
      console.log(response)
      var fileid = response.retParams.fileid
      file.fileid = fileid
      console.log(file)
      this.form.images = this.form.images || [] ;
      this.form.images.push(fileid)
      //var url = axios.defaults.baseURL + '/thumbnail/' + fileid
      //this.fileList.push({"url": url})
      console.log(fileList)
      console.log(this.form)
    },
    handleDetailImgRemove (file, detailimgList) {
      console.log(file, detailimgList)

      var fileid = file.fileid
      var i = this.form.detailImgs.indexOf(fileid) 
      if(i != -1){
        this.form.detailImgs.splice(i,1)
      }

      var self = this

      if(this.form.code != null){   //先更新商品的图片信息 再去删除图片
        axios.post('/goods/admin/'+ this.form.code + '/detailimgs', this.form.detailImgs).then(function (response) {
              console.log(response)
              var data = response.data
              if(data.retCode !== '0'){
                self.$message({ message: data.retMessage, type: 'error' })
                return;
              }
              
              axios.delete('/image/admin/' + fileid).then(function (response) {
                  console.log(response)

              }).catch(function (error) {
                  console.log(error)
              })

          }).catch(function (error) {
              console.log(error)
          })
      
      } else {   //直接删除
          axios.delete('/image/admin/' + fileid).then(function (response) {
              console.log(response)
          }).catch(function (error) {
              console.log(error)
          })
      }  
      
    },
    handleDetailImgSuccess (response, file, detailimgList) {
      console.log(response)
      var fileid = response.retParams.fileid
      file.fileid = fileid
      console.log(file)
      this.form.detailImgs = this.form.detailImgs || [] ;
      this.form.detailImgs.push(fileid)
      console.log(detailimgList)
      console.log(this.form)
    },
    handleChange (file, fileList) {
      console.log(file, fileList)
    },
    handlePreview (file) {
      console.log(file)
    },
    handlePictureCardPreview (file) {
      console.log(file)
      var urlPath = '/restapi/image/'
      this.dialogImageUrl = urlPath + file.fileid
      this.previewdialogVisible = true
    },
    addextParams(){
      if(this.extParams == null){
        this.extParams = []
      }
      this.extParams.push([this.addKey,this.addValue,this.sortValue])
      this.addKey = "",
      this.addValue = "",
      this.sortValue = ""
      this.addParamdialogVisible = false
      console.log(this.extParams)
    },
    addNewSpec(){
      console.log(this.newSpec)

      var specParams = this.newSpec.split('|')

      for(var i=0; i<this.specs.length;i++){
        if(this.specs[i][0] == specParams[0]){
          this.specs.splice(i,1)
          break;
        }
      }
      
      this.specs.push([specParams[0],specParams[1],this.newSpecPrice])
      console.log(this.form)
      this.newSpec = "",
      this.newSpecPrice = null
      this.addSpecdialogVisible = false
    },
    delextSpec(index){
      console.log(index)
      this.specs.splice(index,1) 
      console.log(this.specs)
    },
    delextParam(index){
      this.extParams.splice(index,1) 
      console.log(this.extParams)
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
li{ float:left; list-style:none;width: 30%;margin: 5px} 
.release {
  height: 18px;
  width: 20px;
  float: right;
  cursor: pointer;
  margin-right: 2px;
  background-image: url(../../assets/release.png);
  background-repeat: no-repeat;
}
.draft {
  height: 18px;
  width: 20px;
  float: right;
  cursor: pointer;
  margin-right: 2px;
  background-image: url(../../assets/draft.png);
  background-repeat: no-repeat;
}
</style>