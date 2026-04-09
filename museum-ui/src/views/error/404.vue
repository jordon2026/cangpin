<template>
  <div class="not-found">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <div class="error-desc">抱歉，您访问的页面不存在</div>
      <el-button type="primary" @click="$router.push('/dashboard')">
        <el-icon><HomeFilled /></el-icon>
        返回首页
      </el-button>
    </div>
    <div class="not-found-decor">
      <div class="decor-line"></div>
      <div class="decor-diamond">&#9670;</div>
      <div class="decor-line"></div>
    </div>
  </div>
</template>

<style scoped>
.not-found {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--color-bg-page);
}

.error-code {
  font-size: 120px;
  font-weight: 700;
  color: var(--color-primary);
  opacity: 0.2;
  line-height: 1;
}

.error-desc {
  font-size: 16px;
  color: var(--color-text-light);
  margin: -20px 0 30px;
}

.not-found-decor {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 40px;
}

.decor-line {
  width: 60px;
  height: 1px;
  background: var(--color-border);
}

.decor-diamond {
  color: var(--color-gold);
  font-size: 10px;
  opacity: 0.5;
}
</style>
