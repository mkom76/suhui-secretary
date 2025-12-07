<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { testAPI, type Question } from '../api/client'

const route = useRoute()
const router = useRouter()
const testId = route.params.id as string

const loading = ref(false)
const questions = ref<Question[]>([])
const dialogVisible = ref(false)
const questionCount = ref(10)
const editingRows = ref<Set<number>>(new Set())

const fetchQuestions = async () => {
  loading.value = true
  try {
    const response = await testAPI.getTestQuestions(Number(testId))
    questions.value = response.data.data || response.data
  } catch (error) {
    ElMessage.error('문제 목록을 불러오는데 실패했습니다.')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  questionCount.value = 10
  dialogVisible.value = true
}

const handleBulkAdd = () => {
  const existingMax = questions.value.length > 0
    ? Math.max(...questions.value.map(q => q.number || 0))
    : 0

  const newQuestions: Question[] = []
  for (let i = 1; i <= questionCount.value; i++) {
    newQuestions.push({
      number: existingMax + i,
      answer: ''
    })
  }

  questions.value.push(...newQuestions)
  dialogVisible.value = false
  ElMessage.success(`${questionCount.value}개의 문제가 추가되었습니다. 정답을 입력해주세요.`)
}

const handleUpdateAnswer = async (question: Question, newAnswer: string) => {
  if (!question.number) return

  try {
    if (question.id) {
      // 기존 문제 업데이트는 없으므로 삭제 후 추가
      await testAPI.deleteQuestion(question.id)
    }
    await testAPI.addQuestion(Number(testId), {
      number: question.number,
      answer: newAnswer
    })
    question.answer = newAnswer
    ElMessage.success('정답이 저장되었습니다.')
    fetchQuestions()
  } catch (error) {
    ElMessage.error('정답 저장에 실패했습니다.')
  }
}

const handleDeleteQuestion = async (question: Question, index: number) => {
  try {
    await ElMessageBox.confirm(
      `${question.number}번 문제를 삭제하시겠습니까?`,
      '삭제 확인',
      {
        confirmButtonText: '삭제',
        cancelButtonText: '취소',
        type: 'warning',
      }
    )

    if (question.id) {
      await testAPI.deleteQuestion(question.id)
      ElMessage.success('문제가 삭제되었습니다.')
      fetchQuestions()
    } else {
      // 아직 저장되지 않은 문제
      questions.value.splice(index, 1)
      ElMessage.success('문제가 제거되었습니다.')
    }
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
  fetchQuestions()
})
</script>

<template>
  <div>
    <!-- Header -->
    <el-card shadow="never" style="margin-bottom: 24px">
      <div style="display: flex; justify-content: space-between; align-items: center">
        <div>
          <h1 style="margin: 0; font-size: 28px; font-weight: 600; color: #303133; display: flex; align-items: center; gap: 12px">
            <el-icon size="32" color="#e6a23c">
              <EditPen />
            </el-icon>
            정답 관리
          </h1>
          <p style="margin: 8px 0 0; color: #909399">시험 ID: {{ testId }}</p>
        </div>
        <div style="display: flex; gap: 12px">
          <el-button type="primary" @click="openAddDialog">
            <el-icon><Plus /></el-icon>
            문제 추가
          </el-button>
          <el-button @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            목록으로
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- Questions List -->
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; align-items: center; gap: 8px">
          <el-icon color="#409eff">
            <List />
          </el-icon>
          <span style="font-weight: 600">문제 목록</span>
        </div>
      </template>
      
      <el-table
        :data="questions"
        v-loading="loading"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="number" label="문제 번호" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="large">{{ row.number }}번</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="answer" label="정답" min-width="200">
          <template #default="{ row, $index }">
            <el-input
              v-model="row.answer"
              placeholder="정답을 입력하세요"
              @blur="handleUpdateAnswer(row, row.answer)"
              @keyup.enter="handleUpdateAnswer(row, row.answer)"
            >
              <template #prepend>
                <el-icon color="#67c23a">
                  <Check />
                </el-icon>
              </template>
            </el-input>
          </template>
        </el-table-column>

        <el-table-column label="상태" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.id" type="success" size="small">저장됨</el-tag>
            <el-tag v-else type="warning" size="small">미저장</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="작업" width="80" align="center">
          <template #default="{ row, $index }">
            <el-button
              size="small"
              type="danger"
              circle
              @click="handleDeleteQuestion(row, $index)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="questions.length === 0" description="등록된 문제가 없습니다">
        <template #image>
          <el-icon size="60" color="#c0c4cc">
            <Document />
          </el-icon>
        </template>
      </el-empty>
    </el-card>

    <!-- Add Questions Dialog -->
    <el-dialog
      v-model="dialogVisible"
      title="문제 추가"
      width="400px"
    >
      <el-form label-width="120px">
        <el-form-item label="추가할 문제 수">
          <el-input-number
            v-model="questionCount"
            :min="1"
            :max="100"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span>
          <el-button @click="dialogVisible = false">취소</el-button>
          <el-button type="primary" @click="handleBulkAdd">
            추가
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