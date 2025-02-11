<script setup>
import { ref, computed, watch } from 'vue';
import * as XLSX from 'xlsx';
import Papa from 'papaparse';
import BarChart from '../components/ECharts/BarChart/BarChart.vue';
import LineChart from '../components/ECharts/LineChart/LineChart.vue';
import PieChart from '../components/ECharts/PieChart/PieChart.vue';
import ScatterChart from '../components/ECharts/ScatterChart/ScatterChart.vue';
import { InformationCircleIcon } from '@heroicons/vue/24/outline';

// 响应式状态
const chartType = ref('');
const rawData = ref([]);
const recommendedChartType = ref([]);
const showFormatRequirements = ref(false);
const uploadError = ref('');
const isLoading = ref(false);

// 图表组件映射
const chartComponents = {
  bar: { simple: BarChart },
  line: { simple: LineChart },
  pie: { simple: PieChart },
  scatter: { simple: ScatterChart },
};

// 当前图表组件
const CurrentChartComponent = computed(() => {
  return chartType.value ? chartComponents[chartType.value]?.simple : null;
});

// 修改后的字段映射
const fieldMapping = ref({
  name: ['name', 'xx', 'title', '项目', '类别'],
  value: ['value', 'score', 'amount', '数值', '值'],
  date: ['date', 'timestamp', 'time', '日期', '时间'],
  x: ['x', 'axis_x', '维度x'],
  y: ['y', 'axis_y', '维度y'],
  category: ['category', 'type', 'group']
});

// 当前映射字段
const mappedFields = computed(() => {
  return rawData.value.length > 0 ? inferFieldMapping(rawData.value[0]) : {};
});

// 自动推断字段映射
const inferFieldMapping = (sampleData) => {
  const inferredMapping = {};

  Object.keys(sampleData).forEach((key) => {
    const cleanedKey = key.trim().toLowerCase().replace(/ /g, '_');
    if (fieldMapping.value[cleanedKey]) {
      inferredMapping[cleanedKey] = fieldMapping.value[cleanedKey];
    } else {
      inferredMapping[cleanedKey] = [key]; // 使用原始字段名作为别名
    }
  });

  return inferredMapping;
};


const processData = (data) => {
  if (data.length < 3) {
    uploadError.value = '数据量过少（至少需要3条记录）';
    return;
  }

  const analysis = analyzeData(data);
  recommendedChartType.value = recommendCharts(analysis);
  chartType.value = recommendedChartType.value[0] || '';
  console.log("推荐的图表类型:", recommendedChartType.value);
  console.log("当前选择的图表类型:", chartType.value); // 确认这里是否正确设置
};

// 文件上传处理
const handleFileUpload = async (event) => {
  isLoading.value = true;
  uploadError.value = '';
  const file = event.target.files[0];

  if (!file) {
    isLoading.value = false;
    return;
  }

  try {
    const formattedData = await parseFile(file);
    rawData.value = formattedData;
    processData(formattedData);
  } catch (error) {
    uploadError.value = error.message;
    console.error('文件处理错误:', error);
  } finally {
    isLoading.value = false;
  }
};

// 文件解析器
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
      reader.readAsBinaryString(file);
    }
  });
};

// 数据解析函数
const parseExcel = (data) => {
  const workbook = XLSX.read(data, { type: 'binary' });
  return XLSX.utils.sheet_to_json(workbook.Sheets[workbook.SheetNames[0]]);
};

