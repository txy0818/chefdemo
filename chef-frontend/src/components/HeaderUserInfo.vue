<template>
  <button type="button" class="header-user-info" @click="$emit('click')">
    <el-avatar :size="40" :src="avatarUrl" />
    <span class="header-user-name">{{ userName }}</span>
  </button>
</template>

<script setup>
import { computed } from 'vue'
import defaultAvatar from '@/assets/default-avatar.svg'

defineEmits(['click'])

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
  padding: 6px 10px 6px 6px;
  border: 1px solid rgba(64, 158, 255, 0.12);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: background-color 220ms ease, border-color 220ms ease, transform 220ms ease;
}

.header-user-info:hover {
  background: rgba(64, 158, 255, 0.08);
  border-color: rgba(64, 158, 255, 0.28);
  transform: translateY(-1px);
}

.header-user-info:focus-visible {
  outline: 2px solid #409eff;
  outline-offset: 2px;
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
