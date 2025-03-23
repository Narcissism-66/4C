<script setup>
import { ref, computed, watch, reactive, onMounted } from 'vue'
import axios from 'axios'

// 导入所有图表组件
import BarChart from '../components/ECharts/BarChart/BarChart1.vue'
import LineChart from '../components/ECharts/LineChart/LineChart1.vue'
import PieChart from '../components/ECharts/PieChart/PieChart1.vue'
import ScatterChart from '../components/ECharts/ScatterChart/ScatterChart1.vue'
import BoxPlotChart from '../components/ECharts/BoxPlotChart/BoxPlotChart1.vue'
import WordCloud from '@/components/ECharts/WordCloud/WordCloud1.vue'
import AreaChart from '../components/ECharts/AreaChart/AreaChart1.vue'
import RadarChart from '../components/ECharts/RadarChart/RadarChart1.vue'
import FunnelChart from '../components/ECharts/FunnelChart/FunnelChart1.vue'
import TreemapChart from '../components/ECharts/TreemapChart/TreemapChart1.vue'
import HeatMapChart from '../components/ECharts/HeatmapChart/HeatmapChart1.vue'
import DualAxisChart from '../components/ECharts/DualAxisChart/DualAxisChart1.vue'
import RelationShipChart from '@/components/ECharts/RelationShipChart/RelationShipChart1.vue'
import * as XLSX from 'xlsx'
import Papa from 'papaparse'
import { userUserStore } from '@/stores/UserStore.js'
import { baseURL1, ScreenLink } from '@/main.js'
import { get, post } from '@/net/index.js'
import { message } from 'ant-design-vue'
const [messageApi, contextHolder] = message.useMessage();
const userStore = userUserStore();

//--------------------------------------------------处理图表---------------------------------------------
// 区域定义
const regions = ref([
  { title: "左上", description: "左侧第一行", gridArea: "left1", chartData: null },
  { title: "左中", description: "左侧第二行", gridArea: "left2", chartData: null },
  { title: "左下", description: "左侧第三行", gridArea: "left3", chartData: null },
  { title: "中上", description: "中间上部（较小）", gridArea: "center-top", chartData: null },
  { title: "中下", description: "中间下部（较大）", gridArea: "center-bottom", chartData: null },
  { title: "右上", description: "右侧第一行", gridArea: "right1", chartData: null },
  { title: "右中", description: "右侧第二行", gridArea: "right2", chartData: null },
  { title: "右下", description: "右侧第三行", gridArea: "right3", chartData: null }
])
// 背景图片设置
const options = reactive({
  bg: userStore.user.bg,
})

const isModalOpen = ref(false)
const selectedChart = ref(null)
const currentRegion = ref(null)
const chartIcons = ref({
  LineChart: { label: '折线图', icon: '📈' },
  AreaChart: { label: '面积图', icon: '📉' },
  BarChart: { label: '柱状图', icon: '📊' },
  PieChart: { label: '饼图', icon: '🥧' },
  ScatterChart: { label: '散点图', icon: '🔵' },
  WordCloud: { label: '词云', icon: '☁️' },
  BoxPlotChart: { label: '箱型图', icon: '📦' },
  RadarChart: { label: '雷达图', icon: '🕸️' },
  FunnelChart: { label: '漏斗图', icon: '🧭' },
  TreemapChart: { label: '树图', icon: '🌳' },
  HeatMapChart: { label: '热力图', icon: '🔥' },
  DualAxisChart: { label: '双轴图', icon: '🌓' },
  RelationShipChart: { label: '关系图', icon: '🔗' }
})

const fileInputRef = ref(null)
const filename = ref('')
const uploadError = ref('')
const isLoading = ref(false)
const rawData = ref([])

// 打开模态框
const openChartSelector = (region) => {
  currentRegion.value = region;
  isModalOpen.value = true;
  selectedChart.value = null;
  rawData.value = [];
  filename.value = '';
  uploadError.value = '';
  if (fileInputRef.value) {
    fileInputRef.value.value = null;
  }
};

// 关闭模态框
const closeModal = () => {
  isModalOpen.value = false;
  selectedChart.value = null;
  if (fileInputRef.value) {
    fileInputRef.value.value = null;
  }
};

watch(isModalOpen, (newVal) => {
  if (newVal) {
    document.body.classList.add('overflow-hidden');
  } else {
    document.body.classList.remove('overflow-hidden');
  }
});

const selectChart = (chartKey) => {
  selectedChart.value = chartKey;
};

const chartComponents = {
  LineChart,
  AreaChart,
  BarChart,
  PieChart,
  ScatterChart,
  WordCloud,
  BoxPlotChart,
  RadarChart,
  FunnelChart,
  TreemapChart,
  HeatMapChart,
  DualAxisChart,
  RelationShipChart
};

const generateChart = () => {
  if (!selectedChart.value || !currentRegion.value) return;
  if (isValidData.value) {
    currentRegion.value.chart = selectedChart.value;
    currentRegion.value.chartData = rawData.value;

    // 更新 Screen 对象
    Screen.type = selectedChart.value;
    Screen.data = JSON.stringify(rawData.value); // 可根据需要调整数据格式
    Screen.position = currentRegion.value.gridArea;

    closeModal();

    // 保存到数据库（自动判断更新或添加）
    checkPosition();
  }
};


