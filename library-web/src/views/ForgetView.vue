<template>
  <div class="forget-page">
    <div class="forget-card">
      <img class="forget-img" :src="forgetImg" alt="忘记密码" />
      <h2 class="title">忘记密码？</h2>
      <p class="tip">
        V1.0 版本请联系图书馆管理员重置密码（账号 admin）<br />
        后续版本将支持手机号自助找回，敬请期待~
      </p>
      <el-button type="primary" round @click="$router.push('/login')">返回登录</el-button>
    </div>
  </div>
</template>

<script setup>
// 卡通插画
const forgetImg =
  'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=' +
  encodeURIComponent('cute cartoon empty open book, blue pastel, flat illustration') +
  '&image_size=square_hd'
</script>

<style scoped>
.forget-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.forget-card {
  width: 420px;
  max-width: 100%;
  background: #fff;
  border-radius: 20px;
  box-shadow: var(--shadow-card);
  padding: 40px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.forget-img {
  width: 140px;
  height: 140px;
  border-radius: 16px;
  object-fit: cover;
}

.title {
  font-size: 22px;
  color: var(--color-text-primary);
}

.tip {
  color: var(--color-text-secondary);
  font-size: 14px;
  line-height: 1.8;
}
</style>
