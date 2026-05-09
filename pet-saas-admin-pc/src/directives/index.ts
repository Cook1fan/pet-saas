import type { App } from 'vue'
import { viewer } from './viewer'

export function setupDirectives(app: App) {
  app.directive('viewer', viewer)
}

export { viewer } from './viewer'
export * from './viewer'
