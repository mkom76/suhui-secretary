<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { lessonAPI, testAPI, homeworkAPI, type Lesson, type Test, type Homework } from '../api/client'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const lesson = ref<Lesson | null>(null)
const unattachedTests = ref<Test[]>([])
const unattachedHomeworks = ref<Homework[]>([])
const attachTestDialogVisible = ref(false)
const attachHomeworkDialogVisible = ref(false)
const selectedTestId = ref<number | undefined>(undefined)
const selectedHomeworkId = ref<number | undefined>(undefined)

const lessonId = computed(() => Number(route.params.id))

const fetchLesson = async () => {
  loading.value = true
  try {
    const response = await lessonAPI.getLesson(lessonId.value)
    lesson.value = response.data
  } catch (error) {
    ElMessage.error('수업 정보를 불러오는데 실패했습니다.')
  } finally {
    loading.value = false
  }
}

const fetchUnattachedTests = async () => {
  if (!lesson.value) return
  try {
    const response = await testAPI.getUnattachedTests(lesson.value.academyId!, lesson.value.classId!)
    unattachedTests.value = response.data
  } catch (error) {
    ElMessage.error('연결 가능한 시험 목록을 불러오는데 실패했습니다.')
  }
}

const fetchUnattachedHomeworks = async () => {
  if (!lesson.value) return
  try {
    const response = await homeworkAPI.getUnattachedHomeworks(lesson.value.academyId!, lesson.value.classId!)
    unattachedHomeworks.value = response.data
  } catch (error) {
    ElMessage.error('연결 가능한 숙제 목록을 불러오는데 실패했습니다.')
  }
}

const openAttachTestDialog = async () => {
  await fetchUnattachedTests()
  selectedTestId.value = undefined
  attachTestDialogVisible.value = true
}

const openAttachHomeworkDialog = async () => {
  await fetchUnattachedHomeworks()
  selectedHomeworkId.value = undefined
  attachHomeworkDialogVisible.value = true
}

const handleAttachTest = async () => {
  if (!selectedTestId.value) {
    ElMessage.error('시험을 선택해주세요.')
    return
  }

  try {
    await lessonAPI.attachTest(lessonId.value, selectedTestId.value)
    ElMessage.success('시험이 연결되었습니다.')
    attachTestDialogVisible.value = false
    fetchLesson()
  } catch (error: any) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('시험 연결에 실패했습니다.')
    }
  }
}

const handleAttachHomework = async () => {
  if (!selectedHomeworkId.value) {
    ElMessage.error('숙제를 선택해주세요.')
    return
  }

  try {
    await lessonAPI.attachHomework(lessonId.value, selectedHomeworkId.value)
    ElMessage.success('숙제가 연결되었습니다.')
    attachHomeworkDialogVisible.value = false
    fetchLesson()
  } catch (error: any) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('숙제 연결에 실패했습니다.')
    }
  }
}

const handleDetachTest = async () => {
  try {
    await ElMessageBox.confirm(
      '이 시험과의 연결을 해제하시겠습니까?',
      '확인',
      {
        confirmButtonText: '확인',
        cancelButtonText: '취소',
        type: 'warning',
      }
    )

    await lessonAPI.detachTest(lessonId.value)
    ElMessage.success('시험 연결이 해제되었습니다.')
    fetchLesson()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('시험 연결 해제에 실패했습니다.')
    }
  }
}

const handleDetachHomework = async () => {
  try {
    await ElMessageBox.confirm(
      '이 숙제와의 연결을 해제하시겠습니까?',
      '확인',
      {
        confirmButtonText: '확인',
        cancelButtonText: '취소',
        type: 'warning',
      }
    )

    await lessonAPI.detachHomework(lessonId.value)
    ElMessage.success('숙제 연결이 해제되었습니다.')
    fetchLesson()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('숙제 연결 해제에 실패했습니다.')
    }
  }
}

const navigateToTestDetail = (testId: number) => {
  router.push(`/tests/${testId}`)
}

const navigateToHomeworkDetail = (homeworkId: number) => {
  router.push(`/homeworks/${homeworkId}`)
}

onMounted(() => {
  fetchLesson()
})
</script>

