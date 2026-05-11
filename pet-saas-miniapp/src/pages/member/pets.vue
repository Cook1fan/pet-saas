<template>
  <view class="pets-container">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @tap="goBack">‹</view>
      <view class="nav-title">我的宠物</view>
      <view class="nav-add" @tap="showAddModal">➕</view>
    </view>

    <!-- 宠物列表 -->
    <view class="pets-list" v-if="petsList.length > 0">
      <view class="pet-card" v-for="pet in petsList" :key="pet.id">
        <image class="pet-avatar" :src="pet.avatar || '/static/default-pet.png'" mode="aspectFill" />
        <view class="pet-info">
          <view class="pet-name">
            <text>{{ pet.name }}</text>
            <text class="pet-gender" :class="pet.gender">
              {{ pet.gender === 'male' ? '♂' : '♀' }}
            </text>
          </view>
          <view class="pet-breed">{{ pet.breed }}</view>
        </view>
        <view class="pet-menu" @tap="showPetMenu(pet)">⋯</view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-else>
      <text class="empty-icon">🐱</text>
      <text class="empty-text">暂无宠物</text>
      <view class="add-btn" @tap="showAddModal">添加宠物</view>
    </view>

    <!-- 添加宠物弹窗 -->
    <view class="modal" v-if="showModal" @tap="closeModal">
      <view class="modal-content" @tap.stop>
        <view class="modal-header">
          <text class="modal-title">{{ isEdit ? '编辑宠物' : '添加宠物' }}</text>
          <text class="modal-close" @tap="closeModal">✕</text>
        </view>
        <view class="modal-body">
          <!-- 头像 -->
          <view class="form-item avatar-item">
            <view class="avatar-preview" @tap="chooseAvatar">
              <image class="preview-img" :src="formData.avatar || '/static/default-pet.png'" mode="aspectFill" />
              <view class="avatar-tip">点击上传</view>
            </view>
          </view>
          <!-- 名字 -->
          <view class="form-item">
            <text class="form-label">名字</text>
            <input class="form-input" v-model="formData.name" placeholder="请输入宠物名字" />
          </view>
          <!-- 品种 -->
          <view class="form-item">
            <text class="form-label">品种</text>
            <input class="form-input" v-model="formData.breed" placeholder="请输入品种" />
          </view>
          <!-- 性别 -->
          <view class="form-item">
            <text class="form-label">性别</text>
            <view class="gender-options">
              <view
                class="gender-option"
                :class="{ active: formData.gender === 'male' }"
                @tap="formData.gender = 'male'"
              >
                ♂ 公
              </view>
              <view
                class="gender-option"
                :class="{ active: formData.gender === 'female' }"
                @tap="formData.gender = 'female'"
              >
                ♀ 母
              </view>
            </view>
          </view>
          <!-- 生日 -->
          <view class="form-item">
            <text class="form-label">生日</text>
            <picker mode="date" :value="formData.birthday" @change="onBirthdayChange">
              <view class="picker-text">
                {{ formData.birthday || '请选择生日' }}
              </view>
            </picker>
          </view>
        </view>
        <view class="modal-footer">
          <view class="btn secondary" @tap="closeModal">取消</view>
          <view class="btn primary" @tap="handleSave">保存</view>
        </view>
      </view>
    </view>

    <!-- 宠物操作菜单 -->
    <view class="action-sheet" v-if="showActionSheet" @tap="showActionSheet = false">
      <view class="action-sheet-content" @tap.stop>
        <view class="action-item" @tap="handleEditPet">编辑</view>
        <view class="action-item danger" @tap="handleDeletePet">删除</view>
        <view class="action-item cancel" @tap="showActionSheet = false">取消</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, onLoad, onShow } from 'vue'
import { getMemberPets } from '@/api/index'
import type { PetInfo } from '@/types'

const petsList = ref<PetInfo[]>([])
const showModal = ref(false)
const showActionSheet = ref(false)
const isEdit = ref(false)
const currentPet = ref<PetInfo | null>(null)

const formData = reactive<{
  id?: number
  name: string
  breed: string
  gender: 'male' | 'female'
  birthday?: string
  avatar?: string
}>({
  name: '',
  breed: '',
  gender: 'male'
})

onLoad(() => {})

onShow(() => {
  loadPetsList()
})

async function loadPetsList() {
  try {
    const res = await getMemberPets()
    if (res.code === 200 && res.data) {
      petsList.value = res.data
    }
  } catch (error) {
    console.error('Load pets list failed:', error)
  }
}

function goBack() {
  uni.navigateBack()
}

function showAddModal() {
  isEdit.value = false
  resetForm()
  showModal.value = true
}

function showPetMenu(pet: PetInfo) {
  currentPet.value = pet
  showActionSheet.value = true
}

