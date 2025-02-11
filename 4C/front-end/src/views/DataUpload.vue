<script setup>
import { ref, computed } from 'vue';
import * as XLSX from 'xlsx';
import Papa from 'papaparse';
import { DateTime } from 'luxon';

// 图表组件
import BarChart from '../components/ECharts/BarChart/BarChart.vue';
import LineChart from '../components/ECharts/LineChart/LineChart.vue';
import PieChart from '../components/ECharts/PieChart/PieChart.vue';
import ScatterChart from '../components/ECharts/ScatterChart/ScatterChart.vue';

// 响应式状态
const datasets = ref([]);
const activeDataset = ref(null);
const chartType = ref('');
const uploadError = ref('');
const isLoading = ref(false);
const mergeWarning = ref(''); // 用于存储合并警告信息

// 字段映射配置
const fieldMapping = {
  date: ['date', 'time', 'timestamp', '日期', '时间'],
  value: ['value', 'amount', 'score', '数值', '值'],
  name: ['name', 'category', 'type', '名称', '类别'],
  x: ['x', 'axis_x', '维度x'],
  y: ['y', 'axis_y', '维度y']
};

// 处理后的数据
const processedData = computed(() => {
  if (!activeDataset.value || !activeDataset.value.cleanData) return [];
  return activeDataset.value.cleanData;
});


// 处理上传的数据
const handleFileUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  isLoading.value = true;
  uploadError.value = '';
  chartType.value = ''; // 重置图表类型

  try {
    const rawData = await parseFile(file);
    const { mappedData, cleanData, datasetType } = processData(rawData);

    // 输出 JSON 格式的原始数据和清理后的数据
    console.log('上传的原始数据 (JSON 格式):', JSON.stringify(mappedData, null, 2));
    console.log('上传的清理数据 (JSON 格式):', JSON.stringify(cleanData, null, 2));

    const newDataset = {
      id: Date.now(),
      name: file.name,
      rawData: mappedData, // 保留原始数据
      cleanData,
      fields: detectFields(cleanData),
      type: datasetType, // 每个数据集保存自己的类型
    };

    datasets.value.push(newDataset); // 将新的数据集保存到 datasets
    activeDataset.value = newDataset; // 设置当前活动数据集
    chartType.value = datasetType; // 更新全局图表类型
  } catch (error) {
    uploadError.value = error.message;
  } finally {
    isLoading.value = false;
  }
};



