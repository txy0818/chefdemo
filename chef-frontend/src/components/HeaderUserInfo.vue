<template>
  <div class="header-user-info">
    <el-avatar :size="40" :src="avatarUrl" />
    <span class="header-user-name">{{ userName }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import defaultAvatar from '@/assets/default-avatar.svg'

const props = defineProps({
  userName: {
    type: String,
    default: ''
  },
  avatar: {
    type: String,
    default: ''
  }
})

const avatarUrl = computed(() => props.avatar || defaultAvatar)
</script>

<style scoped>
.header-user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.header-user-name {
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  color: #303133;
}
</style>