const parseCSV = (data) => {
  return new Promise((resolve) => {
    Papa.parse(data, {
      complete: (result) => resolve(result.data),
      header: true,
      skipEmptyLines: true,
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

// 数据清洗和格式化
const cleanJsonData = (data) => {
  return data
    .trim()
    .replace(/\s+/g, ' ')
    .replace(/,\s*}/g, '}')
    .replace(/,\s*]/g, ']');
};

// 在原有代码基础上增加格式化函数
const formatDate = (date) => {
  if (!(date instanceof Date)) return date;
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}/${month}/${day}`;
};

// 修改后的数据格式化方法
const formatData = (data) => {
  if (!Array.isArray(data)) throw new Error('数据格式应为数组');

  return data.map((item) => {
    const formatted = {};
    for (const [key, value] of Object.entries(item)) {
      const cleanKey = key.trim().toLowerCase().replace(/ /g, '_');

      // 查找匹配的标准字段
      let standardKey = Object.keys(fieldMapping.value).find(k =>
        fieldMapping.value[k].includes(cleanKey)
      ) || cleanKey;

      formatted[standardKey] = convertValue(value);
    }
    return formatted;
  }).filter(item => Object.keys(item).length > 0);
};



const convertValue = (value) => {
  if (typeof value === 'string') {
    // 尝试解析日期
    const timestamp = Date.parse(value);
    if (!isNaN(timestamp)) {
      const date = new Date(timestamp);
      return formatDate(date); // 直接存储为格式化字符串
    }

    // 布尔值转换
    if (value.toLowerCase() === 'true') return true;
    if (value.toLowerCase() === 'false') return false;

    // 数值转换
    if (!isNaN(value) && value !== '') return Number(value);
  }
  return value;
};


const analyzeData = (data) => {
  const sample = data[0];
  const timeColumns = findTimeColumns(data);
  const numericColumns = findNumericColumns(sample);
  const categoryColumns = findCategoryColumns(sample);

  console.log("时间字段:", timeColumns); // 查看时间字段
  console.log("数值字段:", numericColumns); // 查看数值字段

  return {
    timeColumns,
    numericColumns,
    categoryColumns,
    rowCount: data.length,
  };
};


const findTimeColumns = (data) => {
  return Object.keys(data[0]).filter((key) => {
    // 检查字段是否能被解析为日期
    return data.some((item) => {
      const value = item[key];
      return !isNaN(Date.parse(value));  // 检查字段是否能转换为有效日期
    });
  });
};


const findNumericColumns = (sample) => {
  return Object.keys(sample).filter((key) => typeof sample[key] === 'number');
};

const findCategoryColumns = (sample) => {
  return Object.keys(sample).filter(
    (key) => !(sample[key] instanceof Date) && typeof sample[key] !== 'number'
  );
};

// 增强的图表推荐逻辑
const recommendCharts = (analysis) => {
  const validCharts = [];
  const hasNameValue = analysis.categoryColumns.includes('name') &&
    analysis.numericColumns.includes('value');
  const hasDateValue = analysis.timeColumns.includes('date') &&
    analysis.numericColumns.includes('value');
  const hasXY = analysis.numericColumns.includes('x') &&
    analysis.numericColumns.includes('y');

  if (hasDateValue) validCharts.push('line');
  if (hasNameValue) validCharts.push('bar', 'pie');
  if (hasXY) validCharts.push('scatter');

  return [...new Set(validCharts)];
};

/// 增强的图表验证
watch(chartType, (newType) => {
  if (!newType) return;

  const analysis = analyzeData(rawData.value);
  let error = '';

  switch(newType) {
    case 'line':
      if (!analysis.timeColumns.includes('date') ||
        !analysis.numericColumns.includes('value')) {
        error = '折线图需要日期(date)和数值(value)字段';
      }
      break;
    case 'bar':
    case 'pie':
      if (!analysis.categoryColumns.includes('name') ||
        !analysis.numericColumns.includes('value')) {
        error = '该图表需要名称(name)和数值(value)字段';
      }
      break;
    case 'scatter':
      if (!analysis.numericColumns.includes('x') ||
        !analysis.numericColumns.includes('y')) {
        error = '散点图需要x和y数值字段';
      }
      break;
  }

  if (error) {
    uploadError.value = error;
    chartType.value = '';
  }
});

// Chart descriptions based on type
const getChartDescription = (type) => {
  switch (type) {
    case 'line':
      return '展示数据随时间的变化趋势';
    case 'bar':
      return '适用于分类数据的比较';
    case 'pie':
      return '适用于显示各部分占比';
    case 'scatter':
      return '用于显示两个变量之间的关系';
    default:
      return '';
  }
};
</script>


<template>
  <div class="container mx-auto p-6">
    <!-- 文件上传区 -->
    <div class="bg-white shadow-lg rounded-xl p-6 mb-6">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-2xl font-bold text-gray-800">📊 数据可视化</h2>
        <button
          @click="showFormatRequirements = !showFormatRequirements"
          class="text-gray-500 hover:text-blue-600 flex items-center"
        >
          <InformationCircleIcon class="w-5 h-5 mr-1"/>
          <span class="text-sm">数据格式要求</span>
        </button>
      </div>

      <!-- 数据格式要求说明 -->
      <div
        v-if="showFormatRequirements"
        class="p-4 bg-gray-50 rounded-lg mb-4 animate-fade-in"
      >
        <h4 class="font-semibold mb-3">📝 各图表数据格式要求：</h4>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
          <div class="p-3 bg-white rounded-lg shadow">
            <div class="font-medium mb-2 text-blue-600">折线图</div>
            <ul class="list-disc pl-5 space-y-1">
              <li>必需字段：date（日期）</li>
              <li>必需字段：value（数值）</li>
              <li>示例格式：{ date: '2023-01', value: 150 }</li>
            </ul>
          </div>
          <div class="p-3 bg-white rounded-lg shadow">
            <div class="font-medium mb-2 text-green-600">柱状图/饼图</div>
            <ul class="list-disc pl-5 space-y-1">
              <li>必需字段：name（名称）</li>
              <li>必需字段：value（数值）</li>
              <li>示例格式：{ name: '类别A', value: 75 }</li>
            </ul>
          </div>
          <div class="p-3 bg-white rounded-lg shadow">
            <div class="font-medium mb-2 text-purple-600">散点图</div>
            <ul class="list-disc pl-5 space-y-1">
              <li>必需字段：x（横坐标）</li>
              <li>必需字段：y（纵坐标）</li>
              <li>示例格式：{ x: 5.2, y: 8.4 }</li>
            </ul>
          </div>
          <div class="p-3 bg-white rounded-lg shadow">
            <div class="font-medium mb-2 text-gray-600">通用说明</div>
            <ul class="list-disc pl-5 space-y-1">
              <li>支持字段别名（如value可对应score、amount等）</li>
              <li>日期字段支持多种格式自动识别</li>
              <li>空值会自动过滤</li>
            </ul>
          </div>
        </div>
      </div>
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

      <!-- 数据预览 -->
      <div v-if="rawData.length > 0" class="mt-6">
        <h3 class="text-lg font-semibold mb-3">数据预览（前5行）</h3>
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
              v-for="(row, rowIndex) in rawData.slice(0, 5)"
              :key="rowIndex"
              class="hover:bg-gray-50"
            >
              <td
                v-for="(value, colIndex) in row"
                :key="colIndex"
                class="px-4 py-2 text-sm text-gray-600 border-b"
              >
                {{ value instanceof Date ? formatDate(value) : value }}
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 图表选择区 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
      <!-- 图表推荐 -->
      <div class="bg-white shadow-lg rounded-xl p-6">
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
                  type === 'bar' ? '📊' :
                    type === 'pie' ? '🥧' : '🟢'
              }}
            </span>
            <div>
              <p class="font-medium">
                {{
                  type === 'line' ? '折线图' :
                    type === 'bar' ? '柱状图' :
                      type === 'pie' ? '饼图' : '散点图'
                }}
              </p>
              <p class="text-sm text-gray-500">
                {{ getChartDescription(type) }}
              </p>
            </div>
          </div>
          <p v-if="recommendedChartType.length === 0" class="text-gray-500">
            暂无推荐图表，请检查数据格式
          </p>
        </div>
      </div>

      <!-- 图表展示 -->
      <div class="bg-white shadow-lg rounded-xl p-6">
        <h3 class="text-xl font-semibold mb-4">🎨 图表展示</h3>
        <div class="h-96 flex items-center justify-center bg-gray-50 rounded-xl">
          <component
            :is="CurrentChartComponent"
            v-if="CurrentChartComponent"
            :rawData="rawData"
            class="w-full h-full"
          />
          <div v-else class="text-gray-400">
            {{
              rawData.length > 0
                ? '👉 请从左侧选择图表类型'
                : '📁 请先上传数据文件'
            }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
/* 自定义滚动条 */
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
</style>