// 文件解析
const parseFile = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    const extension = file.name.split('.').pop().toLowerCase();

    reader.onload = (e) => {
      try {
        let data;
        switch (extension) {
          case 'xlsx': case 'xls':
            data = parseExcel(e.target.result);
            break;
          case 'csv':
            data = parseCSV(e.target.result);
            break;
          case 'json':
            data = JSON.parse(e.target.result);
            break;
          default:
            throw new Error('不支持的文件格式');
        }
        resolve(data);
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

// 改进后的数据处理
const processData = (rawData) => {
  const mappedData = rawData.map(item => {
    const mappedItem = {};

    // 字段映射
    Object.entries(fieldMapping).forEach(([standardKey, aliases]) => {
      const foundKey = Object.keys(item).find(key =>
        aliases.includes(key.toLowerCase().trim())
      );
      if (foundKey) mappedItem[standardKey] = item[foundKey];
    });

    // 保留原始字段
    return { ...item, ...mappedItem };
  });

  // 自动检测数据集类型
  const fields = detectFields(mappedData);
  const datasetType =
    fields.date && fields.value ? 'line' :
      fields.name && fields.value ? 'bar' :
        fields.x && fields.y ? 'scatter' : '';

  // 根据检测类型过滤
  const cleanData = mappedData
    .map(item => ({
      ...item,
      date: parseDate(item.date),
      value: parseNumber(item.value),
      x: parseNumber(item.x),
      y: parseNumber(item.y)
    }))
    .filter(item => {
      switch(datasetType) {
        case 'line': return item.date && item.value !== undefined;
        case 'bar': return item.name && item.value !== undefined;
        case 'scatter': return item.x !== undefined && item.y !== undefined;
        default: return true;
      }
    });

  return { mappedData, cleanData, datasetType };
};

// 日期解析
const parseDate = (value) => {
  if (!value) return null;
  const formats = [
    'yyyy-MM', 'yyyy/MM', 'yyyy-MM-dd',
    'yyyy/MM/dd', 'x' // timestamp
  ];

  for (const format of formats) {
    const dt = DateTime.fromFormat(value, format);
    if (dt.isValid) return dt.toFormat('yyyy/MM/dd'); // 格式化为年/月/日
  }

  const timestamp = Number(value);
  if (!isNaN(timestamp)) return DateTime.fromMillis(timestamp).toFormat('yyyy/MM/dd'); // 转换时间戳为年/月/日格式

  return null;
};

// 数值解析
const parseNumber = (value) => {
  if (typeof value === 'number') return value;
  const num = parseFloat(value);
  return isNaN(num) ? undefined : num;
};

// 自动检测图表类型
const autoDetectChartType = (dataset) => {
  const { fields } = dataset;
  if (fields.date && fields.value) return 'line';
  if (fields.name && fields.value) return 'bar';
  if (fields.x && fields.y) return 'scatter';
  return '';
};

// 字段检测
const detectFields = (data) => {
  if (data.length === 0) return {};
  const sample = data[0];

  return {
    date: !!sample.date,
    value: sample.value !== undefined,
    name: !!sample.name,
    x: sample.x !== undefined,
    y: sample.y !== undefined
  };
};

// 合并数据
const mergeDatasets = () => {
  if (!datasets.value || datasets.value.length === 0) return;

  const selectedDatasets = datasets.value.filter(dataset => dataset.isSelected);
  mergeWarning.value = ''; // 清空之前的警告

  if (selectedDatasets.length === 0) {
    uploadError.value = '请至少选择一个数据集进行合并';
    return;
  }

  // 新增数据一致性检查
  if (['line', 'bar'].includes(chartType.value)) {
    const keyField = chartType.value === 'line' ? 'date' : 'name';
    const allValues = selectedDatasets.map(dataset =>
      new Set(dataset.cleanData.map(item => item[keyField]))
    );

    // 检查所有数据集的关键字段是否完全一致
    const baseValues = Array.from(allValues[0]);
    const hasInconsistency = allValues.some(
      valueSet => valueSet.size !== baseValues.length ||
        !baseValues.every(v => valueSet.has(v))
    );

    if (hasInconsistency) {
      mergeWarning.value = `注意：合并的${chartType.value === 'line' ? '日期' : '名称'}字段不完全一致，可能会影响图表展示效果`;
    }
  }

  // 合并数据
  const mergedData = selectedDatasets.reduce((acc, dataset) => {
    const seriesData = dataset.cleanData.map(item => {
      let newItem = {};
      switch (chartType.value) {
        case 'line':
          newItem = { date: item.date, value: item.value };  // 保留 date 和 value
          break;
        case 'bar':
        case 'pie':
          newItem = { name: item.name, value: item.value };  // 保留 name 和 value
          break;
        case 'scatter':
          newItem = { x: item.x, y: item.y };  // 保留 x 和 y
          break;
        default:
          newItem = { ...item }; // 默认保留所有字段
      }
      return newItem;
    });

    acc.push(...seriesData); // 合并数据
    return acc;
  }, []);

  const mergedData2 = selectedDatasets.reduce((accumulatedData, dataset) => {
    const datasetSeriesData = dataset.cleanData.map(item => {
      const { date, value, name, x, y, ...rest } = item; // 去除不需要的字段
      switch (chartType.value) {
        case 'line':
          return { date, value };  // 仅保留日期和数值
        case 'bar':
        case 'pie':
          return { name, value };  // 仅保留名称和数值
        case 'scatter':
          return { x, y, ...rest };  // 保留 x 和 y 轴数据及其他字段
        default:
          return item; // 默认保留所有字段
      }
    });

    accumulatedData.push({
      NAME: dataset.name,
      data: datasetSeriesData, // 使用处理后的数据
    });

    return accumulatedData;
  }, []);

  // 输出合并后的数据为 JSON 格式
  console.log('合并后的数据 (JSON 格式):', JSON.stringify(mergedData2, null, 2));

  // 更新 activeDataset
  activeDataset.value = {
    id: Date.now(),
    name: selectedDatasets.map(dataset => dataset.name).join(' + '),
    rawData: mergedData, // 更新合并后的原始数据
    cleanData: mergedData2, // 更新合并后的清理数据
    fields: detectFields(mergedData)
  };

  chartType.value = chartType.value; // 保持选定的图表类型
};



// 图表验证逻辑
const validateChart = () => {
  if (!activeDataset.value) return false;

  const { fields } = activeDataset.value;
  const errors = {
    line: !(fields.date && fields.value),
    bar: !(fields.name && fields.value),
    pie: !(fields.name && fields.value),
    scatter: !(fields.x && fields.y)
  };

  // 检查 rawData 是否为空
  if (!activeDataset.value.rawData || activeDataset.value.rawData.length === 0) {
    return false;
  }

  return !errors[chartType.value];
};

</script>

<template>
  <div class="min-h-screen bg-gray-50 p-8">
    <div class="max-w-6xl mx-auto space-y-6">
      <!-- 增强版文件上传区域 -->
      <div class="bg-white rounded-xl shadow-sm p-6 transition-all duration-300 hover:shadow-md">
        <h1 class="text-2xl font-bold text-gray-800 mb-4 flex items-center">
          <span class="mr-2">📊</span>
          <span class="bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
            智能数据可视化平台
          </span>
        </h1>

        <!-- 上传引导界面 -->
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

        <!-- 增强版文件上传区域 -->
        <div class="bg-white rounded-xl shadow-sm p-6 hover:shadow-md">
          <h1 class="text-2xl font-bold text-gray-800 mb-4 flex items-center">
            <span class="mr-2">📊</span>
            <span class="bg-gradient-to-r from-indigo-500 to-purple-600 text-white p-1 rounded-md">上传数据集</span>
          </h1>

          <div
            class="relative w-full h-40 border-2 border-dashed rounded-xl border-gray-300 bg-gray-50 hover:border-gray-500 hover:bg-gray-100 transition-all">
            <input
              type="file"
              @change="handleFileUpload"
              class="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
            />
            <div class="flex justify-center items-center h-full text-center space-y-2">
              <span class="text-lg text-gray-600">拖拽文件到此区域上传</span>
              <span class="text-sm text-gray-400">支持 XLSX, CSV, JSON 格式</span>
            </div>
          </div>

          <div v-if="uploadError" class="text-red-500 text-sm mt-4">{{ uploadError }}</div>
          <div v-if="isLoading" class="text-gray-600 mt-4">正在上传...</div>
        </div>

        <!-- 状态提示 -->
        <div v-if="isLoading"
             class="mt-4 p-3 bg-blue-50 text-blue-700 rounded-lg flex items-center">
          <span class="animate-spin mr-2">🌀</span> 正在处理文件...
        </div>
        <div v-if="uploadError"
             class="mt-4 p-3 bg-red-50 text-red-700 rounded-lg flex items-center">
          <span class="mr-2">❌</span> {{ uploadError }}
        </div>
      </div>

      <!-- 数据集选择 -->
      <div v-if="datasets.length" class="bg-white rounded-xl shadow-sm p-6">
        <h3 class="text-lg font-semibold mb-4">选择数据集</h3>
        <div class="flex flex-wrap gap-2">
          <div v-for="dataset in datasets" :key="dataset.id" class="flex items-center">
            <input type="checkbox" v-model="dataset.isSelected" class="mr-2">
            <button
              @click="activeDataset = dataset"
              :class="[
          'px-4 py-2 rounded-full text-sm',
          activeDataset?.id === dataset.id
            ? 'bg-blue-500 text-white'
            : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
        ]"
            >
              {{ dataset.name }}
            </button>
          </div>
        </div>
        <button
          v-if="datasets.some(dataset => dataset.isSelected)"
          @click="mergeDatasets"
          class="mt-4 px-4 py-2 bg-green-500 text-white rounded-lg"
        >
          合并选中的数据集
        </button>
        <!-- 在合并按钮下方添加 -->
        <div v-if="mergeWarning"
             class="mt-4 p-3 bg-yellow-50 border border-yellow-200 rounded-lg flex items-start">
          <span class="mr-2">⚠️</span>
          <div>
            <p class="text-yellow-700">{{ mergeWarning }}</p>
            <p class="text-yellow-600 text-sm mt-1">
              建议检查以下字段的一致性：
              <span v-if="chartType === 'line'" class="font-medium">日期(date)字段</span>
              <span v-if="chartType === 'bar'" class="font-medium">名称(name)字段</span>
            </p>
          </div>
        </div>
      </div>
      <!-- 显示合并后的数据集名称 -->
      <div v-if="activeDataset" class="bg-white rounded-xl shadow-sm p-6">
        <h3 class="text-lg font-semibold mb-4">合并后的数据集</h3>
        <p class="text-gray-700">合并数据集: {{ activeDataset.name }}</p>
      </div>

      <!-- 图表配置 -->
      <div v-if="activeDataset" class="bg-white rounded-xl shadow-sm p-6">
        <div class="grid md:grid-cols-2 gap-6">
          <!-- 图表类型选择 -->
          <div>
            <h3 class="text-lg font-semibold mb-4">图表类型</h3>
            <div class="grid grid-cols-2 gap-3">
              <button
                v-for="type in ['line', 'bar', 'pie', 'scatter']"
                :key="type"
                @click="chartType = type"
                :class="[
                  'p-4 rounded-lg border flex flex-col items-center',
                  chartType === type
                    ? 'border-blue-500 bg-blue-50'
                    : 'border-gray-200 hover:border-blue-300'
                ]"
              >
                <span class="text-2xl mb-2">
                  {{
                    type === 'line' ? '📈' :
                      type === 'bar' ? '📊' :
                        type === 'pie' ? '🥧' : '🟢'
                  }}
                </span>
                <span class="capitalize">{{ type }}</span>
              </button>
            </div>
          </div>

          <!-- 数据字段状态 -->
          <div v-if="activeDataset" class="bg-white rounded-xl shadow-sm p-6">
            <div class="grid md:grid-cols-2 gap-6">
              <!-- 字段状态 -->
              <div>
                <h3 class="text-lg font-semibold mb-4">数据字段状态</h3>
                <div class="space-y-3">
                  <!-- 折线图要求 -->
                  <div v-if="chartType === 'line'" class="p-4 bg-gray-50 rounded-lg">
                    <h4 class="font-medium mb-2">折线图要求</h4>
                    <div class="flex items-center space-x-2 text-sm">
                      <span class="text-green-600">●</span>
                      <span>日期字段: {{ activeDataset.fields.date ? '✔️ 已识别' : '❌ 缺失'
                        }}</span>
                    </div>
                    <div class="flex items-center space-x-2 text-sm">
                      <span class="text-green-600">●</span>
                      <span>数值字段: {{ activeDataset.fields.value ? '✔️ 已识别' : '❌ 缺失'
                        }}</span>
                    </div>
                  </div>

                  <!-- 柱状图要求 -->
                  <div v-if="chartType === 'bar'" class="p-4 bg-gray-50 rounded-lg">
                    <h4 class="font-medium mb-2">柱状图要求</h4>
                    <div class="flex items-center space-x-2 text-sm">
                      <span class="text-green-600">●</span>
                      <span>名称字段: {{ activeDataset.fields.name ? '✔️ 已识别' : '❌ 缺失'
                        }}</span>
                    </div>
                    <div class="flex items-center space-x-2 text-sm">
                      <span class="text-green-600">●</span>
                      <span>数值字段: {{ activeDataset.fields.value ? '✔️ 已识别' : '❌ 缺失'
                        }}</span>
                    </div>
                  </div>

                  <!-- 饼图要求 -->
                  <div v-if="chartType === 'pie'" class="p-4 bg-gray-50 rounded-lg">
                    <h4 class="font-medium mb-2">饼图要求</h4>
                    <div class="flex items-center space-x-2 text-sm">
                      <span class="text-green-600">●</span>
                      <span>名称字段: {{ activeDataset.fields.name ? '✔️ 已识别' : '❌ 缺失'
                        }}</span>
                    </div>
                    <div class="flex items-center space-x-2 text-sm">
                      <span class="text-green-600">●</span>
                      <span>数值字段: {{ activeDataset.fields.value ? '✔️ 已识别' : '❌ 缺失'
                        }}</span>
                    </div>
                  </div>

                  <!-- 散点图要求 -->
                  <div v-if="chartType === 'scatter'" class="p-4 bg-gray-50 rounded-lg">
                    <h4 class="font-medium mb-2">散点图要求</h4>
                    <div class="flex items-center space-x-2 text-sm">
                      <span class="text-green-600">●</span>
                      <span>X 轴字段: {{ activeDataset.fields.x ? '✔️ 已识别' : '❌ 缺失' }}</span>
                    </div>
                    <div class="flex items-center space-x-2 text-sm">
                      <span class="text-green-600">●</span>
                      <span>Y 轴字段: {{ activeDataset.fields.y ? '✔️ 已识别' : '❌ 缺失' }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 图表展示 -->
      <div v-if="validateChart()" class="bg-white rounded-xl shadow-sm p-6">
        <component :is="chartType === 'line' ? LineChart :
                  chartType === 'bar' ? BarChart :
                  chartType === 'pie' ? PieChart : ScatterChart"
                   :rawData="processedData" />
      </div>

      <!-- 数据预览 -->
      <div v-if="activeDataset" class="bg-white rounded-xl shadow-sm p-6">
        <h3 class="text-lg font-semibold mb-4">数据预览（前5行）</h3>
        <div class="overflow-x-auto">
          <table class="min-w-full">
            <thead class="bg-gray-50">
            <tr>
              <th
                v-for="header in Object.keys(activeDataset.rawData[0])"
                :key="header"
                class="px-4 py-2 text-left text-sm font-medium text-gray-700"
              >
                {{ header }}
              </th>
            </tr>
            </thead>
            <tbody>
            <tr
              v-for="(row, index) in activeDataset.rawData.slice(0, 5)"
              :key="index"
              class="hover:bg-gray-50"
            >
              <td
                v-for="(value, key) in row"
                :key="key"
                class="px-4 py-2 text-sm text-gray-600"
              >
                {{ value }}
              </td>
            </tr>
            </tbody>
          </table>
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

.bg-blue-100 {
  background-color: #bfdbfe;
}

.border-dashed {
  border-style: dashed;
}

.text-center {
  text-align: center;
}
</style>
