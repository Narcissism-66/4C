<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import * as XLSX from 'xlsx'
import Papa from 'papaparse'
import { DateTime } from 'luxon'
import { marked } from 'marked'
import { post } from '@/net/index.js'
import { userUserStore } from '@/stores/UserStore.js'

// 图表组件导入
import BarChart from '../components/ECharts/BarChart/BarChart.vue'
import LineChart from '../components/ECharts/LineChart/LineChart.vue'
import PieChart from '../components/ECharts/PieChart/PieChart.vue'
import ScatterChart from '../components/ECharts/ScatterChart/ScatterChart.vue'
import BoxPlotChart from '../components/ECharts/BoxPlotChart/BoxPlotChart.vue'
import BubbleChart from '../components/ECharts/BubbleChart/BubbleChart.vue'
import AreaChart from '../components/ECharts/AreaChart/AreaChart.vue'
import RadarChart from '../components/ECharts/RadarChart/RadarChart.vue'
import FunnelChart from '../components/ECharts/FunnelChart/FunnelChart.vue'
import TreemapChart from '../components/ECharts/TreemapChart/TreemapChart.vue'
import SankeyChart from '../components/ECharts/SankeyChart/SankeyChart.vue'
import DualAxisChart from '../components/ECharts/DualAxisChart/DualAxisChart.vue'
import { message } from 'ant-design-vue'

const filename=ref();
const [messageApi, contextHolder] = message.useMessage();

// 建立图表组件映射
const chartComponents = {
  line: LineChart,
  area: AreaChart, // 面积图共用折线图组件（可根据需要单独实现）
  bar: BarChart,
  pie: PieChart,
  treemap: TreemapChart,
  scatter: ScatterChart,
  bubble: BubbleChart,
  boxplot: BoxPlotChart,
  radar: RadarChart,
  funnel: FunnelChart,
  sankey: SankeyChart,
  dualaxis: DualAxisChart,
}

// 定义各图表类型所需的必需字段与示例格式
const requiredFieldsMapping = {
  line: { fields: ['date', 'value'], sample: "{ date: '2023-01-01', value: 150 }" },
  area: { fields: ['date', 'value'], sample: "{ date: '2023-01-01', value: 150 }" },
  bar: { fields: ['name', 'value'], sample: "{ name: '类别A', value: 75 }" },
  pie: { fields: ['name', 'value'], sample: "{ name: '类别A', value: 75 }" },
  treemap: { fields: ['name', 'value', 'parent'], sample: "{ name: '类别A', value: 75, parent: '' }" },
  scatter: { fields: ['x', 'y'], sample: "{ x: 5.2, y: 8.4 }" },
  bubble: { fields: ['x', 'y', 'size'], sample: "{ x: 5.2, y: 8.4, size: 10 }" },
  boxplot: { fields: ['name', 'min', 'Q1', 'median', 'Q3', 'max', 'outliers'], sample: "{ name: '组A', min: 10, Q1: 15, median: 20, Q3: 25, max: 30, outliers: [5,35] }" },
  radar: { fields: ['category', 'indicator', 'value'], sample: "{ category: '产品A', indicator: '质量', value: 80 }" },
  funnel: { fields: ['stage', 'value'], sample: "{ stage: '访问', value: 100 }" },
  sankey: { fields: ['source', 'target', 'value'], sample: "{ source: 'A', target: 'B', value: 80 }" },
  dualaxis: { fields: ['date', 'value1', 'value2', 'yAxis1', 'yAxis2'], sample: "{ date: '2023-01-01', value1: 100, value2: 80, yAxis1: '销量', yAxis2: '温度' }" },
}

// 用于图表选择的选项
const chartOptions = [
  { type: 'line', icon: '📈' },
  { type: 'area', icon: '🌊' },
  { type: 'bar', icon: '📊' },
  { type: 'pie', icon: '🥧' },
  { type: 'treemap', icon: '🌳' },
  { type: 'scatter', icon: '🟢' },
  { type: 'bubble', icon: '🔵' },
  { type: 'boxplot', icon: '📦' },
  { type: 'radar', icon: '📡' },
  { type: 'funnel', icon: '🔻' },
  { type: 'sankey', icon: '🔀' },
  { type: 'dualaxis', icon: '🔄' },
]

