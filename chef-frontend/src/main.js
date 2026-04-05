import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import { ElMessage } from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import router from './router'
import App from './App.vue'
import './style.css'

const DEFAULT_MESSAGE_DURATION = 1800

const patchMessage = (type) => {
  const original = ElMessage[type]
  ElMessage[type] = (options = {}, appContext) => {
    if (typeof options === 'string') {
      return original({ message: options, duration: DEFAULT_MESSAGE_DURATION }, appContext)
    }
    return original({ duration: DEFAULT_MESSAGE_DURATION, ...options }, appContext)
  }
}

;['success', 'warning', 'info', 'error'].forEach(patchMessage)

const app = createApp(App)
const pinia = createPinia()

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)

app.mount('#app')
