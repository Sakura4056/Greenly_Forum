<template>
  <div class="common-layout">
    <!-- Skip to main content link for accessibility -->
    <a href="#main-content" class="skip-link" aria-label="跳过导航到主要内容">跳过导航</a>
    
    <!-- Mobile sidebar overlay -->
    <div 
      v-if="isMobile && showMobileSidebar" 
      class="sidebar-overlay" 
      @click="showMobileSidebar = false"
      aria-hidden="true"
    ></div>
    
    <el-container class="h-full">
      <el-aside 
        :width="isSidebarCollapsed ? '64px' : '240px'" 
        class="app-sidebar" 
        :class="{ 'mobile-sidebar': isMobile, 'mobile-sidebar-open': isMobile && showMobileSidebar }"
        role="navigation"
        aria-label="主导航菜单"
      >
        <Sidebar :collapsed="isSidebarCollapsed && !isMobile" @toggle="isSidebarCollapsed = !isSidebarCollapsed" />
      </el-aside>
      
      <el-container class="is-vertical">
        <el-header class="app-header" role="banner">
          <div class="header-left">
            <el-button 
              type="primary" 
              plain 
              circle 
              size="small" 
              class="sidebar-toggle"
              @click="toggleSidebar"
              :icon="(isMobile ? showMobileSidebar : isSidebarCollapsed) ? 'Close' : 'Menu'"
              aria-label="切换侧边栏"
              aria-expanded="!isSidebarCollapsed && !isMobile"
            />
            <h2 class="page-title" aria-level="1">植物养护管理系统</h2>
          </div>
          <div class="header-right">
            <!-- 用户菜单 -->
            <div 
              class="user-profile" 
              @click="showUserMenu = !showUserMenu"
              @keydown.enter="showUserMenu = !showUserMenu"
              @keydown.space="showUserMenu = !showUserMenu"
              @keydown.esc="showUserMenu = false"
              role="button"
              tabindex="0"
              aria-haspopup="true"
              aria-expanded="showUserMenu"
              aria-label="用户菜单"
            >
              <el-avatar :size="32" class="user-avatar" icon="UserFilled" aria-hidden="true" />
              <span class="username">{{ userStore.username }}</span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              <div 
                v-if="showUserMenu" 
                class="user-dropdown"
                role="menu"
                aria-label="用户操作菜单"
              >
                <div 
                  class="dropdown-item" 
                  @click="navigateToProfile"
                  role="menuitem"
                  tabindex="-1"
                  aria-label="个人信息"
                >
                  <el-icon><User /></el-icon>
                  <span>个人信息</span>
                </div>
                <div 
                  class="dropdown-item" 
                  @click="handleLogout"
                  role="menuitem"
                  tabindex="-1"
                  aria-label="退出登录"
                >
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </div>
              </div>
            </div>
          </div>
        </el-header>
        
        <el-main class="app-main" id="main-content" role="main" aria-label="主要内容">
          <!-- Enhanced route transition with more natural animation -->
          <router-view v-slot="{ Component }">
            <transition name="route-transition" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import Sidebar from './components/Sidebar.vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ArrowDown, User, SwitchButton } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
const isSidebarCollapsed = ref(false)
const showUserMenu = ref(false)
const isMobile = ref(false)
const showMobileSidebar = ref(false)

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

const navigateToProfile = () => {
  router.push('/user/update')
  showUserMenu.value = false
}

const toggleSidebar = () => {
  if (isMobile.value) {
    showMobileSidebar.value = !showMobileSidebar.value
  } else {
    isSidebarCollapsed.value = !isSidebarCollapsed.value
  }
}

const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
  if (isMobile.value) {
    showMobileSidebar.value = false
  }
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped lang="scss">
.common-layout {
  height: 100vh;
  width: 100vw;
  background-color: var(--color-background);
  display: flex;
  overflow: hidden;
}

.h-full {
  height: 100%;
  width: 100%;
}