// 用于显示友好名称
const chartTypeLabels = {
  line: '折线图',
  area: '面积图',
  bar: '柱状图',
  pie: '饼图',
  treemap: '树图',
  scatter: '散点图',
  bubble: '气泡图',
  boxplot: '箱型图',
  radar: '雷达图',
  funnel: '漏斗图',
  sankey: '桑基图',
  dualaxis: '双轴图',
}

// 响应式状态
const datasets = ref([])
const activeDataset = ref(null)
const chartType = ref('')
const uploadError = ref('')
const isLoading = ref(false)
const mergeWarning = ref('') // 合并警告
const isMerge = ref(false)

// 用户信息（模拟，可替换为实际数据）
const userStore = userUserStore()

// 扩展字段映射配置，包含所有可能字段
const fieldMapping = {
  date: ['date', 'time', 'timestamp', '日期', '时间'],
  value: ['value', 'amount', 'score', '数值', '值'],
  name: ['name', 'category', 'type', '名称', '类别'],
  x: ['x', 'axis_x', '维度x'],
  y: ['y', 'axis_y', '维度y'],
  parent: ['parent', '父节点'],
  min: ['min', '最小值'],
  Q1: ['Q1', '第一四分位'],
  median: ['median', '中位数'],
  Q3: ['Q3', '第三四分位'],
  max: ['max', '最大值'],
  outliers: ['outliers', '异常值'],
  category: ['category', '类别'],
  indicator: ['indicator', '指标'],
  stage: ['stage', '阶段'],
  source: ['source', '来源'],
  target: ['target', '目标'],
  value1: ['value1', '数值1'],
  value2: ['value2', '数值2'],
  yAxis1: ['yAxis1', 'y轴1'],
  yAxis2: ['yAxis2', 'y轴2'],
  size: ['size', '气泡大小']
}

// 辅助函数：过滤掉所有记录均无数据的列
const getFilteredHeaders = (data) => {
  if (!data || data.length === 0) return []
  const keys = Object.keys(data[0])
  return keys.filter(key => data.some(item => item[key] !== null && item[key] !== undefined && item[key] !== ''))
}

// 重新定义 detectFields，检测所有映射字段
const detectFields = (data) => {
  if (data.length === 0) return {}
  const sample = data[0]
  const fields = {}
  for (const key in fieldMapping) {
    fields[key] = sample[key] !== undefined && sample[key] !== null && sample[key] !== ''
  }
  return fields
}

// ----------------------处理上传的数据----------------------
const handleFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  uploadError.value = ''
  chartType.value = '' // 重置图表类型

  try {
    filename.value=file.name;
    const rawData = await parseFile(file)
    const { mappedData, cleanData, datasetType } = processData(rawData)

    console.log('上传的原始数据:', JSON.stringify(mappedData, null, 2))
    console.log('上传的清理数据:', JSON.stringify(cleanData, null, 2))

    const newDataset = {
      id: Date.now(),
      name: file.name,
      rawData: mappedData, // 原始数据
      cleanData,         // 数据清洗后的结果
      fields: detectFields(cleanData),
      type: datasetType,
      showTable: false   // 用于鼠标悬停显示数据预览表格
    }

    datasets.value.push(newDataset)
    activeDataset.value = newDataset
    chartType.value = datasetType
  } catch (error) {
    uploadError.value = error.message
  }
}

const parseFile = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    const extension = file.name.split('.').pop().toLowerCase()

    reader.onload = (e) => {
      try {
        let data
        switch (extension) {
          case 'xlsx':
          case 'xls':
            data = parseExcel(e.target.result)
            break
          case 'csv':
            data = parseCSV(e.target.result)
            break
          case 'json':
            data = JSON.parse(e.target.result)
            break
          default:
            throw new Error('不支持的文件格式')
        }
        resolve(data)
      } catch (error) {
        reject(error)
      }
    }

    reader.onerror = () => reject(new Error('文件读取失败'))

    if (['csv', 'json'].includes(extension)) {
      reader.readAsText(file)
    } else {
      reader.readAsArrayBuffer(file)
    }
  })
}

const parseExcel = (buffer) => {
  try {
    const workbook = XLSX.read(buffer, { type: 'array' })
    const sheetName = workbook.SheetNames[0]
    const sheet = workbook.Sheets[sheetName]
    return XLSX.utils.sheet_to_json(sheet, { defval: null })
  } catch (error) {
    throw new Error('解析 Excel 文件失败: ' + error.message)
  }
}

