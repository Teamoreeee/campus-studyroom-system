<template>
  <div class="home-page">
    <NavBar />
    <div class="hero">
      <h1>校园自习室智能预约系统</h1>
      <p class="subtitle">AI 赋能 · 智能推荐 · 高效预约 · 实时考勤</p>
      <p class="quote">{{ randomQuote }}</p>
      <div class="actions">
        <el-button type="primary" size="large" @click="$router.push('/rooms')">立即预约</el-button>
        <el-button size="large" @click="$router.push('/ai/recommend')">智能推荐</el-button>
      </div>
    </div>

    <div class="stats" v-if="stats">
      <h2 class="section-title">实时数据</h2>
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value">{{ stats.totalRooms }}</div>
            <div class="stat-label">自习室</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value">{{ stats.totalReservations }}</div>
            <div class="stat-label">累计预约</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value">{{ stats.todayReservations }}</div>
            <div class="stat-label">今日预约</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value">{{ stats.totalUsers }}</div>
            <div class="stat-label">注册用户</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="features">
      <h2 class="section-title">系统功能</h2>
      <el-row :gutter="24">
        <el-col :xs="24" :sm="12" :md="8">
          <div class="feature-card">
            <el-icon size="40" color="#409eff"><OfficeBuilding /></el-icon>
            <h3>自习室查询</h3>
            <p>实时查看各教学楼自习室座位情况，按楼层、设施筛选。</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="feature-card">
            <el-icon size="40" color="#67c23a"><Calendar /></el-icon>
            <h3>在线预约</h3>
            <p>选择日期、时间段、座位一键预约，支持取消与改签。</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="feature-card">
            <el-icon size="40" color="#e6a23c"><CircleCheck /></el-icon>
            <h3>智能考勤</h3>
            <p>扫码签到签退，自动记录学习时长，异常行为智能分析。</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="feature-card">
            <el-icon size="40" color="#f56c6c"><Cpu /></el-icon>
            <h3>AI 推荐</h3>
            <p>基于历史预约行为，智能推荐最适合你的自习室与座位。</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="feature-card">
            <el-icon size="40" color="#909399"><ChatDotRound /></el-icon>
            <h3>AI 客服</h3>
            <p>7×24 小时智能问答，解答预约规则、违规处理等常见问题。</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="feature-card">
            <el-icon size="40" color="#8e44ad"><DataLine /></el-icon>
            <h3>数据看板</h3>
            <p>管理员可查看预约趋势、座位利用率、考勤异常统计。</p>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import NavBar from '@/components/NavBar.vue'
import { adminApi } from '@/api/admin'

const quotes = [
  '宝剑锋从磨砺出，梅花香自苦寒来。',
  '书山有路勤为径，学海无涯苦作舟。',
  '不积跬步，无以至千里；不积小流，无以成江海。',
  '业精于勤，荒于嬉；行成于思，毁于随。',
  '千里之行，始于足下。',
  '天行健，君子以自强不息。',
  '少年易老学难成，一寸光阴不可轻。',
  '路漫漫其修远兮，吾将上下而求索。',
  '锲而舍之，朽木不折；锲而不舍，金石可镂。',
  '博学之，审问之，慎思之，明辨之，笃行之。'
]

const randomQuote = ref('')

const stats = ref({
  totalRooms: 0,
  totalReservations: 0,
  todayReservations: 0,
  totalUsers: 0
})

onMounted(async () => {
  randomQuote.value = quotes[Math.floor(Math.random() * quotes.length)]
  try {
    stats.value = await adminApi.getStatistics()
  } catch (e) {
    // 静态首页，统计失败不阻断
  }
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #f5f7fa;
}
.hero {
  text-align: center;
  padding: 80px 20px 60px;
  background: linear-gradient(135deg, #e3f2fd 0%, #f5f7fa 100%);
}
.hero h1 {
  font-size: 42px;
  margin-bottom: 16px;
  color: #303133;
}
.subtitle {
  font-size: 18px;
  color: #606266;
  margin-bottom: 16px;
}
.quote {
  font-size: 16px;
  color: #409eff;
  font-style: italic;
  margin-bottom: 32px;
  padding: 12px 24px;
  background: rgba(64, 158, 255, 0.08);
  border-radius: 20px;
  display: inline-block;
}
.actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}
.features {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px 60px;
}
.section-title {
  text-align: center;
  font-size: 28px;
  margin-bottom: 32px;
  color: #303133;
  font-weight: 600;
}
.feature-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px 24px;
  text-align: center;
  margin-bottom: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s;
}
.feature-card:hover {
  transform: translateY(-4px);
}
.feature-card h3 {
  margin: 16px 0 8px;
  color: #303133;
}
.feature-card p {
  color: #909399;
  font-size: 14px;
  line-height: 1.6;
}
.stats {
  max-width: 1200px;
  margin: 0 auto;
  padding: 60px 20px 40px;
}
.stat-item {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}
.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #409eff;
}
.stat-label {
  margin-top: 8px;
  color: #606266;
}
</style>
