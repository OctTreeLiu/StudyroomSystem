<template>
  <el-input
    v-model="localPart"
    :placeholder="placeholder"
    clearable
    @input="emitValue"
    @blur="emitValue"
  >
    <template #append>
      <el-select
        v-model="domain"
        class="domain-select"
        placeholder="选择域名"
        @change="emitValue"
      >
        <el-option v-for="d in domainOptions" :key="d" :label="`@${d}`" :value="d" />
      </el-select>
    </template>
  </el-input>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请输入邮箱账号'
  }
})

const emit = defineEmits(['update:modelValue'])

const COMMON_DOMAINS = ['qq.com', '163.com', 'gmail.com', 'outlook.com', 'foxmail.com', 'sina.com']

const localPart = ref('')
const domain = ref('qq.com')
const extraDomain = ref('')

const domainOptions = computed(() => {
  const merged = new Set(COMMON_DOMAINS)
  if (extraDomain.value) merged.add(extraDomain.value)
  return Array.from(merged)
})

watch(
  () => props.modelValue,
  (v) => {
    const raw = (v ?? '').toString().trim()
    if (!raw) {
      localPart.value = ''
      domain.value = 'qq.com'
      extraDomain.value = ''
      return
    }

    const at = raw.indexOf('@')
    if (at > 0 && at < raw.length - 1) {
      localPart.value = raw.slice(0, at)
      const d = raw.slice(at + 1)
      if (!COMMON_DOMAINS.includes(d)) extraDomain.value = d
      domain.value = d
    } else {
      // 用户只输入了账号部分
      localPart.value = raw.replace(/\s+/g, '')
    }
  },
  { immediate: true }
)

const emitValue = () => {
  const lp = (localPart.value ?? '').toString().trim()
  const d = (domain.value ?? '').toString().trim()
  if (!lp) {
    emit('update:modelValue', '')
    return
  }
  emit('update:modelValue', `${lp}@${d}`)
}
</script>

<style scoped>
.domain-select {
  width: 140px;
}
</style>