const parseCSV = (csvString) => {
  return new Promise((resolve, reject) => {
    Papa.parse(csvString, {
      header: true,
      skipEmptyLines: true,
      complete: (results) => {
        if (results.errors.length > 0) {
          reject(new Error('CSV 解析错误: ' + results.errors[0].message))
        } else {
          resolve(results.data)
        }
      },
      error: (error) => reject(new Error('CSV 解析失败: ' + error.message))
    })
  })
}

const processData = (rawData) => {
  // 对数据字段进行映射
  const mappedData = rawData.map(item => {
    const mappedItem = {}
    Object.entries(fieldMapping).forEach(([standardKey, aliases]) => {
      const foundKey = Object.keys(item).find(key =>
        aliases.includes(key.toLowerCase().trim())
      )
      if (foundKey) mappedItem[standardKey] = item[foundKey]
    })
    return { ...item, ...mappedItem }
  })

  const fields = detectFields(mappedData)
  const datasetType =
    fields.date && fields.value ? 'line' :
      fields.name && fields.value ? 'bar' :
        fields.x && fields.y ? 'scatter' : ''

  const cleanData = mappedData
    .map(item => ({
      ...item,
      date: parseDate(item.date),
      value: parseNumber(item.value),
      x: parseNumber(item.x),
      y: parseNumber(item.y)
    }))
    .filter(item => {
      switch (datasetType) {
        case 'line':
          return item.date && item.value !== undefined
        case 'bar':
          return item.name && item.value !== undefined
        case 'scatter':
          return item.x !== undefined && item.y !== undefined
        default:
          return true
      }
    })

  return { mappedData, cleanData, datasetType }
}

const parseDate = (value) => {
  if (!value) return null
  const formats = ['yyyy-MM', 'yyyy/MM', 'yyyy-MM-dd', 'yyyy/MM/dd', 'x']
  for (const format of formats) {
    const dt = DateTime.fromFormat(value, format)
    if (dt.isValid) return dt.toFormat('yyyy/MM/dd')
  }
  const timestamp = Number(value)
  if (!isNaN(timestamp)) return DateTime.fromMillis(timestamp).toFormat('yyyy/MM/dd')
  return null
}

const parseNumber = (value) => {
  if (typeof value === 'number') return value
  const num = parseFloat(value)
  return isNaN(num) ? undefined : num
}

const processedData = computed(() => {
  if (!activeDataset.value || !activeDataset.value.cleanData) return []
  return activeDataset.value.cleanData
})

// 为确保修改数据后图表更新，增加对 activeDataset.cleanData 的 deep watch
watch(() => activeDataset.value && activeDataset.value.cleanData, (newVal) => {
  chartType.value = chartType.value
}, { deep: true })

// ----------------------合并数据----------------------
let previousChartType = ref()
let previousDataset = ref()
let previousData = ref()

const mergeDatasets = () => {
  if (!isMerge.value) {
    previousChartType.value = chartType.value
    previousDataset.value = activeDataset.value
    previousData.value = processedData.value
  }

  if (!datasets.value || datasets.value.length === 0) return

  const selectedDatasets = datasets.value.filter(dataset => dataset.isSelected)
  mergeWarning.value = ''

  if (selectedDatasets.length === 0) {
    uploadError.value = '请至少选择一个数据集进行合并'
    return
  }

  if (['line', 'bar'].includes(chartType.value)) {
    const keyField = chartType.value === 'line' ? 'date' : 'name'
    const allValues = selectedDatasets.map(dataset =>
      new Set(dataset.cleanData.map(item => item[keyField]))
    )
    const baseValues = Array.from(allValues[0])
    const hasInconsistency = allValues.some(
      valueSet => valueSet.size !== baseValues.length ||
        !baseValues.every(v => valueSet.has(v))
    )
    if (hasInconsistency) {
      mergeWarning.value = `注意：合并的${chartType.value === 'line' ? '日期' : '名称'}字段不完全一致，可能会影响图表展示效果`
    }
  }

  const mergedData = selectedDatasets.reduce((acc, dataset) => {
    const seriesData = dataset.cleanData.map(item => {
      let newItem = {}
      switch (chartType.value) {
        case 'line':
          newItem = { date: item.date, value: item.value }
          break
        case 'bar':
        case 'pie':
          newItem = { name: item.name, value: item.value }
          break
        case 'scatter':
          newItem = { x: item.x, y: item.y }
          break
        default:
          newItem = { ...item }
      }
      return newItem
    })
    acc.push(...seriesData)
    return acc
  }, [])

  const mergedData2 = selectedDatasets.reduce((accumulatedData, dataset) => {
    const datasetSeriesData = dataset.cleanData.map(item => {
      const { date, value, name, x, y, ...rest } = item
      switch (chartType.value) {
        case 'line':
          return { date, value }
        case 'bar':
        case 'pie':
          return { name, value }
        case 'scatter':
          return { x, y }
        default:
          return item
      }
    })
    accumulatedData.push({
      NAME: dataset.name,
      data: datasetSeriesData,
    })
    return accumulatedData
  }, [])

  updateMerge()
  const mergedNames = selectedDatasets.map(dataset => dataset.name)
  mergeLog(...mergedNames)

  console.log('合并后的数据:', JSON.stringify(mergedData2, null, 2))

  activeDataset.value = {
    id: Date.now(),
    name: selectedDatasets.map(dataset => dataset.name).join(' + '),
    rawData: mergedData, // 用于图表展示
    cleanData: mergedData2, // 分部分用于生成报告
    fields: detectFields(mergedData)
  }

  isMerge.value = true
  chartType.value = chartType.value
}

