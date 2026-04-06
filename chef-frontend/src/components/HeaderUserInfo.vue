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
  padding: 6px 14px 6px 6px;
  border: 1px solid rgba(220, 38, 38, 0.10);
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(255, 245, 241, 0.88));
  box-shadow: 0 10px 20px rgba(127, 29, 29, 0.08);
  cursor: pointer;
  transition: background-color 220ms ease, border-color 220ms ease, transform 220ms ease, box-shadow 220ms ease;
}

.header-user-info:hover {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(255, 239, 233, 0.94));
  border-color: rgba(220, 38, 38, 0.18);
  box-shadow: 0 16px 28px rgba(127, 29, 29, 0.12);
  transform: translateY(-2px);
}

.header-user-info:focus-visible {
  outline: 2px solid #dc2626;
  outline-offset: 2px;
}

.header-user-name {
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  font-weight: 600;
  color: #4b2d2d;
}
</style>
