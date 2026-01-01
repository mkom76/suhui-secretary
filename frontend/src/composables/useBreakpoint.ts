import { ref, onMounted, onUnmounted, computed } from 'vue'

export function useBreakpoint() {
  const windowWidth = ref(window.innerWidth)

  const updateWidth = () => {
    windowWidth.value = window.innerWidth
  }

  onMounted(() => {
    window.addEventListener('resize', updateWidth)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', updateWidth)
  })

  const isMobile = computed(() => windowWidth.value < 768)
  const isTablet = computed(() => windowWidth.value >= 768 && windowWidth.value < 992)
  const isDesktop = computed(() => windowWidth.value >= 992)
  const isSmallScreen = computed(() => windowWidth.value < 992)

  return {
    windowWidth,
    isMobile,
    isTablet,
    isDesktop,
    isSmallScreen
  }
}
