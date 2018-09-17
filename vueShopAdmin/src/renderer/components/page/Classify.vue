<template>
  <div>
    <div class="crumbs" style="margin-bottom: 8px">
          <el-breadcrumb separator="/">
              <el-breadcrumb-item><i class="el-icon-menu"></i> 分类</el-breadcrumb-item>
          </el-breadcrumb>
    </div>
    <el-button type="primary" icon="el-icon-circle-plus" style="margin-bottom: 8px" @click="newClassify()">新增分类</el-button>
    <el-table :data="tableData" stripe border style="width: 100%">
      <el-table-column prop="name" label="分类名称" min-width="15px" >
      </el-table-column>
      <el-table-column prop="remark" label="分类描述" min-width="60px">
      </el-table-column>
      <el-table-column fixed="right" label="操作" min-width="15px">
      <template slot-scope="scope">
        <el-button @click="preDelete(scope.row,scope.$index)" type="danger" size="mini">删除</el-button>
        <el-button @click="editClassify(scope.row)" size="mini">编辑</el-button>
      </template>
    </el-table-column>
    </el-table>
    <el-dialog title="提示" :visible.sync="dialogVisible" width="30%" >
        <span>确定删除此分类吗？</span>
        <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="deleteClassify()">确 定</el-button>
        </span>
    </el-dialog>
    <el-dialog title="编辑分类" :visible.sync="editDialogVisible" width="70%" >
        <el-form ref="form" label-width="80px">
            <el-form-item label="分类名称">
                <el-input v-model="form.name"></el-input>
            </el-form-item>
            <el-form-item label="分类描述">
                <el-input type="textarea" v-model="form.remark"></el-input>
            </el-form-item>
        </el-form>    
        <span slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitClassify()">确 定</el-button>
        </span>
    </el-dialog>
    <el-dialog title="新增分类" :visible.sync="newDialogVisible" width="70%" >
        <el-form label-width="80px">
            <el-form-item label="分类名称">
                <el-input v-model="form.name"></el-input>
            </el-form-item>
            <el-form-item label="分类描述">
                <el-input type="textarea" v-model="form.remark"></el-input>
            </el-form-item>
        </el-form>    
        <span slot="footer" class="dialog-footer">
        <el-button @click="newDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitClassify()">确 定</el-button>
        </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  data () {
    return {
      tableData: [
        {
          code: 'GreenTea',
          name: '绿茶',
          remark: '绿茶是未经发酵的,中国产量最多,饮用最为广泛的一种茶.它的特点是汤清叶绿。'
        }
      ],
      dialogVisible: false,
      editDialogVisible: false,
      newDialogVisible: false,
      form: {
        code: '',
        name: '',
        remark: ''
      },
      delItem:{}
    }
  },
  created: function () {
    this.getClassify()
  },
  methods: {
    getClassify() {
      //获取所有分类
      var self = this
      axios.get('/classify')
        .then(function (response) {
          console.log(response)
          var data = response.data
          self.tableData = data.retParams.classifyList
        
        }).catch(function (error) {
          self.$message({ message: error, type: 'error' })
        })
    },
    submitClassify () {
      var self = this
      axios.post('/classify/admin'+ (this.form.code == null ? '' : '/'+ this.form.code), this.form).then(function (response) {
        console.log(response)
        var data = response.data
        if (data.retCode !== '0') {
                self.$message({ message: data.retMessage, type: 'error' })
        } else {
            self.$message('成功');
            self.newDialogVisible = false
            self.editDialogVisible = false
            self.getClassify()
        }
        
      }).catch(function (error) {
          self.$message({ message: error, type: 'error' })
      })
    },
    editClassify (classify) {
      this.form = classify
      this.editDialogVisible = true
    },
    newClassify () {
      this.form = {name: '', remark: ''}
      this.newDialogVisible = true
    },
    preDelete(classify,index){
      this.delItem.classify = classify;
      this.delItem.index = index;
      this.dialogVisible = true;
    },
    deleteClassify () {
      var self = this
      console.log(this.delItem.index)
      axios.delete('/classify/admin/' + self.delItem.classify.code).then(function (response) {
        console.log(response)
        var data = response.data
        if (data.retCode !== '0') {
                self.$message({ message: data.retMessage, type: 'error' })
        } else {
            self.$message('成功');
            self.dialogVisible = false;
            self.tableData.splice(self.delItem.index,1)
        }
        
      }).catch(function (error) {
          self.$message({ message: error, type: 'error' })
      })

      this.dialogVisible = false;
    }
      
  }
}
</script>