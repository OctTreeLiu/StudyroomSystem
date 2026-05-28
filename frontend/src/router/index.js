import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores'
import { ElMessage } from 'element-plus'
import { getCurrentUserInfo } from '@/api/user'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/announcements',
    name: 'Announcements',
    component: () => import('@/views/AnnouncementList.vue'),
    meta: { title: '系统公告' }
  },
  {
    path: '/contact',
    name: 'Contact',
    component: () => import('@/views/Contact.vue'),
    meta: { title: '联系我们' }
  },
  {
    path: '/user',
    name: 'User',
    component: () => import('@/views/user/UserLayout.vue'),
    meta: { title: '用户中心', requiresAuth: true, requiresRole: 0 },
    children: [
      {
        path: '',
        name: 'EntryTicket',
        component: () => import('@/views/user/EntryTicket.vue'),
        meta: { title: '入场凭证' }
      },
      {
        path: 'info',
        name: 'UserHome',
        component: () => import('@/views/user/UserHome.vue'),
        meta: { title: '个人信息' }
      },
      {
        path: 'studyroom',
        name: 'StudyRoomList',
        component: () => import('@/views/user/StudyRoomList.vue'),
        meta: { title: '自习室列表' }
      },
      {
        path: 'announcement',
        name: 'AnnouncementList',
        component: () => import('@/views/user/AnnouncementList.vue'),
        meta: { title: '系统公告' }
      },
      {
        path: 'reservation',
        name: 'ReservationList',
        component: () => import('@/views/user/ReservationList.vue'),
        meta: { title: '我的预约' }
      },
      {
        path: 'reservation/create',
        name: 'ReservationCreate',
        component: () => import('@/views/user/ReservationCreate.vue'),
        meta: { title: '新建预约' }
      },
      {
        path: 'lease',
        name: 'LeaseList',
        component: () => import('@/views/user/LeaseList.vue'),
        meta: { title: '长期租赁' }
      },
      {
        path: 'lease/apply',
        name: 'LeaseApply',
        component: () => import('@/views/user/LeaseApply.vue'),
        meta: { title: '长期租赁申请' }
      },
      {
        path: 'usage',
        name: 'UsageRecordList',
        component: () => import('@/views/user/UsageRecordList.vue'),
        meta: { title: '使用记录' }
      },
      {
        path: 'message-board',
        name: 'MessageBoard',
        component: () => import('@/views/user/MessageBoard.vue'),
        meta: { title: '留言板' }
      },
      {
        path: 'study-time',
        name: 'StudyTimeStatistics',
        component: () => import('@/views/user/StudyTimeStatistics.vue'),
        meta: { title: '学习时长统计' }
      },
      {
        path: 'study-ranking',
        name: 'StudyRanking',
        component: () => import('@/views/user/StudyRanking.vue'),
        meta: { title: '学习红人榜' }
      },
      {
        path: 'points',
        name: 'PointsCenter',
        component: () => import('@/views/user/PointsCenter.vue'),
        meta: { title: '积分中心' }
      },
      {
        path: 'points/history',
        name: 'PointsHistory',
        component: () => import('@/views/user/PointsHistory.vue'),
        meta: { title: '积分流水' }
      },
      {
        path: 'points/exchange',
        name: 'PointsExchange',
        component: () => import('@/views/user/PointsExchange.vue'),
        meta: { title: '积分兑换预约' }
      },
      {
        path: 'member',
        name: 'MemberCenter',
        component: () => import('@/views/user/MemberCenter.vue'),
        meta: { title: '会员中心' }
      },
      {
        path: 'memo',
        name: 'MemoManagement',
        component: () => import('@/views/user/MemoManagement.vue'),
        meta: { title: '备忘录' }
      },
      {
        path: 'extra-charge',
        name: 'ExtraChargeManagement',
        component: () => import('@/views/user/ExtraChargeManagement.vue'),
        meta: { title: '额外收费' }
      },
      {
        path: 'ai-agent',
        name: 'GuangAgent',
        component: () => import('@/views/user/GuangAgent.vue'),
        meta: { title: '小光智能体' }
      }
    ]
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { title: '管理员后台', requiresAuth: true, requiresRole: 1 },
    children: [
      {
        path: '',
        name: 'AdminHome',
        component: () => import('@/views/admin/AdminHome.vue'),
        meta: { title: '管理后台' }
      },
      {
        path: 'user',
        name: 'AdminUserManagement',
        component: () => import('@/views/admin/UserManagement.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'studyroom',
        name: 'AdminStudyRoomManagement',
        component: () => import('@/views/user/StudyRoomList.vue'),
        meta: { title: '自习室管理' }
      },
      {
        path: 'seat',
        name: 'AdminSeatManagement',
        component: () => import('@/views/admin/SeatManagement.vue'),
        meta: { title: '座位管理' }
      },
      {
        path: 'seat/:roomId',
        name: 'AdminSeatDetailManagement',
        component: () => import('@/views/admin/SeatDetailManagement.vue'),
        meta: { title: '座位详情管理' }
      },
      {
        path: 'reservation',
        name: 'AdminReservationManagement',
        component: () => import('@/views/admin/ReservationManagement.vue'),
        meta: { title: '预约管理' }
      },
      {
        path: 'lease',
        name: 'AdminLeaseAudit',
        component: () => import('@/views/admin/LeaseAudit.vue'),
        meta: { title: '长期租赁情况' }
      },
      {
        path: 'announcement',
        name: 'AdminAnnouncementManagement',
        component: () => import('@/views/admin/AnnouncementManagement.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'statistics',
        name: 'AdminStatistics',
        component: () => import('@/views/admin/Statistics.vue'),
        meta: { title: '统计分析' }
      },
      {
        path: 'call',
        name: 'AdminCallManagement',
        component: () => import('@/views/admin/AdminCallManagement.vue'),
        meta: { title: '呼叫记录管理' }
      },
      {
        path: 'study-time',
        name: 'AdminStudyTimeStatistics',
        component: () => import('@/views/admin/AdminStudyTimeStatistics.vue'),
        meta: { title: '学习时长统计' }
      },
      {
        path: 'message-board',
        name: 'AdminMessageBoardManagement',
        component: () => import('@/views/admin/MessageBoardManagement.vue'),
        meta: { title: '留言板管理' }
      },
      {
        path: 'points',
        name: 'AdminPointsManagement',
        component: () => import('@/views/admin/AdminPointsManagement.vue'),
        meta: { title: '积分管理' }
      },
      {
        path: 'price-config',
        name: 'AdminPriceConfigManagement',
        component: () => import('@/views/admin/PriceConfigManagement.vue'),
        meta: { title: '定价管理' }
      },
      {
        path: 'memo',
        name: 'AdminMemoManagement',
        component: () => import('@/views/user/MemoManagement.vue'),
        meta: { title: '备忘录' }
      },
      {
        path: 'extra-charge',
        name: 'AdminExtraChargeManagement',
        component: () => import('@/views/admin/ExtraChargeManagement.vue'),
        meta: { title: '额外收费管理' }
      },
      {
        path: 'user-modify-log',
        name: 'AdminUserModifyLogManagement',
        component: () => import('@/views/admin/UserModifyLogManagement.vue'),
        meta: { title: '修改日志' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  // 如果 state 中没有 token，尝试从存储兜底恢复（应对支付回跳丢失 window.name 的情况）
  if (!userStore.token) {
    const storedToken = sessionStorage.getItem('token') || localStorage.getItem('token')
    if (storedToken) {
      userStore.setToken(storedToken)
    }
  }
  
  // 如果路由需要认证
  if (to.meta.requiresAuth) {
    if (!userStore.isLogin) {
      // 未登录，跳转到登录页
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }

    // 若已登录但用户信息为空，尝试拉取，确保角色可用（回跳后常见）
    if (!userStore.userInfo) {
      try {
        const res = await getCurrentUserInfo()
        if (res.code === 200 && res.data) {
          // 确保role字段存在
          if (res.data.role === undefined || res.data.role === null) {
            res.data.role = 0
          }
          userStore.setUserInfo(res.data)
        } else {
          throw new Error(res.message || '获取用户信息失败')
        }
      } catch (e) {
        // 拉取失败则视为未登录
        console.error('获取用户信息失败:', e)
        userStore.logout()
        next({ path: '/login', query: { redirect: to.fullPath } })
        return
      }
    }
    
    // 检查角色权限
    if (to.meta.requiresRole !== undefined) {
      const userRole = userStore.userInfo?.role
      const requiredRole = to.meta.requiresRole
      
      // 如果role为null或undefined，默认为0（普通用户）
      const actualRole = userRole !== null && userRole !== undefined ? userRole : 0
      
      if (actualRole !== requiredRole) {
        // 角色不匹配，跳转到首页
        ElMessage.warning('您没有权限访问该页面')
        next('/')
        return
      }
    }
  }
  
  // 如果已登录用户访问登录页，重定向到对应页面（避免循环）
  if (to.path === '/login' && userStore.isLogin) {
    // 如果有用户信息，直接重定向
    if (userStore.userInfo) {
      const targetPath = userStore.userInfo.role === 1 ? '/admin' : '/user'
      next(targetPath)
      return
    }
    // 如果没有用户信息但有token，尝试获取用户信息
    if (userStore.token) {
      try {
        const res = await getCurrentUserInfo()
        if (res.code === 200 && res.data) {
          if (res.data.role === undefined || res.data.role === null) {
            res.data.role = 0
          }
          userStore.setUserInfo(res.data)
          const targetPath = res.data.role === 1 ? '/admin' : '/user'
          next(targetPath)
          return
        }
      } catch (e) {
        // 获取失败，清除token，允许访问登录页
        userStore.logout()
      }
    }
  }
  
  next()
})

export default router

