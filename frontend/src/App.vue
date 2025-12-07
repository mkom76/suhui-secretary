<script setup lang="ts">
import { RouterView } from 'vue-router'
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const activeIndex = ref('/')

const handleSelect = (index: string) => {
  router.push(index)
}
</script>

<template>
  <el-container style="min-height: 100vh">
    <!-- Header with Navigation -->
    <el-header style="background-color: #fff; border-bottom: 1px solid #dcdfe6; padding: 0">
      <el-row justify="space-between" align="middle" style="height: 100%; padding: 0 20px">
        <el-col :span="6">
          <div style="display: flex; align-items: center; gap: 12px">
            <el-icon size="32" color="#409eff">
              <School />
            </el-icon>
            <span style="font-size: 20px; font-weight: 600; color: #303133">
              학원 관리 시스템
            </span>
          </div>
        </el-col>
        
        <el-col :span="12">
          <el-menu
            :default-active="route.path"
            mode="horizontal"
            style="border-bottom: none; justify-content: center"
            @select="handleSelect"
          >
            <el-menu-item index="/">
              <el-icon><House /></el-icon>
              홈
            </el-menu-item>
            <el-menu-item index="/students">
              <el-icon><User /></el-icon>
              학생 관리
            </el-menu-item>
            <el-menu-item index="/tests">
              <el-icon><Document /></el-icon>
              시험 관리
            </el-menu-item>
          </el-menu>
        </el-col>
        
        <el-col :span="6"></el-col>
      </el-row>
    </el-header>

    <!-- Main Content -->
    <el-main style="padding: 24px; background-color: #f5f7fa">
      <RouterView />
    </el-main>
  </el-container>
</template>

<style scoped>
.el-menu--horizontal {
  border-bottom: none !important;
}

.el-menu-item.is-active {
  background-color: #ecf5ff !important;
  color: #409eff !important;
}

.el-header {
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}
</style>
