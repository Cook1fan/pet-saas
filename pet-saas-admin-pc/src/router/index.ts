import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/platform/dashboard'
  },
  {
    path: '/platform/login',
    name: 'PlatformLogin',
    component: () => import('@/views/platform/login/index.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/platform',
    component: () => import('@/views/platform/layout/index.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: 'dashboard'
      },
      {
        path: 'dashboard',
        name: 'PlatformDashboard',
        component: () => import('@/views/platform/dashboard/index.vue')
      },
      {
        path: 'tenant/list',
        name: 'TenantList',
        component: () => import('@/views/platform/tenant/list.vue')
      }
    ]
  },
  {
    path: '/shop/login',
    name: 'ShopLogin',
    component: () => import('@/views/shop/login/index.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/shop',
    component: () => import('@/views/shop/layout/index.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: 'dashboard'
      },
      {
        path: 'dashboard',
        name: 'ShopDashboard',
        component: () => import('@/views/shop/dashboard/index.vue')
      },
      {
        path: 'config/info',
        name: 'ShopConfigInfo',
        component: () => import('@/views/shop/config/info.vue')
      },
      {
        path: 'config/staff',
        name: 'ShopStaff',
        component: () => import('@/views/shop/config/staff.vue')
      },
      {
        path: 'config/payment',
        name: 'ShopPayment',
        component: () => import('@/views/shop/config/payment.vue')
      },
      {
        path: 'member/list',
        name: 'MemberList',
        component: () => import('@/views/shop/member/list.vue')
      },
      {
        path: 'member/detail/:id',
        name: 'MemberDetail',
        component: () => import('@/views/shop/member/detail.vue')
      },
      {
        path: 'member/import',
        name: 'MemberImport',
        component: () => import('@/views/shop/member/import.vue')
      },
      {
        path: 'goods/list',
        name: 'GoodsList',
        component: () => import('@/views/shop/inventory/goods.vue')
      },
      {
        path: 'goods/create',
        name: 'GoodsCreate',
        component: () => import('@/views/shop/inventory/goods-create.vue')
      },
      {
        path: 'goods/edit/:id',
        name: 'GoodsEdit',
        component: () => import('@/views/shop/inventory/goods-edit.vue')
      },
      {
        path: 'goods/stock-record',
        name: 'GoodsStockRecord',
        component: () => import('@/views/shop/inventory/stock-record.vue')
      },
      // 兼容旧路由重定向
      {
        path: 'inventory/goods',
        redirect: '/shop/goods/list'
      },
      {
        path: 'inventory/goods-create',
        redirect: '/shop/goods/create'
      },
      {
        path: 'inventory/goods-edit/:id',
        redirect: '/shop/goods/edit/:id'
      },
      {
        path: 'inventory/stock-record',
        redirect: '/shop/goods/stock-record'
      },
      // 开单收银
      {
        path: 'cashier/index',
        name: 'Cashier',
        component: () => import('@/views/shop/cashier/index.vue')
      },
      // 订单管理
      {
        path: 'order/list',
        name: 'OrderList',
        component: () => import('@/views/shop/order/list.vue')
      },
      {
        path: 'order/detail/:id',
        name: 'OrderDetail',
        component: () => import('@/views/shop/order/detail.vue')
      },
      // 营销活动
      {
        path: 'marketing/group-buy',
        name: 'GroupBuy',
        component: () => import('@/views/shop/marketing/group-buy.vue'),
        meta: { title: '拼团活动', icon: 'shopping' }
      },
      {
        path: 'marketing/seckill',
        name: 'Seckill',
        component: () => import('@/views/shop/marketing/seckill.vue'),
        meta: { title: '秒杀活动', icon: 'trophy' }
      },
      // 二维码管理
      {
        path: 'qrcode',
        name: 'QrCode',
        component: () => import('@/views/shop/qrcode/index.vue'),
        meta: { title: '店铺二维码', icon: 'qrcode' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  console.log('路由跳转:', to.path, 'isLoggedIn:', userStore.isLoggedIn)

  if (to.meta.requiresAuth) {
    if (!userStore.isLoggedIn) {
      const isPlatform = to.path.startsWith('/platform')
      console.log('未登录，跳转到登录页:', isPlatform ? '/platform/login' : '/shop/login')
      next(isPlatform ? '/platform/login' : '/shop/login')
      return
    }
    // 登录时已经保存了用户信息，不需要再调用接口获取
  } else {
    // 已登录用户访问登录页时，直接跳转到对应 dashboard
    if (userStore.isLoggedIn) {
      if (to.path === '/platform/login') {
        console.log('已登录，跳转到平台 dashboard')
        next('/platform/dashboard')
        return
      }
      if (to.path === '/shop/login') {
        console.log('已登录，跳转到门店 dashboard')
        next('/shop/dashboard')
        return
      }
    }
  }
  next()
})

export default router
