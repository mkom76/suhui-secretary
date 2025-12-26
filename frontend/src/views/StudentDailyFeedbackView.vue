<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { dailyFeedbackAPI, type DailyFeedback, authAPI, lessonAPI, type Lesson, studentAPI } from '../api/client'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const feedback = ref<DailyFeedback | null>(null)
const currentUser = ref<any>(null)
const lessons = ref<Lesson[]>([])
const selectedLessonId = ref<number | null>(null)
const studentId = ref<number | null>(null)
const studentName = ref<string>('')
const isTeacherView = computed(() => route.params.id !== undefined)

const fetchLessons = async () => {
  try {
    const userRes = await authAPI.getCurrentUser()
    currentUser.value = userRes.data

    if (!currentUser.value.userId) {
      router.push('/login')
      return
    }

    // 선생님이 학생 피드백을 보는 경우와 학생이 본인 피드백을 보는 경우 구분
    if (isTeacherView.value) {
      studentId.value = Number(route.params.id)
    } else {
      studentId.value = currentUser.value.userId
    }

    // 학생 정보에서 classId 가져오기
    const studentRes = await studentAPI.getStudent(studentId.value)
    studentName.value = studentRes.data.name
    const classId = studentRes.data.classId

    if (classId) {
      const lessonsRes = await lessonAPI.getLessonsByClass(classId)
      lessons.value = lessonsRes.data

      // 오늘 날짜와 가장 가까운 레슨 자동 선택
      const today = new Date().toISOString().split('T')[0]
      const todayLesson = lessons.value.find(l => l.lessonDate === today)

      if (todayLesson?.id) {
        selectedLessonId.value = todayLesson.id
      } else if (lessons.value.length > 0 && lessons.value[0].id) {
        // 오늘 레슨이 없으면 가장 최근 레슨 선택
        selectedLessonId.value = lessons.value[0].id
      }

      // 선택된 레슨의 피드백 가져오기
      if (selectedLessonId.value) {
        await fetchFeedback()
      }
    }
  } catch (error) {
    ElMessage.error('수업 목록을 불러오는데 실패했습니다')
  }
}

const fetchFeedback = async () => {
  if (!selectedLessonId.value || !studentId.value) return

  loading.value = true
  try {
    const feedbackRes = await dailyFeedbackAPI.getDailyFeedback(
      studentId.value,
      selectedLessonId.value
    )
    feedback.value = feedbackRes.data
  } catch (error: any) {
    if (error.response?.status === 404) {
      ElMessage.warning('해당 수업의 피드백이 없습니다')
      feedback.value = null
    } else {
      ElMessage.error('피드백을 불러오는데 실패했습니다')
    }
  } finally {
    loading.value = false
  }
}

const onLessonChange = () => {
  fetchFeedback()
}

const todayHomeworkStatus = computed(() => {
  if (!feedback.value?.todayHomework) return null
  const hw = feedback.value.todayHomework
  if (hw.completion === null || hw.completion === undefined) {
    return { status: 'not-started', text: '미완료', color: '#909399' }
  } else if (hw.completion >= 80) {
    return { status: 'excellent', text: '완료', color: '#67c23a' }
  } else if (hw.completion >= 50) {
    return { status: 'good', text: '진행중', color: '#e6a23c' }
  } else {
    return { status: 'needs-work', text: '미흡', color: '#f56c6c' }
  }
})

const testPerformance = computed(() => {
  if (!feedback.value?.todayTest) return null
  const score = feedback.value.todayTest.studentScore
  if (score >= 90) return { level: 'success', text: '우수', color: '#67c23a' }
  if (score >= 70) return { level: '', text: '양호', color: '#409eff' }
  if (score >= 50) return { level: 'warning', text: '보통', color: '#e6a23c' }
  return { level: 'danger', text: '노력필요', color: '#f56c6c' }
})

const incorrectQuestionsWithRate = computed(() => {
  if (!feedback.value?.todayTest) return []

  const incorrectSet = new Set(feedback.value.todayTest.incorrectQuestions)
  const accuracyMap = new Map(
    feedback.value.todayTest.questionAccuracyRates.map(q => [q.questionNumber, q.correctRate])
  )

  return feedback.value.todayTest.incorrectQuestions
    .map(qNum => ({
      questionNumber: qNum,
      academyCorrectRate: accuracyMap.get(qNum) || 0,
      difficulty: getDifficulty(accuracyMap.get(qNum) || 0)
    }))
    .sort((a, b) => a.questionNumber - b.questionNumber)
})

const getDifficulty = (correctRate: number) => {
  if (correctRate >= 80) return { level: 'easy', text: '쉬움', color: '#67c23a' }
  if (correctRate >= 60) return { level: 'medium', text: '보통', color: '#409eff' }
  if (correctRate >= 40) return { level: 'hard', text: '어려움', color: '#e6a23c' }
  return { level: 'very-hard', text: '매우 어려움', color: '#f56c6c' }
}

