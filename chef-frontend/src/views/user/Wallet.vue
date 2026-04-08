<template>
  <div class="wallet-page">
    <div class="wallet-hero" v-loading="loading">
      <div class="wallet-main">
        <div class="wallet-copy">
          <div class="wallet-eyebrow">WALLET CENTER</div>
          <h2>我的余额</h2>
          <div class="balance-value">¥{{ wallet.balanceDesc }}</div>
        </div>
        <div class="wallet-actions">
          <div class="quick-title">快捷充值</div>
          <div class="quick-amounts">
            <el-button
              v-for="amount in quickAmounts"
              :key="amount"
              @click="rechargeAmount = amount"
            >
              ¥{{ amount }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="wallet-grid">
      <el-card class="wallet-panel recharge-panel" shadow="hover">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">余额充值</div>
              <div class="panel-subtitle">模拟充值会直接增加钱包余额，并生成一条充值流水。</div>
            </div>
          </div>
        </template>

        <el-form label-width="96px" class="panel-form">
          <el-form-item label="充值金额(元)">
            <div class="amount-input-wrap">
              <el-input-number
                v-model="rechargeAmount"
                :precision="2"
                :step="50"
                @change="handleRechargeAmountChange"
                style="width: 100%"
              />
              <span class="unit-text">元</span>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="recharging" @click="handleRecharge">
              立即充值
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="wallet-panel records-panel" shadow="hover" v-loading="recordLoading">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">余额流水</div>
              <div class="panel-subtitle">记录每一次充值、支付和退款的金额变化。</div>
            </div>
          </div>
        </template>

        <el-table :data="records" border style="width: 100%">
          <el-table-column label="金额(元)" width="140">
            <template #default="{ row }">
              <span :class="row.type === 1 ? 'income' : 'expense'">
                {{ row.type === 1 || row.type === 3 ? '+' : '-' }}¥{{ row.amountDesc }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="typeDesc" label="类型" width="110" />
          <el-table-column label="关联订单" width="120">
            <template #default="{ row }">
              {{ row.reservationOrderId > 0 ? row.reservationOrderId : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="时间" min-width="180">
            <template #default="{ row }">
              {{ row.createTimeDesc }}
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!recordLoading && records.length === 0" description="当前还没有余额流水记录" />

        <div class="pagination-wrap" v-if="recordTotal > 0">
          <el-pagination
            v-model:current-page="recordQuery.page"
            v-model:page-size="recordQuery.size"
            background
            layout="total, prev, pager, next"
            :total="recordTotal"
            @current-change="loadWalletRecords"
            class="app-pagination"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getWalletBalance, getWalletRecords, rechargeWallet } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const recordLoading = ref(false)
const recharging = ref(false)
const rechargeAmount = ref(100)
const quickAmounts = [50, 100, 200, 500, 1000]
const records = ref([])
const recordTotal = ref(0)

const wallet = reactive({
  walletId: 0,
  balance: 0,
  balanceDesc: '0.00'
})

const recordQuery = reactive({
  page: 1,
  size: 10
})

const applyWalletData = (data) => {
  wallet.walletId = data?.walletId || 0
  wallet.balance = data?.balance || 0
  wallet.balanceDesc = data?.balanceDesc || '0.00'
}

const loadWalletBalance = async () => {
  loading.value = true
  try {
    const res = await getWalletBalance()
    applyWalletData(res.data)
  } catch (error) {
    console.error('加载钱包失败:', error)
  } finally {
    loading.value = false
  }
}

const loadWalletRecords = async () => {
  recordLoading.value = true
  try {
    const res = await getWalletRecords({
      page: recordQuery.page,
      size: recordQuery.size
    })
    records.value = res.data || []
    recordTotal.value = res.total || 0
  } catch (error) {
    console.error('加载钱包流水失败:', error)
  } finally {
    recordLoading.value = false
  }
}

const handleRechargeAmountChange = (value) => {
  if (value == null || Number.isNaN(Number(value))) {
    return
  }
  if (Number(value) < 0) {
    rechargeAmount.value = null
  }
}

const handleRecharge = async () => {
  if (rechargeAmount.value == null || Number.isNaN(Number(rechargeAmount.value))) {
    ElMessage.warning('请输入正确的充值金额，单位为元')
    return
  }
  if (Number(rechargeAmount.value) < 0.01) {
    ElMessage.warning('充值金额最小为0.01元')
    return
  }
  if (Number(rechargeAmount.value) <= 0) {
    ElMessage.warning('充值金额必须大于0元')
    return
  }
  recharging.value = true
  try {
    const res = await rechargeWallet({
      amount: Math.round(rechargeAmount.value * 100)
    })
    applyWalletData(res.data)
    recordQuery.page = 1
    await loadWalletRecords()
    ElMessage.success('充值成功')
  } catch (error) {
    console.error('充值失败:', error)
  } finally {
    recharging.value = false
  }
}

onMounted(() => {
  loadWalletBalance()
  loadWalletRecords()
})
</script>

<style scoped>
.wallet-page {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.wallet-hero {
  display: block;
  padding: 30px 34px;
  border-radius: 26px;
  background:
    radial-gradient(circle at top right, rgba(255, 180, 66, 0.18), transparent 35%),
    linear-gradient(135deg, #ffffff 0%, #fff8ed 100%);
  box-shadow: 0 18px 42px rgba(74, 53, 12, 0.10);
}

.wallet-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.wallet-eyebrow {
  font-size: 12px;
  letter-spacing: 0.18em;
  color: #d9942f;
  font-weight: 700;
}

.wallet-copy h2 {
  margin: 10px 0 14px;
  font-size: 30px;
  color: #2f2412;
}

.balance-value {
  font-size: 42px;
  font-weight: 800;
  color: #d48806;
}

.wallet-actions {
  min-width: 280px;
}

.quick-title {
  margin-bottom: 12px;
  font-size: 14px;
  color: #8a6a2c;
}

.quick-amounts {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.wallet-grid {
  display: grid;
  grid-template-columns: minmax(320px, 0.8fr) minmax(0, 1.4fr);
  gap: 24px;
}

.wallet-panel {
  border-radius: 24px;
  box-shadow: 0 16px 36px rgba(17, 24, 39, 0.08);
}

.panel-header {
  display: flex;
  align-items: center;
}

.panel-title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2a44;
}

.panel-subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: #8a94a6;
}

.panel-form {
  padding-top: 6px;
}

.amount-input-wrap {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
}

.unit-text {
  color: #606266;
  white-space: nowrap;
}

.income {
  color: #67c23a;
  font-weight: 700;
}

.expense {
  color: #f56c6c;
  font-weight: 700;
}

.pagination-wrap {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1200px) {
  .wallet-grid {
    grid-template-columns: 1fr;
  }

  .wallet-main {
    flex-direction: column;
    align-items: flex-start;
  }

  .wallet-actions {
    width: 100%;
  }
}
</style>