const getChartComponent = (chartKey) => {
  return chartComponents[chartKey] || null;
};

//--------------------------------------------------处理图表---------------------------------------------


//---------------------------------------------------格式验证--------------------------------------------

// 必需字段定义
const requiredFields = {
  LineChart: ['date', 'value'],
  AreaChart: ['date', 'value'],
  BarChart: ['name', 'value'],
  PieChart: ['name', 'value'],
  TreemapChart: ['name', 'value'],
  ScatterChart: ['x', 'y'],
  WordCloud: ['name', 'value'],
  BoxPlotChart: ['name', 'min', 'q1', 'median', 'q3', 'max', 'outliers'],
  RadarChart: ['category', 'indicator', 'value'],
  FunnelChart: ['stage', 'value'],
  HeatMapChart: ['x', 'y', 'value'],
  DualAxisChart: ['date', 'value1', 'value2', 'yaxis1', 'yaxis2'],
  RelationShipChart: ['type', 'id', 'name', 'symbolsize', 'category', 'source', 'target', 'value']
}

// 辅助函数：返回指定字段预期的数据类型
const getExpectedType = (field) => {
  if (selectedChart.value === 'HeatMapChart' && (field === 'x' || field === 'y')) {
    return 'string';
  }
  if (['date', 'name', 'indicator', 'category', 'stage', 'yaxis1', 'yaxis2'].includes(field)) {
    return 'string';
  }
  if (field === 'outliers') return 'string';
  return 'number';
}


// 针对关系图的部分数据验证
const hasValidRelationshipData = (data) => {
  let hasValidNode = false;
  let hasValidLink = false;
  data.forEach(record => {
    if (record.type === 'node') {
      const errors = [];
      if (typeof record.id !== 'number') {
        errors.push(`节点 id 应为数字，实际: ${typeof record.id}`);
      }
      if (typeof record.name !== 'string' || record.name.trim() === '') {
        errors.push(`节点 name 应为非空字符串，实际: ${record.name}`);
      }
      if (typeof record.symbolsize !== 'number') {
        errors.push(`节点 symbolsize 应为数字，实际: ${typeof record.symbolsize}`);
      }
      if (typeof record.category !== 'string' || record.category.trim() === '') {
        errors.push(`节点 category 应为非空字符串，实际: ${record.category}`);
      }
      if (errors.length === 0) {
        hasValidNode = true;
      }
    }
    if (record.type === 'link') {
      const errors = [];
      if (typeof record.source !== 'number') {
        errors.push(`链接 source 应为数字，实际: ${typeof record.source}`);
      }
      if (typeof record.target !== 'number') {
        errors.push(`链接 target 应为数字，实际: ${typeof record.target}`);
      }
      if (typeof record.value !== 'number') {
        errors.push(`链接 value 应为数字，实际: ${typeof record.value}`);
      }
      if (errors.length === 0) {
        hasValidLink = true;
      }
    }
  });
  return hasValidNode && hasValidLink;
}

//---------------------------------------------------格式验证--------------------------------------------

//------------------------------------------------错误信息---------------------------------------------------
const validationErrors = computed(() => {
  const errors = [];
  if (!selectedChart.value || !rawData.value.length) return errors;
  if (selectedChart.value === 'RelationShipChart') {
    rawData.value.forEach((record, i) => {
      if (record.type === 'node') {
        if (typeof record.id !== 'number') {
          errors.push(`记录 ${i+1} 节点: 字段 "id" 类型错误，预期: number，实际: ${typeof record.id}`);
        }
        if (typeof record.name !== 'string' || record.name.trim() === '') {
          errors.push(`记录 ${i+1} 节点: 缺少或空 "name"，必需: non-empty string`);
        }
        if (typeof record.symbolsize !== 'number') {
          errors.push(`记录 ${i+1} 节点: 字段 "symbolsize" 类型错误，预期: number，实际: ${typeof record.symbolsize}`);
        }
        if (typeof record.category !== 'string' || record.category.trim() === '') {
          errors.push(`记录 ${i+1} 节点: 缺少或空 "category"，必需: non-empty string`);
        }
      } else if (record.type === 'link') {
        if (typeof record.source !== 'number') {
          errors.push(`记录 ${i+1} 链接: 字段 "source" 类型错误，预期: number，实际: ${typeof record.source}`);
        }
        if (typeof record.target !== 'number') {
          errors.push(`记录 ${i+1} 链接: 字段 "target" 类型错误，预期: number，实际: ${typeof record.target}`);
        }
        if (typeof record.value !== 'number') {
          errors.push(`记录 ${i+1} 链接: 字段 "value" 类型错误，预期: number，实际: ${typeof record.value}`);
        }
      } else {
        errors.push(`记录 ${i+1}: 未识别的 type 值 "${record.type}"，请设置为 "node" 或 "link"`);
      }
    });
    if (!hasValidRelationshipData(rawData.value)) {
      errors.push("关系图错误：至少需要一条有效节点和一条有效链接记录");
    }
    return errors;
  } else {
    const req = requiredFields[selectedChart.value];
    rawData.value.forEach((record, i) => {
      req.forEach(field => {
        const expectedType = getExpectedType(field);
        const value = record[field];
        if (value === undefined || value === null || (typeof value === 'string' && value.trim() === '')) {
          errors.push(`记录 ${i+1}: 缺少字段 "${field}" (必需: ${expectedType})`);
        } else {
          let actualType = typeof value;
          if (expectedType === 'array') {
            actualType = Array.isArray(value) ? 'array' : actualType;
          }
          if (expectedType !== actualType) {
            errors.push(`记录 ${i+1}: 字段 "${field}" 类型错误，预期: ${expectedType}，实际: ${actualType}`);
          }
        }
      });
    });
    return errors;
  }
});