<template>
  <div v-loading="loading" style="padding: 24px; max-width: 1200px; margin: 0 auto">
    <el-card shadow="never" style="margin-bottom: 24px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <h2 style="margin: 0; font-size: 24px; font-weight: 600">수업 상세</h2>
          <el-button @click="router.push('/lessons')">목록으로</el-button>
        </div>
      </template>

      <div v-if="lesson">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="수업 날짜">
            {{ new Date(lesson.lessonDate).toLocaleDateString('ko-KR', {
              year: 'numeric', month: 'long', day: 'numeric', weekday: 'long'
            }) }}
          </el-descriptions-item>
          <el-descriptions-item label="학원">
            {{ lesson.academyName }}
          </el-descriptions-item>
          <el-descriptions-item label="반">
            {{ lesson.className }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <el-row :gutter="24">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <h3 style="margin: 0; font-size: 18px; font-weight: 600">시험 관리</h3>
              <el-button
                v-if="!lesson?.testTitle"
                type="primary"
                size="small"
                @click="openAttachTestDialog"
              >
                시험 연결
              </el-button>
            </div>
          </template>

          <div v-if="lesson?.testTitle">
            <div style="margin-bottom: 16px">
              <div style="font-size: 18px; font-weight: 600; margin-bottom: 8px">
                {{ lesson.testTitle }}
              </div>
              <el-tag type="success">연결됨</el-tag>
            </div>
            <div style="display: flex; gap: 12px">
              <el-button type="primary" @click="navigateToTestDetail(lesson.testId!)">
                시험 상세
              </el-button>
              <el-button type="warning" @click="handleDetachTest">
                시험 제거
              </el-button>
            </div>
          </div>
          <el-empty v-else description="연결된 시험이 없습니다" :image-size="100" />
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <h3 style="margin: 0; font-size: 18px; font-weight: 600">숙제 관리</h3>
              <el-button
                v-if="!lesson?.homeworkTitle"
                type="primary"
                size="small"
                @click="openAttachHomeworkDialog"
              >
                숙제 연결
              </el-button>
            </div>
          </template>

          <div v-if="lesson?.homeworkTitle">
            <div style="margin-bottom: 16px">
              <div style="font-size: 18px; font-weight: 600; margin-bottom: 8px">
                {{ lesson.homeworkTitle }}
              </div>
              <el-tag type="success">연결됨</el-tag>
            </div>
            <div style="display: flex; gap: 12px">
              <el-button type="primary" @click="navigateToHomeworkDetail(lesson.homeworkId!)">
                숙제 상세
              </el-button>
              <el-button type="warning" @click="handleDetachHomework">
                숙제 제거
              </el-button>
            </div>
          </div>
          <el-empty v-else description="연결된 숙제가 없습니다" :image-size="100" />
        </el-card>
      </el-col>
    </el-row>

    <!-- Attach Test Dialog -->
    <el-dialog v-model="attachTestDialogVisible" title="시험 연결" width="500px">
      <el-form label-width="80px">
        <el-form-item label="시험 선택">
          <el-select v-model="selectedTestId" placeholder="시험을 선택하세요" style="width: 100%">
            <el-option
              v-for="test in unattachedTests"
              :key="test.id"
              :label="test.title"
              :value="test.id"
            />
          </el-select>
        </el-form-item>
        <el-alert
          v-if="unattachedTests.length === 0"
          title="연결 가능한 시험이 없습니다"
          type="info"
          :closable="false"
          show-icon
        />
      </el-form>
      <template #footer>
        <el-button @click="attachTestDialogVisible = false">취소</el-button>
        <el-button
          type="primary"
          @click="handleAttachTest"
          :disabled="unattachedTests.length === 0"
        >
          연결
        </el-button>
      </template>
    </el-dialog>

    <!-- Attach Homework Dialog -->
    <el-dialog v-model="attachHomeworkDialogVisible" title="숙제 연결" width="500px">
      <el-form label-width="80px">
        <el-form-item label="숙제 선택">
          <el-select v-model="selectedHomeworkId" placeholder="숙제를 선택하세요" style="width: 100%">
            <el-option
              v-for="homework in unattachedHomeworks"
              :key="homework.id"
              :label="homework.title"
              :value="homework.id"
            />
          </el-select>
        </el-form-item>
        <el-alert
          v-if="unattachedHomeworks.length === 0"
          title="연결 가능한 숙제가 없습니다"
          type="info"
          :closable="false"
          show-icon
        />
      </el-form>
      <template #footer>
        <el-button @click="attachHomeworkDialogVisible = false">취소</el-button>
        <el-button
          type="primary"
          @click="handleAttachHomework"
          :disabled="unattachedHomeworks.length === 0"
        >
          연결
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
