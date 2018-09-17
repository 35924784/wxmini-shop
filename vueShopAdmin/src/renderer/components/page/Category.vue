<template>
    <div>
        <div class="crumbs" style="margin-bottom: 8px">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item>
                    <i class="el-icon-menu"></i> 销售目录</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div style="border:1px solid #ebeef5;margin:4px"></div>
        <el-container>
            <el-header>
                <el-tabs v-model="activetab" type="card" @tab-click="handleClick">
                    <el-tab-pane :label="category.name" :name="category.code" v-for="category in categoryList">提醒：右侧的商品是展示在小程序首页的，最好为偶数个。</el-tab-pane>
                </el-tabs>
            </el-header>
            <el-main style="margin-top:10px">
                <div style="width:28%;border:1px solid #ebeef5;float:left">
                    <el-input placeholder="输入关键字进行过滤" v-model="filterText">
                    </el-input>

                    <el-tree
                      class="filter-tree"
                      :data="treeGoodsList" show-checkbox
                      :props="defaultProps"
                      node-key="code"
                      default-expand-all
                      :filter-node-method="filterNode"
                      ref="tree">
                    </el-tree>
                </div>
                <div style="width:60px;margin:20px 10px 0 10px;float:left">
                    <el-button type="primary" icon="el-icon-arrow-right" style="width:50px" @click="addtoCategory()"></el-button>
                </div>
                <div style="width:60%;border:1px solid #ebeef5;float:left">
                    <el-card :body-style="{ padding: '4px',position:'absolute',bottom:0,width:'95%' }" v-for="(goods,index) in category_goods" :key="index" class="goods-card" v-bind:style="{'backgroundImage':'url('+ url_pre + '/restapi/thumbnail/' + (goods.images==null?'':goods.images[0]) + ')', 'background-size': '100% 100%','position': 'relative'}">
                        <div style="">
                          <div class="clearfix">
                            <span >{{ goods.name }}</span>
                            <el-button  icon="el-icon-delete" style="float:right" @click="handleDelete(goods,index)"></el-button>
                          </div>
                        </div> 
                    </el-card>
                </div>
            </el-main>
        </el-container>
    </div>
</template>

<script>
    import axios from 'axios'

    export default {
        data() {
            return {
                activetab:'hot',
                inputVisible: false,
                inputValue: '',
                filterText: '',
                categoryList:[],
                goodsList:[],      //左侧的商品列表
                treeGoodsList:[],      //左侧的商品列表
                category_goods:[], //右侧的商品列表
                onCategory:[],     //当前被选中
                defaultProps: {
                    label: function(data, node){
                        //console.log(data)
                        return data.name + '(' + data.code + ')'
                    }
                },
                url_pre:window.location.origin
            };
        },
        created: function () {
            this.getCategoryGoods()
        },
        watch: {
            filterText(val) {
                this.$refs.tree.filter(val);
            }
        },
        methods: {
            filterNode(value, data) {
                console.log("filterNode")
                console.log(this.filterText)
                if (!value) return true;
                return data.name.indexOf(value) !== -1;
            },
            addtoCategory() {
                var checkedNodes = this.$refs.tree.getCheckedNodes();
                console.log(checkedNodes)
                //console.log(this.category_goods)
                this.category_goods = this.category_goods.concat(this.$refs.tree.getCheckedNodes())

                for(var i =0;i < this.category_goods.length;i++){
                    var goodscode = this.category_goods[i].code 
                    if(this.onCategory.goodsList.indexOf(goodscode) == -1){
                        this.onCategory.goodsList.push(goodscode)
                    }
                }

                axios.post('/category/admin',this.onCategory)
                .then(function (response) {
                  console.log(response)
        
                }).catch(function (error) {
                  self.$message({ message: error, type: 'error' })
                })

                for (var i=0;i<checkedNodes.length;i++)
                {
                    this.$refs.tree.remove(checkedNodes[i])
                }
                console.log(this.treeGoodsList)
            },
            handleDelete(goods,index) {
                console.log(goods)
                console.log(index)

                this.$confirm('确认从目录移除吗, 是否继续?', '提示', {
                  confirmButtonText: '确定',
                  cancelButtonText: '取消',
                  type: 'warning'
                }).then(() => {
                  this.category_goods.splice(index,1)

                  this.onCategory.goodsList=[]
                  for(var i=0;i<this.category_goods.length;i++){
                    this.onCategory.goodsList.push(this.category_goods[i].code)
                  }

                  axios.post('/category/admin',this.onCategory)
                    .then(function (response) {
                        console.log(response)
                        self.$refs.tree.append(goods)
        
                    }).catch(function (error) {
                        self.$message({ message: error, type: 'error' })
                    })
                  
                  console.log(this.treeGoodsList)
                }).catch(() => {
                  this.$message({
                    type: 'info',
                    message: '已取消删除'
                  });          
                });
            },
            handleClick(tab, event){
                console.log(tab, event)
                var curcode = tab.name

                for(var i =0;i < this.categoryList.length;i++){
                    if(this.categoryList[i].code == curcode){
                        this.onCategory = this.categoryList[i]
                        break;
                    }
                }

                this.category_goods = []
                this.treeGoodsList = []
                for(var i=0;i<this.goodsList.length;i++){
                    if(this.onCategory.goodsList.indexOf(this.goodsList[i].code) != -1){
                        this.category_goods.push(this.goodsList[i])
                    } else{
                        this.treeGoodsList.push(this.goodsList[i])
                    }
                  }
            },
            getCategoryGoods () {
        
              var self = this
              axios.get('/category',{})
                .then(function (response) {
                  console.log(response)
                  var data = response.data
                  self.categoryList = data.retParams.categoryList

                  self.activetab = self.categoryList[0].code
                  self.onCategory = self.categoryList[0]
                  self.getAllGoods();
        
                }).catch(function (error) {
                  self.$message({ message: error, type: 'error' })
                })

              
        
            },

            getAllGoods(){
                self = this
                axios.get('/goods',{ params: { "pageSize": 100,"curPage":1,"release":"Y" } })
                .then(function (response) {
                  console.log(response)
                  var data = response.data
                  self.goodsList = data.retParams.goodsList

                  for(var i=0;i<self.goodsList.length;i++){
                    if(self.onCategory.goodsList.indexOf(self.goodsList[i].code) != -1){
                        self.category_goods.push(self.goodsList[i])
                    } else{
                        self.treeGoodsList.push(self.goodsList[i])
                    }
                  }
                  
        
                }).catch(function (error) {
                  self.$message({ message: error, type: 'error' })
                })
            }
        }
    }
</script>

<style>

    body>.el-container {
        margin-bottom: 40px;
    }

    .goods-card {
        width: 45%;
        height: 180px;
        padding-bottom: 8%;
        float: left;
        margin: 4px;
    }
</style>