const cancelMerge = () => {
  activeDataset.value = previousDataset.value
  chartType.value = ''
  setTimeout(() => {
    chartType.value = previousChartType.value
  }, 10)
  datasets.value.forEach(dataset => {
    dataset.isSelected = false
  })
  isMerge.value = false
  nextTick(() => {
    chartType.value = chartType.value
  })
}

// ----------------------图表验证逻辑----------------------
// 检查当前活动数据集是否包含所选图表必需字段
const validateChart = () => {
  if (!activeDataset.value) return false
  if (!activeDataset.value.rawData || activeDataset.value.rawData.length === 0) return false

  const fields = activeDataset.value.fields
  const required = requiredFieldsMapping[chartType.value]
  if (!required) return false

  // 检查必需字段
  let missingFields = required.fields.filter(field => !fields[field])
  return missingFields.length === 0
}

// ----------------------计算当前图表必需字段----------------------
const currentRequiredFields = computed(() => {
  return requiredFieldsMapping[chartType.value]?.fields || []
})

// 将字段名称转换为中文显示（可根据需要扩展）
const fieldDisplayName = (field) => {
  const mapping = {
    date: '日期',
    value: '数值',
    name: '名称',
    parent: '父节点',
    x: 'X 坐标',
    y: 'Y 坐标',
    size: '气泡大小',
    min: '最小值',
    Q1: '第一四分位',
    median: '中位数',
    Q3: '第三四分位',
    max: '最大值',
    outliers: '异常值',
    category: '类别',
    indicator: '指标',
    stage: '阶段',
    source: '来源',
    target: '目标',
    value1: '数值1',
    value2: '数值2',
    yAxis1: 'Y 轴1',
    yAxis2: 'Y 轴2'
  }
  return mapping[field] || field
}

// ----------------------AI 分析----------------------
const isAiAnalyse = ref(false)
const convertToNormalString = (data) => {
  if (!Array.isArray(data)) {
    data = [data]
  }
  const nameMapping = {
    name: '名称',
    value: '值',
    date: '日期',
    x: '维度x',
    y: '维度y'
  }
  return data.map(item =>
    Object.keys(item).map(key => {
      const mappedKey = nameMapping[key] || key
      let value = item[key]
      return value ? `${mappedKey}: ${value}` : ''
    }).filter(str => str !== '').join(', ')
  ).join('; ')
}

const AnalyseResult = ref(null)
const getAnalyse = (data) => {
  post('ai/chat', {
    message: data + "请以整洁格式分析以上数据。"
  }, (data) => {
    AnalyseResult.value = data
  })
}

const IsShowAnalyse = async () => {
  isAiAnalyse.value = !isAiAnalyse.value
  if (isAiAnalyse.value && AnalyseResult.value == null) {
    const dataToAnalyze = activeDataset.value ? activeDataset.value.cleanData : datasets.value
    const normalString = convertToNormalString(JSON.stringify(dataToAnalyze, null, 2))
    await getAnalyse(normalString)
  }
}

