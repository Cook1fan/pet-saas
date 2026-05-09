<template>
  <div class="member-import">
    <el-page-header @back="goBack" content="批量导入会员" />

    <el-card class="mt-md">
      <!-- 导入步骤 -->
      <el-steps :active="currentStep" finish-status="success" simple style="margin-bottom: 24px">
        <el-step title="下载模板" />
        <el-step title="填写数据" />
        <el-step title="上传文件" />
        <el-step title="确认导入" />
      </el-steps>

      <!-- 步骤1: 下载模板 -->
      <div v-if="currentStep >= 0" class="step-content">
        <el-alert
          title="下载导入模板"
          type="info"
          description="请先下载模板，按照模板格式填写会员信息"
          show-icon
          style="margin-bottom: 24px"
        />
        <div class="template-info">
          <h4>模板说明：</h4>
          <ul>
            <li>支持 .xlsx 和 .xls 格式</li>
            <li>标有 * 的列为必填项</li>
            <li>一个会员可以添加多只宠物</li>
            <li>性别请填写：公/母</li>
            <li>日期格式请填写：yyyy-MM-dd（如：2024-01-01）</li>
          </ul>
        </div>
        <el-button type="primary" size="large" @click="downloadTemplate">
          <el-icon><Download /></el-icon>
          下载导入模板
        </el-button>
        <el-button size="large" @click="currentStep = 2" style="margin-left: 16px">
          已有模板，下一步
        </el-button>
      </div>

      <!-- 步骤2-3: 上传文件 -->
      <div v-if="currentStep === 2" class="step-content">
        <el-upload
          class="upload-demo"
          drag
          :auto-upload="false"
          :on-change="handleFileChange"
          :limit="1"
          accept=".xlsx,.xls"
          :show-file-list="false"
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              只能上传 .xlsx / .xls 文件
            </div>
          </template>
        </el-upload>
        <div v-if="selectedFile" class="selected-file">
          <el-icon><Document /></el-icon>
          <span>{{ selectedFile.name }}</span>
          <el-button type="danger" link @click="clearFile">移除</el-button>
        </div>
        <div class="step-buttons" style="margin-top: 24px">
          <el-button @click="currentStep = 0">上一步</el-button>
          <el-button type="primary" @click="loadPreviewData" :disabled="!selectedFile" :loading="previewLoading">
            预览数据
          </el-button>
        </div>
      </div>

      <!-- 步骤4: 数据预览 -->
      <div v-if="currentStep === 3" class="step-content">
        <el-alert
          title="请确认导入数据"
          type="warning"
          :description="previewDescription"
          show-icon
          style="margin-bottom: 16px"
        />
        <div class="preview-table-wrapper">
          <el-table :data="previewData" border stripe style="width: 100%" max-height="400">
            <el-table-column type="index" label="序号" width="60" fixed />
            <el-table-column prop="phone" label="手机号*" width="120">
              <template #default="{ row }">
                <span :class="{ 'error-text': row._error }">{{ row.phone }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="姓名*" width="100">
              <template #default="{ row }">
                <span :class="{ 'error-text': row._error }">{{ row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="tags" label="标签" width="150" />
            <el-table-column prop="pet1Name" label="宠物1名称" width="100" />
            <el-table-column prop="pet1Breed" label="宠物1品种" width="100" />
            <el-table-column prop="pet1Gender" label="宠物1性别" width="90" />
            <el-table-column prop="pet1Birthday" label="宠物1生日" width="110" />
            <el-table-column label="错误信息" min-width="150">
              <template #default="{ row }">
                <el-tag v-if="row._error" type="danger" size="small">
                  {{ row._errorMsg }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div class="step-buttons" style="margin-top: 16px">
          <el-button @click="currentStep = 2">上一步</el-button>
          <el-button type="primary" @click="handleImport" :disabled="errorData.length > 0" :loading="importLoading">
            确认导入
          </el-button>
        </div>
      </div>

      <!-- 导入结果 -->
      <div v-if="currentStep === 4" class="step-content">
        <el-result icon="success" title="导入完成">
            <template #sub-title>
              <div class="import-result">
                <p>成功：<span class="success-count">{{ importResult.success }}</span> 条</p>
                <p>失败：<span class="error-count">{{ importResult.fail }}</span> 条</p>
              </div>
            </template>
            <template #extra>
              <el-button type="primary" @click="goToList">返回会员列表</el-button>
              <el-button @click="resetImport">继续导入</el-button>
            </template>
          </el-result>
          <div v-if="importResult.failDetails?.length" class="fail-details">
            <h4>失败详情：</h4>
            <el-table :data="importResult.failDetails" border stripe size="small">
              <el-table-column prop="phone" label="手机号" width="120" />
              <el-table-column prop="name" label="姓名" width="100" />
              <el-table-column prop="errorMsg" label="失败原因" />
            </el-table>
            <el-button type="primary" text style="margin-top: 8px" @click="downloadFailData">
              <el-icon><Download /></el-icon>
              下载失败数据
            </el-button>
          </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Download, UploadFilled, Document } from '@element-plus/icons-vue'

const router = useRouter()

const currentStep = ref(0)
const selectedFile = ref<File | null>(null)
const previewLoading = ref(false)
const importLoading = ref(false)
const previewData = ref<any[]>([])
const importResult = ref({ success: 0, fail: 0, failDetails: [] as any[] })

const errorData = computed(() => previewData.value.filter(row => row._error))

const previewDescription = computed(() => `共 ${previewData.value.length} 条数据，其中 ${errorData.value.length} 条错误`)

function goBack() {
  router.back()
}

function goToList() {
  router.push('/shop/member/list')
}

function downloadTemplate() {
  // TODO: 实现模板下载
  ElMessage.info('模板下载功能待实现')
}

function handleFileChange(file: any) {
  selectedFile.value = file.raw
}

function clearFile() {
  selectedFile.value = null
}

async function loadPreviewData() {
  if (!selectedFile.value) return
  previewLoading.value = true
  try {
    // TODO: 调用后端接口解析文件，获取预览数据
    // 模拟预览数据
    await new Promise(resolve => setTimeout(resolve, 1000))

    previewData.value = [
      {
        phone: '13800138001',
        name: '张三',
        tags: 'VIP客户',
        pet1Name: '咪咪',
        pet1Breed: '英短',
        pet1Gender: '母',
        pet1Birthday: '2023-01-01',
        _error: false
      },
      {
        phone: '13800138002',
        name: '',
        tags: '',
        pet1Name: '',
        pet1Breed: '',
        pet1Gender: '',
        pet1Birthday: '',
        _error: true,
        _errorMsg: '姓名不能为空'
      }
    ]

    currentStep.value = 3
  } catch (e) {
    ElMessage.error('解析文件失败')
  } finally {
    previewLoading.value = false
  }
}

async function handleImport() {
  importLoading.value = true
  try {
    // TODO: 调用后端接口导入数据
    // 模拟导入结果
    await new Promise(resolve => setTimeout(resolve, 1500))

    importResult.value = {
      success: 1,
      fail: 1,
      failDetails: [
        {
          phone: '13800138002',
          name: '',
          errorMsg: '姓名不能为空'
        }
      ]
    }

    currentStep.value = 4
  } catch (e) {
    ElMessage.error('导入失败')
  } finally {
    importLoading.value = false
  }
}

function resetImport() {
  currentStep.value = 0
  selectedFile.value = null
  previewData.value = []
  importResult.value = { success: 0, fail: 0, failDetails: [] }
}

function downloadFailData() {
  // TODO: 实现下载失败数据
  ElMessage.info('下载失败数据功能待实现')
}
</script>

<style scoped lang="scss">
.member-import {
  .mt-md {
    margin-top: 16px;
  }

  .step-content {
    padding: 20px 0;
  }

  .template-info {
    background: #f5f7fa;
    padding: 16px;
    border-radius: 4px;
    margin-bottom: 16px;

    h4 {
      margin: 0 0 8px 0;
    }

    ul {
      margin: 0;
      padding-left: 20px;

      li {
        margin: 4px 0;
      }
    }
  }

  .selected-file {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 16px;
    padding: 12px;
    background: #f0f9eb;
    border-radius: 4px;
  }

  .preview-table-wrapper {
    overflow-x: auto;
  }

  .error-text {
    color: #f56c6c;
  }

  .step-buttons {
    display: flex;
    gap: 12px;
  }

  .import-result {
    p {
      margin: 8px 0;
      font-size: 16px;

      .success-count {
        color: #67c23a;
        font-weight: bold;
      }

      .error-count {
        color: #f56c6c;
        font-weight: bold;
      }
    }
  }

  .fail-details {
    margin-top: 24px;

    h4 {
      margin: 0 0 12px 0;
    }
  }
}
</style>
