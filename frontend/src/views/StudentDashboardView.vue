<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authAPI, lessonAPI, submissionAPI, studentAPI } from '@/api/client'
import type { AuthResponse, Test, Submission, Student, Lesson } from '@/api/client'
import { useBreakpoint } from '@/composables/useBreakpoint'

const router = useRouter()
const loading = ref(false)
const currentUser = ref<AuthResponse>({})
const studentInfo = ref<Student | null>(null)
const availableTests = ref<Test[]>([])
const mySubmissions = ref<Submission[]>([])
const pastTestsDialogVisible = ref(false)

const { isMobile } = useBreakpoint()
const containerPadding = computed(() => isMobile.value ? '12px' : '24px')

const fetchCurrentUser = async () => {
  try {
    const response = await authAPI.getCurrentUser()
    currentUser.value = response.data

    if (currentUser.value.userId) {
      // Fetch student info
      const studentResponse = await studentAPI.getStudent(currentUser.value.userId)
      studentInfo.value = studentResponse.data

      // Fetch lessons for student's class (lesson-based filtering)
      const lessonsResponse = await lessonAPI.getLessonsByStudent(currentUser.value.userId)
      const lessons: Lesson[] = lessonsResponse.data

      // Extract tests from lessons that have tests attached
      availableTests.value = lessons
        .filter(lesson => lesson.testId && lesson.testTitle)
        .map(lesson => ({
          id: lesson.testId,
          title: lesson.testTitle,
          className: lesson.className,
          academyName: lesson.academyName,
          // Add lesson date info for display
          lessonDate: lesson.lessonDate
        } as Test))

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

const showPastTestsDialog = () => {
  pastTestsDialogVisible.value = true
}

// 미응시한 시험 개수
const untakenTestsCount = () => {
  const submittedTestIds = new Set(mySubmissions.value.map(s => s.testId))
  return availableTests.value.filter(t => !submittedTestIds.has(t.id)).length
}

onMounted(() => {
  fetchCurrentUser()
})
</script>

<template>
  <div :style="{ padding: containerPadding, maxWidth: '1200px', margin: '0 auto' }">
    <!-- Top Right Actions -->
    <div style="display: flex; justify-content: flex-end; gap: 12px; margin-bottom: 16px">
      <el-button @click="$router.push('/settings')">
        <el-icon style="margin-right: 8px"><Setting /></el-icon>
        설정
      </el-button>
      <el-button type="danger" @click="handleLogout">
        로그아웃
      </el-button>
    </div>

    <!-- Header -->
    <el-card shadow="never" style="margin-bottom: 24px">
      <div>
        <div style="margin-bottom: 16px">
          <h1 style="margin: 0; font-size: 28px; font-weight: 600; color: #303133; display: flex; align-items: center; gap: 12px">
            <el-icon size="32" color="#409eff">
              <UserFilled />
            </el-icon>
            <span>{{ currentUser.name }}님</span>
          </h1>
          <p v-if="studentInfo" style="margin: 8px 0 0 0; color: #909399; font-size: 14px">
            {{ studentInfo.academyName }} · {{ studentInfo.className }}
          </p>
        </div>
        <div style="display: flex; gap: 12px; flex-wrap: wrap; align-content: flex-start; justify-content: flex-start; margin: 0; width: 100%">
          <el-button type="success" @click="$router.push(`/student/stats`)" style="margin: 0">
            <el-icon style="margin-right: 8px"><TrendCharts /></el-icon>
            내 학습 통계
          </el-button>
          <el-button type="primary" @click="$router.push('/student/daily-feedback')" style="margin: 0">
            <el-icon style="margin-right: 8px"><Document /></el-icon>
            수업 피드백
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- Student Info Card -->
    <el-row :gutter="24" style="margin-bottom: 24px">
      <el-col :xs="24" :sm="12" :md="8">
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

      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="display: inline-block; padding: 16px; background: #67c23a; border-radius: 16px; margin-bottom: 16px">
              <el-icon size="48" color="white">
                <Document />
              </el-icon>
            </div>
            <h3 style="margin: 0; font-size: 18px; color: #303133">미응시한 시험</h3>
            <p style="margin: 8px 0; color: #606266; font-size: 32px; font-weight: 700">
              {{ untakenTestsCount() }}
            </p>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" style="cursor: pointer" @click="showPastTestsDialog">
          <div style="text-align: center">
            <div style="display: inline-block; padding: 16px; background: #e6a23c; border-radius: 16px; margin-bottom: 16px">
              <el-icon size="48" color="white">
                <Check />
              </el-icon>
            </div>
            <h3 style="margin: 0; font-size: 18px; color: #303133">지난 시험</h3>
            <p style="margin: 8px 0; color: #606266; font-size: 32px; font-weight: 700">
              {{ mySubmissions.length }}
            </p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Available Tests -->
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <h2 style="margin: 0; font-size: 20px; font-weight: 600">시험 리스트</h2>
        </div>
      </template>

      <!-- Desktop: Table View -->
      <el-table
        v-if="!isMobile"
        :data="availableTests"
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="title" label="시험 제목" min-width="200" />
        <el-table-column label="수업 날짜" width="150">
          <template #default="{ row }">
            {{ row.lessonDate ? new Date(row.lessonDate).toLocaleDateString('ko-KR', {
              year: 'numeric', month: 'long', day: 'numeric'
            }) : '-' }}
          </template>
        </el-table-column>
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

      <!-- Mobile: Card View -->
      <div v-else v-loading="loading">
        <el-card
          v-for="test in availableTests"
          :key="test.id"
          shadow="hover"
          style="margin-bottom: 16px"
        >
          <div style="display: flex; flex-direction: column; gap: 12px">
            <!-- Title and Status -->
            <div style="display: flex; justify-content: space-between; align-items: start; gap: 12px">
              <h3 style="margin: 0; font-size: 18px; font-weight: 600; color: #303133; flex: 1">
                {{ test.title }}
              </h3>
              <el-tag v-if="getSubmissionForTest(test.id)" type="success" size="large">
                제출 완료
              </el-tag>
              <el-tag v-else type="warning" size="large">
                미제출
              </el-tag>
            </div>

            <!-- Info Grid -->
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px; font-size: 14px; color: #606266">
              <div style="display: flex; align-items: center; gap: 4px">
                <el-icon><Calendar /></el-icon>
                <span>{{ test.lessonDate ? new Date(test.lessonDate).toLocaleDateString('ko-KR', {
                  month: 'long', day: 'numeric'
                }) : '-' }}</span>
              </div>
              <div style="display: flex; align-items: center; gap: 4px">
                <el-icon><School /></el-icon>
                <span>{{ test.className }}</span>
              </div>
              <div v-if="test.questionCount" style="display: flex; align-items: center; gap: 4px">
                <el-icon><Document /></el-icon>
                <span>{{ test.questionCount }}문제</span>
              </div>
              <div v-if="getSubmissionForTest(test.id)" style="display: flex; align-items: center; gap: 4px">
                <el-icon><Trophy /></el-icon>
                <span style="font-weight: 600; color: #409eff">
                  {{ getSubmissionForTest(test.id)?.totalScore }}점
                </span>
              </div>
            </div>

            <!-- Action Button -->
            <el-button
              v-if="!getSubmissionForTest(test.id)"
              type="primary"
              size="large"
              @click="handleTakeTest(test.id)"
              style="width: 100%; margin-top: 4px"
            >
              <el-icon style="margin-right: 8px"><Edit /></el-icon>
              시험 보기
            </el-button>
            <el-button
              v-else
              type="info"
              size="large"
              disabled
              style="width: 100%; margin-top: 4px"
            >
              <el-icon style="margin-right: 8px"><Check /></el-icon>
              완료
            </el-button>
          </div>
        </el-card>
      </div>

      <el-empty
        v-if="availableTests.length === 0"
        description="시험이 없습니다"
        style="padding: 60px 0"
      />
    </el-card>

    <!-- Past Tests Dialog -->
    <el-dialog v-model="pastTestsDialogVisible" title="지난 시험 기록" :width="isMobile ? '95%' : '800px'" :fullscreen="isMobile">
      <el-table :data="mySubmissions" style="width: 100%" stripe>
        <el-table-column label="시험 제목" min-width="200">
          <template #default="{ row }">
            {{ row.testTitle || row.test?.title || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="제출 날짜" width="180">
          <template #default="{ row }">
            {{ row.submittedAt ? new Date(row.submittedAt).toLocaleDateString('ko-KR', {
              year: 'numeric', month: 'long', day: 'numeric'
            }) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="내 점수" width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.totalScore >= 90 ? 'success' : row.totalScore >= 70 ? 'warning' : 'danger'"
              size="large"
            >
              {{ row.totalScore }}점
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="반 평균" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.classAverage !== null && row.classAverage !== undefined">
              {{ Math.round(row.classAverage) }}점
            </span>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column label="반 등수" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.rank !== null && row.rank !== undefined">
              {{ row.rank }}등
            </span>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-if="mySubmissions.length === 0"
        description="제출한 시험이 없습니다"
        :image-size="100"
      />

      <template #footer>
        <el-button @click="pastTestsDialogVisible = false">닫기</el-button>
      </template>
    </el-dialog>
  </div>
</template>
