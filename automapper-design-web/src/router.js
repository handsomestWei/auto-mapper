import { createRouter, createWebHistory } from 'vue-router'
import Home from './views/Home.vue'
import SchemaDesigner from './views/SchemaDesigner.vue'
import RuleDesigner from './views/RuleDesigner.vue'

export default createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: Home, meta: { title: '首页' } },
    { path: '/schema', name: 'schema', component: SchemaDesigner, meta: { title: 'Schema 设计器' } },
    { path: '/rule', name: 'rule', component: RuleDesigner, meta: { title: '字段转换规则设计器' } }
  ]
})
