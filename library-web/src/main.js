import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'
import './router/guard'
import './styles/variables.css'
import './styles/global.css'
import './styles/seat-map.css'

const app = createApp(App)

// 注册 Pinia / Router / Element Plus（中文语言包）
app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
