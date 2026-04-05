import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  // 管理员模块
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/admin/Layout.vue'),
    meta: { requiresAuth: true, role: 1 },
    children: [
      {
        path: 'chef-list',
        name: 'AdminChefList',
        component: () => import('@/views/admin/ChefList.vue')
      },
      {
        path: 'user-list',
        name: 'AdminUserList',
        component: () => import('@/views/admin/UserList.vue')
      },
      {
        path: 'chef-audit',
        name: 'AdminChefAudit',
        component: () => import('@/views/admin/ChefAudit.vue')
      },
      {
        path: 'review-list',
        name: 'AdminReviewList',
        component: () => import('@/views/admin/ReviewList.vue')
      },
      {
        path: 'report-list',
        name: 'AdminReportList',
        component: () => import('@/views/admin/ReportList.vue')
      },
      {
        path: 'order-list',
        name: 'AdminOrderList',
        component: () => import('@/views/admin/OrderList.vue')
      },
      {
        path: 'statistics',
        name: 'AdminStatistics',
        component: () => import('@/views/admin/Statistics.vue')
      }
    ]
  },
  // 厨师模块
  {
    path: '/chef',
    name: 'Chef',
    component: () => import('@/views/chef/Layout.vue'),
    meta: { requiresAuth: true, role: 2 },
    children: [
      {
        path: 'account',
        name: 'ChefAccount',
        component: () => import('@/views/chef/Account.vue')
      },
      {
        path: 'profile',
        name: 'ChefProfile',
        component: () => import('@/views/chef/Profile.vue')
      },
      {
        path: 'time-manage',
        name: 'ChefTimeManage',
        component: () => import('@/views/chef/TimeManage.vue')
      },
      {
        path: 'order-manage',
        name: 'ChefOrderManage',
        component: () => import('@/views/chef/OrderManage.vue')
      },
      {
        path: 'notifications',
        name: 'ChefNotifications',
        component: () => import('@/views/chef/Notifications.vue')
      }
    ]
  },
  // 用户模块
  {
    path: '/user',
    name: 'User',
    component: () => import('@/views/user/Layout.vue'),
    meta: { requiresAuth: true, role: 3 },
    children: [
      {
        path: 'chef-search',
        name: 'UserChefSearch',
        component: () => import('@/views/user/ChefSearch.vue')
      },
      {
        path: 'chef-detail/:id',
        name: 'UserChefDetail',
        component: () => import('@/views/user/ChefDetail.vue')
      },
      {
        path: 'create-order/:chefId',
        name: 'UserCreateOrder',
        component: () => import('@/views/user/CreateOrder.vue')
      },
      {
        path: 'order-list',
        name: 'UserOrderList',
        component: () => import('@/views/user/OrderList.vue')
      },
      {
        path: 'order-detail/:id',
        name: 'UserOrderDetail',
        component: () => import('@/views/user/OrderDetail.vue')
      },
      {
        path: 'review/:orderId',
        name: 'UserReview',
        component: () => import('@/views/user/Review.vue')
      },
      {
        path: 'report/:orderId',
        name: 'UserReport',
        component: () => import('@/views/user/Report.vue')
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/user/Profile.vue')
      },
      {
        path: 'notifications',
        name: 'UserNotifications',
        component: () => import('@/views/user/Notifications.vue')
      },
      {
        path: 'wallet',
        name: 'UserWallet',
        component: () => import('@/views/user/Wallet.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  console.log('路由守卫:', {
    to: to.path,
    from: from.path,
    requiresAuth: to.meta.requiresAuth,
    isLoggedIn: userStore.isLoggedIn(),
    userRole: userStore.getUserRole(),
    requiredRole: to.meta.role
  })
  
  // 需要登录的页面
  if (to.meta.requiresAuth) {
    if (!userStore.isLoggedIn()) {
      console.log('未登录，跳转到登录页')
      next('/login')
      return
    }
    
    // 检查角色权限
    if (to.meta.role && userStore.getUserRole() !== to.meta.role) {
      console.log('角色不匹配，跳转到首页')
      next('/')
      return
    }
  }
  
  console.log('路由守卫通过')
  next()
})

export default router
