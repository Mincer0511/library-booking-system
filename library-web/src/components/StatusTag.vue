<template>
  <!-- 订单状态标签：按 status 映射颜色 -->
  <el-tag :type="tagType" effect="light" round>{{ tagText }}</el-tag>
</template>

<script setup>
import { computed } from 'vue'
import { BOOKING_STATUS } from '@/utils/format'

const props = defineProps({
  status: {
    type: [Number, String],
    required: true
  }
})

const conf = computed(() => BOOKING_STATUS[Number(props.status)] || { text: '未知', type: 'info' })
const tagText = computed(() => conf.value.text)
const tagType = computed(() => conf.value.type)
</script>
