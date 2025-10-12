import {defineConfig} from 'vitest/config';

export default defineConfig({
  test: {
    environment: 'happy-dom',
    include: ['src/**/__tests__/**/*.{test,spec}.ts'],
    globals: true,
    coverage: {
      reporter: ['text', 'html'],
    },
  },
});
