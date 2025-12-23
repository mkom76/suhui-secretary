<script setup lang="ts">
import { RouterView } from 'vue-router'
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authAPI } from '@/api/client'
import type { AuthResponse } from '@/api/client'

const router = useRouter()
const route = useRoute()

const activeIndex = ref('/')
const currentUser = ref<AuthResponse>({})

const isLoginPage = computed(() => route.path === '/login')
const isTeacherRoute = computed(() => currentUser.value.role === 'TEACHER' && !isLoginPage.value)
const isStudentRoute = computed(() => currentUser.value.role === 'STUDENT')

const fetchCurrentUser = async () => {
  try {
    const response = await authAPI.getCurrentUser()
    currentUser.value = response.data
  } catch (error) {
    // User not logged in
  }
}

const handleSelect = (index: string) => {
  router.push(index)
}

const handleLogout = async () => {
  try {
    await authAPI.logout()
    currentUser.value = {}
    ElMessage.success('로그아웃 되었습니다')
    router.push('/login')
  } catch (error) {
    console.error('Logout failed:', error)
    ElMessage.error('로그아웃에 실패했습니다')
  }
}

onMounted(() => {
  fetchCurrentUser()
})

// Refetch user when route changes (e.g., after login)
watch(() => route.path, () => {
  fetchCurrentUser()
})</script>

<template>
  <el-container style="min-height: 100vh">
    <!-- Header with Navigation (Teacher only) -->
    <el-header v-if="isTeacherRoute" style="background-color: #fff; border-bottom: 1px solid #dcdfe6; padding: 0">
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
            <el-menu-item index="/students">
              <el-icon><User /></el-icon>
              학생 관리
            </el-menu-item>
            <el-menu-item index="/tests">
              <el-icon><Document /></el-icon>
              시험 관리
            </el-menu-item>
            <el-menu-item index="/homeworks">
              <el-icon><Notebook /></el-icon>
              숙제 관리
            </el-menu-item>
            <el-menu-item index="/academies">
              <el-icon><OfficeBuilding /></el-icon>
              학원 관리
            </el-menu-item>
            <el-menu-item index="/academy-classes">
              <el-icon><Grid /></el-icon>
              반 관리
            </el-menu-item>
          </el-menu>
        </el-col>

        <el-col :span="6" style="text-align: right">
          <div style="display: flex; align-items: center; justify-content: flex-end; gap: 12px">
            <span style="color: #606266; font-size: 14px">{{ currentUser.name }}님</span>
            <el-button type="danger" size="small" @click="handleLogout">
              로그아웃
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-header>

    <!-- Main Content -->
    <el-main :style="{ padding: isLoginPage ? '0' : '24px', backgroundColor: isLoginPage ? '#fff' : '#f5f7fa' }">
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
