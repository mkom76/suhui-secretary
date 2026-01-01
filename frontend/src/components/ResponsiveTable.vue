<script setup lang="ts">
import { useBreakpoint } from '@/composables/useBreakpoint'

interface Column {
  prop: string
  label: string
  width?: string
  minWidth?: string
  align?: 'left' | 'center' | 'right'
  fixed?: boolean | 'left' | 'right'
  formatter?: (row: any) => string
  slot?: string
}

const props = defineProps<{
  data: any[]
  columns: Column[]
  loading?: boolean
}>()

const { isMobile } = useBreakpoint()
</script>

<template>
  <!-- Desktop: Standard table -->
  <el-table
    v-if="!isMobile"
    :data="data"
    :loading="loading"
    style="width: 100%"
    stripe
  >
    <el-table-column
      v-for="col in columns"
      :key="col.prop"
      :prop="col.prop"
      :label="col.label"
      :width="col.width"
      :min-width="col.minWidth"
      :align="col.align"
      :fixed="col.fixed"
    >
      <template v-if="col.slot" #default="scope">
        <slot :name="col.slot" :row="scope.row" />
      </template>
      <template v-else #default="scope">
        <span>{{ col.formatter ? col.formatter(scope.row) : scope.row[col.prop] }}</span>
      </template>
    </el-table-column>
  </el-table>

  <!-- Mobile: Card layout -->
  <div v-else class="mobile-cards">
    <el-card
      v-for="(row, index) in data"
      :key="index"
      shadow="hover"
      class="mobile-card"
    >
      <div
        v-for="col in columns"
        :key="col.prop"
        class="mobile-card-row"
      >
        <div class="mobile-card-label">{{ col.label }}</div>
        <div class="mobile-card-value">
          <slot v-if="col.slot" :name="col.slot" :row="row" />
          <span v-else>{{ col.formatter ? col.formatter(row) : row[col.prop] }}</span>
        </div>
      </div>
    </el-card>
    <el-empty v-if="data.length === 0" description="데이터가 없습니다" />
  </div>
</template>

<style scoped>
.mobile-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mobile-card {
  border-radius: 8px;
}

.mobile-card-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.mobile-card-row:last-child {
  border-bottom: none;
}

.mobile-card-label {
  font-weight: 600;
  color: #606266;
  font-size: 14px;
}

.mobile-card-value {
  color: #303133;
  font-size: 14px;
  text-align: right;
}
</style>