// ----------------------记录使用次数 & 日志----------------------
const updateMerge = () => {
  post("api/function/updateMerge", {}, (message) => {
    console.log(message)
  })
}

const mergeLog = (...names) => {
  const namesStr = names.join('、')
  post("api/log/insertLog", {
    type: "多源数据整合分析",
    content: `合并了名为:${namesStr}的数据`,
  }, (msg) => {
    console.log(msg)
  })
}

// ----------------------生成报告功能----------------------
const showReport = ref(false)
const reportTimeRange = ref({})
const saveReport = () => {
  post('api/report/addReport', {
    filename: "数据分析报告--" + filename.value,
    starttime: reportTimeRange.value.start || '未识别',
    endtime: reportTimeRange.value.end || '未识别',
    data: JSON.stringify(activeDataset.value.rawData),
    analyse: AnalyseResult.value || '未使用AI分析',
    type: chartType.value,
  }, (message) => {
    messageApi.success(message)
  })
}
</script>


<template>
  <contextHolder/>
  <div class="min-h-screen bg-gray-50 p-6 pb-24">
    <!-- 主体内容 -->
    <div class="max-w-6xl mx-auto space-y-6">
      <!-- 文件上传区域 -->
      <div class="bg-white rounded-xl shadow-md p-6 transition-all duration-300 hover:shadow-lg">
        <h1 class="text-2xl font-bold text-gray-800 flex items-center mb-4">
          <span class="mr-2">📊</span>
          <span class="bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
            智能数据可视化平台
          </span>
        </h1>
        <div v-if="datasets.length === 0" class="text-center py-12">
          <div class="max-w-md mx-auto">
            <div class="text-6xl mb-6">📁</div>
            <h2 class="text-xl font-semibold text-gray-800 mb-2">立即开始数据探索</h2>
            <p class="text-gray-600 mb-6">
              上传您的数据文件，自动生成可视化图表<br>
              支持格式：CSV, XLSX, JSON
            </p>
          </div>
        </div>
        <div class="bg-white rounded-xl shadow-md p-6 hover:shadow-lg">
          <h2 class="text-2xl font-bold text-gray-800 mb-4 flex items-center">
            <span class="mr-2">📊</span>
            <span class="bg-gradient-to-r from-indigo-500 to-purple-600 text-white p-1 rounded-md">
              上传数据集
            </span>
          </h2>
          <div class="relative w-full h-40 border-2 border-dashed rounded-xl border-gray-300 bg-gray-50 hover:border-gray-500 hover:bg-gray-100 transition-all">
            <input type="file" @change="handleFileUpload"
                   class="absolute inset-0 w-full h-full opacity-0 cursor-pointer" />
            <div class="flex flex-col justify-center items-center h-full text-center space-y-2">
              <span class="text-lg text-gray-600">拖拽文件到此区域上传</span>
              <span class="text-sm text-gray-400">支持 XLSX, CSV, JSON 格式</span>
            </div>
          </div>
          <div v-if="uploadError" class="mt-4 text-red-500 text-sm">
            {{ uploadError }}
          </div>
        </div>
      </div>

      <!-- 数据集选择 -->
      <div v-if="datasets.length" class="bg-white rounded-xl shadow-md p-6">
        <h3 class="text-lg font-semibold mb-4">选择数据集</h3>
        <div class="flex flex-wrap gap-2">
          <div v-for="dataset in datasets" :key="dataset.id" class="relative flex items-center"
               @mouseover="dataset.showTable = true" @mouseleave="dataset.showTable = false">
            <input type="checkbox" v-model="dataset.isSelected" class="mr-2" />
            <button @click="activeDataset = dataset"
                    :class="['px-4 py-2 rounded-full text-sm', activeDataset?.id === dataset.id ? 'bg-blue-500 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200']">
              {{ dataset.name }}
            </button>
            <!-- 悬停数据预览：表格展示并支持修改数据 -->
            <div v-if="dataset.showTable && dataset.cleanData && dataset.cleanData.length"
                 class="absolute left-0 top-full mt-0 w-64 bg-white border rounded shadow-lg p-2 z-10">
              <table class="table-fixed w-full text-sm">
                <thead>
                <tr>
                  <th v-for="(header, index) in getFilteredHeaders(dataset.cleanData)" :key="index"
                      class="px-2 py-1 border-b text-left">
                    {{ header }}
                  </th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="(row, index) in dataset.cleanData.slice(0, 5)" :key="index"
                    class="hover:bg-gray-100">
                  <td v-for="(header, idx) in getFilteredHeaders(dataset.cleanData)" :key="idx"
                      class="px-2 py-1 border-b">
                    <input type="text" v-model="row[header]"
                           class="w-full bg-transparent border border-gray-300 rounded p-1 focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  </td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
        <button v-if="datasets.some(dataset => dataset.isSelected)"
                @click="isMerge ? cancelMerge() : mergeDatasets()"
                class="mt-4 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700">
          {{ isMerge ? '取消合并' : '合并选中的数据集' }}
        </button>
        <div v-if="mergeWarning"
             class="mt-4 p-3 bg-yellow-50 border border-yellow-200 rounded-lg flex items-start">
          <span class="mr-2">⚠️</span>
          <div>
            <p class="text-yellow-700">{{ mergeWarning }}</p>
            <p class="text-yellow-600 text-sm mt-1">
              建议检查以下字段的一致性：
              <span v-if="chartType === 'line' || chartType === 'area'" class="font-medium">日期(date)和数值(value)</span>
              <span v-if="chartType === 'bar' || chartType === 'pie'" class="font-medium">名称(name)和数值(value)</span>
              <span v-if="chartType === 'treemap'" class="font-medium">名称(name)、数值(value)及父节点(parent)</span>
              <span v-if="chartType === 'scatter'" class="font-medium">X、Y 坐标(x,y)</span>
              <span v-if="chartType === 'bubble'" class="font-medium">X、Y 坐标(x,y)及气泡大小(size)</span>
            </p>
          </div>
        </div>
      </div>

      <!-- 当前数据集展示 -->
      <div v-if="activeDataset" class="bg-white rounded-xl shadow-md p-6">
        <h3 class="text-lg font-semibold mb-4">当前数据集</h3>
        <p class="text-gray-700">数据集名称: {{ activeDataset.name }}</p>
      </div>

      <!-- 图表配置与展示 -->
      <div v-if="activeDataset" class="bg-white rounded-xl shadow-md p-6">
        <div class="grid md:grid-cols-2 gap-6">
          <!-- 图表选择区域 -->
          <div>
            <h3 class="text-lg font-semibold mb-4">图表类型</h3>
            <!-- 美化后的滚动区域 -->
            <div class="overflow-x-auto">
              <div class="inline-flex space-x-4 p-2 bg-gray-50 rounded-lg">
                <button v-for="option in chartOptions" :key="option.type"
                        @click="chartType = option.type"
                        :class="['px-6 py-3 rounded-lg border-2 flex flex-col items-center transition-transform duration-200 ease-in-out transform hover:scale-105', chartType === option.type ? 'border-blue-500 bg-blue-100' : 'border-gray-300 bg-white hover:border-blue-300']">
                  <span class="text-3xl mb-2">{{ option.icon }}</span>
                  <span class="capitalize font-medium">{{ option.type }}</span>
                </button>
              </div>
            </div>
          </div>
          <!-- 数据字段提示区域 -->
          <div>
            <div class="p-6 bg-white rounded-xl shadow-lg" v-if="chartType">
              <h4 class="text-lg font-semibold mb-3 border-b pb-2">{{ chartTypeLabels[chartType] }} 要求</h4>
              <div class="space-y-2">
                <div v-for="reqField in currentRequiredFields" :key="reqField" class="flex items-center justify-between">
                  <span class="text-gray-700">{{ fieldDisplayName(reqField) }}</span>
                  <span :class="activeDataset.fields[reqField] ? 'text-green-500' : 'text-red-500'">
                    {{ activeDataset.fields[reqField] ? '✔️ 已识别' : '❌ 缺失' }}
                  </span>
                </div>
              </div>
              <div class="mt-3 text-sm text-gray-500">
                示例格式:
                <code class="bg-gray-100 p-1 rounded">
                  {{ requiredFieldsMapping[chartType].sample }}
                </code>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 图表展示 -->
      <div v-if="validateChart()" class="bg-white rounded-xl shadow-md p-6" style="height: 600px">
        <component :is="chartComponents[chartType]" :rawData="processedData" />
      </div>

      <!-- AI 分析结果区域 -->
      <div v-if="isAiAnalyse" class="bg-white rounded-xl shadow-md p-6 mt-6">
        <h3 class="text-xl font-bold text-gray-800 mb-4">AI 分析结果</h3>
        <div v-if="AnalyseResult" class="prose" v-html="marked(AnalyseResult)"></div>
        <div v-else class="text-gray-500">正在加载 AI 分析结果...</div>
      </div>
    </div>

    <!-- 固定底部工具栏，仅在上传数据后显示 -->
    <div v-if="datasets.length > 0"
         class="fixed bottom-4 left-1/2 transform -translate-x-1/2 space-x-4 z-50">
      <button @click="IsShowAnalyse"
              class="px-4 py-2 bg-green-600 text-white rounded-full shadow-lg hover:bg-green-700 transition">
        🤖 AI 分析 ({{ isAiAnalyse ? '已开启' : '未开启' }})
      </button>
      <button @click="showReport = true"
              class="px-4 py-2 bg-blue-600 text-white rounded-full shadow-lg hover:bg-blue-700 transition">
        📄 生成报告
      </button>
    </div>
  </div>

  <!-- 报告弹窗 -->
  <div v-if="showReport"
       class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50 max-h-screen overflow-y-auto">
    <div class="bg-white rounded-xl w-full max-w-4xl max-h-[90vh] overflow-auto" id="report">
      <div class="p-6">
        <div class="flex justify-between items-center mb-8">
          <h2 class="text-3xl font-bold text-gray-800">数据分析报告</h2>
          <button @click="showReport = false" class="text-gray-500 hover:text-red-600">✕</button>
        </div>
        <section class="mb-8">
          <h3 class="text-xl font-semibold mb-4 border-l-4 border-blue-600 pl-3">👤 用户信息</h3>
          <div class="bg-gray-50 p-4 rounded-lg">
            <p class="text-gray-600">报告生成用户：{{ userStore.user.username }}</p>
            <p class="text-gray-600">生成时间：{{ new Date().toLocaleString() }}</p>
          </div>
        </section>
        <section class="mb-8" v-if="Object.keys(reportTimeRange).length > 0">
          <h3 class="text-xl font-semibold mb-4 border-l-4 border-green-600 pl-3">⏳ 时间范围</h3>
          <div class="bg-gray-50 p-4 rounded-lg">
            <p class="text-gray-600">数据起始时间：{{ reportTimeRange.start }}</p>
            <p class="text-gray-600">数据结束时间：{{ reportTimeRange.end }}</p>
          </div>
        </section>
        <section class="mb-8"
                 v-if="activeDataset && activeDataset.rawData && activeDataset.rawData.length">
          <h3 class="text-xl font-semibold mb-4 border-l-4 border-purple-600 pl-3">📊 完整数据</h3>
          <div class="overflow-x-auto rounded-lg border max-h-96">
            <table class="min-w-full table-fixed">
              <thead class="bg-gray-50">
              <tr>
                <th v-for="(header, index) in getFilteredHeaders(activeDataset.rawData)"
                    :key="index"
                    class="px-4 py-2 text-left text-sm font-medium text-gray-700 border-b">
                  {{ header }}
                </th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="(row, rowIndex) in activeDataset.rawData" :key="rowIndex"
                  class="hover:bg-gray-50">
                <td v-for="(header, colIndex) in getFilteredHeaders(activeDataset.rawData)"
                    :key="colIndex" class="px-4 py-2 text-sm text-gray-600 border-b">
                  {{ row[header] }}
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </section>
        <section class="mb-8">
          <h3 class="text-xl font-semibold mb-4 border-l-4 border-orange-600 pl-3">🤖 AI 分析</h3>
          <div class="bg-gray-50 p-6 rounded-lg markdown-content prose"
               v-html="marked(AnalyseResult || '暂无分析结果')"></div>
        </section>
        <div class="flex justify-end mt-4">
          <button @click="saveReport"
                  class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition">
            📥 保存报告
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.bg-gray-50 {
  background-color: #f9fafb;
}

.text-gray-600 {
  color: #4b5563;
}

.text-gray-800 {
  color: #1f2937;
}

.bg-white {
  background-color: #ffffff;
}

.border-dashed {
  border-style: dashed;
}

.text-center {
  text-align: center;
}
</style>
