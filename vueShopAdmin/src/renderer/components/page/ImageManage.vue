<template>
    <div class="table">
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item>
                    <i class="el-icon-menu"></i> 图片管理 
                </el-breadcrumb-item>
            </el-breadcrumb>
            <p/>
        </div>
        <el-button type="primary" icon="el-icon-circle-plus" style="margin-bottom: 8px" @click="uploadDialogVisible=true">上传图片</el-button>
        <span style="margin-left:20px">提示：双击图片名称可以修改图片名及设置图片是否轮播图片</span>
        <div >
            <el-card :body-style="{ padding: '4px',position:'absolute',bottom:0,width:'95%' }" v-for="(image,index) in images" :key="index" class="box-card card" v-bind:style="{'backgroundImage':`url(${image.url})`, 'background-size': '100% 100%','position': 'relative'}" >
                <div style="">
                  <div class="clearfix">
                    <span @dblclick="edit(image)">{{ image.fileName }}</span>
                    
                    <el-button type="danger" class="button" @click="tryDeleteImage(image,index)">删除</el-button>
                  </div>
                </div> 
            </el-card>
            <el-dialog title="提示" :visible.sync="dialogVisible" width="30%" >
              <span v-if="delItem.image && delItem.image.isbanner">此图是微信小商城顶部轮播图片</span>
              <span>确定删除吗？</span>
              <span slot="footer" class="dialog-footer">
                <el-button @click="dialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="deleteImage()">确 定</el-button>
              </span>
            </el-dialog>
            <el-dialog title="图片详情" :visible.sync="detailDialogVisible" width="70%">
                <img width="100%" :src="image.detailurl" alt="">
                <el-switch active-text="轮播图片" inactive-text="普通图片" v-model="image.isbanner"></el-switch>
              </p>
                <span>上传时间: </span>
                <time class="time">{{ new Date(image.createDate).toLocaleString() }}</time>
                <el-input v-model="image.fileName" style="margin-top:14px;margin-bottom:14px;"></el-input>
                <el-button @click="detailDialogVisible = false">取消</el-button>
                <el-button @click="updateImage()" type="primary">确定</el-button>
            </el-dialog>
            <el-dialog title="上传图片" :visible.sync="uploadDialogVisible" width="70%">
              <el-upload class="upload-demo" ref="upload"
                        action="/restapi/image/uploadfile"
                        :beforeUpload="beforeUpload"
                        :data="extendData"
                        list-type="picture-card"
                        :limit="5"
                        :auto-upload="true">
                        <el-button slot="trigger" size="small" type="primary">选择</el-button>
                        
                        <div class="el-upload__tip" slot="tip">只能上传jpg/png文件，大小不超过4M</div>
                </el-upload>
            </el-dialog>
        </div>
        <div style="clear: both;"> </div>
        <div class="block">
      <el-pagination
        @current-change="handleCurrentChange"
        :current-page.sync="curPage"
        :page-size="12"
        layout="total, prev, pager, next"
        :total="totalNum">
      </el-pagination>
    </div>
    </div>
</template>

<script>
import axios from 'axios'

export default {
  data () {
    return {
      curPage:1,
      totalNum:0,
      images:[],
      dialogVisible:false,
      uploadDialogVisible:false,
      detailDialogVisible:false,
      image:{},   //选中的那个image
      delItem:{},
      extendData: {},
      isBanner:false
    }
  },
  created: function () {
    this.getImages(12,1)
  },
  methods: {
    handleCurrentChange(val) {
        console.log(`当前页: ${val}`);
        this.curPage = val
        this.getImages(12,val)
    },
    getImages (pageSize,curPage) {
      pageSize = pageSize == null ? 12: pageSize
      curPage = curPage == null ? 1: curPage

      var self = this
      axios.get('/image/all',{ headers: {'Cache-Control': 'public'}, 
                                params: { "pageSize": pageSize,"curPage":curPage } })
        .then(function (response) {
          console.log(response)
          var data = response.data
          self.images = data.retParams.images

          for (var i =0; i < self.images.length; i++) {
            self.images[i].url = axios.defaults.baseURL + '/thumbnail/' + self.images[i].fileid
            self.images[i].detailurl = axios.defaults.baseURL + '/image/' + self.images[i].fileid
          }

          self.curPage = curPage
          self.totalNum = data.retParams.totalNum

        }).catch(function (error) {
          self.$message({ message: error, type: 'error' })
        })

    },
    edit(image){
      this.image = image;
      this.detailDialogVisible = true;
    },
    updateImage(){
      console.log(this.image)
      var self = this
      axios.post('/image/admin/update', 
        {"fileid":this.image.fileid,
          "fileName" : this.image.fileName,
          "isbanner" : this.image.isbanner ? "Y":"N"
        }).then(function (response) {
        console.log(response)
        var data = response.data
        
        self.$message('成功');
        self.detailDialogVisible = false
        
      }).catch(function (error) {
          self.$message({ message: error, type: 'error' })
      })
    },
    tryDeleteImage(image,index) {
      this.dialogVisible = true;
      this.delItem.image = image;
      this.delItem.index = index;
      console.log(this.delItem)
    },
    deleteImage(){
      console.log(this.delItem)
      var self = this;
      axios.delete('/image/admin/' + self.delItem.image.fileid).then(function (response) {
        console.log(response)
        var data = response.data
        if (data.retCode !== '0') {
                self.$message({ message: data.retMessage, type: 'error' })
        } else {
            self.$message('成功');
            console.log(self.delItem)
            self.images.splice(self.delItem.index,1)
        }
        
        }).catch(function (error) {
          self.$message({ message: error, type: 'error' })
        })

        this.dialogVisible = false;
    },
    beforeUpload (file) {
      console.log('before')
      this.extendData.encodefilename = encodeURI(file.name)
      console.log(this.extendData)
    },
    submitUpload() {
      this.$refs.upload.submit();
    },
    changePage (values) {
      this.information.pagination.per_page = values.perpage
      this.information.data = this.information.data
    },
    onSearch (searchQuery) {
      this.query = searchQuery
    }
  },
  computed: {
    
  }
}
</script>

<style>
  .time {
    font-size: 13px;
    color: #999;
  }
  
  .bottom {
    margin-top: 13px;
    line-height: 12px;
  }

  .button {
    padding: 0;
    float: right;
  }

  .card {
    width: 20%;
    height: 0;
    padding-bottom: 18%;
    float: left;
    margin: 4px;
  }

</style>