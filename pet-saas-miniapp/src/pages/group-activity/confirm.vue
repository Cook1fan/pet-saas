<template>
  <view class="group-confirm">
    <!-- 商品信息 -->
    <view class="goods-section">
      <image
        :src="activityDetail.goodsImage"
        class="goods-image"
        mode="aspectFill"
      />
      <view class="goods-info">
        <text class="goods-title">{{ activityDetail.title }}</text>
        <view class="goods-spec">
          <text class="spec-label">数量</text>
          <view class="quantity-control">
            <view class="quantity-btn" @click="decreaseQuantity">
              <text>-</text>
            </view>
            <text class="quantity-value">{{ orderNum }}</text>
            <view class="quantity-btn" @click="increaseQuantity">
              <text>+</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 拼团类型 -->
    <view class="type-section">
      <view class="type-item">
        <text class="type-label">拼团类型</text>
        <text class="type-value">{{ groupId ? '加入拼团' : '发起拼团' }}</text>
      </view>
      <view v-if="groupId" class="type-item">
        <text class="type-label">团信息</text>
        <text class="type-value">{{ groupId }}</text>
      </view>
    </view>

    <!-- 金额信息 -->
    <view class="price-section">
      <view class="price-item">
        <text class="price-label">商品金额</text>
        <text class="price-value">¥{{ (activityDetail.price * orderNum).toFixed(2) }}</text>
      </view>
      <view class="price-item">
        <text class="price-label">优惠金额</text>
        <text class="price-value success">-¥{{ ((activityDetail.originPrice - activityDetail.price) * orderNum).toFixed(2) }}</text>
      </view>
      <view class="price-divider"></view>
      <view class="price-item total">
        <text class="price-label">实付金额</text>
        <text class="price-value">¥{{ (activityDetail.price * orderNum).toFixed(2) }}</text>
      </view>
    </view>

    <!-- 支付提示 -->
    <view class="payment-tips">
      <text class="tips-text">温馨提示：支付后请尽快分享给好友，完成拼团</text>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-action-bar">
      <view class="price-info">
        <text class="total-label">实付</text>
        <text class="total-price">¥{{ (activityDetail.price * orderNum).toFixed(2) }}</text>
      </view>
      <view
        class="submit-btn"
        :class="{ disabled: submitting }"
        @click="handleSubmit"
      >
        <text class="submit-text">{{ submitting ? '提交中...' : '立即支付' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
  getGroupActivityDetail,
  launchGroup,
  joinGroup,
  GroupActivityDetail
} from '@/api/group-activity'

const activityId = ref(0)
const groupId = ref<number | undefined>()

onLoad((options) => {
  if (options.activityId) {
    activityId.value = Number(options.activityId)
  }
  if (options.groupId) {
    groupId.value = Number(options.groupId)
  }
  fetchData()
})

const activityDetail = ref<GroupActivityDetail>({
  id: 0,
  title: '',
  goodsImage: '',
  goodsName: '',
  price: 0,
  originPrice: 0,
  groupCount: 0,
  remainStock: 0,
  endTime: '',
  goodsId: 0,
  goodsImages: [],
  limitNum: 0,
  userLimitRemain: 0,
  ongoingGroups: []
})

const orderNum = ref(1)
const submitting = ref(false)

// 减少数量
const decreaseQuantity = () => {
  if (orderNum.value > 1) {
    orderNum.value--
  }
}

// 增加数量
const increaseQuantity = () => {
  const maxLimit = activityDetail.value.limitNum > 0 ? activityDetail.value.limitNum : 5
  if (orderNum.value < maxLimit) {
    orderNum.value++
  }
}

// 处理图片加载错误
const handleImageError = (e: any) => {
  const imgElement = e.target as HTMLImageElement
  imgElement.src = '/static/images/default-placeholder.png'
}

const fetchData = async () => {
  try {
    const res = await getGroupActivityDetail(activityId.value)
    activityDetail.value = res.data
  } catch (error) {
    console.error('获取拼团活动详情失败:', error)
    uni.showToast({
      title: '获取活动详情失败',
      icon: 'none'
    })
  }
}

const goToSuccessPage = (orderId: number) => {
  uni.redirectTo({
    url: `/pages/group-activity/success?orderId=${orderId}&groupId=${groupId.value}&isJoinGroup=${!!groupId.value}`
  })
}

const handleSubmit = async () => {
  if (submitting.value) return
  submitting.value = true

  try {
    let res
    if (groupId.value) {
      // 加入拼团
      res = await joinGroup({
        activityId: activityId.value,
        groupId: groupId.value,
        num: orderNum.value
      })
    } else {
      // 发起拼团
      res = await launchGroup({
        activityId: activityId.value,
        num: orderNum.value
      })
    }

    // 调起微信支付或使用测试模式
    try {
      await requestWechatPayment(res.data.wechatPayParams)
    } catch (payError) {
      if (payError === 'cancel') {
        // 用户取消，不跳转
        return
      }
      // 支付失败，使用测试模式直接跳转成功页（仅用于测试）
      uni.showModal({
        title: '提示',
        content: '是否使用测试模式直接完成支付？',
        success: (modalRes) => {
          if (modalRes.confirm) {
            goToSuccessPage(res.data.orderId)
          }
        }
      })
      throw payError
    }

    // 支付成功，跳转到成功页
    goToSuccessPage(res.data.orderId)
  } catch (error) {
    console.error('提交订单失败:', error)
    if (error !== 'cancel') {
      uni.showToast({
        title: '订单提交失败',
        icon: 'none'
      })
    }
  } finally {
    submitting.value = false
  }
}

const requestWechatPayment = (params: any) => {
  return new Promise<void>((resolve, reject) => {
    uni.requestPayment({
      timeStamp: params.timeStamp,
      nonceStr: params.nonceStr,
      package: params.package,
      signType: params.signType,
      paySign: params.paySign,
      success: () => {
        resolve()
      },
      fail: (err) => {
        if (err.errMsg.includes('cancel')) {
          reject('cancel')
        } else {
          reject(err)
        }
      }
    })
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.group-confirm {
  min-height: 100vh;
  background: #f5f5f5;
}

.goods-section {
  display: flex;
  padding: 30rpx;
  background: #fff;
  margin-bottom: 20rpx;

  .goods-image {
    width: 160rpx;
    height: 160rpx;
    border-radius: 8rpx;
    background: #f5f5f5;
    flex-shrink: 0;
  }

  .goods-info {
    flex: 1;
    margin-left: 20rpx;
    display: flex;
    flex-direction: column;
    justify-content: space-between;

    .goods-title {
      font-size: 30rpx;
      color: #333;
      font-weight: 500;
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 2;
      overflow: hidden;
    }

    .goods-spec {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .spec-label {
        font-size: 26rpx;
        color: #999;
      }

      .quantity-control {
        display: flex;
        align-items: center;
        gap: 20rpx;

        .quantity-btn {
          width: 44rpx;
          height: 44rpx;
          border-radius: 50%;
          background: #f5f5f5;
          display: flex;
          align-items: center;
          justify-content: center;

          text {
            font-size: 32rpx;
            color: #333;
            font-weight: 500;
          }
        }

        .quantity-value {
          font-size: 28rpx;
          color: #333;
          min-width: 40rpx;
          text-align: center;
        }
      }
    }
  }
}

.type-section {
  padding: 0 30rpx;
  background: #fff;
  margin-bottom: 20rpx;

  .type-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 0;
    border-bottom: 1rpx solid #f5f5f5;

    &:last-child {
      border-bottom: none;
    }

    .type-label {
      font-size: 28rpx;
      color: #666;
    }

    .type-value {
      font-size: 28rpx;
      color: #333;
    }
  }
}

.price-section {
  padding: 30rpx;
  background: #fff;
  margin-bottom: 20rpx;

  .price-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;

    &.total {
      margin-bottom: 0;
    }

    .price-label {
      font-size: 28rpx;
      color: #666;
    }

    .price-value {
      font-size: 28rpx;
      color: #333;

      &.success {
        color: #52c41a;
      }
    }
  }

  .price-divider {
    height: 1rpx;
    background: #f5f5f5;
    margin: 24rpx 0;
  }

  .total {
    .price-label {
      font-size: 32rpx;
      font-weight: bold;
    }

    .price-value {
      font-size: 36rpx;
      font-weight: bold;
      color: #ff4d4f;
    }
  }
}

.payment-tips {
  padding: 24rpx 30rpx;
  background: #fffbe6;
  margin: 0 20rpx 140rpx;
  border-radius: 8rpx;

  .tips-text {
    font-size: 24rpx;
    color: #999;
    line-height: 1.6;
  }
}

.bottom-action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background: #fff;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
  z-index: 100;

  .price-info {
    display: flex;
    align-items: baseline;
    gap: 8rpx;

    .total-label {
      font-size: 28rpx;
      color: #666;
    }

    .total-price {
      font-size: 40rpx;
      font-weight: bold;
      color: #ff4d4f;
    }
  }

  .submit-btn {
    padding: 20rpx 60rpx;
    background: #ff4d4f;
    border-radius: 40rpx;

    &.disabled {
      opacity: 0.6;
    }

    .submit-text {
      font-size: 32rpx;
      font-weight: bold;
      color: #fff;
    }
  }
}
</style>
