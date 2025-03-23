import './assets/css/main.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import axios from "axios";

//还有Personal里面的要改
// axios.defaults.baseURL = 'http://localhost:8080'//设置全局的基础路径
axios.defaults.baseURL = 'https://www.fztestweb.top'
// const ShareLink='http://localhost:5173/shareReport/';
const ShareLink='https://www.fztestweb.top/shareReport/';
// const ScreenLink='http://localhost:5173/shareScreen/';
const ScreenLink='https://www.fztestweb.top/shareScreen/';
// const baseURL1='http://localhost:8080/';
const baseURL1='https://www.fztestweb.top/api/';


axios.defaults.withCredentials=true;
//后端基础url 之后在请求时只用填写路径 Axios会自动以该url为基础添加路径

const app = createApp(App)
app.config.globalProperties.$axios = axios
app.use(createPinia())
app.use(router).use(ElementPlus).use(Antd);
app.mount('#app')

export default ShareLink ;
export {baseURL1};
export {ScreenLink};