.app-sidebar {
  background-color: var(--color-surface);
  box-shadow: var(--shadow-sm);
  z-index: var(--z-index-fixed);
  transition: width var(--transition-normal), box-shadow var(--transition-normal);
  overflow-x: hidden;
  flex-shrink: 0;
  
  &:hover {
    box-shadow: var(--shadow-md);
  }
  
  &.mobile-sidebar {
    position: fixed;
    left: -240px;
    top: 0;
    height: 100vh;
    z-index: var(--z-index-modal);
    transition: left var(--transition-normal);
    box-shadow: var(--shadow-xl);
    
    &.mobile-sidebar-open {
      left: 0;
    }
  }
}

.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: var(--z-index-modal-backdrop);
  animation: fade-in 0.3s ease;
}

@keyframes fade-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.app-header {
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0,0,0,0.05);
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-lg);
  box-shadow: var(--shadow-sm);
  z-index: var(--z-index-sticky);
  transition: box-shadow var(--transition-normal);
  
  &:hover {
    box-shadow: var(--shadow-md);
  }
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  
  .sidebar-toggle {
    transition: all var(--transition-fast);
    
    &:hover {
      transform: scale(1.05);
    }
  }
  
  .page-title {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: var(--color-text-main);
    transition: all var(--transition-normal);
  }
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  margin: 0 40px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);

  /* 用户菜单 */
  .user-profile {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    padding: 6px 12px;
    background: rgba(0,0,0,0.03);
    border-radius: 24px;
    cursor: pointer;
    transition: all var(--transition-fast);
    position: relative;
    z-index: var(--z-index-dropdown);
    
    &:hover {
      background: rgba(0,0,0,0.06);
      transform: translateY(-1px);
    }
    
    .username {
      font-size: 14px;
      font-weight: 500;
      color: var(--color-text-main);
      transition: color var(--transition-fast);
    }
    
    .user-avatar {
      background-color: var(--color-primary);
      transition: transform var(--transition-fast);
      
      &:hover {
        transform: scale(1.1);
      }
    }
    
    .dropdown-icon {
      font-size: 12px;
      transition: transform var(--transition-fast);
      
      .is-active & {
        transform: rotate(180deg);
      }
    }
    
    .user-dropdown {
      position: absolute;
      top: 100%;
      right: 0;
      margin-top: 8px;
      background: var(--color-surface);
      border-radius: var(--border-radius-base);
      box-shadow: var(--shadow-lg);
      min-width: 160px;
      padding: 8px 0;
      z-index: var(--z-index-dropdown);
      animation: dropdown-fade-in 0.2s ease-out;
      
      .dropdown-item {
        display: flex;
        align-items: center;
        gap: var(--spacing-sm);
        padding: 10px 16px;
        font-size: 14px;
        color: var(--color-text-main);
        transition: all var(--transition-fast);
        cursor: pointer;
        
        &:hover {
          background-color: var(--color-background);
          color: var(--color-primary);
          padding-left: 20px;
        }
        
        el-icon {
          font-size: 16px;
        }
      }
    }
  }
}

.app-main {
  padding: var(--spacing-lg);
  overflow-y: auto;
  position: relative;
  flex: 1;
  transition: padding var(--transition-normal);
}

/* Enhanced Route Transition Styles */
.route-transition-enter-active,
.route-transition-leave-active {
  transition: all var(--transition-normal);
}

.route-transition-enter-from {
  opacity: 0;
  transform: translateY(10px) scale(0.98);
}

.route-transition-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(1.02);
}

/* Animations */
@keyframes dropdown-fade-in {
  from {
    opacity: 0;
    transform: translateY(-10px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* Responsive Adjustments */
@media (max-width: 768px) {
  .app-header {
    padding: 0 var(--spacing-md);
  }
  
  .header-left .page-title {
    font-size: 16px;
  }
  
  .app-main {
    padding: var(--spacing-md);
  }
}
</style>
