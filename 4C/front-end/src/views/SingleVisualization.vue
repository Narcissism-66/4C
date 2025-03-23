<script setup>
import { ref, computed, watch, nextTick } from 'vue';
import * as XLSX from 'xlsx';
import Papa from 'papaparse';
import { ArrowDownTrayIcon, InformationCircleIcon } from '@heroicons/vue/24/outline';
import BarChart from '../components/ECharts/BarChart/BarChart.vue';
import LineChart from '../components/ECharts/LineChart/LineChart.vue';
import PieChart from '../components/ECharts/PieChart/PieChart.vue';
import ScatterChart from '../components/ECharts/ScatterChart/ScatterChart.vue';
import BoxPlotChart from '../components/ECharts/BoxPlotChart/BoxPlotChart.vue';
import BubbleChart from '../components/ECharts/BubbleChart/BubbleChart.vue';
import AreaChart from '../components/ECharts/AreaChart/AreaChart.vue';
import RadarChart from '../components/ECharts/RadarChart/RadarChart.vue';
import FunnelChart from '../components/ECharts/FunnelChart/FunnelChart.vue';
import TreemapChart from '../components/ECharts/TreemapChart/TreemapChart.vue';
import SankeyChart from '../components/ECharts/SankeyChart/SankeyChart.vue';
import DualAxisChart from '../components/ECharts/DualAxisChart/DualAxisChart.vue';
import HeatmapChart from '../components/ECharts/HeatmapChart/HeatmapChart.vue';
import WordCloud from '@/components/ECharts/WordCloud/WordCloud.vue';
import RelationShipChart from '@/components/ECharts/RelationShipChart/RelationShipChart.vue';

import { get, post } from '@/net/index.js';
import { marked } from 'marked';
import { userUserStore } from '@/stores/UserStore.js';
import { message } from 'ant-design-vue';

const [messageApi, contextHolder] = message.useMessage();

// 响应式状态
const userStore = userUserStore();
const chartType = ref('');
const rawData = ref([]);
const recommendedChartType = ref([]);
const showFormatRequirements = ref(false);
const uploadError = ref('');
const isLoading = ref(false);
const isAiAnalyse = ref(false);
let filename = ref('');

// 增加一个用于强制刷新图表的 key
const chartKey = ref(0);

// 图表组件映射（key 与推荐函数返回值对应）
const chartComponents = {
  line: { simple: LineChart },
  area: { simple: AreaChart },
  bar: { simple: BarChart },
  pie: { simple: PieChart },
  scatter: { simple: ScatterChart },
  bubble: { simple: BubbleChart },
  box: { simple: BoxPlotChart },
  radar: { simple: RadarChart },
  funnel: { simple: FunnelChart },
  treemap: { simple: TreemapChart },
  sankey: { simple: SankeyChart },
  dual: { simple: DualAxisChart },
  heatmap: { simple: HeatmapChart },
  wordcloud: { simple: WordCloud },
  relationship: { simple: RelationShipChart }  // 新增关系图
};

// 当前图表组件（添加 key 强制刷新）
const CurrentChartComponent = computed(() => {
  return chartType.value ? chartComponents[chartType.value]?.simple : null;
});

// 字段映射
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

// 当数据发生修改后调用，重新分析并更新图表
const handleDataChange = () => {
  processData(rawData.value);
};

// 同时增加深度 watch，当 rawData 变化时触发
watch(rawData, () => {
  processData(rawData.value);
}, { deep: true });

//-----------------------------------------------------上传的数据处理---------------------------------------------
const processData = (data) => {
  if (data.length < 3) {
    uploadError.value = '数据量过少（至少需要3条记录）';
    return;
  }
  const analysisRec = recommendCharts(data);
  recommendedChartType.value = analysisRec;
  // 如果当前选择的图表类型不在推荐中，则自动切换为第一个推荐
  if (!analysisRec.includes(chartType.value)) {
    chartType.value = analysisRec[0] || '';
  }
  // 如数据中存在日期字段，计算时间范围（可用于报告）
  if (analysisRec.includes('line') || analysisRec.includes('area') || analysisRec.includes('dual')) {
    const dates = data.map(d => new Date(d.date)).filter(d => !isNaN(d));
    if (dates.length > 0) {
      dates.sort((a, b) => a - b);
      reportTimeRange.value = {
        start: formatDate(dates[0]),
        end: formatDate(dates[dates.length - 1])
      };
    }
  }
  nextTick(() => {
    chartKey.value++;
  });
  console.log(rawData.value);
};

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
    rawData.value = formatData(raw);
    console.log('标准化数据:', JSON.stringify(rawData.value, null, 2));
    updateSingle();
    singleLog();
    processData(rawData.value);
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

const excelDateToJSDate = (serial) => {
  const excelStartDate = new Date(1899, 11, 30); // Excel 起始时间
  return new Date(excelStartDate.getTime() + serial * 86400000); // 86400000 = 1天的毫秒数
};

