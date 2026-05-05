<template>
  <div class="admin-layout">
    <!-- Admin banner -->
    <div class="admin-banner">
      <span class="admin-banner-icon">⚙</span>
      <span class="admin-banner-text">Administrator Mode</span>
    </div>
    <slot />
  </div>
</template>

<style scoped>
.admin-layout { min-height: 100vh; }

.admin-banner {
  position: fixed;
  top: var(--header-height);
  left: 0;
  right: 0;
  background: linear-gradient(90deg, rgba(200,155,60,0.08), rgba(200,155,60,0.04));
  border-bottom: 1px solid rgba(200,155,60,0.2);
  padding: 0.25rem 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  z-index: 800;
  font-family: 'Cinzel', serif;
  font-size: 0.65rem;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  color: var(--lol-gold);
}
</style>
