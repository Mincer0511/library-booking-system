<template>
  <div class="page-container">
    <!-- 页头 -->
    <div class="card-container help-header">
      <span class="header-deco">🎡</span>
      <h2 class="header-title">帮助中心</h2>
      <p class="text-secondary">三分钟了解自习室预约的全部规则，文明预约从你我做起~</p>
    </div>

    <!-- 规则卡片三栏 -->
    <div class="help-grid">
      <!-- 预约规则 -->
      <div class="card-container help-card">
        <div class="help-icon blue">📖</div>
        <h3 class="help-title">预约规则</h3>
        <ul class="help-list">
          <li>每人每日最多预约 <b>2</b> 次</li>
          <li>预约时长以 <b>30 分钟</b> 为粒度</li>
          <li>单次预约最长 <b>4 小时</b></li>
          <li>可预约 <b>今天、明天</b> 的座位</li>
          <li>预约开始前可随时取消</li>
        </ul>
      </div>

      <!-- 签到与爽约 -->
      <div class="card-container help-card">
        <div class="help-icon green">✅</div>
        <h3 class="help-title">签到与爽约</h3>
        <ul class="help-list">
          <li>预约开始后 <b>30 分钟内</b> 需完成签到</li>
          <li>超时未签到将记 <b>爽约</b> 并自动取消订单</li>
          <li>每次爽约扣除信用分 <b>10 分</b></li>
          <li>学习结束请记得签退，释放座位给其他同学</li>
        </ul>
      </div>

      <!-- 信用分规则 -->
      <div class="card-container help-card">
        <div class="help-icon orange">💯</div>
        <h3 class="help-title">信用分规则</h3>
        <ul class="help-list">
          <li>初始信用分 <b>100 分</b></li>
          <li>爽约一次 <b>-10 分</b></li>
          <li>信用分低于 <b>60 分</b> 将禁用预约 7 天</li>
          <li>解禁后信用分恢复至 <b>60 分</b></li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
// 帮助中心：纯静态规则说明页
</script>

<style scoped>
.help-header {
  text-align: center;
  padding: 28px 20px;
  margin-bottom: 20px;
}

.header-deco {
  font-size: 40px;
}

.header-title {
  font-size: 24px;
  margin: 10px 0 8px;
  color: var(--color-text-primary);
}

/* 三栏卡片网格 */
.help-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

@media (max-width: 900px) {
  .help-grid {
    grid-template-columns: 1fr;
  }
}

.help-card {
  text-align: center;
  padding: 28px 22px;
  transition: all 0.2s ease;
}

.help-card:hover {
  transform: translateY(-4px);
}

/* 图标圆底 */
.help-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
}

.help-icon.blue {
  background: var(--color-primary-light);
}

.help-icon.green {
  background: #f6ffed;
}

.help-icon.orange {
  background: #fff7e6;
}

.help-title {
  margin: 14px 0 12px;
  font-size: 17px;
  color: var(--color-text-primary);
}

.help-list {
  list-style: none;
  text-align: left;
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 2;
}

.help-list li::before {
  content: '🌟';
  margin-right: 6px;
  font-size: 12px;
}

.help-list b {
  color: var(--color-primary);
}
</style>