// 验证是否全部通过（无错误）
const isValidData = computed(() => {
  if (!selectedChart.value || !rawData.value.length) return false;
  if (selectedChart.value === 'RelationShipChart') {
    return hasValidRelationshipData(rawData.value);
  }
  return validationErrors.value.length === 0;
});

//------------------------------------------------错误信息---------------------------------------------------


// ----------------------------------------------- 数据处理相关代码(严禁修改) -------------------------------------------------
const fieldMapping = ref({
  name: ['name', '名称', '项目', '类别', '品类'],
  value: ['value', '数值', '值', '数量', '金额'],
  date: ['date', '日期', '时间', 'timestamp'],
  x: ['x', '横坐标', '维度x'],
  y: ['y', '纵坐标', '维度y'],
  category: ['category', '类型', '种类', '组别'],
  size: ['size', '大小', '规模'],
  min: ['min', '最小值'],
  q1: ['q1', '第一四分位数'],
  median: ['median', '中位数'],
  q3: ['q3', '第三四分位数'],
  max: ['max', '最大值'],
  outliers: ['outliers', '离群值'],
  indicator: ['indicator', '指标'],
  stage: ['stage', '阶段'],
  parent: ['parent', '父级'],
  source: ['source', '来源'],
  target: ['target', '目标'],
  value1: ['value1', '值1'],
  value2: ['value2', '值2'],
  yAxis1: ['yAxis1', 'y轴1'],
  yAxis2: ['yAxis2', 'y轴2'],
});

const handleFileUpload = async (event) => {
  isLoading.value = true;
  uploadError.value = '';
  const file = event.target.files[0];
  if (!file) {
    isLoading.value = false;
    return;
  }
  filename.value = file.name;
  userStore.learn = filename.value;
  try {
    const raw = await parseFile(file);
    rawData.value = raw;
    ScreenLog();
    console.log('标准化数据:', JSON.stringify(rawData.value, null, 2));
  } catch (error) {
    uploadError.value = error.message;
    console.error('文件处理错误:', error);
  } finally {
    isLoading.value = false;
  }
};

const parseFile = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    const extension = file.name.split('.').pop().toLowerCase();
    reader.onload = (e) => {
      try {
        let data;
        switch (extension) {
          case 'xlsx':
          case 'xls':
            data = parseExcel(e.target.result);
            break;
          case 'csv':
            data = parseCSV(e.target.result);
            break;
          case 'json':
            data = parseJSON(e.target.result);
            break;
          default:
            reject(new Error('不支持的文件格式'));
        }
        resolve(formatData(data));
      } catch (error) {
        reject(error);
      }
    };
    reader.onerror = () => reject(new Error('文件读取失败'));
    if (['csv', 'json'].includes(extension)) {
      reader.readAsText(file);
    } else {
      reader.readAsArrayBuffer(file);
    }
  });
};

const parseExcel = (buffer) => {
  try {
    const workbook = XLSX.read(buffer, { type: 'array' });
    const sheetName = workbook.SheetNames[0];
    const sheet = workbook.Sheets[sheetName];
    return XLSX.utils.sheet_to_json(sheet, { defval: null });
  } catch (error) {
    throw new Error('解析 Excel 文件失败: ' + error.message);
  }
};

const parseCSV = (csvString) => {
  return new Promise((resolve, reject) => {
    Papa.parse(csvString, {
      header: true,
      skipEmptyLines: true,
      complete: (results) => {
        if (results.errors.length > 0) {
          reject(new Error('CSV 解析错误: ' + results.errors[0].message));
        } else {
          resolve(results.data);
        }
      },
      error: (error) => reject(new Error('CSV 解析失败: ' + error.message))
    });
  });
};

const parseJSON = (data) => {
  try {
    return JSON.parse(cleanJsonData(data));
  } catch (error) {
    throw new Error('无效的JSON格式');
  }
};

const cleanJsonData = (data) => {
  return data
    .trim()
    .replace(/\s+/g, ' ')
    .replace(/,\s*}/g, '}')
    .replace(/,\s*]/g, ']');
};

const formatData = (rawData) => {
  return rawData
    .map((item) => {
      const formattedItem = {};
      Object.entries(item).forEach(([key, value]) => {
        const cleanKey = key.toString().trim().toLowerCase().replace(/[\s_]+/g, '_');
        let processedValue = value;
        if (processedValue === null || processedValue === undefined) return;
        if (typeof processedValue === 'string') {
          const timestamp = Date.parse(processedValue);
          if (!isNaN(timestamp)) {
            processedValue = formatDate(new Date(timestamp));
            formattedItem.date = processedValue;
            return;
          }
        }
        if (typeof processedValue === 'string' && !isNaN(processedValue)) {
          processedValue = parseFloat(processedValue);
        }
        const standardKey =
          Object.entries(fieldMapping.value).find(([_, aliases]) =>
            aliases.some((alias) => cleanKey === alias)
          )?.[0] || cleanKey;
        formattedItem[standardKey] = processedValue;
      });
      return formattedItem;
    })
    .filter((item) => Object.keys(item).length > 0);
};

