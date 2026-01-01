<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {
  type Student,
  studentAPI,
  type StudentHomework,
  studentHomeworkAPI,
  type Submission,
  submissionAPI,
  testAPI,
  dailyFeedbackAPI,
  authAPI,
  type Lesson,
  lessonAPI
} from '../api/client'
import { useBreakpoint } from '@/composables/useBreakpoint'
import { useTouchGestures } from '@/composables/useTouchGestures'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const currentUser = ref<any>({})
const studentId = ref<string>('')
const student = ref<Student | null>(null)
const submissions = ref<Submission[]>([])
const testStats = ref<Record<number, any>>({})
const studentHomeworks = ref<StudentHomework[]>([])
const activeTab = ref('test')
const lessons = ref<Lesson[]>([])

// 선생님인지 확인
const isTeacher = computed(() => currentUser.value.role === 'TEACHER')

// 모달 상태
const chartModalVisible = ref(false)
const feedbackDialogVisible = ref(false)
const incompleteHomeworksDialogVisible = ref(false)
const feedbackForm = ref({
  lessonId: null as number | null,
  content: '',
  authorName: ''
})

// 반응형 breakpoint
const { isMobile, isSmallScreen } = useBreakpoint()

// 터치 제스처 (차트용)
const {
  zoom: touchZoom,
  panX: touchPanX,
  panY: touchPanY,
  isDragging: isTouchDragging,
  handleTouchStart,
  handleTouchMove,
  handleTouchEnd,
  resetZoomAndPan: resetTouch
} = useTouchGestures()

// 확대/축소 및 이동 상태 (마우스용)
const zoom = ref(1)
const panX = ref(0)
const panY = ref(0)
const isDragging = ref(false)
const dragStartX = ref(0)
const dragStartY = ref(0)

// 통합 zoom/pan (마우스와 터치 제스처 통합)
const combinedZoom = computed(() => isDragging.value ? zoom.value : touchZoom.value)
const combinedPanX = computed(() => isDragging.value ? panX.value : touchPanX.value)
const combinedPanY = computed(() => isDragging.value ? panY.value : touchPanY.value)

// 반응형 스타일
const containerPadding = computed(() => isMobile.value ? '12px' : '24px')
const progressSize = computed(() => isMobile.value ? 80 : 120)
const progressStrokeWidth = computed(() => isMobile.value ? 8 : 10)
const chartViewBox = computed(() => isMobile.value ? '0 0 350 200' : '0 0 700 250')
const chartFontSize = computed(() => isMobile.value ? 10 : 12)
const dialogWidth = computed(() => isMobile.value ? '95%' : '90%')

// 차트 포인트 계산 (모든 좌표 사전 계산, 반응형)
const chartPoints = computed(() => {
  if (submissions.value.length === 0) return []

  // 반응형 차트 크기
  const chartWidth = isMobile.value ? 350 : 700
  const chartHeight = isMobile.value ? 200 : 250
  const marginLeft = isMobile.value ? 30 : 60
  const marginRight = isMobile.value ? 20 : 40
  const marginTop = isMobile.value ? 15 : 30
  const marginBottom = isMobile.value ? 15 : 30

  const points = submissions.value.map((s, index) => {
    const testId = s.testId
    const stats = testStats.value[testId]
    const studentScore = s.totalScore
    const academyAverage = stats?.averageScore || 0

    // X 좌표 계산
    const totalPoints = submissions.value.length
    const divisor = totalPoints > 1 ? totalPoints - 1 : 1
    const plotWidth = chartWidth - marginLeft - marginRight
    const x = marginLeft + (index * plotWidth / divisor)

    // Y 좌표 계산
    const plotHeight = chartHeight - marginTop - marginBottom
    const plotTop = marginTop
    const plotBottom = chartHeight - marginBottom
    const studentY = plotBottom - (studentScore / 100) * plotHeight
    const academyY = plotBottom - (academyAverage / 100) * plotHeight
    const labelY = studentY - 12

    return {
      date: s.submittedAt ? new Date(s.submittedAt).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' }) : '',
      studentScore: studentScore,
      academyAverage: academyAverage,
      x: x,
      studentY: studentY,
      academyY: academyY,
      labelY: labelY
    }
  })

  return points
})

// 꺾은선 그래프 경로
const studentLinePoints = computed(() => {
  if (chartPoints.value.length < 2) return ''
  return chartPoints.value.map(p => `${p.x},${p.studentY}`).join(' ')
})

