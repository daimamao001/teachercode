<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAdminStore } from '@/stores/admin'

const adminStore = useAdminStore()

onMounted(() => {
  adminStore.initAdmin()
})
</script>

<style lang="scss">
#app {
  width: 100%;
  height: 100%;
}
</style>