const formatData = (rawData) => {
  if (!Array.isArray(rawData)) {
    throw new Error("❌ 数据格式错误：JSON 数据必须是数组！");
  }

  return rawData.map(item => {
    const formattedItem = {};
    Object.entries(item).forEach(([key, value]) => {
      const cleanKey = key.toString().trim().toLowerCase().replace(/[\s_]+/g, '_');
      let processedValue = value;

      // **转换 Excel 日期**
      if (typeof processedValue === "number" && processedValue > 40000) {
        processedValue = excelDateToJSDate(processedValue);
        formattedItem.date = formatDate(processedValue); // 格式化日期
        return;
      }

      // **尝试解析日期**
      if (typeof processedValue === "string") {
        const timestamp = Date.parse(processedValue);
        if (!isNaN(timestamp)) {
          processedValue = formatDate(new Date(timestamp));
          formattedItem.date = processedValue;
          return;
        }
      }

      // **将数值型字符串转换为数字**
      if (typeof processedValue === "string" && !isNaN(processedValue)) {
        processedValue = parseFloat(processedValue);
      }

      // **字段映射（标准化键名）**
      const standardKey = Object.entries(fieldMapping.value).find(([_, aliases]) =>
        aliases.some(alias => cleanKey === alias)
      )?.[0] || cleanKey;
      formattedItem[standardKey] = processedValue;
    });

    return formattedItem;
  }).filter(item => Object.keys(item).length > 0);
};