const academyLinePoints = computed(() => {
  if (chartPoints.value.length < 2) return ''
  return chartPoints.value.map(p => `${p.x},${p.academyY}`).join(' ')
})

// 평균 점수 계산
const averageScore = computed(() => {
  if (submissions.value.length === 0) return 0
  const total = submissions.value.reduce((sum, s) => sum + s.totalScore, 0)
  return Math.round(total / submissions.value.length * 10) / 10
})

// 최고 점수
const maxScore = computed(() => {
  if (submissions.value.length === 0) return 0
  return Math.max(...submissions.value.map(s => s.totalScore))
})

// 최저 점수
const minScore = computed(() => {
  if (submissions.value.length === 0) return 0
  return Math.min(...submissions.value.map(s => s.totalScore))
})

// 제출 기한이 지난 숙제만 필터링 (통계 계산용)
const duePastHomeworks = computed(() => {
  const today = new Date()
  today.setHours(0, 0, 0, 0) // 오늘 날짜의 00:00:00으로 설정

  return studentHomeworks.value.filter(sh => {
    if (!sh.dueDate) return true // dueDate가 없으면 포함
    const dueDate = new Date(sh.dueDate)
    return dueDate < today // 제출 기한이 오늘 이전인 것만 포함
  })
})

// 숙제 통계
const incompleteHomeworks = computed(() => {
  return duePastHomeworks.value.filter(sh => (sh.completion || 0) < 100)
})

const incompleteHomeworkCount = computed(() => {
  return incompleteHomeworks.value.length
})

const averageCompletion = computed(() => {
  if (duePastHomeworks.value.length === 0) return 0
  const total = duePastHomeworks.value.reduce((sum, sh) => sum + (sh.completion || 0), 0)
  return Math.round(total / duePastHomeworks.value.length * 10) / 10
})

const minCompletion = computed(() => {
  if (duePastHomeworks.value.length === 0) return 0
  return Math.min(...duePastHomeworks.value.map(sh => sh.completion || 0))
})

const totalIncorrectCount = computed(() => {
  return duePastHomeworks.value.reduce((sum, sh) => sum + (sh.incorrectCount || 0), 0)
})

// 모달 열기
const openChartModal = () => {
  chartModalVisible.value = true
  resetZoomAndPan()
}

// 줌/팬 초기화 (마우스와 터치 모두 초기화)
const resetZoomAndPan = () => {
  zoom.value = 1
  panX.value = 0
  panY.value = 0
  resetTouch()
}

// 마우스 휠로 줌
const handleWheel = (event: WheelEvent) => {
  event.preventDefault()
  const delta = event.deltaY > 0 ? 0.9 : 1.1
  const newZoom = Math.max(0.5, Math.min(5, zoom.value * delta))
  zoom.value = newZoom
}

// 드래그 시작
const handleMouseDown = (event: MouseEvent) => {
  isDragging.value = true
  dragStartX.value = event.clientX - panX.value
  dragStartY.value = event.clientY - panY.value
}

// 드래그 중
const handleMouseMove = (event: MouseEvent) => {
  if (!isDragging.value) return
  panX.value = event.clientX - dragStartX.value
  panY.value = event.clientY - dragStartY.value
}

// 드래그 종료
const handleMouseUp = () => {
  isDragging.value = false
}

