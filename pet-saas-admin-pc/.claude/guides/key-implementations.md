# 关键实现

## 请求封装

```typescript
// utils/request.ts
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.satoken = userStore.token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const { code, message, data } = response.data
    if (code === 200) {
      return data
    } else {
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message))
    }
  },
  (error) => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      window.location.href = '/shop/login'
    }
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
```

## 表格组件示例

```vue
<template>
  <el-table :data="tableData" v-loading="loading">
    <el-table-column prop="name" label="姓名" />
    <el-table-column label="操作" fixed="right" width="200">
      <template #default="{ row }">
        <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
        <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
  <el-pagination
    v-model:current-page="query.page"
    v-model:page-size="query.pageSize"
    :total="total"
    @current-change="fetchData"
  />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getMemberList, type Member, type MemberQuery } from '@/api/member'

const loading = ref(false)
const tableData = ref<Member[]>([])
const total = ref(0)

const query = reactive<MemberQuery>({
  page: 1,
  pageSize: 10
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getMemberList(query)
    tableData.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>
```