function handleEditPet() {
  if (!currentPet.value) return
  showActionSheet.value = false
  isEdit.value = true
  formData.id = currentPet.value.id
  formData.name = currentPet.value.name
  formData.breed = currentPet.value.breed
  formData.gender = currentPet.value.gender
  formData.avatar = currentPet.value.avatar
  showModal.value = true
}

function handleDeletePet() {
  if (!currentPet.value) return
  showActionSheet.value = false
  uni.showModal({
    title: '提示',
    content: '确定要删除这只宠物吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({ title: '删除成功', icon: 'success' })
        loadPetsList()
      }
    }
  })
}

function resetForm() {
  formData.id = undefined
  formData.name = ''
  formData.breed = ''
  formData.gender = 'male'
  formData.birthday = ''
  formData.avatar = ''
}

function closeModal() {
  showModal.value = false
  resetForm()
}

function chooseAvatar() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      if (res.tempFilePaths.length > 0) {
        formData.avatar = res.tempFilePaths[0]
      }
    }
  })
}

function onBirthdayChange(e: any) {
  formData.birthday = e.detail.value
}

function handleSave() {
  if (!formData.name.trim()) {
    uni.showToast({ title: '请输入宠物名字', icon: 'none' })
    return
  }

  uni.showLoading({ title: '保存中...' })
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({ title: '保存成功', icon: 'success' })
    closeModal()
    loadPetsList()
  }, 1000)
}
</script>

<style lang="scss" scoped>
.pets-container {
  min-height: 100vh;
  background: #f5f5f5;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fff;
}

.nav-back {
  font-size: 24px;
  font-weight: 600;
  width: 30px;
}

.nav-title {
  font-size: 16px;
  font-weight: 600;
}

.nav-add {
  font-size: 24px;
  width: 30px;
  text-align: right;
}

.pets-list {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pet-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.pet-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #f0f0f0;
  flex-shrink: 0;
}

.pet-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.pet-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.pet-gender {
  font-size: 14px;
  margin-left: 6px;

  &.male {
    color: #1890ff;
  }

  &.female {
    color: #ff4d4f;
  }
}

.pet-breed {
  font-size: 13px;
  color: #999;
}

.pet-menu {
  font-size: 24px;
  color: #ccc;
  padding: 8px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 100px;
  gap: 16px;
}

.empty-icon {
  font-size: 60px;
}

.empty-text {
  font-size: 14px;
  color: #999;
}

.add-btn {
  padding: 10px 32px;
  background: linear-gradient(135deg, #ff4d4f, #ff6b6b);
  color: #fff;
  border-radius: 20px;
  font-size: 14px;
  margin-top: 8px;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  z-index: 1000;
}

.modal-content {
  width: 100%;
  background: #fff;
  border-radius: 16px 16px 0 0;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.modal-close {
  font-size: 20px;
  color: #999;
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px 16px;
}

.form-item {
  display: flex;
  align-items: center;
  margin-bottom: 20px;

  &.avatar-item {
    justify-content: center;
    margin-bottom: 24px;
  }
}

.form-label {
  width: 80px;
  font-size: 14px;
  color: #333;
}

.form-input {
  flex: 1;
  height: 40px;
  padding: 0 12px;
  background: #f5f5f5;
  border-radius: 8px;
  font-size: 14px;
}

.picker-text {
  flex: 1;
  height: 40px;
  padding: 0 12px;
  background: #f5f5f5;
  border-radius: 8px;
  font-size: 14px;
  display: flex;
  align-items: center;
  color: #666;
}

.avatar-preview {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
}

.preview-img {
  width: 100%;
  height: 100%;
  background: #f0f0f0;
}

.avatar-tip {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 30px;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.gender-options {
  flex: 1;
  display: flex;
  gap: 16px;
}

.gender-option {
  flex: 1;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  font-size: 14px;
  color: #666;

  &.active {
    border-color: #ff4d4f;
    background: #fff0f0;
    color: #ff4d4f;
  }
}

.modal-footer {
  display: flex;
  padding: 16px;
  gap: 16px;
  border-top: 1px solid #f0f0f0;
}

.btn {
  flex: 1;
  height: 44px;
  border-radius: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 600;

  &.secondary {
    background: #f5f5f5;
    color: #666;
  }

  &.primary {
    background: linear-gradient(135deg, #ff4d4f, #ff6b6b);
    color: #fff;
  }
}

.action-sheet {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  z-index: 1000;
}

.action-sheet-content {
  width: 100%;
  background: #fff;
  border-radius: 16px 16px 0 0;
}

.action-item {
  padding: 16px;
  text-align: center;
  font-size: 15px;
  border-bottom: 1px solid #f0f0f0;

  &.danger {
    color: #ff4d4f;
  }

  &.cancel {
    background: #f5f5f5;
    border-bottom: none;
  }
}
</style>
