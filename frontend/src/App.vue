<template>
  <router-view />
  <CallAdminButton />
</template>

<script setup>
import CallAdminButton from '@/components/CallAdminButton.vue'
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body {
  width: 100%;
  max-width: 100%;
  overflow-x: hidden;
}

#app {
  width: 100%;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 移动端：表格内容允许在自身区域横向滚动，避免撑开页面导致右侧留白 */
@media (max-width: 768px) {
  /* 强制布局容器铺满屏幕，避免 max-width + margin 居中导致右侧留白 */
  :global(.user-layout),
  :global(.admin-layout) {
    width: 100vw !important;
    max-width: 100vw !important;
  }

  :global(.header-content),
  :global(.main-content) {
    width: 100% !important;
    max-width: 100% !important;
    margin-left: 0 !important;
    margin-right: 0 !important;
  }

  :global(.el-table) {
    max-width: 100%;
  }

  :global(.el-table__body-wrapper) {
    overflow-x: auto;
  }
}

/* 只显示时间面板中未禁用的分钟项（配合禁用规则，只保留 00/15/30/45） */
:global(.el-time-spinner__list.is-minute .el-time-spinner__item.is-disabled) {
  display: none !important;
  height: 0 !important;
  margin: 0 !important;
  padding: 0 !important;
  overflow: hidden !important;
}

/* 滚动条 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>

