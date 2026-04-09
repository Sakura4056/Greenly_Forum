<template>
  <el-menu
    :default-active="activeMenu"
    class="sidebar-menu"
    :collapse="collapsed"
    router
    :collapse-transition="false"
  >
    <div class="logo-container" :class="{ 'collapsed': collapsed }">
      <span class="logo-text" v-show="!collapsed">Greenly</span>
      <span class="logo-text-collapsed" v-show="collapsed">G</span>
    </div>
    
    <template v-for="route in menuRoutes" :key="route.path">
      <el-sub-menu v-if="route.children && route.children.length > 1" :index="route.path">
        <template #title>
          <el-icon v-if="route.meta && route.meta.icon" :size="collapsed ? 20 : 18">
            <component :is="route.meta.icon" />
          </el-icon>
          <span v-show="!collapsed">{{ route.meta?.title || '' }}</span>
        </template>
        
        <el-menu-item 
          v-for="child in route.children.filter(c => !c.meta?.hidden)" 
          :key="child.path" 
          :index="resolvePath(route.path, child.path)"
        >
          <span v-show="!collapsed">{{ child.meta?.title || '' }}</span>
        </el-menu-item>
      </el-sub-menu>

      <el-menu-item 
        v-else-if="route.children && route.children.length === 1" 
        :index="resolvePath(route.path, route.children[0].path)"
      >
        <el-icon v-if="route.children[0].meta && route.children[0].meta.icon" :size="collapsed ? 20 : 18">
          <component :is="route.children[0].meta.icon" />
        </el-icon>
        <template #title v-show="!collapsed">{{ route.children[0].meta?.title || '' }}</template>
      </el-menu-item>
      
       <el-menu-item 
        v-else-if="!route.children"
        :index="route.path"
      >
         <el-icon v-if="route.meta && route.meta.icon" :size="collapsed ? 20 : 18">
          <component :is="route.meta.icon" />
        </el-icon>
        <template #title v-show="!collapsed">{{ route.meta?.title || '' }}</template>
      </el-menu-item>
    </template>
  </el-menu>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toggle'])

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const menuRoutes = computed(() => {
  return router.options.routes.filter(r => {
    if (r.meta && r.meta.hidden) return false
    // Handle roles if needed
    if (r.meta && r.meta.roles && !r.meta.roles.includes(userStore.role)) return false
    return true
  })
})

const resolvePath = (basePath, routePath) => {
  if (basePath === '/') return '/' + routePath
  if (routePath.startsWith('/')) return routePath // Handle absolute paths in children
  // Ensure we don't double slash
  const base = basePath.endsWith('/') ? basePath : basePath + '/'
  const path = routePath.startsWith('/') ? routePath.slice(1) : routePath
  return (base + path).replace(/\/+/g, '/')
}
</script>

<style scoped lang="scss">
.sidebar-menu {
  border-right: none;
  height: 100%;
  background-color: var(--color-surface);
  transition: all var(--transition-normal);
  
  &:not(.el-menu--collapse) {
    width: 240px;
  }
  
  &.el-menu--collapse {
    width: 64px;
  }
}

.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  color: white;
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
    transition: left var(--transition-slow);
  }
  
  &:hover::before {
    left: 100%;
  }
  
  &.collapsed {
    justify-content: center;
  }
  
  .logo-text {
    font-size: 24px;
    font-weight: 700;
    letter-spacing: 1px;
    font-family: 'Inter', sans-serif;
    transition: all var(--transition-normal);
    white-space: nowrap;
  }
  
  .logo-text-collapsed {
    font-size: 20px;
    font-weight: 700;
    transition: all var(--transition-normal);
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

/* Menu Item Styling Override */
:deep(.el-menu-item), :deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
  margin: 4px 8px;
  border-radius: 8px;
  color: var(--color-text-main);
  transition: all var(--transition-fast);
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(46, 204, 113, 0.1), transparent);
    transition: left var(--transition-slow);
  }
  
  &:hover {
    background-color: var(--el-color-primary-light-9);
    color: var(--color-primary);
    
    &::before {
      left: 100%;
    }
  }
  
  &.is-active {
    background-color: var(--el-color-primary-light-9);
    color: var(--color-primary);
    font-weight: 600;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      height: 24px;
      width: 4px;
      background-color: var(--color-primary);
      border-radius: 0 4px 4px 0;
    }
  }
}

:deep(.el-menu) {
  background-color: transparent; 
}

:deep(.el-sub-menu .el-menu) {
  background-color: transparent !important;
  
  .el-menu-item {
    padding-left: 40px !important;
    
    &:hover {
      padding-left: 44px !important;
    }
  }
}

:deep(.el-menu--collapse .el-sub-menu .el-menu) {
  min-width: 200px;
  left: 100%;
  top: 0;
  background-color: var(--color-surface) !important;
  box-shadow: var(--shadow-lg);
  border-radius: 0 var(--border-radius-base) var(--border-radius-base) 0;
  margin-top: 0 !important;
  
  .el-menu-item {
    padding-left: 16px !important;
    
    &:hover {
      padding-left: 20px !important;
    }
  }
}
</style>
