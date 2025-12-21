<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { academyClassAPI, academyAPI, type AcademyClass, type Academy } from '../api/client'

const loading = ref(false)
const academyClasses = ref<AcademyClass[]>([])
const academies = ref<Academy[]>([])
const searchQuery = ref('')
const dialogVisible = ref(false)
const editMode = ref(false)
const currentClass = ref<AcademyClass>({ name: '', academyId: undefined })

const tableData = computed(() => {
  if (!searchQuery.value) return academyClasses.value
  return academyClasses.value.filter(cls =>
    (cls.name || '').toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    (cls.academyName || '').toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

const fetchAcademyClasses = async () => {
  loading.value = true
  try {
    const response = await academyClassAPI.getAcademyClasses()
    academyClasses.value = response.data.content || response.data
  } catch (error) {
    ElMessage.error('반 목록을 불러오는데 실패했습니다.')
  } finally {
    loading.value = false
  }
}

const fetchAcademies = async () => {
  try {
    const response = await academyAPI.getAcademies()
    academies.value = response.data.content || response.data
  } catch (error) {
    ElMessage.error('학원 목록을 불러오는데 실패했습니다.')
  }
}

const openAddDialog = () => {
  editMode.value = false
  currentClass.value = { name: '', academyId: undefined }
  dialogVisible.value = true
}

const openEditDialog = (cls: AcademyClass) => {
  editMode.value = true
  currentClass.value = { ...cls }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!currentClass.value.academyId) {
    ElMessage.error('학원을 선택해주세요.')
    return
  }

  try {
    if (editMode.value && currentClass.value.id) {
      await academyClassAPI.updateAcademyClass(currentClass.value.id, currentClass.value)
      ElMessage.success('반 정보가 수정되었습니다.')
    } else {
      await academyClassAPI.createAcademyClass(currentClass.value)
      ElMessage.success('반이 추가되었습니다.')
    }
    dialogVisible.value = false
    fetchAcademyClasses()
  } catch (error) {
    ElMessage.error('작업을 완료할 수 없습니다.')
  }
}

const handleDelete = async (cls: AcademyClass) => {
  if (!cls.id) return

  try {
    await ElMessageBox.confirm(
      `${cls.name} 반을 삭제하시겠습니까?`,
      '삭제 확인',
      {
        confirmButtonText: '삭제',
        cancelButtonText: '취소',
        type: 'warning',
      }
    )

    await academyClassAPI.deleteAcademyClass(cls.id)
    ElMessage.success('반이 삭제되었습니다.')
    fetchAcademyClasses()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('삭제에 실패했습니다.')
    }
  }
}

onMounted(() => {
  fetchAcademyClasses()
  fetchAcademies()
})
</script>

<template>
  <div class="container">
    <div class="header">
      <h1>반 관리</h1>
      <div class="controls">
        <el-input
          v-model="searchQuery"
          placeholder="반명 또는 학원명 검색"
          clearable
          style="width: 250px"
        />
        <el-button type="primary" @click="openAddDialog">반 추가</el-button>
      </div>
    </div>

    <el-table
      :data="tableData"
      v-loading="loading"
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="academyName" label="학원" width="200" />
      <el-table-column prop="name" label="반명" />
      <el-table-column label="작업" width="150" align="center">
        <template #default="{ row }">
          <el-button size="small" @click="openEditDialog(row)">수정</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">삭제</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="editMode ? '반 수정' : '반 추가'"
      width="500px"
    >
      <el-form :model="currentClass" label-width="100px">
        <el-form-item label="학원">
          <el-select
            v-model="currentClass.academyId"
            placeholder="학원을 선택하세요"
            style="width: 100%"
          >
            <el-option
              v-for="academy in academies"
              :key="academy.id"
              :label="academy.name"
              :value="academy.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="반명">
          <el-input v-model="currentClass.name" placeholder="반명을 입력하세요" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">취소</el-button>
        <el-button type="primary" @click="handleSubmit">
          {{ editMode ? '수정' : '추가' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.controls {
  display: flex;
  gap: 10px;
}

h1 {
  margin: 0;
  font-size: 24px;
}
</style>
