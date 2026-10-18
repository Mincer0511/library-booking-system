<template>
  <div class="notfound-page">
    <div class="notfound-card">
      <img class="notfound-img" :src="notFoundImg" alt="404" />
      <h2 class="code">404</h2>
      <p class="tip">哎呀，页面走丢了~</p>
      <el-button type="primary" round @click="$router.push('/home')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
// 卡通 404 插画
const notFoundImg =
  'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=' +
  encodeURIComponent('cute cartoon empty open book, blue pastel, flat illustration') +
  '&image_size=square_hd'
</script>

<style scoped>
.notfound-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.notfound-card {
  background: #fff;
  border-radius: 20px;
  box-shadow: var(--shadow-card);
  padding: 48px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}

.notfound-img {
  width: 180px;
  height: 180px;
  border-radius: 16px;
  object-fit: cover;
}

.code {
  font-size: 40px;
  color: var(--color-primary);
  font-weight: 700;
}

.tip {
  color: var(--color-text-secondary);
  font-size: 14px;
}
</style>