const fetchStudentDetail = async () => {
  loading.value = true
  try {
    // 현재 사용자 정보 가져오기
    const userRes = await authAPI.getCurrentUser()
    currentUser.value = userRes.data

    // 학생 ID 설정: STUDENT면 본인 ID, TEACHER면 route params ID
    if (currentUser.value.role === 'STUDENT') {
      studentId.value = String(currentUser.value.userId)
    } else {
      studentId.value = route.params.id as string
    }

    const [studentRes, submissionsRes, homeworksRes] = await Promise.all([
      studentAPI.getStudent(Number(studentId.value)),
      submissionAPI.getStudentSubmissions(Number(studentId.value)),
      studentHomeworkAPI.getByStudentId(Number(studentId.value))
    ])

    student.value = studentRes.data
    submissions.value = (submissionsRes.data.content || submissionsRes.data)
      .sort((a: Submission, b: Submission) => {
        const dateA = a.submittedAt ? new Date(a.submittedAt).getTime() : 0
        const dateB = b.submittedAt ? new Date(b.submittedAt).getTime() : 0
        return dateA - dateB
      })
    studentHomeworks.value = homeworksRes.data.content || homeworksRes.data

    // 각 시험의 통계 가져오기
    const testIds = [...new Set(submissions.value.map(s => s.testId).filter(id => id))]
    const statsPromises = testIds.map(async (testId) => {
      try {
        const response = await testAPI.getTestStats(testId)
        testStats.value[testId] = response.data
      } catch (error) {
        console.error(`Failed to fetch stats for test ${testId}`)
      }
    })
    await Promise.all(statsPromises)
  } catch (error) {
    ElMessage.error('학생 정보를 불러오는데 실패했습니다.')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  if (isTeacher.value) {
    router.push('/students')
  } else {
    router.push('/student/dashboard')
  }
}

const navigateToTest = (testId: number | undefined) => {
  if (!testId) {
    ElMessage.warning('시험 정보를 찾을 수 없습니다')
    return
  }
  router.push(`/tests/${testId}`)
}

const openTodayFeedbackDialog = async () => {
  try {
    const currentUser = await authAPI.getCurrentUser()
    feedbackForm.value.authorName = currentUser.data.name || ''

    // 학생의 반에 속한 레슨 목록 가져오기
    if (student.value?.classId) {
      const lessonsRes = await lessonAPI.getLessonsByClass(student.value.classId)
      lessons.value = lessonsRes.data

      // 오늘 날짜와 가장 가까운 레슨을 기본 선택
      const today = new Date().toISOString().split('T')[0]
      const todayLesson = lessons.value.find(l => l.lessonDate === today)
      if (todayLesson) {
        feedbackForm.value.lessonId = todayLesson.id || null
      } else if (lessons.value.length > 0) {
        // 오늘 레슨이 없으면 가장 최근 레슨 선택
        feedbackForm.value.lessonId = lessons.value[0].id || null
      }
    }

    feedbackDialogVisible.value = true
  } catch (error) {
    ElMessage.error('사용자 정보를 불러오는데 실패했습니다')
  }
}

const submitTodayFeedback = async () => {
  if (!feedbackForm.value.content.trim()) {
    ElMessage.warning('피드백 내용을 입력해주세요')
    return
  }

  if (!feedbackForm.value.lessonId) {
    ElMessage.warning('수업을 선택해주세요')
    return
  }

  try {
    await dailyFeedbackAPI.updateInstructorFeedback(
      Number(studentId.value),
      feedbackForm.value.lessonId,
      feedbackForm.value.content,
      feedbackForm.value.authorName
    )

    ElMessage.success('피드백이 저장되었습니다')
    feedbackDialogVisible.value = false
    feedbackForm.value.content = ''
    feedbackForm.value.lessonId = null
  } catch (error: any) {
    ElMessage.error('피드백 저장에 실패했습니다')
  }
}

onMounted(() => {
  fetchStudentDetail()
})
</script>

<template>
  <div v-loading="loading" :style="{ padding: containerPadding, maxWidth: '1200px', margin: '0 auto' }">
    <!-- Header -->
    <el-card shadow="never" style="margin-bottom: 24px">
      <div>
        <h1 style="margin: 0 0 16px 0; font-size: 28px; font-weight: 600; color: #303133; display: flex; align-items: center; gap: 12px">
          <el-icon size="32" color="#409eff">
            <User />
          </el-icon>
          <span>{{ student?.name || '학생 상세' }}</span>
        </h1>
        <div style="display: flex; gap: 12px; flex-wrap: wrap; align-content: flex-start; justify-content: flex-start; margin: 0; width: 100%">
          <el-button v-if="isTeacher" type="primary" @click="openTodayFeedbackDialog" style="margin: 0">
            <el-icon style="margin-right: 8px"><Edit /></el-icon>
            수업 피드백 작성
          </el-button>
          <el-button type="success" @click="router.push(isTeacher ? `/students/${studentId}/feedback` : '/student/daily-feedback')" style="margin: 0">
            <el-icon style="margin-right: 8px"><View /></el-icon>
            수업 피드백 보기
          </el-button>
          <el-button @click="goBack" :icon="ArrowLeft" style="margin: 0">
            {{ isTeacher ? '목록으로' : '대시보드로' }}
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- Student Info & Stats -->
    <el-row :gutter="24" style="margin-bottom: 24px">
      <!-- 학생 정보 -->
      <el-col :xs="24" :sm="24" :md="8">
        <el-card shadow="never">
          <template #header>
            <div style="display: flex; align-items: center; gap: 8px">
              <el-icon color="#409eff">
                <InfoFilled />
              </el-icon>
              <span style="font-weight: 600">학생 정보</span>
            </div>
          </template>

          <div style="text-align: center; padding: 20px 0">
            <el-avatar :size="80" :icon="UserFilled" style="margin-bottom: 16px" />
            <h2 style="margin: 0 0 8px; font-size: 24px; font-weight: 600">{{ student?.name }}</h2>

            <div style="margin-top: 24px; text-align: left">
              <div style="padding: 12px 0; border-bottom: 1px solid #ebeef5">
                <span style="color: #909399; font-size: 14px">학년</span>
                <div style="margin-top: 4px; font-weight: 500">{{ student?.grade }}</div>
              </div>
              <div style="padding: 12px 0; border-bottom: 1px solid #ebeef5">
                <span style="color: #909399; font-size: 14px">학교</span>
                <div style="margin-top: 4px; font-weight: 500">{{ student?.school }}</div>
              </div>
              <div style="padding: 12px 0; border-bottom: 1px solid #ebeef5">
                <span style="color: #909399; font-size: 14px">학원</span>
                <div style="margin-top: 4px; font-weight: 500">{{ student?.academyName }}</div>
              </div>
              <div style="padding: 12px 0">
                <span style="color: #909399; font-size: 14px">반</span>
                <div style="margin-top: 4px; font-weight: 500">{{ student?.className }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 통계 -->
      <el-col :xs="24" :sm="24" :md="16">
        <el-card shadow="never">
          <el-tabs v-model="activeTab" type="card">
            <!-- 시험 통계 탭 -->
            <el-tab-pane label="시험 통계" name="test">
              <el-row :gutter="16" style="margin-bottom: 16px">
                <el-col :xs="12" :sm="12" :md="12">
                  <div style="text-align: center; padding: 20px; background: #f0f9ff; border-radius: 8px">
                    <div style="font-size: 32px; font-weight: 600; color: #409eff; margin-bottom: 8px">
                      {{ submissions.length }}
                    </div>
                    <div style="color: #909399; font-size: 14px">총 응시 시험</div>
                  </div>
                </el-col>
                <el-col :xs="12" :sm="12" :md="12">
                  <div style="text-align: center; padding: 20px; background: #f0fdf4; border-radius: 8px">
                    <div style="font-size: 32px; font-weight: 600; color: #67c23a; margin-bottom: 8px">
                      {{ averageScore }}
                    </div>
                    <div style="color: #909399; font-size: 14px">평균 점수</div>
                  </div>
                </el-col>
              </el-row>

              <el-row :gutter="16">
                <el-col :xs="12" :sm="12" :md="12">
                  <div style="text-align: center; padding: 20px; background: #fffbeb; border-radius: 8px">
                    <div style="font-size: 32px; font-weight: 600; color: #e6a23c; margin-bottom: 8px">
                      {{ maxScore }}
                    </div>
                    <div style="color: #909399; font-size: 14px">최고 점수</div>
                  </div>
                </el-col>
                <el-col :xs="12" :sm="12" :md="12">
                  <div style="text-align: center; padding: 20px; background: #fef2f2; border-radius: 8px">
                    <div style="font-size: 32px; font-weight: 600; color: #f56c6c; margin-bottom: 8px">
                      {{ minScore }}
                    </div>
                    <div style="color: #909399; font-size: 14px">최저 점수</div>
                  </div>
                </el-col>
              </el-row>

          <!-- 꺾은선 그래프 -->
          <div v-if="chartPoints && chartPoints.length > 0" style="margin-top: 24px; padding: 20px; background: #fafafa; border-radius: 8px; cursor: pointer; transition: all 0.3s" @click="openChartModal">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; gap: 12px">
              <div style="font-weight: 600; color: #303133; font-size: 15px">점수 추이</div>
              <div style="display: flex; gap: 12px; font-size: 11px; align-items: center; flex-wrap: nowrap">
                <div style="display: flex; align-items: center; gap: 4px; white-space: nowrap">
                  <div style="width: 14px; height: 2.5px; background: #409eff; border-radius: 2px"></div>
                  <span style="color: #606266">내 점수</span>
                </div>
                <div style="display: flex; align-items: center; gap: 4px; white-space: nowrap">
                  <div style="width: 14px; height: 2.5px; background: #67c23a; border-radius: 2px"></div>
                  <span style="color: #606266">학원 평균</span>
                </div>
                <el-button type="primary" size="small" :icon="ZoomIn" @click.stop="openChartModal" style="padding: 4px 8px; font-size: 11px">
                  확대하여 보기
                </el-button>
              </div>
            </div>

            <svg :viewBox="chartViewBox" style="width: 100%; height: auto">
              <!-- 배경 격자선 -->
              <line x1="60" y1="30" x2="680" y2="30" stroke="#e0e0e0" stroke-width="1" stroke-dasharray="4,4" />
              <line x1="60" y1="70" x2="680" y2="70" stroke="#e0e0e0" stroke-width="1" stroke-dasharray="4,4" />
              <line x1="60" y1="110" x2="680" y2="110" stroke="#e0e0e0" stroke-width="1" stroke-dasharray="4,4" />
              <line x1="60" y1="150" x2="680" y2="150" stroke="#e0e0e0" stroke-width="1" stroke-dasharray="4,4" />
              <line x1="60" y1="190" x2="680" y2="190" stroke="#e0e0e0" stroke-width="1" stroke-dasharray="4,4" />

              <!-- Y축 레이블 -->
              <text x="50" y="35" text-anchor="end" font-size="12" fill="#909399">100</text>
              <text x="50" y="75" text-anchor="end" font-size="12" fill="#909399">75</text>
              <text x="50" y="115" text-anchor="end" font-size="12" fill="#909399">50</text>
              <text x="50" y="155" text-anchor="end" font-size="12" fill="#909399">25</text>
              <text x="50" y="195" text-anchor="end" font-size="12" fill="#909399">0</text>

              <!-- X축 -->
              <line x1="60" y1="190" x2="680" y2="190" stroke="#303133" stroke-width="2" />

              <!-- 학원 평균 선 -->
              <polyline
                v-if="chartPoints.length > 1"
                :points="academyLinePoints"
                fill="none" stroke="#67c23a" stroke-width="2.5"
              />

              <!-- 내 점수 선 -->
              <polyline
                v-if="chartPoints.length > 1"
                :points="studentLinePoints"
                fill="none" stroke="#409eff" stroke-width="3"
              />

              <!-- 데이터 포인트 및 레이블 -->
              <template v-for="(point, index) in chartPoints" :key="index">
                <!-- X축 레이블 -->
                <text
                  :x="point.x"
                  y="210"
                  text-anchor="middle"
                  font-size="11"
                  fill="#606266"
                >
                  {{ point.date }}
                </text>

                <!-- 학원 평균 점 -->
                <circle
                  :cx="point.x"
                  :cy="point.academyY"
                  r="5"
                  fill="#67c23a"
                  stroke="white"
                  stroke-width="2"
                />

                <!-- 내 점수 점 -->
                <circle
                  :cx="point.x"
                  :cy="point.studentY"
                  r="6"
                  fill="#409eff"
                  stroke="white"
                  stroke-width="2"
                >
                  <title>{{ point.studentScore }}점 (평균: {{ point.academyAverage }}점)</title>
                </circle>

                <!-- 점수 레이블 -->
                <text
                  :x="point.x"
                  :y="point.labelY"
                  text-anchor="middle"
                  font-size="12"
                  font-weight="600"
                  fill="#409eff"
                >
                  {{ point.studentScore }}
                </text>
              </template>
            </svg>
          </div>

          <el-empty v-else description="아직 응시한 시험이 없습니다" />
            </el-tab-pane>

            <!-- 숙제 통계 탭 -->
            <el-tab-pane label="숙제 통계" name="homework">
              <el-row :gutter="16" style="margin-bottom: 16px">
                <el-col :xs="12" :sm="12" :md="12">
                  <div style="text-align: center; padding: 20px; background: #fef2f2; border-radius: 8px; position: relative">
                    <div style="font-size: 32px; font-weight: 600; color: #f56c6c; margin-bottom: 8px">
                      {{ incompleteHomeworkCount }}
                    </div>
                    <div
                      style="color: #409eff; font-size: 14px; cursor: pointer; text-decoration: underline; transition: color 0.3s"
                      @click="incompleteHomeworksDialogVisible = true"
                      @mouseenter="e => e.currentTarget.style.color = '#66b1ff'"
                      @mouseleave="e => e.currentTarget.style.color = '#409eff'"
                    >
                      미완성 숙제 수
                    </div>
                  </div>
                </el-col>
                <el-col :xs="12" :sm="12" :md="12">
                  <div style="text-align: center; padding: 20px; background: #fff7e6; border-radius: 8px">
                    <div style="font-size: 32px; font-weight: 600; color: #e6a23c; margin-bottom: 8px">
                      {{ totalIncorrectCount }}개
                    </div>
                    <div style="color: #909399; font-size: 14px">총 오답 개수</div>
                  </div>
                </el-col>
              </el-row>

              <el-row :gutter="16">
                <el-col :xs="12" :sm="12" :md="12">
                  <div style="padding: 10px; background: #f0fdf4; border-radius: 8px; text-align: center">
                    <div style="color: #909399; font-size: 14px; margin-bottom: 12px">평균 완성도</div>
                    <el-progress
                      type="circle"
                      :percentage="averageCompletion"
                      :color="averageCompletion >= 80 ? '#67c23a' : averageCompletion >= 60 ? '#e6a23c' : '#f56c6c'"
                      :width="progressSize"
                      :stroke-width="progressStrokeWidth"
                    />
                  </div>
                </el-col>
                <el-col :xs="12" :sm="12" :md="12">
                  <div style="padding: 10px; background: #fffbeb; border-radius: 8px; text-align: center">
                    <div style="color: #909399; font-size: 14px; margin-bottom: 12px">최저 완성도</div>
                    <el-progress
                      type="circle"
                      :percentage="minCompletion"
                      :color="minCompletion >= 80 ? '#67c23a' : minCompletion >= 60 ? '#e6a23c' : '#f56c6c'"
                      :width="progressSize"
                      :stroke-width="progressStrokeWidth"
                    />
                  </div>
                </el-col>
              </el-row>

              <!-- 숙제 목록 -->
              <div v-if="studentHomeworks.length > 0" style="margin-top: 24px">
                <el-table :data="studentHomeworks" style="width: 100%">
                  <el-table-column prop="homeworkTitle" label="숙제명" min-width="200" />
                  <el-table-column label="오답 개수" width="120" align="center">
                    <template #default="{ row }">
                      <el-tag :type="(row.incorrectCount || 0) === 0 ? 'success' : 'warning'">
                        {{ row.incorrectCount || 0 }}개
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="완성도" width="150" align="center">
                    <template #default="{ row }">
                      <el-progress
                        :percentage="row.completion || 0"
                        :color="row.completion >= 80 ? '#67c23a' : row.completion >= 60 ? '#e6a23c' : '#f56c6c'"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column label="제출 기한" width="150">
                    <template #default="{ row }">
                      <span v-if="row.dueDate">{{ new Date(row.dueDate).toLocaleDateString('ko-KR') }}</span>
                      <span v-else style="color: #909399">-</span>
                    </template>
                  </el-table-column>
                </el-table>
              </div>

              <el-empty v-else description="아직 완성한 숙제가 없습니다" />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <!-- 시험 목록 -->
    <el-card shadow="never" style="margin-bottom: 24px">
      <template #header>
        <div style="display: flex; align-items: center; gap: 8px">
          <el-icon color="#67c23a">
            <Document />
          </el-icon>
          <span style="font-weight: 600">응시한 시험</span>
        </div>
      </template>

      <!-- Desktop: Table View -->
      <el-table v-if="!isMobile" :data="submissions" style="width: 100%" stripe>
        <el-table-column label="시험명" min-width="200">
          <template #default="{ row }">
            <div
              style="cursor: pointer; color: #409eff; font-weight: 500"
              @click="navigateToTest(row.testId)"
            >
              <el-icon style="margin-right: 4px"><Document /></el-icon>
              {{ row.testTitle || '시험명 없음' }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="점수" width="120" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.totalScore >= 80 ? 'success' : row.totalScore >= 60 ? 'warning' : 'danger'"
              size="large"
            >
              {{ row.totalScore }}점
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="응시일" width="180">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 8px">
              <el-icon color="#909399">
                <Calendar />
              </el-icon>
              {{ row.submittedAt ? new Date(row.submittedAt).toLocaleString('ko-KR') : '-' }}
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- Mobile: Card View -->
      <div v-else>
        <el-card
          v-for="submission in submissions"
          :key="submission.id"
          shadow="hover"
          style="margin-bottom: 16px; cursor: pointer"
          @click="navigateToTest(submission.testId)"
        >
          <div style="display: flex; flex-direction: column; gap: 12px">
            <!-- Title and Score -->
            <div style="display: flex; justify-content: space-between; align-items: start; gap: 12px">
              <div style="flex: 1">
                <h3 style="margin: 0; font-size: 18px; font-weight: 600; color: #409eff">
                  {{ submission.testTitle || '시험명 없음' }}
                </h3>
              </div>
              <el-tag
                :type="submission.totalScore >= 80 ? 'success' : submission.totalScore >= 60 ? 'warning' : 'danger'"
                size="large"
              >
                {{ submission.totalScore }}점
              </el-tag>
            </div>

            <!-- Date Info -->
            <div style="display: flex; align-items: center; gap: 8px; font-size: 14px; color: #606266">
              <el-icon color="#909399">
                <Calendar />
              </el-icon>
              <span>{{ submission.submittedAt ? new Date(submission.submittedAt).toLocaleString('ko-KR', {
                year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit'
              }) : '-' }}</span>
            </div>
          </div>
        </el-card>
      </div>

      <el-empty v-if="submissions.length === 0" description="아직 응시한 시험이 없습니다" />
    </el-card>

    <!-- 차트 확대 모달 -->
    <el-dialog
      v-model="chartModalVisible"
      title="점수 추이 상세"
      :width="dialogWidth"
      :fullscreen="isMobile"
      top="5vh"
      :close-on-click-modal="false"
    >
      <div style="margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center">
        <div style="display: flex; gap: 16px; font-size: 14px">
          <div style="display: flex; align-items: center; gap: 8px">
            <div style="width: 20px; height: 4px; background: #409eff; border-radius: 2px"></div>
            <span style="color: #606266">내 점수</span>
          </div>
          <div style="display: flex; align-items: center; gap: 8px">
            <div style="width: 20px; height: 4px; background: #67c23a; border-radius: 2px"></div>
            <span style="color: #606266">학원 평균</span>
          </div>
        </div>
        <div style="display: flex; gap: 12px; align-items: center">
          <span style="color: #909399; font-size: 13px">확대: {{ Math.round(combinedZoom * 100) }}%</span>
          <el-button size="small" @click="resetZoomAndPan" :icon="Refresh">초기화</el-button>
        </div>
      </div>

      <div
        style="
          background: #fafafa;
          border-radius: 8px;
          overflow: hidden;
          position: relative;
          height: 600px;
          touch-action: none;
        "
        @wheel="handleWheel"
        @mousedown="handleMouseDown"
        @mousemove="handleMouseMove"
        @mouseup="handleMouseUp"
        @mouseleave="handleMouseUp"
        @touchstart="handleTouchStart"
        @touchmove="handleTouchMove"
        @touchend="handleTouchEnd"
        @touchcancel="handleTouchEnd"
      >
        <svg
          :viewBox="chartViewBox"
          style="width: 100%; height: 100%; cursor: grab"
          :style="{ cursor: isDragging || isTouchDragging ? 'grabbing' : 'grab' }"
        >
          <g :transform="`translate(${combinedPanX}, ${combinedPanY}) scale(${combinedZoom})`" style="transform-origin: center">
            <!-- 배경 격자선 -->
            <line x1="60" y1="30" x2="680" y2="30" stroke="#e0e0e0" stroke-width="1" stroke-dasharray="4,4" />
            <line x1="60" y1="70" x2="680" y2="70" stroke="#e0e0e0" stroke-width="1" stroke-dasharray="4,4" />
            <line x1="60" y1="110" x2="680" y2="110" stroke="#e0e0e0" stroke-width="1" stroke-dasharray="4,4" />
            <line x1="60" y1="150" x2="680" y2="150" stroke="#e0e0e0" stroke-width="1" stroke-dasharray="4,4" />
            <line x1="60" y1="190" x2="680" y2="190" stroke="#e0e0e0" stroke-width="1" stroke-dasharray="4,4" />

            <!-- Y축 레이블 -->
            <text x="50" y="35" text-anchor="end" font-size="12" fill="#909399">100</text>
            <text x="50" y="75" text-anchor="end" font-size="12" fill="#909399">75</text>
            <text x="50" y="115" text-anchor="end" font-size="12" fill="#909399">50</text>
            <text x="50" y="155" text-anchor="end" font-size="12" fill="#909399">25</text>
            <text x="50" y="195" text-anchor="end" font-size="12" fill="#909399">0</text>

            <!-- X축 -->
            <line x1="60" y1="190" x2="680" y2="190" stroke="#303133" stroke-width="2" />

            <!-- 학원 평균 선 -->
            <polyline
              v-if="chartPoints.length > 1"
              :points="academyLinePoints"
              fill="none" stroke="#67c23a" stroke-width="2.5"
            />

            <!-- 내 점수 선 -->
            <polyline
              v-if="chartPoints.length > 1"
              :points="studentLinePoints"
              fill="none" stroke="#409eff" stroke-width="3"
            />

            <!-- 데이터 포인트 및 레이블 -->
            <template v-for="(point, index) in chartPoints" :key="index">
              <!-- X축 레이블 -->
              <text
                :x="point.x"
                y="210"
                text-anchor="middle"
                font-size="12"
                fill="#606266"
              >
                {{ point.date }}
              </text>

              <!-- 학원 평균 점 -->
              <circle
                :cx="point.x"
                :cy="point.academyY"
                r="6"
                fill="#67c23a"
                stroke="white"
                stroke-width="2"
              />

              <!-- 내 점수 점 -->
              <circle
                :cx="point.x"
                :cy="point.studentY"
                r="7"
                fill="#409eff"
                stroke="white"
                stroke-width="2"
              >
                <title>{{ point.studentScore }}점 (평균: {{ point.academyAverage }}점)</title>
              </circle>

              <!-- 점수 레이블 -->
              <text
                :x="point.x"
                :y="point.labelY"
                text-anchor="middle"
                font-size="14"
                font-weight="600"
                fill="#409eff"
              >
                {{ point.studentScore }}
              </text>

              <!-- 학원 평균 레이블 -->
              <text
                :x="point.x"
                :y="point.academyY + 20"
                text-anchor="middle"
                font-size="12"
                font-weight="500"
                fill="#67c23a"
              >
                {{ Math.round(point.academyAverage) }}
              </text>
            </template>
          </g>
        </svg>
      </div>

      <div style="margin-top: 16px; padding: 12px; background: #f0f9ff; border-radius: 8px; font-size: 13px; color: #606266">
        <div style="font-weight: 600; margin-bottom: 8px; color: #409eff">사용 방법</div>
        <div v-if="isMobile">• 핀치: 확대/축소</div>
        <div v-if="isMobile">• 드래그: 차트 이동</div>
        <div v-if="!isMobile">• 마우스 휠: 확대/축소</div>
        <div v-if="!isMobile">• 클릭 & 드래그: 차트 이동</div>
        <div>• 초기화 버튼: 원래 크기로 복원</div>
      </div>
    </el-dialog>

    <!-- 수업 피드백 작성 다이얼로그 -->
    <el-dialog
      v-model="feedbackDialogVisible"
      title="수업 피드백 작성"
      :width="isMobile ? '95%' : '600px'"
      :fullscreen="isMobile"
      :close-on-click-modal="false"
    >
      <el-form label-width="100px">
        <el-form-item label="수업 선택" required>
          <el-select v-model="feedbackForm.lessonId" placeholder="수업을 선택하세요" style="width: 100%">
            <el-option
              v-for="lesson in lessons"
              :key="lesson.id"
              :label="`${lesson.lessonDate} - ${lesson.testTitle || lesson.homeworkTitle || '수업'}`"
              :value="lesson.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="작성자">
          <el-input v-model="feedbackForm.authorName" placeholder="선생님 이름" />
        </el-form-item>
        <el-form-item label="피드백 내용">
          <el-input
            v-model="feedbackForm.content"
            type="textarea"
            :rows="10"
            placeholder="수업에 대한 피드백을 작성하세요..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="feedbackDialogVisible = false">취소</el-button>
        <el-button type="primary" @click="submitTodayFeedback">저장</el-button>
      </template>
    </el-dialog>

    <!-- 미완성 숙제 목록 다이얼로그 -->
    <el-dialog
      v-model="incompleteHomeworksDialogVisible"
      title="미완성 숙제 목록"
      :width="isMobile ? '95%' : '700px'"
      :fullscreen="isMobile"
    >
      <div v-if="incompleteHomeworks.length > 0">
        <el-table :data="incompleteHomeworks" style="width: 100%">
          <el-table-column prop="homeworkTitle" label="숙제명" min-width="200" />
          <el-table-column label="오답 개수" width="120" align="center">
            <template #default="{ row }">
              <el-tag type="warning">{{ row.incorrectCount || 0 }}개</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="완성도" width="150" align="center">
            <template #default="{ row }">
              <el-progress
                :percentage="row.completion || 0"
                :color="row.completion >= 80 ? '#67c23a' : row.completion >= 60 ? '#e6a23c' : '#f56c6c'"
              />
            </template>
          </el-table-column>
          <el-table-column label="제출 기한" width="120">
            <template #default="{ row }">
              <span v-if="row.dueDate">{{ new Date(row.dueDate).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' }) }}</span>
              <span v-else style="color: #909399">-</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <el-empty v-else description="모든 숙제를 완료했습니다!" />
      <template #footer>
        <el-button @click="incompleteHomeworksDialogVisible = false">닫기</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.el-card {
  border-radius: 8px;
}

.el-table {
  border-radius: 8px;
  overflow: hidden;
}

/* 차트 호버 효과 */
div[style*="cursor: pointer"]:hover {
  background: #f0f9ff !important;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
</style>
