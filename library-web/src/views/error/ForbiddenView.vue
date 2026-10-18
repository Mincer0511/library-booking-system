<template>
  <div class="forbidden-page">
    <div class="forbidden-card">
      <div class="emoji">🚫</div>
      <p class="tip">无权限访问该页面</p>
      <p class="desc">您当前的账号角色没有访问此页面的权限</p>
      <el-button type="primary" round @click="$router.push('/home')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
// 403 无权限页面
</script>

<style scoped>
.forbidden-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg-page);
}

.forbidden-card {
  background: #fff;
  border-radius: 20px;
  box-shadow: var(--shadow-card);
  padding: 48px 64px;
  text-align: center;
}

.emoji {
  font-size: 56px;
}

.tip {
  margin: 16px 0 4px;
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.desc {
  margin-bottom: 24px;
  font-size: 14px;
  color: var(--color-text-secondary);
}
</style>
