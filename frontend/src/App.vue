<template>
  <!-- Global aurora background — fixed, behind all pages -->
  <div class="aurora-root" aria-hidden="true">
    <div class="aurora-tendril aurora-t1"></div>
    <div class="aurora-tendril aurora-t2"></div>
    <div class="aurora-tendril aurora-t3"></div>
    <div class="aurora-tendril aurora-t4"></div>
  </div>

  <AppHeader />

  <RouterView v-slot="{ Component }">
    <Transition name="fade" mode="out-in">
      <component :is="Component" />
    </Transition>
  </RouterView>
</template>

<script setup>
import { useRoute } from 'vue-router'
import AppHeader from './components/layout/AppHeader.vue'

const route = useRoute()

</script>

<style>
/* ── Aurora root layer ─────────────────────────────────────── */
.aurora-root {
  position: fixed;
  inset: 0;
  z-index: -1;
  pointer-events: none;
  background: linear-gradient(170deg, #061018 0%, #0A1825 60%, #050e18 100%);
  overflow: hidden;
}

.aurora-tendril {
  position: absolute;
  border-radius: 50%;
  filter: blur(48px);
  opacity: 0;
  will-change: transform, opacity;
}

/* Top-left primary tendril */
.aurora-t1 {
  width: 90vw; height: 55vh;
  top: -22%; left: -15%;
  background: radial-gradient(ellipse 55% 60% at 35% 50%,
    rgba(11,196,227,0.22) 0%,
    rgba(11,196,227,0.08) 40%,
    transparent 70%);
  animation: aurora-t1 14s ease-in-out infinite;
}

/* Top-right accent */
.aurora-t2 {
  width: 60vw; height: 40vh;
  top: -10%; right: -10%;
  background: radial-gradient(ellipse 50% 55% at 60% 40%,
    rgba(11,180,210,0.14) 0%,
    rgba(11,180,210,0.04) 50%,
    transparent 70%);
  animation: aurora-t2 18s ease-in-out infinite;
}

/* Bottom-right primary tendril */
.aurora-t3 {
  width: 85vw; height: 50vh;
  bottom: -20%; right: -15%;
  background: radial-gradient(ellipse 55% 60% at 65% 50%,
    rgba(11,196,227,0.20) 0%,
    rgba(11,196,227,0.07) 40%,
    transparent 70%);
  animation: aurora-t3 16s ease-in-out infinite;
}

/* Bottom-left accent */
.aurora-t4 {
  width: 55vw; height: 38vh;
  bottom: -8%; left: -8%;
  background: radial-gradient(ellipse 50% 55% at 40% 60%,
    rgba(5,180,200,0.12) 0%,
    rgba(5,180,200,0.04) 50%,
    transparent 70%);
  animation: aurora-t4 20s ease-in-out infinite;
}

@keyframes aurora-t1 {
  0%   { opacity: 0;    transform: translate(0,  0  ) rotate(-4deg) scaleY(1);    }
  25%  { opacity: 0.9;  transform: translate(4%, 3% ) rotate( 2deg) scaleY(1.08); }
  60%  { opacity: 1;    transform: translate(6%, 5% ) rotate(-1deg) scaleY(1.12); }
  85%  { opacity: 0.75; transform: translate(2%, 1% ) rotate(-6deg) scaleY(0.95); }
  100% { opacity: 0;    transform: translate(0,  0  ) rotate(-4deg) scaleY(1);    }
}
@keyframes aurora-t2 {
  0%   { opacity: 0;    transform: translate(0,   0  ) rotate(3deg);  }
  30%  { opacity: 0.6;  transform: translate(-3%, 4% ) rotate(-2deg); }
  65%  { opacity: 0.85; transform: translate(-5%, 6% ) rotate( 1deg); }
  100% { opacity: 0;    transform: translate(0,   0  ) rotate(3deg);  }
}
@keyframes aurora-t3 {
  0%   { opacity: 0;    transform: translate(0,   0   ) rotate(4deg)  scaleY(1);    }
  20%  { opacity: 0.85; transform: translate(-4%,-3%  ) rotate(-2deg) scaleY(1.06); }
  55%  { opacity: 1;    transform: translate(-6%,-5%  ) rotate( 1deg) scaleY(1.1);  }
  80%  { opacity: 0.7;  transform: translate(-2%,-1%  ) rotate( 5deg) scaleY(0.92); }
  100% { opacity: 0;    transform: translate(0,   0   ) rotate(4deg)  scaleY(1);    }
}
@keyframes aurora-t4 {
  0%   { opacity: 0;    transform: translate(0,  0   ) rotate(-3deg); }
  35%  { opacity: 0.55; transform: translate(3%,-4%  ) rotate( 2deg); }
  70%  { opacity: 0.75; transform: translate(5%,-6%  ) rotate(-1deg); }
  100% { opacity: 0;    transform: translate(0,  0   ) rotate(-3deg); }
}
</style>