onMounted(() => {
  fetchLessons()
})
</script>

<template>
  <div v-loading="loading" style="max-width: 1200px; margin: 0 auto; padding: 24px">
    <!-- 선생님용 헤더 (뒤로가기 버튼 포함) -->
    <el-card v-if="isTeacherView" shadow="never" style="margin-bottom: 24px">
      <div style="display: flex; justify-content: space-between; align-items: center">
        <div>
          <h1 style="margin: 0 0 8px; font-size: 28px; font-weight: 600; color: #303133">
            {{ studentName }}님의 학습 피드백
          </h1>
        </div>
        <el-button @click="router.push(`/students/${studentId}`)" :icon="ArrowLeft" size="large">
          학생 상세로
        </el-button>
      </div>
    </el-card>

    <!-- 공통 헤더 -->
    <el-card shadow="never" style="margin-bottom: 24px">
      <div style="text-align: center">
        <h1 v-if="!isTeacherView" style="margin: 0 0 8px; font-size: 32px; font-weight: 600; color: #303133">
          학습 피드백
        </h1>
        <p v-if="feedback" style="margin: 0 0 16px; color: #909399; font-size: 16px">
          {{ new Date(feedback.lessonDate).toLocaleDateString('ko-KR', {
            year: 'numeric', month: 'long', day: 'numeric', weekday: 'long'
          }) }}
        </p>

        <!-- 수업 선택 -->
        <div style="max-width: 400px; margin: 0 auto">
          <el-select
            v-model="selectedLessonId"
            placeholder="수업을 선택하세요"
            style="width: 100%"
            @change="onLessonChange"
            size="large"
          >
            <el-option
              v-for="lesson in lessons"
              :key="lesson.id"
              :label="`${lesson.lessonDate} - ${lesson.testTitle || lesson.homeworkTitle || '수업'}`"
              :value="lesson.id"
            />
          </el-select>
        </div>
      </div>
    </el-card>

    <div v-if="feedback">
      <el-row :gutter="24" style="margin-bottom: 24px">
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div style="display: flex; align-items: center; gap: 8px">
                <el-icon size="20" color="#409eff"><Document /></el-icon>
                <span style="font-weight: 600; font-size: 16px">오늘의 숙제</span>
              </div>
            </template>

            <div v-if="feedback.todayHomework">
              <div style="font-size: 20px; font-weight: 600; margin-bottom: 16px">
                {{ feedback.todayHomework.homeworkTitle }}
              </div>

              <el-descriptions :column="1" border>
                <el-descriptions-item label="문제 수">
                  {{ feedback.todayHomework.questionCount }}문제
                </el-descriptions-item>
                <el-descriptions-item label="완성도">
                  <div style="display: flex; align-items: center; gap: 12px">
                    <el-progress
                      :percentage="feedback.todayHomework.completion || 0"
                      :color="todayHomeworkStatus?.color"
                      style="flex: 1"
                    />
                    <el-tag :color="todayHomeworkStatus?.color" style="color: white">
                      {{ todayHomeworkStatus?.text }}
                    </el-tag>
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="마감일" v-if="feedback.todayHomework.dueDate">
                  {{ new Date(feedback.todayHomework.dueDate).toLocaleDateString('ko-KR') }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
            <el-empty v-else description="오늘 숙제가 없습니다" :image-size="80" />
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div style="display: flex; align-items: center; gap: 8px">
                <el-icon size="20" color="#67c23a"><Calendar /></el-icon>
                <span style="font-weight: 600; font-size: 16px">다음 수업 숙제</span>
              </div>
            </template>

            <div v-if="feedback.nextHomework">
              <div style="font-size: 20px; font-weight: 600; margin-bottom: 16px">
                {{ feedback.nextHomework.homeworkTitle }}
              </div>

              <el-descriptions :column="1" border>
                <el-descriptions-item label="문제 수">
                  {{ feedback.nextHomework.questionCount }}문제
                </el-descriptions-item>
                <el-descriptions-item label="완성도">
                  <div v-if="feedback.nextHomework.completion !== null && feedback.nextHomework.completion !== undefined">
                    <el-progress :percentage="feedback.nextHomework.completion" />
                  </div>
                  <span v-else style="color: #909399">아직 시작하지 않음</span>
                </el-descriptions-item>
                <el-descriptions-item label="마감일" v-if="feedback.nextHomework.dueDate">
                  {{ new Date(feedback.nextHomework.dueDate).toLocaleDateString('ko-KR') }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
            <el-empty v-else description="다음 숙제가 없습니다" :image-size="80" />
          </el-card>
        </el-col>
      </el-row>

      <el-card v-if="feedback.todayTest" shadow="hover" style="margin-bottom: 24px">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center">
            <div style="display: flex; align-items: center; gap: 8px">
              <el-icon size="20" color="#e6a23c"><EditPen /></el-icon>
              <span style="font-weight: 600; font-size: 16px">오늘의 시험 결과</span>
            </div>
            <el-tag :type="testPerformance?.level" size="large">
              {{ feedback.todayTest.studentScore }}점 - {{ testPerformance?.text }}
            </el-tag>
          </div>
        </template>

        <div style="margin-bottom: 20px">
          <h3 style="margin: 0 0 12px; font-size: 16px; color: #303133">
            {{ feedback.todayTest.testTitle }}
          </h3>
        </div>

        <div v-if="incorrectQuestionsWithRate.length > 0">
          <div style="font-weight: 600; margin-bottom: 12px; color: #f56c6c; font-size: 16px">
            틀린 문제 분석 ({{ incorrectQuestionsWithRate.length }}개)
          </div>
          <el-table :data="incorrectQuestionsWithRate" stripe>
            <el-table-column prop="questionNumber" label="문제 번호" width="120" align="center">
              <template #default="{ row }">
                <el-tag type="danger" size="large">{{ row.questionNumber }}번</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="학원 평균 정답률" align="center">
              <template #default="{ row }">
                <div style="display: flex; align-items: center; gap: 12px">
                  <el-progress
                    :percentage="Math.round(row.academyCorrectRate)"
                    :color="row.difficulty.color"
                    style="flex: 1"
                  />
                  <span style="font-weight: 600; color: #606266">
                    {{ Math.round(row.academyCorrectRate) }}%
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="난이도" width="130" align="center">
              <template #default="{ row }">
                <el-tag :color="row.difficulty.color" style="color: white; border: none">
                  {{ row.difficulty.text }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div style="margin-top: 12px; padding: 12px; background: #f0f9ff; border-radius: 8px; font-size: 13px; color: #606266">
            <div style="font-weight: 600; margin-bottom: 6px; color: #409eff">💡 분석 팁</div>
            <div>• <span style="color: #67c23a; font-weight: 600">정답률이 높은 문제</span>: 실수를 줄이기 위해 꼼꼼히 확인하세요</div>
            <div>• <span style="color: #f56c6c; font-weight: 600">정답률이 낮은 문제</span>: 어려운 문제입니다. 개념을 다시 공부해보세요</div>
          </div>
        </div>

        <el-empty v-else description="모든 문제를 맞췄습니다! 훌륭해요!"
                  :image-size="80">
          <template #image>
            <el-icon size="80" color="#67c23a"><CircleCheck /></el-icon>
          </template>
        </el-empty>
      </el-card>

      <!-- 시험 없는 경우 표시 -->
      <el-card v-else shadow="hover" style="margin-bottom: 24px">
        <template #header>
          <div style="display: flex; align-items: center; gap: 8px">
            <el-icon size="20" color="#909399"><EditPen /></el-icon>
            <span style="font-weight: 600; font-size: 16px">시험 결과</span>
          </div>
        </template>
        <el-empty description="응시한 시험이 없습니다" :image-size="80">
          <template #image>
            <el-icon size="80" color="#c0c4cc"><Document /></el-icon>
          </template>
        </el-empty>
      </el-card>

      <el-card shadow="hover">
        <template #header>
          <div style="display: flex; align-items: center; gap: 8px">
            <el-icon size="20" color="#f56c6c"><ChatDotRound /></el-icon>
            <span style="font-weight: 600; font-size: 16px">선생님 피드백</span>
          </div>
        </template>

        <div v-if="feedback.instructorFeedback">
          <div style="margin-bottom: 12px">
            <el-tag type="success">{{ feedback.feedbackAuthor || '선생님' }}</el-tag>
          </div>
          <div style="
            background: #f5f7fa;
            padding: 20px;
            border-radius: 8px;
            line-height: 1.8;
            font-size: 15px;
            color: #303133;
            white-space: pre-wrap;
          ">
            {{ feedback.instructorFeedback }}
          </div>
        </div>
        <el-empty v-else description="아직 선생님 피드백이 없습니다" :image-size="80">
          <template #image>
            <el-icon size="80" color="#c0c4cc"><ChatDotRound /></el-icon>
          </template>
        </el-empty>
      </el-card>
    </div>

    <el-empty v-else-if="!loading" description="오늘 수업이 없습니다" :image-size="120" />
  </div>
</template>

<style scoped>
.el-card {
  border-radius: 12px;
}

.el-descriptions :deep(.el-descriptions__label) {
  font-weight: 600;
}
</style>