const formatDate = (date) => {
  if (!(date instanceof Date)) return date;
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}/${month}/${day}`;
};
// ----------------------------------------------- 数据处理相关代码(严禁修改) -------------------------------------------------


//------------------------------------------------上传背景（禁止修改）------------------------------------------
const updateUserBg=()=>{
  post("api/user/updateUserBg",{
    id:userStore.user.id,
    bg:options.bg,
  },(message)=>{
    console.log("cg");
  })
}

const onUploadImg = async (e, callback) => {
  const file = e.target.files[0];
  if (!file) return;
  const form = new FormData();
  form.append('image', file);
  try {
    const response = await axios.post("api/user/uploadImg", form, {
      headers: {
        "Content-Type": "multipart/form-data",
        "Authorization": `${localStorage.getItem("authToken")}`
      }
    });
    if (response.data.success) {
      options.bg = baseURL1+response.data.data;
      console.log("上传成功，新的背景地址：", options.bg);
      await updateUserBg();
      return options.bg;
    } else {
      throw new Error(response.data.message);
    }
  } catch (error) {
    console.error("上传失败：", error);
    callback([]);
  }
};


const selectedColor = ref(userStore.user.bgcolor || '#4a5568'); // #4a5568 是 bg-gray-700 的十六进制值
const rgbaColor = ref(userStore.user.bgcolor);
const updateBackgroundColor = () => {
  // 将十六进制颜色转换为 rgba 格式，并添加透明度
  const hexToRgba = (hex, opacity) => {
    let r = parseInt(hex.slice(1, 3), 16);
    let g = parseInt(hex.slice(3, 5), 16);
    let b = parseInt(hex.slice(5, 7), 16);
    return `rgba(${r}, ${g}, ${b}, ${opacity})`;
  };
  // 设置透明度为 0.5（50%）
   rgbaColor.value = hexToRgba(selectedColor.value, 0.5);
  // 保存到数据库
  post("api/user/updateUserBgColor", {
    id: userStore.user.id,
    bgColor: rgbaColor.value,
  }, (message) => {
    console.log("背景颜色已更新");
  });
};

//------------------------------------------------上传背景（禁止修改）------------------------------------------



//---------------------------------保存---------------------------------------------------
const Screen=reactive({
  type: '',
  data: '',
  position: '',
})
const addScreen=()=>{
  post("api/screen/addScreen",{
    type:Screen.type,
    data:Screen.data,
    position:Screen.position,
  },(message)=>{
    console.log(message);
  })
}

const updateScreen=()=>{
  post("api/screen/updateScreen",{
    type:Screen.type,
    data:Screen.data,
    position:Screen.position,
  },(message)=>{
    console.log(message);
  })
}

const checkPosition=()=>{
  get("api/screen/checkPosition",{
    position:Screen.position,
  },(message,data)=>{
    if (data!==null)
    {
      updateScreen()
    }else addScreen()
  })
}
onMounted(() => {
  regions.value.forEach(region => {
    get("api/screen/getScreenByUserId", { position: region.gridArea }, (message, data) => {
      if (data) {
        // 更新图表类型
        region.chart = data.type;
        // 解析存储的图表数据（假设 data.data 是 JSON 格式的字符串）
        try {
          region.chartData = JSON.parse(data.data);
        } catch (e) {
          console.error("解析图表数据失败:", e);
          region.chartData = data.data;
        }
      }
    });
  });
});

//---------------------------------保存---------------------------------------------------


//-------------------------------------新手教程----------------------------------
const showTutorialModal = ref(false)
const currentStep = ref(0)
// 动态加载图片的函数
const getImageUrl = (imageName) => {
  return new URL(`../assets/image/${imageName}`, import.meta.url).href;
};

const tutorialSteps = ref([
  {
    image: '第一步.png', // 图片放在src/assets/image目录
    title: '第一步：选择图表类型',
    description: '点击任意区域打开图表选择器，浏览并选择最适合您数据的图表类型'
  },
  {
    image: '第二步.png',
    title: '第二步：上传数据文件',
    description: '支持Excel、CSV和JSON格式，确保数据格式符合所选图表要求，上传完成之后生成图表即可'
  },
  {
    image: '第三步.png',
    title: '第三步：自定义样式',
    description: '右键点击需要配置的图表，即可打开图表配置面板'
  },
  {
    image: '第四步.png',  // 修正重复的第三步为第四步
    title: '第四步：更新图表',
    description: '双击需要更新的图表所对应的位置，即可重新打开图表选择面板'
  }
])

const showTutorial = () => {
  showTutorialModal.value = true
  currentStep.value = 0
}

const closeTutorial = () => {
  showTutorialModal.value = false
}

const nextStep = () => {
  if (currentStep.value < tutorialSteps.value.length - 1) {
    currentStep.value++
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}
//-------------------------------------新手教程----------------------------------

//--------------------------------------日志-----------------------------------
const ScreenLog=()=>{
  post("api/log/insertLog", {
    type: "万象合璧",
    content: `更新了大屏的数据`
  }, (message) => {
    console.log(filename.value + message);
  });
}

</script>


<template>
  <contextHolder/>
  <div
    class="min-h-screen flex flex-col bg-opacity-50 "
    :style="{
      backgroundImage: options.bg ? 'url(' + options.bg + ')' : '',
      backgroundSize: 'cover'
    }"
  >
    <!-- 主体内容 -->
    <main class="flex-1 p-6 grid-layout ">
      <section
        v-for="(region, index) in regions"
        :key="index"
        :style="{ gridArea: region.gridArea ,backgroundColor: rgbaColor }"
        class="bg-opacity-50 shadow-lg rounded-xl hover:shadow-2xl transition-transform duration-300 cursor-pointer"
        :class="{ 'border-2 border-blue-700 scale-105 shadow-2xl': region.chart === selectedChart }"

        @click="!region.chart && openChartSelector(region)"
        @dblclick="region.chart && openChartSelector(region)">

        <!-- 如果当前区域没有图表数据，则显示默认的趣味信息 -->
        <div v-if="!region.chartData || (Array.isArray(region.chartData) && region.chartData.length === 0)"
             class="data-empty-container">
          <!-- 动态图标 -->
          <div class="dynamic-icon">
            <template v-if="region.gridArea.includes('left1')">
              <span class="chart-icon">📊</span>
            </template>
            <template v-else-if="region.gridArea.includes('right1')">
              <span class="chart-icon">📈</span>
            </template>
            <template v-else-if="region.gridArea.includes('left2')">
              <span class="chart-icon">🌳</span>
            </template>
            <template v-else-if="region.gridArea.includes('right2')">
              <span class="chart-icon">🔥</span>
            </template>
            <template v-else-if="region.gridArea.includes('left3')">
              <span class="chart-icon">🔗</span>
            </template>
            <template v-else-if="region.gridArea.includes('right3')">
              <span class="chart-icon">📦</span>
            </template>
            <template v-else-if="region.gridArea.includes('center-top')">
              <span class="chart-icon">☁️</span>
            </template>
            <template v-else-if="region.gridArea.includes('center-bottom')">
              <span class="chart-icon">🕸️</span>
            </template>
            <template v-else>
              <div class="emoji-rain">
        <span v-for="(e,i) in ['📈','📉','📊','🕸️','☁️', '🌳','🔥', '🔗','📦','🥧']" :key="i"
              class="emoji" :style="{
                left: `${Math.random()*80}%`,
                animationDelay: `${i*0.5}s`
              }">{{ e }}</span>
              </div>
            </template>
          </div>

          <!-- 趣味提示文字 -->
          <div class="upload-prompt">
            <template v-if="region.gridArea === 'center-bottom'">
              <h3 class="text-lg font-bold mb-2">🎉 发现数据宇宙！</h3>
              <p>双击这里上传数据，开启可视化之旅 🚀</p>
            </template>
            <template v-else>
              <h3 class="text-lg font-bold mb-2">✨ 空画布大挑战！</h3>
              <p>点击添加{{ ['第一个'][Math.floor(Math.random()*3)] }}图表吧</p>
              <p class="text-xs mt-1 opacity-75">支持Excel/CSV/JSON格式</p>
            </template>
          </div>

          <!-- 微交互元素 -->
          <div class="mt-4 animate-pulse">
            <svg xmlns="http://www.w3.org/2000/svg"  viewBox="0 0 24 24" class="size-7 text-blue-300">
              <g fill="none"><path d="m12.593 23.258l-.011.002l-.071.035l-.02.004l-.014-.004l-.071-.035q-.016-.005-.024.005l-.004.01l-.017.428l.005.02l.01.013l.104.074l.015.004l.012-.004l.104-.074l.012-.016l.004-.017l-.017-.427q-.004-.016-.017-.018m.265-.113l-.013.002l-.185.093l-.01.01l-.003.011l.018.43l.005.012l.008.007l.201.093q.019.005.029-.008l.004-.014l-.034-.614q-.005-.018-.02-.022m-.715.002a.02.02 0 0 0-.027.006l-.006.014l-.034.614q.001.018.017.024l.015-.002l.201-.093l.01-.008l.004-.011l.017-.43l-.003-.012l-.01-.01z"/><path fill="currentColor" d="M9.5 2a2.5 2.5 0 0 1 2.495 2.336L12 4.5v4.605l5.442.605a4 4 0 0 1 3.553 3.772l.005.203V14a8 8 0 0 1-7.75 7.996L13 22h-.674a8 8 0 0 1-7.024-4.171l-.131-.251l-2.842-5.684c-.36-.72-.093-1.683.747-2.028c1.043-.427 2.034-.507 3.055.012q.333.17.654.414l.215.17V4.5A2.5 2.5 0 0 1 9.5 2m0 2a.5.5 0 0 0-.492.41L9 4.5V13a1 1 0 0 1-1.78.625l-.332-.407l-.303-.354c-.58-.657-1.001-1.02-1.36-1.203a1.2 1.2 0 0 0-.694-.137l-.141.02l2.57 5.14a6 6 0 0 0 5.123 3.311l.243.005H13a6 6 0 0 0 5.996-5.775L19 14v-.315a2 2 0 0 0-1.621-1.964l-.158-.024l-5.442-.604a2 2 0 0 1-1.773-1.829L10 9.105V4.5a.5.5 0 0 0-.5-.5M4 6a1 1 0 0 1 0 2H3a1 1 0 0 1 0-2zm12-1a1 1 0 0 1 .117 1.993L16 7h-1a1 1 0 0 1-.117-1.993L15 5zM4.707 1.293l1 1a1 1 0 0 1-1.414 1.414l-1-1a1 1 0 0 1 1.414-1.414m11 0a1 1 0 0 1 0 1.414l-1 1a1 1 0 1 1-1.414-1.414l1-1a1 1 0 0 1 1.414 0"/></g>
            </svg>
          </div>
        </div>
        <!-- 否则展示图表组件 -->
        <div v-else class="w-full h-full z-10">
          <component :is="getChartComponent(region.chart)" :rawData="region.chartData" />
        </div>
      </section>
    </main>

    <!-- 底部栏 -->
    <footer class=" bg-gradient-to-r from-blue-600/80 to-purple-600/80 backdrop-blur-lg text-white p-4 flex justify-between items-center shadow-2xl border-t border-white/20">
      <div class="flex items-center space-x-4">
        <!-- 用户信息 -->
        <div class="flex items-center space-x-3 bg-white/10 px-4 py-2 rounded-full backdrop-blur-sm">
          <img
            :src="userStore.user.avatar || 'https://th.bing.com/th/id/R.37f9ac02be45c44ae7f94a73728d1baa?rik=kFogg7UByzuWew&pid=ImgRaw&r=0'"
            class="w-8 h-8 rounded-full object-cover border-2 border-white/30"
            alt="用户头像"
          >
          <span class="font-medium text-sm">{{ userStore.user.username }}的大屏</span>
        </div>

        <!-- 操作按钮组 -->
        <div class="flex items-center space-x-2 bg-white/10 p-2 rounded-full backdrop-blur-sm">
          <!-- 上传背景按钮 -->
          <label class="flex items-center space-x-2 cursor-pointer hover:bg-white/20 px-4 py-2 rounded-full transition-all duration-300">
            <svg class="w-5 h-5 text-white/90" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/>
            </svg>
            <span class="text-sm">背景</span>
            <input
              type="file"
              @change="onUploadImg"
              class="hidden"
            />
          </label>

          <!-- 颜色选择器 -->
          <label class="flex items-center space-x-2 cursor-pointer hover:bg-white/20 px-4 py-2 rounded-full transition-all duration-300">
            <svg class="w-5 h-5 text-white/90" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a1 1 0 00-1-1h-2.236a1 1 0 00-.894.553l-1.448 2.895a1 1 0 01-.894.553H13"/>
            </svg>
            <input
              type="color"
              v-model="selectedColor"
              @change="updateBackgroundColor"
              class="w-6 h-6 rounded-full cursor-pointer bg-transparent border-none"
            />
          </label>
        </div>
      </div>

      <!-- 右侧按钮区域 -->
      <div class="flex items-center space-x-3">
        <!-- 新增新手手册按钮 -->
        <button
          @click="showTutorial"
          class="flex items-center space-x-2 bg-indigo-500/90 hover:bg-indigo-400 px-4 py-2 rounded-full transition-all duration-300 shadow-lg"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
          <span class="text-sm">新手手册</span>
        </button>
      </div>
    </footer>

<!--    新手手册-->
    <div v-if="showTutorialModal"  class="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center">
      <div class="bg-white rounded-xl shadow-2xl w-[90%] max-w-4xl p-6 relative mx-auto">
        <!-- 关闭按钮 -->
        <button
          @click="closeTutorial"
          class="absolute top-4 right-4 text-gray-500 hover:text-gray-700 transition-colors z-50"
        >
          <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
          </svg>
        </button>

        <!-- 轮播内容 -->
        <div class="relative overflow-hidden rounded-lg h-[70vh]">
          <!-- 轮播图片 -->
          <div
            class="flex transition-transform duration-500 ease-in-out h-full"
            :style="{ transform: `translateX(-${currentStep * 100}%)` }"
          >
            <div
              v-for="(step, index) in tutorialSteps"
              :key="index"
              class="w-full flex-shrink-0 p-4 h-full flex flex-col items-center justify-center"
            >
              <img
                :src="getImageUrl(step.image)"
                class="w-full h-[400px] object-contain rounded-lg mb-4"
                :alt="step.title"
              >
              <div class="text-center px-4">
                <h3 class="text-2xl font-bold mb-4 text-gray-800">{{ step.title }}</h3>
                <p class="text-gray-600 text-lg leading-relaxed max-w-2xl mx-auto">{{ step.description }}</p>
              </div>
            </div>
          </div>

          <!-- 导航箭头 -->
          <button
            v-if="currentStep > 0"
            @click="prevStep"
            class="absolute left-4 top-1/2 -translate-y-1/2 bg-white/80 p-3 rounded-full shadow-lg hover:bg-white transition-colors backdrop-blur-sm"
          >
            <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
            </svg>
          </button>
          <button
            v-if="currentStep < tutorialSteps.length - 1"
            @click="nextStep"
            class="absolute right-4 top-1/2 -translate-y-1/2 bg-white/80 p-3 rounded-full shadow-lg hover:bg-white transition-colors backdrop-blur-sm"
          >
            <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
            </svg>
          </button>
        </div>

        <!-- 步骤指示器 -->
        <div class="flex justify-center space-x-3 mt-6">
          <button
            v-for="(step, index) in tutorialSteps"
            :key="index"
            @click="currentStep = index"
            class="w-3 h-3 rounded-full transition-all duration-300"
            :class="index === currentStep ? 'bg-blue-500 scale-125' : 'bg-gray-300 hover:bg-gray-400'"
          />
        </div>
      </div>
    </div>

    <!-- 修改后的图表选择模态框 -->
    <div
      v-if="isModalOpen"
      class="mt-10 fixed inset-0 bg-black bg-opacity-50 backdrop-blur-sm flex justify-center items-center z-10"
    >
      <div class="bg-white rounded-lg shadow-xl w-full max-w-4xl relative animate-fadeIn">
        <!-- 关闭按钮 -->
        <button
          @click="closeModal"
          class="absolute top-4 right-4 text-gray-500 hover:text-gray-700 text-2xl"
        >
          &times;
        </button>
        <div class="flex ">
          <!-- 左侧：图表类型选择 -->
          <div class="w-1/2 p-6 border-r">
            <h2 class="text-2xl font-bold mb-4 text-center">选择图表类型</h2>
            <div class="grid grid-cols-3 gap-4">
              <div
                v-for="(chart, key) in chartIcons"
                :key="key"
                @click="selectChart(key)"
                class="text-center cursor-pointer hover:bg-gray-200 p-4 rounded-lg transition duration-300"
                :class="{'bg-blue-100 border border-blue-500': selectedChart === key}"
              >
                <span class="text-4xl mb-2 block">{{ chart.icon }}</span>
                <span class="block text-sm font-semibold">{{ chart.label }}</span>
              </div>
            </div>
          </div>
          <!-- 右侧：上传文件和格式要求 -->
          <!-- 修改后的上传文件和格式要求区域 -->
          <div class="w-1/2 flex flex-col">
            <!-- 上部分：文件上传区域（较小） -->
            <div class="p-6 border-b" style="flex: 1;">
              <h3 class="text-xl font-semibold mb-2 text-center">上传文件</h3>
              <label
                class="block w-full border-2 border-dashed border-gray-300 p-4 rounded-lg text-center cursor-pointer hover:bg-gray-100 transition duration-300"
              >
                <div class="flex flex-col items-center justify-center">
                  <svg class="w-6 h-6 text-gray-500 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M7 16v-4m0 0v-4m0 4h4m-4 0H3m10 4v-4m0 0v-4m0 4h4m-4 0h-4"/>
                  </svg>
                  <span class="text-gray-700">点击选择文件或拖拽文件到此处</span>
                </div>
                <input
                  ref="fileInputRef"
                  type="file"
                  @change="handleFileUpload"
                  class="hidden"
                />
              </label>
              <!-- 显示文件名 -->
              <div v-if="filename" class="mt-2 text-center text-sm text-gray-600">
                已选择：{{ filename }}
              </div>
              <!-- 上传错误提示 -->
              <div v-if="uploadError" class="mt-2 text-center text-sm text-red-500">
                {{ uploadError }}
              </div>
            </div>
            <!-- 下部分：数据格式要求区域（较大） -->
            <div class="p-6 overflow-y-auto scrollbar-hide" style="flex: 2;">
              <h3 class="text-xl font-semibold mb-2 text-center">数据格式要求</h3>
              <div class="bg-gray-50 p-4 rounded-lg shadow-inner">
                <ul class="list-disc pl-5 text-sm text-gray-700 space-y-2">
                  <li v-if="selectedChart === 'LineChart' || selectedChart === 'AreaChart'">
                    必需字段：<strong>date</strong>（日期-字符串）、<strong>value</strong>（数值）
                  </li>
                  <li v-if="selectedChart === 'BarChart' || selectedChart === 'PieChart' || selectedChart === 'TreemapChart'">
                    必需字段：<strong>name</strong>（名称-字符串）、<strong>value</strong>（数值）
                  </li>
                  <li v-if="selectedChart === 'ScatterChart'">
                    必需字段：<strong>x</strong>（横坐标-数值）、<strong>y</strong>（纵坐标-数值）
                  </li>
                  <li v-if="selectedChart === 'BoxPlotChart'">
                    必需字段：<strong>name</strong>（字符串）、<strong>min</strong>（数值）、<strong>q1</strong>（数值）、<strong>median</strong>（数值）、<strong>q3</strong>（数值）、<strong>max</strong>（数值）、<strong>outliers</strong>（数组）
                  </li>
                  <li v-if="selectedChart === 'RadarChart'">
                    必需字段：<strong>category</strong>（字符串）、<strong>indicator</strong>（字符串）、<strong>value</strong>（数值）
                  </li>
                  <li v-if="selectedChart === 'FunnelChart'">
                    必需字段：<strong>stage</strong>（字符串）、<strong>value</strong>（数值）
                  </li>
                  <li v-if="selectedChart === 'WordCloud'">
                    必需字段：<strong>name</strong>（名称-字符串）、<strong>value</strong>（数值）
                  </li>
                  <li v-if="selectedChart === 'HeatMapChart'">
                    必需字段：<strong>x</strong>（字符串）、<strong>y</strong>（字符串）、<strong>value</strong>（数值）
                  </li>
                  <li v-if="selectedChart === 'DualAxisChart'">
                    必需字段：<strong>date</strong>（字符串）、<strong>value1</strong>（数值）、<strong>value2</strong>（数值）、<strong>yaxis1</strong>（字符串）、<strong>yaxis2</strong>（字符串）
                  </li>
                  <li v-if="selectedChart === 'RelationShipChart'">
                    关系图数据要求至少包含一条有效节点和一条有效链接记录，其中：
                    <ul class="list-inside list-disc mt-2">
                      <li>
                        节点：type 为 "node"，<strong>id</strong>（数值）、<strong>name</strong>（非空字符串）、<strong>symbolsize</strong>（数值）、<strong>category</strong>（非空字符串）
                      </li>
                      <li>
                        链接：type 为 "link"，<strong>source</strong>（数值）、<strong>target</strong>（数值）、<strong>value</strong>（数值）
                      </li>
                    </ul>
                  </li>
                </ul>
              </div>
              <!-- 数据格式验证错误：显示详细错误列表 -->
              <div v-if="rawData.length && !isValidData" class="mt-2 text-center text-sm text-red-500">
                上传的数据格式不符合要求:
                <ul class="list-disc text-left inline-block mt-2">
                  <li v-for="error in validationErrors" :key="error">{{ error }}</li>
                </ul>
              </div>
            </div>
          </div>

        </div>
        <!-- 按钮区域 -->
        <div class="p-6 flex justify-between">
          <button
            @click="closeModal"
            class="bg-gray-700 text-white py-2 px-4 rounded hover:bg-gray-800 transition duration-300"
          >
            取消
          </button>
          <button
            v-if="selectedChart && rawData.length && isValidData"
            @click="generateChart"
            class="bg-blue-500 text-white py-2 px-4 rounded hover:bg-blue-600 transition duration-300"
          >
            生成图表
          </button>
        </div>
      </div>
    </div>


  </div>
</template>


<style scoped>
/* 确保弹窗相对于整个界面居中 */
.fixed {
  display: flex;
  align-items: center;
  justify-content: center;
  position: fixed;
  z-index: 50;
}

/* 弹窗容器样式 */
.bg-white {
  margin: auto; /* 确保水平和垂直居中 */
  max-height: 90vh; /* 限制最大高度 */
  overflow-y: auto; /* 如果内容过多，允许滚动 */
  width: 90%; /* 限制宽度 */
  max-width: 48rem; /* 最大宽度 */
}

/* 优化图片加载 */
img {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border: 1px solid #e2e8f0;
}

/* 导航箭头优化 */
button[class*="absolute"] svg {
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.1));
}

/* 步骤指示器动画 */
button[class*="bg-gray-300"] {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
</style>
<style scoped>
/* 新增动画效果 */
@keyframes float {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
  100% { transform: translateY(0px); }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.bounce-enter-active {
  animation: bounce-in 0.5s;
}
.bounce-leave-active {
  animation: bounce-in 0.5s reverse;
}
@keyframes bounce-in {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

.data-empty-container {
  @apply h-full w-full flex flex-col items-center justify-center p-4;
}

.dynamic-icon {
  @apply text-6xl mb-4;
  animation: float 3s ease-in-out infinite;
}

.upload-prompt {
  @apply text-center text-gray-600 text-sm;
}

.chart-icon {
  @apply text-4xl;
  animation: spin 8s linear infinite;
}

.emoji-rain {
  position: relative;
  height: 100px;
  width: 100px;
  margin: 0 auto;
}

.emoji {
  position: absolute;
  animation: emoji-fall 3s linear infinite;
  opacity: 0;
}

@keyframes emoji-fall {
  0% {
    transform: translateY(-100%) rotate(0deg);
    opacity: 1;
  }
  100% {
    transform: translateY(100vh) rotate(360deg);
    opacity: 0;
  }
}
</style>

<style scoped>
.grid-layout {
  display: grid;
  grid-template-columns: 1fr 2fr 1fr;
  grid-template-rows: repeat(3, 1fr);
  gap: 10px;
  grid-template-areas:
    "left1 center-top right1"
    "left2 center-bottom right2"
    "left3 center-bottom right3";
}
.left1 {
  grid-area: left1;
}
.left2 {
  grid-area: left2;
}
.left3 {
  grid-area: left3;
}
.center-top {
  grid-area: center-top;
}
.center-bottom {
  grid-area: center-bottom;
}
.right1 {
  grid-area: right1;
}
.right2 {
  grid-area: right2;
}
.right3 {
  grid-area: right3;
}
/* Modal fade-in animation */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
.animate-fadeIn {
  animation: fadeIn 0.3s ease-out;
}
.modal-content {
  max-height: 80vh;
  overflow-y: auto;
}
/*用于隐藏滚动条,::-webkit-scrollbar:这个伪元素用于隐藏 WebKit 浏览器（如 Chrome、Safari）中的滚动条。*/
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}

.scrollbar-hide {
  -ms-overflow-style: none; /*-ms-overflow-style: none;: 这个属性用于隐藏 IE 和 Edge 浏览器中的滚动条。*/
  scrollbar-width: none;  /*scrollbar-width: none;: 这个属性用于隐藏 Firefox 浏览器中的滚动条。*/
}
</style>
