<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {
  type Feedback,
  feedbackAPI,
  type Student,
  studentAPI,
  type Submission,
  submissionAPI,
  testAPI
} from '../api/client'

const route = useRoute()
const router = useRouter()
const studentId = route.params.id as string

const loading = ref(false)
const student = ref<Student | null>(null)
const submissions = ref<Submission[]>([])
const feedbacks = ref<Feedback[]>([])
const testStats = ref<Record<number, any>>({})

// 모달 상태
const chartModalVisible = ref(false)

// 확대/축소 및 이동 상태
const zoom = ref(1)
const panX = ref(0)
const panY = ref(0)
const isDragging = ref(false)
const dragStartX = ref(0)
const dragStartY = ref(0)

// 차트 포인트 계산 (모든 좌표 사전 계산)
const chartPoints = computed(() => {
  if (submissions.value.length === 0) return []

  const points = submissions.value.map((s, index) => {
    const testId = s.test?.id || s.testId
    const stats = testStats.value[testId]
    const studentScore = s.totalScore
    const academyAverage = stats?.averageScore || 0

    // X 좌표 계산
    const totalPoints = submissions.value.length
    const divisor = totalPoints > 1 ? totalPoints - 1 : 1
    const x = 60 + (index * 620 / divisor)

    // Y 좌표 계산
    const studentY = 190 - (studentScore / 100) * 160
    const academyY = 190 - (academyAverage / 100) * 160
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

// 모달 열기
const openChartModal = () => {
  chartModalVisible.value = true
  resetZoomAndPan()
}

// 줌/팬 초기화
const resetZoomAndPan = () => {
  zoom.value = 1
  panX.value = 0
  panY.value = 0
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

// SVG 변환 계산
const svgTransform = computed(() => {
  return `translate(${panX.value}, ${panY.value}) scale(${zoom.value})`
})

const fetchStudentDetail = async () => {
  loading.value = true
  try {
    const [studentRes, submissionsRes, feedbacksRes] = await Promise.all([
      studentAPI.getStudent(Number(studentId)),
      submissionAPI.getStudentSubmissions(Number(studentId)),
      feedbackAPI.getStudentFeedbacks(Number(studentId))
    ])

    student.value = studentRes.data
    submissions.value = (submissionsRes.data.content || submissionsRes.data)
      .sort((a: Submission, b: Submission) => {
        const dateA = a.submittedAt ? new Date(a.submittedAt).getTime() : 0
        const dateB = b.submittedAt ? new Date(b.submittedAt).getTime() : 0
        return dateA - dateB
      })
    feedbacks.value = feedbacksRes.data.content || feedbacksRes.data

    // 각 시험의 통계 가져오기
    const testIds = [...new Set(submissions.value.map(s => s.test?.id || s.testId).filter(id => id))]
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
  router.push('/students')
}

const navigateToTest = (testId: number) => {
  router.push(`/tests/${testId}`)
}

onMounted(() => {
  fetchStudentDetail()
})
</script>

<template>
  <div v-loading="loading">
    <!-- Header -->
    <el-card shadow="never" style="margin-bottom: 24px">
      <div style="display: flex; justify-content: space-between; align-items: center">
        <div>
          <h1 style="margin: 0; font-size: 28px; font-weight: 600; color: #303133; display: flex; align-items: center; gap: 12px">
            <el-icon size="32" color="#409eff">
              <User />
            </el-icon>
            {{ student?.name || '학생 상세' }}
          </h1>
          <p style="margin: 8px 0 0; color: #909399">학생 ID: {{ studentId }}</p>
        </div>
        <el-button @click="goBack" :icon="ArrowLeft" size="large">
          목록으로
        </el-button>
      </div>
    </el-card>

    <!-- Student Info & Stats -->
    <el-row :gutter="24" style="margin-bottom: 24px">
      <!-- 학생 정보 -->
      <el-col :span="8">
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
              <div style="padding: 12px 0">
                <span style="color: #909399; font-size: 14px">학원</span>
                <div style="margin-top: 4px; font-weight: 500">{{ student?.academy }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 통계 -->
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div style="display: flex; align-items: center; gap: 8px">
              <el-icon color="#e6a23c">
                <TrendCharts />
              </el-icon>
              <span style="font-weight: 600">성적 통계</span>
            </div>
          </template>

          <el-row :gutter="16">
            <el-col :span="6">
              <div style="text-align: center; padding: 20px; background: #f0f9ff; border-radius: 8px">
                <div style="font-size: 32px; font-weight: 600; color: #409eff; margin-bottom: 8px">
                  {{ submissions.length }}
                </div>
                <div style="color: #909399; font-size: 14px">총 응시 시험</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div style="text-align: center; padding: 20px; background: #f0fdf4; border-radius: 8px">
                <div style="font-size: 32px; font-weight: 600; color: #67c23a; margin-bottom: 8px">
                  {{ averageScore }}
                </div>
                <div style="color: #909399; font-size: 14px">평균 점수</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div style="text-align: center; padding: 20px; background: #fffbeb; border-radius: 8px">
                <div style="font-size: 32px; font-weight: 600; color: #e6a23c; margin-bottom: 8px">
                  {{ maxScore }}
                </div>
                <div style="color: #909399; font-size: 14px">최고 점수</div>
              </div>
            </el-col>
            <el-col :span="6">
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
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
              <div style="font-weight: 600; color: #303133">점수 추이</div>
              <div style="display: flex; gap: 16px; font-size: 12px; align-items: center">
                <el-button type="primary" size="small" :icon="ZoomIn" @click.stop="openChartModal">확대하여 보기</el-button>
                <div style="display: flex; align-items: center; gap: 4px">
                  <div style="width: 16px; height: 3px; background: #409eff; border-radius: 2px"></div>
                  <span style="color: #606266">내 점수</span>
                </div>
                <div style="display: flex; align-items: center; gap: 4px">
                  <div style="width: 16px; height: 3px; background: #67c23a; border-radius: 2px"></div>
                  <span style="color: #606266">학원 평균</span>
                </div>
              </div>
            </div>

            <svg viewBox="0 0 700 250" style="width: 100%; height: 250px">
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

      <el-table :data="submissions" style="width: 100%" stripe>
        <el-table-column label="시험명" min-width="200">
          <template #default="{ row }">
            <div
              style="cursor: pointer; color: #409eff; font-weight: 500"
              @click="navigateToTest(row.test?.id)"
            >
              <el-icon style="margin-right: 4px"><Document /></el-icon>
              {{ row.test?.title || row.testTitle || '시험명 없음' }}
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

      <el-empty v-if="submissions.length === 0" description="아직 응시한 시험이 없습니다" />
    </el-card>

    <!-- 피드백 목록 -->
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; align-items: center; gap: 8px">
          <el-icon color="#e6a23c">
            <ChatLineRound />
          </el-icon>
          <span style="font-weight: 600">선생님 피드백</span>
        </div>
      </template>

      <el-timeline v-if="feedbacks.length > 0">
        <el-timeline-item
          v-for="feedback in feedbacks"
          :key="feedback.id"
          :timestamp="feedback.createdAt ? new Date(feedback.createdAt).toLocaleString('ko-KR') : ''"
          placement="top"
        >
          <el-card>
            <div style="margin-bottom: 8px">
              <el-tag type="info" size="small">{{ feedback.submission?.testTitle || '시험' }}</el-tag>
              <el-tag type="success" size="small" style="margin-left: 8px">
                {{ feedback.submission?.totalScore }}점
              </el-tag>
            </div>
            <div style="font-weight: 600; margin-bottom: 8px; color: #303133">
              {{ feedback.teacherName }}
            </div>
            <div style="color: #606266; line-height: 1.6">
              {{ feedback.content }}
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>

      <el-empty v-else description="아직 피드백이 없습니다">
        <template #image>
          <el-icon size="60" color="#c0c4cc">
            <ChatLineRound />
          </el-icon>
        </template>
      </el-empty>
    </el-card>

    <!-- 차트 확대 모달 -->
    <el-dialog
      v-model="chartModalVisible"
      title="점수 추이 상세"
      width="90%"
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
          <span style="color: #909399; font-size: 13px">확대: {{ Math.round(zoom * 100) }}%</span>
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
        "
        @wheel="handleWheel"
        @mousedown="handleMouseDown"
        @mousemove="handleMouseMove"
        @mouseup="handleMouseUp"
        @mouseleave="handleMouseUp"
      >
        <svg
          viewBox="0 0 700 250"
          style="width: 100%; height: 100%; cursor: grab"
          :style="{ cursor: isDragging ? 'grabbing' : 'grab' }"
        >
          <g :transform="svgTransform" style="transform-origin: center">
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
        <div>• 마우스 휠: 확대/축소</div>
        <div>• 클릭 & 드래그: 차트 이동</div>
        <div>• 초기화 버튼: 원래 크기로 복원</div>
      </div>
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
