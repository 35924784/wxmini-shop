<template>
    <el-container style="border: 1px solid #eee">
        <el-header style="text-align: right; font-size: 12px; padding:0">
            <v-head></v-head>
        </el-header>
        
        <el-container>
            <el-aside style="width:15%; background-color: rgb(238, 241, 246)">
                <v-sidebar></v-sidebar>
            </el-aside>
            <el-main style="width:83%">
                <transition name="move" mode="out-in"><router-view></router-view></transition>
            </el-main>
        </el-container>
    </el-container>
</template>

<script>
import vHead from './Header.vue'
import vSidebar from './Sidebar.vue'
export default {
  components: {
    vHead, vSidebar
  }
}
</script>
