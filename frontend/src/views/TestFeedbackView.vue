<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { feedbackAPI, submissionAPI, type Feedback, type Submission } from '../api/client'

const route = useRoute()
const router = useRouter()
const testId = route.params.id as string

const loading = ref(false)
const feedbacks = ref<Feedback[]>([])
const submissions = ref<Submission[]>([])
const dialogVisible = ref(false)
const editMode = ref(false)
const currentFeedbackId = ref<number | null>(null)
const newFeedback = ref({
  submissionId: '',
  teacherName: '',
  content: ''
})

const availableSubmissions = computed(() => {
  return submissions.value.filter(submission => submission.student)
})

const fetchData = async () => {
  loading.value = true
  try {
    const [feedbacksResponse, submissionsResponse] = await Promise.all([
      feedbackAPI.getByTestId(Number(testId)),
      submissionAPI.getByTestId(Number(testId))
    ])
    
    feedbacks.value = feedbacksResponse.data.content || feedbacksResponse.data
    submissions.value = submissionsResponse.data.content || submissionsResponse.data
  } catch (error) {
    ElMessage.error('피드백 정보를 불러오는데 실패했습니다.')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  editMode.value = false
  currentFeedbackId.value = null
  newFeedback.value = {
    submissionId: '',
    teacherName: '',
    content: ''
  }
  dialogVisible.value = true
}

const openEditDialog = (feedback: Feedback) => {
  editMode.value = true
  currentFeedbackId.value = feedback.id || null
  newFeedback.value = {
    submissionId: feedback.submissionId?.toString() || '',
    teacherName: feedback.teacherName || '',
    content: feedback.content || ''
  }
  dialogVisible.value = true
}

const handleAddFeedback = async () => {
  if (!newFeedback.value.submissionId || !newFeedback.value.teacherName || !newFeedback.value.content) {
    ElMessage.warning('모든 필드를 입력해주세요.')
    return
  }

  try {
    if (editMode.value && currentFeedbackId.value) {
      await feedbackAPI.updateFeedback(currentFeedbackId.value, {
        submissionId: Number(newFeedback.value.submissionId),
        teacherName: newFeedback.value.teacherName,
        content: newFeedback.value.content
      })
      ElMessage.success('피드백이 수정되었습니다.')
    } else {
      await feedbackAPI.create({
        submissionId: Number(newFeedback.value.submissionId),
        teacherName: newFeedback.value.teacherName,
        content: newFeedback.value.content
      })
      ElMessage.success('피드백이 추가되었습니다.')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    ElMessage.error(editMode.value ? '피드백 수정에 실패했습니다.' : '피드백 추가에 실패했습니다.')
  }
}

const handleDeleteFeedback = async (feedback: Feedback) => {
  if (!feedback.id) return
  
  try {
    await ElMessageBox.confirm(
      '이 피드백을 삭제하시겠습니까?',
      '삭제 확인',
      {
        confirmButtonText: '삭제',
        cancelButtonText: '취소',
        type: 'warning',
      }
    )
    
    await feedbackAPI.delete(feedback.id)
    ElMessage.success('피드백이 삭제되었습니다.')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('삭제에 실패했습니다.')
    }
  }
}

const goBack = () => {
  router.push('/tests')
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div>
    <!-- Header -->
    <el-card shadow="never" style="margin-bottom: 24px">
      <div style="display: flex; justify-content: space-between; align-items: center">
        <div>
          <h1 style="margin: 0; font-size: 28px; font-weight: 600; color: #303133; display: flex; align-items: center; gap: 12px">
            <el-icon size="32" color="#409eff">
              <ChatLineRound />
            </el-icon>
            피드백 관리
          </h1>
          <p style="margin: 8px 0 0; color: #909399">시험 ID: {{ testId }}</p>
        </div>
        <div style="display: flex; gap: 12px">
          <el-button type="primary" @click="openAddDialog" :icon="Plus">
            피드백 추가
          </el-button>
          <el-button @click="goBack" :icon="ArrowLeft">
            목록으로
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- Feedbacks List -->
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; align-items: center; gap: 8px">
          <el-icon color="#67c23a">
            <MessageBox />
          </el-icon>
          <span style="font-weight: 600">피드백 목록</span>
        </div>
      </template>
      
      <el-table 
        :data="feedbacks" 
        v-loading="loading"
        style="width: 100%"
        stripe
      >
        <el-table-column label="학생" min-width="120">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 8px">
              <el-avatar size="small" :icon="UserFilled" />
              <span style="font-weight: 500">{{ row.submission?.student?.name || '알 수 없음' }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="teacherName" label="선생님" width="120">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.teacherName }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="content" label="피드백 내용" min-width="300">
          <template #default="{ row }">
            <div style="background: #f8fafc; padding: 12px; border-radius: 6px; border-left: 4px solid #409eff">
              <el-text line-clamp="3">{{ row.content }}</el-text>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="작성일" width="120">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 8px">
              <el-icon color="#909399">
                <Calendar />
              </el-icon>
              <span style="font-size: 13px">
                {{ row.createdAt ? new Date(row.createdAt).toLocaleDateString('ko-KR') : '-' }}
              </span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="작업" width="100" align="center">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              circle
              @click="openEditDialog(row)"
            >
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button
              size="small"
              type="danger"
              circle
              @click="handleDeleteFeedback(row)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="feedbacks.length === 0" description="등록된 피드백이 없습니다">
        <template #image>
          <el-icon size="60" color="#c0c4cc">
            <ChatLineRound />
          </el-icon>
        </template>
      </el-empty>
    </el-card>

    <!-- Add/Edit Feedback Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="editMode ? '피드백 수정' : '새 피드백 추가'"
      width="600px"
      :before-close="() => dialogVisible = false"
    >
      <el-form 
        :model="newFeedback" 
        label-width="100px"
        label-position="left"
      >
        <el-form-item label="학생 선택" required>
          <el-select
            v-model="newFeedback.submissionId"
            placeholder="학생을 선택하세요"
            style="width: 100%"
            clearable
            :disabled="editMode"
          >
            <el-option
              v-for="submission in availableSubmissions"
              :key="submission.id"
              :label="`${submission.student?.name} (점수: ${submission.totalScore}점)`"
              :value="submission.id?.toString()"
            >
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span>{{ submission.student?.name }}</span>
                <el-tag size="small" :type="submission.totalScore >= 80 ? 'success' : submission.totalScore >= 60 ? 'warning' : 'danger'">
                  {{ submission.totalScore }}점
                </el-tag>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="선생님 이름" required>
          <el-input 
            v-model="newFeedback.teacherName" 
            placeholder="선생님 이름을 입력하세요"
            :prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item label="피드백 내용" required>
          <el-input 
            v-model="newFeedback.content" 
            placeholder="학생에게 전달할 피드백을 입력하세요"
            type="textarea"
            :rows="4"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span>
          <el-button @click="dialogVisible = false">취소</el-button>
          <el-button
            type="primary"
            @click="handleAddFeedback"
            :disabled="!newFeedback.submissionId || !newFeedback.teacherName || !newFeedback.content"
          >
            {{ editMode ? '수정' : '추가' }}
          </el-button>
        </span>
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
</style>