<template>
  <view class="group-activity-detail">
    <!-- 商品轮播图 -->
    <swiper
      class="goods-swiper"
      :indicator-dots="true"
      :autoplay="true"
      :interval="3000"
      :duration="500"
      indicator-active-color="#ff4d4f"
    >
      <swiper-item
        v-for="(image, index) in activityDetail.goodsImages"
        :key="index"
      >
        <image
          :src="image"
          class="goods-image"
          mode="aspectFill"
        />
      </swiper-item>
    </swiper>

    <!-- 活动信息 -->
    <view class="activity-info">
      <text class="activity-title">{{ activityDetail.title }}</text>

      <view class="price-section">
        <text class="group-price">¥{{ activityDetail.price }}</text>
        <text class="origin-price">¥{{ activityDetail.originPrice }}</text>
      </view>

      <view class="activity-meta">
        <view class="meta-item">
          <text class="meta-label">成团人数</text>
          <text class="meta-value">{{ activityDetail.groupCount }}人团</text>
        </view>
        <view class="meta-item">
          <text class="meta-label">限购</text>
          <text class="meta-value">每人限买{{ activityDetail.limitNum }}件</text>
        </view>
        <view class="meta-item">
          <text class="meta-label">剩余库存</text>
          <text class="meta-value">仅剩{{ activityDetail.remainStock }}件</text>
        </view>
        <view class="meta-item">
          <text class="meta-label">结束时间</text>
          <text class="meta-value">{{ formatEndTime(activityDetail.endTime) }}</text>
        </view>
      </view>

      <!-- 可加入的拼团 -->
      <view v-if="activityDetail.ongoingGroups && activityDetail.ongoingGroups.length > 0" class="joinable-groups">
        <view class="joinable-groups-title">
          <text>可加入的拼团</text>
        </view>
        <view class="joinable-groups-list">
          <joinable-group-card
            v-for="group in activityDetail.ongoingGroups"
            :key="group.groupId"
            :group-data="group"
            :activity-id="activityDetail.id"
            @join="handleJoinGroup"
          />
        </view>
      </view>
    </view>

    <!-- 商品详情 -->
    <view class="goods-detail-section">
      <text class="section-title">商品详情</text>
      <view class="goods-detail-content">
        <!-- 这里可以是富文本内容 -->
        <text class="detail-text">商品详情描述...</text>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-action-bar">
      <view class="action-left">
        <view class="price-info">
          <text class="total-price">¥{{ activityDetail.price }}</text>
          <text class="price-label">起</text>
        </view>
      </view>
      <view class="action-right">
        <view class="launch-group-btn" @click="handleLaunchGroup">
          <text class="btn-text">发起拼团</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getGroupActivityDetail, GroupActivityDetail } from '@/api/group-activity'
import JoinableGroupCard from '@/components/joinable-group-card.vue'

const activityId = ref(0)

onLoad((options) => {
  if (options.activityId) {
    activityId.value = Number(options.activityId)
    fetchData()
  }
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

const formatEndTime = (timeStr: string) => {
  const date = new Date(timeStr)
  return date.toLocaleDateString() + ' ' + date.toLocaleTimeString()
}

const handleLaunchGroup = () => {
  uni.navigateTo({
    url: `/pages/group-activity/confirm?activityId=${activityId.value}`
  })
}

const handleJoinGroup = (data: { activityId: number; groupId: number }) => {
  uni.navigateTo({
    url: `/pages/group-activity/confirm?activityId=${data.activityId}&groupId=${data.groupId}`
  })
}

</script>

<style scoped lang="scss">
.group-activity-detail {
  min-height: 100vh;
  background: #f5f5f5;
}

.goods-swiper {
  height: 400rpx;
  width: 100%;

  .goods-image {
    width: 100%;
    height: 100%;
    background: #f5f5f5;
  }
}

.activity-info {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;

  .activity-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
    line-height: 1.4;
    margin-bottom: 24rpx;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
  }

  .price-section {
    display: flex;
    align-items: baseline;
    gap: 16rpx;
    margin-bottom: 24rpx;

    .group-price {
      font-size: 56rpx;
      font-weight: bold;
      color: #ff4d4f;
    }

    .origin-price {
      font-size: 28rpx;
      color: #999;
      text-decoration: line-through;
    }
  }

  .activity-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;
    padding: 24rpx 0;
    border-top: 1rpx solid #f5f5f5;
    margin-bottom: 24rpx;

    .meta-item {
      display: flex;
      flex-direction: column;
      gap: 4rpx;

      .meta-label {
        font-size: 24rpx;
        color: #999;
      }

      .meta-value {
        font-size: 28rpx;
        color: #333;
        font-weight: 500;
      }
    }
  }

  .joinable-groups {
    border-top: 1rpx solid #f5f5f5;

    .joinable-groups-title {
      padding: 20rpx 0;

      text {
        font-size: 28rpx;
        color: #333;
        font-weight: 500;
      }
    }
  }
}

.goods-detail-section {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 120rpx;

  .section-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 24rpx;
  }

  .goods-detail-content {
    .detail-text {
      font-size: 28rpx;
      color: #666;
      line-height: 1.6;
    }
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

  .action-left {
    flex: 1;

    .price-info {
      display: flex;
      align-items: baseline;
      gap: 4rpx;

      .total-price {
        font-size: 36rpx;
        font-weight: bold;
        color: #ff4d4f;
      }

      .price-label {
        font-size: 24rpx;
        color: #999;
      }
    }
  }

  .action-right {
    .launch-group-btn {
      padding: 20rpx 56rpx;
      background: #ff4d4f;
      border-radius: 40rpx;

      .btn-text {
        font-size: 32rpx;
        font-weight: bold;
        color: #fff;
      }
    }
  }
}
</style>
