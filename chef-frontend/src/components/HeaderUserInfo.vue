<template>
  <div class="header-user-info">
    <div class="avatar-ring">
      <el-avatar :size="42" :src="avatarUrl" />
    </div>
    <div class="header-user-meta">
      <span class="header-user-label">当前账户</span>
      <span class="header-user-name">{{ userName }}</span>
    </div>
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
  gap: 12px;
  min-width: 0;
}

.avatar-ring {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 3px;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(248, 113, 113, 0.85), rgba(161, 98, 7, 0.72));
  box-shadow: 0 10px 24px rgba(161, 98, 7, 0.18);
}

.header-user-meta {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.header-user-label {
  font-size: 11px;
  line-height: 1.2;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #a17862;
}

.header-user-name {
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  font-weight: 700;
  color: #3f1111;
}
</style>
