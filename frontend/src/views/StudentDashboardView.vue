<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authAPI, testAPI, submissionAPI, studentAPI } from '@/api/client'
import type { AuthResponse, Test, Submission, Student } from '@/api/client'

const router = useRouter()
const loading = ref(false)
const currentUser = ref<AuthResponse>({})
const studentInfo = ref<Student | null>(null)
const availableTests = ref<Test[]>([])
const mySubmissions = ref<Submission[]>([])

const fetchCurrentUser = async () => {
  try {
    const response = await authAPI.getCurrentUser()
    currentUser.value = response.data

    if (currentUser.value.userId) {
      // Fetch student info
      const studentResponse = await studentAPI.getStudent(currentUser.value.userId)
      studentInfo.value = studentResponse.data

      // Fetch available tests for student's class
      if (studentInfo.value.classId) {
        const testsResponse = await testAPI.getTests({ classId: studentInfo.value.classId })
        availableTests.value = testsResponse.data.content || []
      }

      // Fetch student's submissions
      const submissionsResponse = await submissionAPI.getStudentSubmissions(currentUser.value.userId)
      mySubmissions.value = submissionsResponse.data
    }
  } catch (error) {
    console.error('Failed to fetch user data:', error)
    ElMessage.error('사용자 정보를 불러오는데 실패했습니다')
  }
}

const handleLogout = async () => {
  try {
    await authAPI.logout()
    ElMessage.success('로그아웃 되었습니다')
    router.push('/login')
  } catch (error) {
    console.error('Logout failed:', error)
    ElMessage.error('로그아웃에 실패했습니다')
  }
}

const getSubmissionForTest = (testId: number) => {
  return mySubmissions.value.find(s => s.testId === testId)
}

const handleTakeTest = (testId: number) => {
  router.push(`/student/tests/${testId}`)
}

onMounted(() => {
  fetchCurrentUser()
})
</script>

<template>
  <div style="padding: 24px; max-width: 1200px; margin: 0 auto">
    <!-- Header -->
    <el-card shadow="never" style="margin-bottom: 24px">
      <div style="display: flex; justify-content: space-between; align-items: center">
        <div>
          <h1 style="margin: 0; font-size: 32px; font-weight: 700; color: #303133">
            학생 대시보드
          </h1>
          <p style="margin: 8px 0 0; color: #909399">
            안녕하세요, <strong>{{ currentUser.name }}</strong>님
          </p>
          <p v-if="studentInfo" style="margin: 4px 0 0; color: #909399; font-size: 14px">
            {{ studentInfo.academyName }} - {{ studentInfo.className }}
          </p>
        </div>
        <div style="display: flex; gap: 12px">
          <el-button type="primary" size="large" @click="$router.push('/student/daily-feedback')">
            <el-icon style="margin-right: 8px"><Document /></el-icon>
            오늘의 학습 피드백
          </el-button>
          <el-button @click="$router.push('/settings')">
            <el-icon style="margin-right: 8px"><Setting /></el-icon>
            설정
          </el-button>
          <el-button type="danger" @click="handleLogout">
            로그아웃
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- Student Info Card -->
    <el-row :gutter="24" style="margin-bottom: 24px">
      <el-col :span="8">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="display: inline-block; padding: 16px; background: #409eff; border-radius: 16px; margin-bottom: 16px">
              <el-icon size="48" color="white">
                <User />
              </el-icon>
            </div>
            <h3 style="margin: 0; font-size: 18px; color: #303133">내 정보</h3>
            <p style="margin: 8px 0; color: #606266">{{ studentInfo?.school || '-' }}</p>
            <p style="margin: 0; color: #909399; font-size: 14px">{{ studentInfo?.grade || '-' }}</p>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="display: inline-block; padding: 16px; background: #67c23a; border-radius: 16px; margin-bottom: 16px">
              <el-icon size="48" color="white">
                <Document />
              </el-icon>
            </div>
            <h3 style="margin: 0; font-size: 18px; color: #303133">사용 가능한 시험</h3>
            <p style="margin: 8px 0; color: #606266; font-size: 32px; font-weight: 700">
              {{ availableTests.length }}
            </p>
            <p style="margin: 0; color: #909399; font-size: 14px">개의 시험</p>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="display: inline-block; padding: 16px; background: #e6a23c; border-radius: 16px; margin-bottom: 16px">
              <el-icon size="48" color="white">
                <Check />
              </el-icon>
            </div>
            <h3 style="margin: 0; font-size: 18px; color: #303133">제출한 시험</h3>
            <p style="margin: 8px 0; color: #606266; font-size: 32px; font-weight: 700">
              {{ mySubmissions.length }}
            </p>
            <p style="margin: 0; color: #909399; font-size: 14px">개의 시험</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Available Tests -->
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <h2 style="margin: 0; font-size: 20px; font-weight: 600">사용 가능한 시험</h2>
        </div>
      </template>

      <el-table
        :data="availableTests"
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="title" label="시험 제목" min-width="200" />
        <el-table-column prop="questionCount" label="문제 수" width="120">
          <template #default="{ row }">
            {{ row.questionCount || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="className" label="반" width="150" />
        <el-table-column label="상태" width="120">
          <template #default="{ row }">
            <el-tag v-if="getSubmissionForTest(row.id)" type="success">제출 완료</el-tag>
            <el-tag v-else type="warning">미제출</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="점수" width="100">
          <template #default="{ row }">
            <span v-if="getSubmissionForTest(row.id)">
              {{ getSubmissionForTest(row.id)?.totalScore }}점
            </span>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column label="작업" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="!getSubmissionForTest(row.id)"
              type="primary"
              size="small"
              @click="handleTakeTest(row.id)"
            >
              시험 보기
            </el-button>
            <el-button
              v-else
              type="info"
              size="small"
              disabled
            >
              완료
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-if="availableTests.length === 0"
        description="사용 가능한 시험이 없습니다"
        style="padding: 60px 0"
      />
    </el-card>
  </div>
</template>