const formatDate = (date) => {
  if (!(date instanceof Date)) return date;
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}/${month}/${day}`;
};
//-----------------------------------------------------上传的数据处理---------------------------------------------

//-------------------------------------------------图表推荐逻辑------------------------------------------------------
// 辅助函数：检查数据数组中指定字段是否所有记录都为非空且符合预期类型的值
const hasAllValidValues = (data, key, type) => {
  return data.every(item => {
    const value = item[key];
    if (value === undefined || value === null) return false;
    if (typeof value === "string" && value.trim() === "") return false;
    if (type === "string") return typeof value === "string";
    if (type === "number") return typeof value === "number";
    if (type === "array") return Array.isArray(value);
    return false;
  });
};

// 针对关系图，允许数据中部分记录为空，但必须至少有一条符合要求
const hasValidRelationshipData = (data) => {
  const validNode = data.some(item =>
    item.type === 'node' &&
    (typeof item.id === 'number') && // 直接检查是否为数字
    (typeof item.name === 'string' && item.name.trim() !== '') && // name 是字符串，调用 trim()
    (typeof item.symbolsize === 'number') && // symbolsize 是数字，直接检查
    (typeof item.category === 'string' && item.category.trim() !== '') // category 是字符串，调用 trim()
  );
  const validLink = data.some(item =>
    item.type === 'link' &&
    (typeof item.source === 'number') && // source 是数字，直接检查
    (typeof item.target === 'number') && // target 是数字，直接检查
    (typeof item.value === 'number') // value 是数字，直接检查
  );
  return validNode && validLink;
};

const recommendCharts = (data) => {
  if (!data.length) {
    console.log("No data available.");
    return [];
  }
  // 获取所有字段（统一转换为小写）
  const sample = data[0];
  const keys = Object.keys(sample).map(key => key.toLowerCase());
  console.log("Sample keys:", keys);
  const rec = [];

  // 折线图和面积图要求所有记录均有非空的 date（字符串）和 value（数字）字段
  if (keys.includes('date') && keys.includes('value') &&
    hasAllValidValues(data, 'date', 'string') &&
    hasAllValidValues(data, 'value', 'number')) {
    rec.push('line', 'area');
  }
  // 柱状图和饼图要求所有记录均有非空的 name（字符串）和 value（数字）字段
  if (keys.includes('name') && keys.includes('value') &&
    hasAllValidValues(data, 'name', 'string') &&
    hasAllValidValues(data, 'value', 'number')) {
    rec.push('bar', 'pie');
  }
  // 散点图：要求所有记录均有非空的 x 和 y（数字）字段，同时不存在 size 字段
  if (keys.includes('x') && keys.includes('y') &&
    hasAllValidValues(data, 'x', 'number') &&
    hasAllValidValues(data, 'y', 'number') &&
    !keys.includes('size')) {
    rec.push('scatter');
  }
  // 气泡图：要求所有记录均有非空的 x、y（数字）以及 size（数字）字段
  if (keys.includes('x') && keys.includes('y') && keys.includes('size') &&
    hasAllValidValues(data, 'x', 'number') && hasAllValidValues(data, 'y', 'number') && hasAllValidValues(data, 'size', 'number')) {
    rec.push('bubble');
  }
  // 箱型图：要求所有记录均有非空的 name（字符串）、min、q1、median、q3、max（数字）及 outliers（数组）字段
  if (keys.includes('name') && hasAllValidValues(data, 'name', 'string') &&
    keys.includes('min') && hasAllValidValues(data, 'min', 'number') &&
    keys.includes('q1') && hasAllValidValues(data, 'q1', 'number') &&
    keys.includes('median') && hasAllValidValues(data, 'median', 'number') &&
    keys.includes('q3') && hasAllValidValues(data, 'q3', 'number') &&
    keys.includes('max') && hasAllValidValues(data, 'max', 'number') &&
    keys.includes('outliers') && hasAllValidValues(data, 'outliers', 'string')) {
    rec.push('box');
  }
  // 雷达图：要求所有记录均有非空的 category、indicator（字符串）以及 value（数字）字段
  if (keys.includes('category') && keys.includes('indicator') && keys.includes('value') &&
    hasAllValidValues(data, 'category', 'string') &&
    hasAllValidValues(data, 'indicator', 'string') &&
    hasAllValidValues(data, 'value', 'number')) {
    rec.push('radar');
  }
  // 漏斗图：要求所有记录均有非空的 stage（字符串）和 value（数字）字段
  if (keys.includes('stage') && keys.includes('value') &&
    hasAllValidValues(data, 'stage', 'string') &&
    hasAllValidValues(data, 'value', 'number')) {
    rec.push('funnel');
  }
  // 矩形树图：要求所有记录均有非空的 name、parent（字符串）以及 value（数字）字段
  if (keys.includes('name') && keys.includes('value') && keys.includes('parent') &&
    hasAllValidValues(data, 'name', 'string') &&
    hasAllValidValues(data, 'value', 'number') &&
    hasAllValidValues(data, 'parent', 'string')) {
    rec.push('treemap');
  }
  // 桑基图：要求所有记录均有非空的 source、target（字符串）和 value（数字）字段
  if (keys.includes('source') && keys.includes('target') && keys.includes('value') &&
    hasAllValidValues(data, 'source', 'string') &&
    hasAllValidValues(data, 'target', 'string') &&
    hasAllValidValues(data, 'value', 'number')) {
    rec.push('sankey');
  }
  // 双轴图：要求所有记录均有非空的 date（字符串）、value1、value2（数字）以及 yaxis1、yaxis2（字符串）字段
  if (keys.includes('date') && hasAllValidValues(data, 'date', 'string') &&
    keys.includes('value1') && hasAllValidValues(data, 'value1', 'number') &&
    keys.includes('value2') && hasAllValidValues(data, 'value2', 'number') &&
    keys.includes('yaxis1') && hasAllValidValues(data, 'yaxis1', 'string') &&
    keys.includes('yaxis2') && hasAllValidValues(data, 'yaxis2', 'string')) {
    rec.push('dual');
  }
  // 热力图：要求所有记录均有非空的 x、y（字符串）和 value（数字）字段
  if (keys.includes('x') && keys.includes('y') && keys.includes('value') &&
    hasAllValidValues(data, 'x', 'string') &&
    hasAllValidValues(data, 'y', 'string') &&
    hasAllValidValues(data, 'value', 'number')) {
    rec.push('heatmap');
  }
  // 词云图：要求所有记录均有非空的 name（字符串）和 value（数字）字段
  if (keys.includes('name') && keys.includes('value') &&
    hasAllValidValues(data, 'name', 'string') &&
    hasAllValidValues(data, 'value', 'number')) {
    rec.push('wordcloud');
  }
  // 关系图：调用局部检测函数，不要求所有记录都必须有数据
  if (hasValidRelationshipData(data)) {
    rec.push('relationship');
  }

  console.log("Recommended chart types:", rec);
  return [...new Set(rec)];
};



//-------------------------------------------------图表推荐逻辑------------------------------------------------------

//-------------------------------图表说明------------------------------
const getChartDescription = (type) => {
  switch (type) {
    case 'line': return '展示数据随时间的变化趋势';
    case 'area': return '适用于累积趋势展示';
    case 'bar': return '适用于分类数据的比较';
    case 'pie': return '适用于显示各部分占比';
    case 'scatter': return '展示两个变量之间的关系';
    case 'bubble': return '通过气泡大小显示第三变量';
    case 'box': return '展示数据分布及异常值';
    case 'radar': return '用于多维指标的比较';
    case 'funnel': return '展示流程各阶段转化率';
    case 'treemap': return '以面积展示分层数据';
    case 'sankey': return '展示数据流动或转化过程';
    case 'dual': return '对比不同量纲的数据';
    case 'heatmap': return '通过颜色深浅展示数据密度';
    case 'wordcloud': return '以词频大小展示关键词';
    case 'relationship': return '展示节点间的关系';
    default: return '';
  }
};
//-------------------------------图表说明------------------------------

//----------------------------------------------------------AI分析------------------------------------------------------
const convertToNormalString = (data) => {
  if (!Array.isArray(data)) {
    data = [data];
  }
  const nameMapping = {
    name: '名称',
    value: '值',
    date: '日期',
    x: '维度x',
    y: '维度y'
  };
  return data.map(item =>
    Object.keys(item).map(key => {
      const mappedKey = nameMapping[key] || key;
      let value = item[key];
      return value ? `${mappedKey}: ${value}` : '';
    }).filter(str => str !== '').join(', ')
  ).join('; ');
};

const AnalyseResult = ref(null);
const getAnalyse = (data) => {
  post('ai/chat', {
    message: data + "这是一个"+chartType.value+"的数据"+"请解释以上数据不需要解释它的格式,同时对数据进行预测。最后我只要解释的内容和预测结果，解释的内容与预测结果二者之间用虚线隔开（明显一点），解释内容和预测结果两个小标题要突出显示"
  }, (data) => {
    AnalyseResult.value = data;
    updateAI();
    AILog();
    console.log(isAiAnalyse.value);
    console.log(AnalyseResult.value);
  });
};

const IsShowResults = ref(false);
const IsShowAnalyse = async () => {
  isAiAnalyse.value = !isAiAnalyse.value;
  if (isAiAnalyse.value && AnalyseResult.value == null) {
    const normalString = convertToNormalString(JSON.stringify(rawData.value, null, 2));
    await getAnalyse(normalString);
    IsShowResults.value = true;
  }
};
//----------------------------------------------------------AI分析------------------------------------------------------

//-------------------------------------------------------生成报告------------------------------------------------
const showReport = ref(false);
const reportTimeRange = ref({});
const saveReport = () => {
  post('api/report/addReport', {
    filename: "数据分析报告--" + filename.value,
    starttime: reportTimeRange.value.start || '未识别',
    endtime: reportTimeRange.value.end || '未识别',
    data: JSON.stringify(rawData.value),
    analyse: AnalyseResult.value || '未使用AI分析',
    type: chartType.value,
  }, (message) => {
    messageApi.success(message);
    setTimeout(function(){
      showReport.value=false;
    },1000)
    updateReport();
    reportLog();
  });
};
//-------------------------------------------------------生成报告------------------------------------------------

//-------------------------------------------------------------下载模板---------------------------------------------------
const downloadTemplate = (templateType) => {
  let type = templateType || chartType.value;
  let templateData = [];
  switch (type) {
    case 'line':
    case 'area':
      templateData = [{ date: '2023/01/01', value: 100 }];
      break;
    case 'bar':
    case 'pie':
      templateData = [{ name: '类别A', value: 75 }];
      break;
    case 'scatter':
      templateData = [{ x: 5.2, y: 8.4 }];
      break;
    case 'bubble':
      templateData = [{ x: 5.2, y: 8.4, size: 10 }];
      break;
    case 'box':
      templateData = [{ name: '组A', min: 10, Q1: 15, median: 20, Q3: 25, max: 30, outliers: '[5,35]' }];
      break;
    case 'radar':
      templateData = [{ category: '产品A', indicator: '质量', value: 80 }];
      break;
    case 'funnel':
      templateData = [{ stage: '访问', value: 100 }];
      break;
    case 'treemap':
      templateData = [{ name: '根节点', value: 100, parent: '根节点' }];
      break;
    case 'sankey':
      templateData = [{ source: 'A', target: 'B', value: 80 }];
      break;
    case 'dual':
      templateData = [{ date: '2023/01/01', value1: 100, value2: 80, yAxis1: '销量', yAxis2: '温度' }];
      break;
    case 'heatmap':
      templateData = [{ x: '周一', y: '上午', value: 10 }, { x: '周一', y: '下午', value: 20 }];
      break;
    case 'wordcloud':
      templateData = [{ name: 'Vue', value: 100 }, { name: 'ECharts', value: 80 }];
      break;
    case 'relationship':
      templateData = [
        { type: 'node', id: 1, name: '节点1', symbolsize: 40, category: 'A', source: null, target: null, value: null },
        { type: 'node', id: 2, name: '节点2', symbolsize: 50, category: 'B', source: null, target: null, value: null },
        { type: 'link', id: null, name: null, symbolsize: null, category: '', source: null, target: null, value: null }
      ];
      break;
    default:
      templateData = [{ name: '示例', value: 100, date: '2023/01/01', x: 5.2, y: 8.4 }];
      break;
  }
  const ws = XLSX.utils.json_to_sheet(templateData);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, "Template");
  const wbout = XLSX.write(wb, { bookType: "xlsx", type: "array" });
  const blob = new Blob([wbout], { type: "application/octet-stream" });
  let fileName = `${type}模板.xlsx`;
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
  messageApi.success("模板已下载: " + fileName);
};
//-------------------------------------------------------------下载模板---------------------------------------------------

//--------------------------------------------日志----------------------------------------------------------
const updateSingle = () => {
  post("api/function/updateSingle", {}, (message) => {
    console.log(message);
  });
};
const updateAI = () => {
  post("api/function/updateAI", {}, (message) => {
    console.log(message);
  });
};
const updateReport = () => {
  post("api/function/updateReport", {}, (message) => {
    console.log(message);
  });
};

const singleLog = () => {
  post("api/log/insertLog", {
    type: "单文件深度解析",
    content: `上传了名为 ${filename.value} 的数据 `
  }, (message) => {
    console.log(filename.value + message);
  });
};
const AILog = () => {
  post("api/log/insertLog", {
    type: "AI分析",
    content: `AI 分析了名为 ${filename.value} 的数据 `
  }, (message) => {
    console.log(message);
  });
};
const reportLog = () => {
  post("api/log/insertLog", {
    type: "生成报告",
    content: `生成了名为 ${filename.value} 的数据 的报告 `
  }, (message) => {
    console.log(message);
  });
};
//--------------------------------------------日志----------------------------------------------------------
</script>

<template>
  <contextHolder/>
  <div class="container mx-auto p-6">
    <!-- 文件上传区 -->
    <div class="bg-white shadow-lg rounded-xl p-6 mb-6">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-2xl font-bold text-gray-800">📊 数据可视化</h2>
        <!-- AI分析图标 -->
        <div v-if="rawData.length > 0" class="mb-6 flex justify-center">
          <button
            @click="IsShowAnalyse"
            class="flex items-center justify-center p-3 bg-blue-600 text-white rounded-lg shadow-md hover:bg-blue-700 transition"
          >
            <span class="mr-2">🤖</span>
            <span v-if="isAiAnalyse===true" class="text-sm font-medium">AI分析已开启</span>
            <span v-else class="text-sm font-medium">AI分析未开启</span>
          </button>
        </div>
        <!-- 生成报告按钮 -->
        <div v-if="rawData.length > 0" class="mb-6 flex justify-center">
          <button
            @click="showReport = true"
            class="px-6 py-3 bg-green-600 text-white rounded-lg shadow-md hover:bg-green-700 transition"
          >
            📄 一键保存分析报告
          </button>
        </div>
        <button class="text-gray-500 flex items-center gap-4">
          <span class="text-gray-500 hover:text-blue-600 flex items-center text-sm" @click="showFormatRequirements = !showFormatRequirements">
            <InformationCircleIcon class="w-5 h-5 mr-1"/>
            <span>数据格式要求</span>
          </span>
          <span class="relative inline-block group">
            <span class="text-gray-500 hover:text-blue-600 flex items-center text-sm cursor-pointer">
              <ArrowDownTrayIcon class="w-5 h-5 mr-1"/>
              <span>下载模板</span>
            </span>
            <span class="absolute left-0 mt-1 w-40 bg-white border border-gray-200 rounded shadow-lg opacity-0 group-hover:opacity-100 transition-opacity duration-200 z-10">
              <span class="py-1">
                <span @click="downloadTemplate('line')" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 cursor-pointer">
                  折线图模板
                </span>
                <span @click="downloadTemplate('area')" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 cursor-pointer">
                  面积图模板
                </span>
                <span @click="downloadTemplate('bar')" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 cursor-pointer">
                  柱状图模板
                </span>
                <span @click="downloadTemplate('pie')" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 cursor-pointer">
                  饼图模板
                </span>
                <span @click="downloadTemplate('scatter')" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 cursor-pointer">
                  散点图模板
                </span>
                <span @click="downloadTemplate('bubble')" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 cursor-pointer">
                  气泡图模板
                </span>
                <span @click="downloadTemplate('box')" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 cursor-pointer">
                  箱型图模板
                </span>
                <span @click="downloadTemplate('radar')" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 cursor-pointer">
                  雷达图模板
                </span>
                <span @click="downloadTemplate('funnel')" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 cursor-pointer">
                  漏斗图模板
                </span>
                <span @click="downloadTemplate('treemap')" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 cursor-pointer">
                  树图模板
                </span>
                <span @click="downloadTemplate('sankey')" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 cursor-pointer">
                  桑基图模板
                </span>
                <span @click="downloadTemplate('dual')" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 cursor-pointer">
                  双轴图模板
                </span>
                <span @click="downloadTemplate('heatmap')" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 cursor-pointer">
                  热力图模板
                </span>
                <span @click="downloadTemplate('wordcloud')" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 cursor-pointer">
                  词云模板
                </span>
                <!-- 关系图模板，如需要可在downloadTemplate中扩展 -->
                <span @click="downloadTemplate('relationship')" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 cursor-pointer">
                  关系图模板
                </span>
              </span>
            </span>
          </span>
        </button>
      </div>

      <!-- 数据格式要求 -->
      <div v-if="showFormatRequirements" class="p-4 bg-gray-50 rounded-lg mb-4">
        <h4 class="font-semibold mb-3">📝 各图表数据格式要求：</h4>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
          <!-- 折线图、面积图 -->
          <div class="p-3 bg-white rounded-lg shadow">
            <div class="font-medium mb-2 text-blue-600">折线图 / 面积图</div>
            <ul class="list-disc pl-5 space-y-1">
              <li>必需字段：date（日期--字符串）</li>
              <li>必需字段：value（数值）</li>
              <li>示例格式：{ date: '2023-01-01', value: 150 }</li>
            </ul>
          </div>
          <!-- 柱状图、饼图、树图 -->
          <div class="p-3 bg-white rounded-lg shadow">
            <div class="font-medium mb-2 text-green-600">柱状图 / 饼图 / 树图</div>
            <ul class="list-disc pl-5 space-y-1">
              <li>必需字段：name（名称--字符串）</li>
              <li>必需字段：value（数值）</li>
              <li>示例格式：{ name: '类别A', value: 75 }</li>
              <li>树图需额外字段：parent（父节点，空为根节点）</li>
            </ul>
          </div>
          <!-- 散点图、气泡图 -->
          <div class="p-3 bg-white rounded-lg shadow">
            <div class="font-medium mb-2 text-purple-600">散点图 / 气泡图</div>
            <ul class="list-disc pl-5 space-y-1">
              <li>散点图必需字段：x（横坐标）, y（纵坐标）</li>
              <li>气泡图需额外字段：size（气泡大小）</li>
              <li>示例格式：{ x: 5.2, y: 8.4, size: 10 }</li>
            </ul>
          </div>
          <!-- 箱型图 -->
          <div class="p-3 bg-white rounded-lg shadow">
            <div class="font-medium mb-2 text-gray-600">箱型图</div>
            <ul class="list-disc pl-5 space-y-1">
              <li>必需字段：name, min, Q1, median, Q3, max, outliers</li>
              <li>示例格式：{ name: '组A', min: 10, Q1: 15, median: 20, Q3: 25, max: 30, outliers: [5,35] }</li>
            </ul>
          </div>
          <!-- 雷达图 -->
          <div class="p-3 bg-white rounded-lg shadow">
            <div class="font-medium mb-2 text-orange-600">雷达图</div>
            <ul class="list-disc pl-5 space-y-1">
              <li>必需字段：category, indicator, value</li>
              <li>示例格式：{ category: '产品A', indicator: '质量', value: 80 }</li>
            </ul>
          </div>
          <!-- 漏斗图 -->
          <div class="p-3 bg-white rounded-lg shadow">
            <div class="font-medium mb-2 text-red-600">漏斗图</div>
            <ul class="list-disc pl-5 space-y-1">
              <li>必需字段：stage, value</li>
              <li>示例格式：{ stage: '访问', value: 100 }</li>
            </ul>
          </div>
          <!-- 桑基图 -->
          <div class="p-3 bg-white rounded-lg shadow">
            <div class="font-medium mb-2 text-teal-600">桑基图</div>
            <ul class="list-disc pl-5 space-y-1">
              <li>必需字段：source, target, value</li>
              <li>示例格式：{ source: 'A', target: 'B', value: 80 }</li>
            </ul>
          </div>
          <!-- 双轴图 -->
          <div class="p-3 bg-white rounded-lg shadow">
            <div class="font-medium mb-2 text-indigo-600">双轴图</div>
            <ul class="list-disc pl-5 space-y-1">
              <li>必需字段：date, value1, value2, yAxis1, yAxis2</li>
              <li>示例格式：{ date: '2023-01-01', value1: 100, value2: 80, yAxis1: '销量', yAxis2: '温度' }</li>
            </ul>
          </div>
          <!-- 热力图 -->
          <div class="p-3 bg-white rounded-lg shadow">
            <div class="font-medium mb-2 text-blue-700">热力图</div>
            <ul class="list-disc pl-5 space-y-1">
              <li>必需字段：x, y, value</li>
              <li>示例格式：{ x: '周一', y: '上午', value: 10 }</li>
            </ul>
          </div>
          <!-- 词云 -->
          <div class="p-3 bg-white rounded-lg shadow">
            <div class="font-medium mb-2 text-pink-600">词云</div>
            <ul class="list-disc pl-5 space-y-1">
              <li>必需字段：name, value</li>
              <li>示例格式：{ name: 'Vue', value: 100 }</li>
            </ul>
          </div>
          <!-- 关系图 -->
          <!-- 关系图 -->
          <div class="p-3 bg-white rounded-lg shadow">
            <div class="font-medium mb-2 text-gray-800">关系图</div>
            <ul class="list-disc pl-5 space-y-1">
              <li>数据格式要求：</li>
              <li>type: 节点类型（字符串）</li>
              <li>id: 节点唯一标识（数字）</li>
              <li>name: 节点名称（字符串）</li>
              <li>symbolsize: 节点大小（数字）</li>
              <li>category: 节点类别（字符串）</li>
              <li>source: 边的起始节点（数字）</li>
              <li>target: 边的目标节点（数字）</li>
              <li>value: 边的权重（数字）</li>
              <li>示例格式：</li>
              <li> type: 'node', id: 1, name: '节点1', symbolsize: 50, category: 'A' </li>
              <li>type: 'link', source: 1, target: 2, value: 10 </li>
            </ul>
          </div>
        </div>
      </div>

      <!-- 文件上传 -->
      <div class="relative mb-4">
        <input
          type="file"
          @change="handleFileUpload"
          class="w-full px-4 py-2 border-2 border-dashed border-gray-300 rounded-lg
                hover:border-blue-500 focus:outline-none focus:border-blue-500
                file:mr-4 file:py-2 file:px-4 file:rounded-md
                file:border-0 file:bg-blue-50 file:text-blue-700
                hover:file:bg-blue-100 transition-colors"
          accept=".xlsx,.xls,.csv,.json"
          :disabled="isLoading"
        >
        <div v-if="isLoading" class="absolute inset-0 bg-white/50 flex items-center justify-center">
          <div class="animate-spin text-blue-500">⌛</div>
        </div>
      </div>
      <!-- 错误提示 -->
      <div v-if="uploadError" class="p-3 bg-red-50 text-red-700 rounded-lg mb-4">
        ⚠️ {{ uploadError }}
      </div>

      <!-- 报告展示模态框 -->
      <div v-if="showReport" class="fixed mt-20 inset-0 bg-black/50 flex items-center justify-center p-4 z-50 max-h-screen overflow-y-auto">
        <div class="bg-white rounded-xl w-full max-w-4xl max-h-[90vh] overflow-auto" id="report">
          <div class="p-6">
            <div class="flex justify-between items-center mb-8">
              <h2 class="text-3xl font-bold text-gray-800">数据分析报告</h2>
              <button @click="showReport = false" class="text-gray-500 hover:text-red-600">
                ✕
              </button>
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
            <section class="mb-8">
              <h3 class="text-xl font-semibold mb-4 border-l-4 border-purple-600 pl-3">📊 完整数据</h3>
              <div class="overflow-x-auto rounded-lg border max-h-96">
                <table class="min-w-full">
                  <thead class="bg-gray-50">
                  <tr>
                    <th
                      v-for="(header, index) in Object.keys(rawData[0])"
                      :key="index"
                      class="px-4 py-2 text-left text-sm font-medium text-gray-700 border-b"
                    >
                      {{ header }}
                    </th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr
                    v-for="(row, rowIndex) in rawData"
                    :key="rowIndex"
                    class="hover:bg-gray-50"
                  >
                    <td
                      v-for="(key, colIndex) in Object.keys(rawData[0])"
                      :key="colIndex"
                      class="px-4 py-2 text-sm text-gray-600 border-b"
                    >
                      <input
                        type="text"
                        v-model="rawData[rowIndex][key]"
                        @change="handleDataChange"
                        class="w-full border p-1"
                      />
                    </td>
                  </tr>
                  </tbody>
                </table>
              </div>
            </section>

              <h3 class="text-xl font-semibold mb-4 p-2">🎨 图表展示</h3>
              <div class="flex items-center justify-center bg-gray-50 rounded-xl" style="height: 500px">
                <component
                  :is="CurrentChartComponent"
                  v-if="CurrentChartComponent"
                  :rawData="rawData"
                  :key="chartKey"
                />
              </div>

            <section class="mb-8 mt-8">
              <h3 class="text-xl font-semibold mb-4 border-l-4 border-orange-600 pl-3">🤖 AI分析</h3>
              <div class="bg-gray-50 p-6 rounded-lg markdown-content" v-html="marked(AnalyseResult || '暂无分析结果')"></div>
            </section>
            <div class="flex justify-end mt-4">
              <button @click="saveReport" class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
                📥保存报告
              </button>
            </div>
          </div>
        </div>
      </div>
      <!-- 数据预览：完整数据展示，所有行均可编辑 -->
      <div v-if="rawData.length > 0" class="mt-6 overflow-y-auto scrollbar-hide" style="max-height: 400px">
        <h3 class="text-lg font-semibold mb-3">数据展示</h3>
        <div class="overflow-x-auto rounded-lg border">
          <table class="min-w-full">
            <thead class="bg-gray-50">
            <tr>
              <th
                v-for="(header, index) in Object.keys(rawData[0])"
                :key="index"
                class="px-4 py-2 text-left text-sm font-medium text-gray-700 border-b"
              >
                {{ header }}
              </th>
            </tr>
            </thead>
            <tbody>
            <tr
              v-for="(row, rowIndex) in rawData"
              :key="rowIndex"
              class="hover:bg-gray-50"
            >
              <td
                v-for="(key, colIndex) in Object.keys(rawData[0])"
                :key="colIndex"
                class="px-4 py-2 text-sm text-gray-600 border-b"
              >
                <input
                  type="text"
                  v-model="rawData[rowIndex][key]"
                  @change="handleDataChange"
                  class="w-full border p-1"
                />
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 图表选择区 -->
    <div class="flex flex-wrap gap-4 mb-6 w-full">
      <!-- 图表推荐 -->
      <div class="bg-white shadow-lg rounded-xl p-6 w-full">
        <h3 class="text-xl font-semibold mb-4">🤖 智能推荐</h3>
        <div class="space-y-2">
          <div
            v-for="type in recommendedChartType"
            :key="type"
            class="flex items-center p-3 hover:bg-gray-50 rounded-lg cursor-pointer"
            @click="chartType = type"
            :class="{ 'bg-blue-50': chartType === type }"
          >
            <span class="text-lg mr-3">
              {{
                type === 'line' ? '📈' :
                  type === 'area' ? '📉' :
                    type === 'bar' ? '📊' :
                      type === 'pie' ? '🥧' :
                        type === 'scatter' ? '🔵' :
                          type === 'bubble' ? '🟢' :
                            type === 'box' ? '📦' :
                              type === 'radar' ? '🕸️' :
                                type === 'funnel' ? '🧭' :
                                  type === 'treemap' ? '🌳' :
                                    type === 'sankey' ? '🔀' :
                                      type === 'dual' ? '🌓' :
                                        type === 'heatmap' ? '🔥' :
                                          type === 'wordcloud' ? '☁️' :
                                            type === 'relationship' ? '🔗' : type
              }}
            </span>
            <div>
              <p class="font-medium">
                {{
                  type === 'line' ? '折线图' :
                    type === 'area' ? '面积图' :
                      type === 'bar' ? '柱状图' :
                        type === 'pie' ? '饼图' :
                          type === 'scatter' ? '散点图' :
                            type === 'bubble' ? '气泡图' :
                              type === 'box' ? '箱型图' :
                                type === 'radar' ? '雷达图' :
                                  type === 'funnel' ? '漏斗图' :
                                    type === 'treemap' ? '树图' :
                                      type === 'sankey' ? '桑基图' :
                                        type === 'dual' ? '双轴图' :
                                          type === 'heatmap' ? '热力图' :
                                            type === 'wordcloud' ? '词云' :
                                              type === 'relationship' ? '关系图' : type
                }}
              </p>
              <p class="text-sm text-gray-500">{{ getChartDescription(type) }}</p>
            </div>
          </div>
          <p v-if="recommendedChartType.length === 0" class="text-gray-500">
            暂无推荐图表，请检查数据格式
          </p>
        </div>
      </div>

      <!-- 图表展示区域 -->
      <div class="bg-white shadow-lg rounded-xl p-6 w-full">
        <h3 class="text-xl font-semibold mb-4">🎨 图表展示</h3>
        <div class="flex items-center justify-center bg-gray-50 rounded-xl" style="height: 560px">
          <component
            :is="CurrentChartComponent"
            v-if="CurrentChartComponent"
            :rawData="rawData"
            :key="chartKey"
          />
          <div v-if="!CurrentChartComponent" class="text-gray-400">
            {{ rawData.length > 0 ? '请从上方选择图表类型' : '📁 请先上传数据文件' }}
          </div>
        </div>
      </div>
    </div>

    <!-- AI分析结果展示区 -->
    <div v-if="isAiAnalyse===true" class="mt-12 bg-white shadow-xl rounded-xl p-6">
      <h3 class="text-xl font-semibold text-gray-800 mb-4">AI分析结果</h3>
      <div class="space-y-4">
        <div v-if="AnalyseResult==null">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" class="w-10 h-10">
            <g fill="currentColor">
              <circle cx="12" cy="3.5" r="1.5">
                <animateTransform attributeName="transform" calcMode="discrete" dur="2.4s" repeatCount="indefinite" type="rotate" values="0 12 12;90 12 12;180 12 12;270 12 12"/>
                <animate attributeName="opacity" dur="0.6s" repeatCount="indefinite" values="1;1;0"/>
              </circle>
              <circle cx="12" cy="3.5" r="1.5" transform="rotate(30 12 12)">
                <animateTransform attributeName="transform" begin="0.2s" calcMode="discrete" dur="2.4s" repeatCount="indefinite" type="rotate" values="30 12 12;120 12 12;210 12 12;300 12 12"/>
                <animate attributeName="opacity" begin="0.2s" dur="0.6s" repeatCount="indefinite" values="1;1;0"/>
              </circle>
              <circle cx="12" cy="3.5" r="1.5" transform="rotate(60 12 12)">
                <animateTransform attributeName="transform" begin="0.4s" calcMode="discrete" dur="2.4s" repeatCount="indefinite" type="rotate" values="60 12 12;150 12 12;240 12 12;330 12 12"/>
                <animate attributeName="opacity" begin="0.4s" dur="0.6s" repeatCount="indefinite" values="1;1;0"/>
              </circle>
            </g>
          </svg>
        </div>
        <div v-else-if="IsShowResults===true" v-html="marked(AnalyseResult)" class="markdown-content"></div>
      </div>
    </div>
    <!-- AI分析结果展示区 -->
  </div>
</template>


<style scoped>
.container {
  max-width: 1200px;
}
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.markdown-content p {
  margin-bottom: 1rem;
}
</style>


<style>
@media print {
  @page {
    size: A4 portrait;
    margin: 15mm 10mm;
  }
  body {
    margin: 0 !important;
    padding: 0 !important;
    zoom: 90%;
  }
  .chart-container {
    page-break-inside: avoid;
    max-height: 250mm !important;
  }
  table {
    page-break-inside: auto;
  }
  tr {
    page-break-inside: avoid;
  }
}
@media print {
  body * {
    visibility: hidden;
  }
  #report, #report * {
    visibility: visible;
  }
  #report {
    position: absolute;
    left: 0;
    top: 0;
  }
}
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}
::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
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
