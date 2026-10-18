<template>
  <div class="empty-state">
    <!-- 卡通空状态插画 -->
    <img class="empty-img" :src="emptyImg" alt="空状态" />
    <p class="empty-text">{{ text }}</p>
    <!-- 可选操作按钮插槽 -->
    <slot />
  </div>
</template>

<script setup>
defineProps({
  text: {
    type: String,
    default: '这里空空如也~'
  }
})

// 卡通空书插画（统一图片生成服务）
const emptyImg =
  'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=' +
  encodeURIComponent('cute cartoon empty open book, blue pastel, flat illustration') +
  '&image_size=square_hd'
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 0;
  gap: 12px;
}

.empty-img {
  width: 160px;
  height: 160px;
  border-radius: 16px;
  object-fit: cover;
}

.empty-text {
  color: var(--color-text-secondary);
  font-size: 14px;
}
</style>
