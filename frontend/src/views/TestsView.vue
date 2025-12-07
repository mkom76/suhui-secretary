<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { testAPI, type Test } from '../api/client'

const router = useRouter()
const loading = ref(false)
const tests = ref<Test[]>([])
const searchQuery = ref('')
const dialogVisible = ref(false)
const editMode = ref(false)
const currentTest = ref<Test>({ title: '' })

const tableData = computed(() => {
  if (!searchQuery.value) return tests.value
  return tests.value.filter(test =>
    (test.title || '').toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

const fetchTests = async () => {
  loading.value = true
  try {
    const response = await testAPI.getTests()
    tests.value = response.data.content || response.data
  } catch (error) {
    ElMessage.error('시험 목록을 불러오는데 실패했습니다.')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  editMode.value = false
  currentTest.value = { title: '' }
  dialogVisible.value = true
}

const openEditDialog = (test: Test) => {
  editMode.value = true
  currentTest.value = { ...test }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (editMode.value && currentTest.value.id) {
      await testAPI.updateTest(currentTest.value.id, currentTest.value)
      ElMessage.success('시험 정보가 수정되었습니다.')
    } else {
      await testAPI.createTest(currentTest.value)
      ElMessage.success('시험이 추가되었습니다.')
    }
    dialogVisible.value = false
    fetchTests()
  } catch (error) {
    ElMessage.error('작업을 완료할 수 없습니다.')
  }
}

const handleDelete = async (test: Test) => {
  if (!test.id) return
  
  try {
    await ElMessageBox.confirm(
      `${test.title} 시험을 삭제하시겠습니까?`,
      '삭제 확인',
      {
        confirmButtonText: '삭제',
        cancelButtonText: '취소',
        type: 'warning',
      }
    )
    
    await testAPI.deleteTest(test.id)
    ElMessage.success('시험이 삭제되었습니다.')
    fetchTests()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('삭제에 실패했습니다.')
    }
  }
}

const navigateToAnswers = (testId: number) => {
  router.push(`/tests/${testId}/answers`)
}

const navigateToFeedback = (testId: number) => {
  router.push(`/tests/${testId}/feedback`)
}

const navigateToDetail = (testId: number) => {
  router.push(`/tests/${testId}`)
}

onMounted(() => {
  fetchTests()
})
</script>

<template>
  <div>
    <!-- Header -->
    <el-card shadow="never" style="margin-bottom: 24px">
      <div style="display: flex; justify-content: space-between; align-items: center">
        <div>
          <h1 style="margin: 0; font-size: 28px; font-weight: 600; color: #303133; display: flex; align-items: center; gap: 12px">
            <el-icon size="32" color="#67c23a">
              <Document />
            </el-icon>
            시험 관리
          </h1>
          <p style="margin: 8px 0 0; color: #909399">시험을 생성하고 관리합니다</p>
        </div>
        <el-button type="primary" @click="openAddDialog" :icon="Plus" size="large">
          시험 추가
        </el-button>
      </div>
    </el-card>

    <!-- Search and Filters -->
    <el-card shadow="never" style="margin-bottom: 24px">
      <el-row :gutter="16" align="middle">
        <el-col :span="8">
          <el-input
            v-model="searchQuery"
            placeholder="시험명으로 검색"
            :prefix-icon="Search"
            clearable
            size="large"
          />
        </el-col>
        <el-col :span="4">
          <el-button @click="fetchTests" :icon="Refresh" size="large">
            새로고침
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- Tests Table -->
    <el-card shadow="never">
      <el-table 
        :data="tableData" 
        v-loading="loading"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="title" label="시험명" min-width="180">
          <template #default="{ row }">
            <div
              style="display: flex; align-items: center; gap: 8px; cursor: pointer"
              @click="navigateToDetail(row.id)"
            >
              <el-icon color="#67c23a">
                <Document />
              </el-icon>
              <span style="font-weight: 500; color: #409eff">{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="questionCount" label="문제 수" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.questionCount || 0 }}문제</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="등록일" width="120">
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
        
        <el-table-column label="관리" width="180" fixed="right">
          <template #default="{ row }">
            <el-tooltip content="정답 관리" placement="top">
              <el-button
                size="small"
                type="warning"
                circle
                @click="navigateToAnswers(row.id)"
              >
                <el-icon><EditPen /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="피드백" placement="top">
              <el-button
                size="small"
                type="info"
                circle
                @click="navigateToFeedback(row.id)"
              >
                <el-icon><ChatLineRound /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="수정" placement="top">
              <el-button
                size="small"
                type="primary"
                circle
                @click="openEditDialog(row)"
              >
                <el-icon><Edit /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="삭제" placement="top">
              <el-button
                size="small"
                type="danger"
                circle
                @click="handleDelete(row)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog 
      v-model="dialogVisible"
      :title="editMode ? '시험 정보 수정' : '새 시험 추가'"
      width="600px"
      :before-close="() => dialogVisible = false"
    >
      <el-form
        :model="currentTest"
        label-width="100px"
        label-position="left"
      >
        <el-form-item label="시험명" required>
          <el-input
            v-model="currentTest.title"
            placeholder="시험명을 입력하세요"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span>
          <el-button @click="dialogVisible = false">취소</el-button>
          <el-button
            type="primary"
            @click="handleSubmit"
            :disabled="!currentTest.title"
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

.el-button-group .el-button {
  margin: 0 2px;
}
</style>