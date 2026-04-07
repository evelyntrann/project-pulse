# Frontend — Claude Code Instructions

## Stack
- Vue.js 3 (Composition API with `<script setup>`)
- Vuetify 3 — do NOT use ElementPlus or any other UI library
- Pinia (state management)
- Vue Router 4
- Axios (HTTP calls)
- Vite (build tool)

## Commands
```bash
npm run dev        # Start dev server on http://localhost:5173
npm run build      # Production build → dist/
npm run preview    # Preview production build locally
npm run lint       # ESLint check
```

## Project Structure
```
src/
├── api/               # All Axios calls — one file per feature
│   ├── auth.js
│   ├── sections.js
│   ├── teams.js
│   ├── students.js
│   ├── instructors.js
│   ├── wars.js
│   ├── peerEvaluations.js
│   └── reports.js
├── components/        # Reusable components (not tied to one page)
│   ├── AppNavBar.vue
│   ├── ConfirmDialog.vue
│   └── DataTable.vue
├── views/             # Page-level components (one per route)
│   ├── LoginView.vue
│   ├── section/
│   │   ├── SectionListView.vue
│   │   └── SectionDetailView.vue
│   ├── team/
│   ├── student/
│   ├── war/
│   └── report/
├── stores/            # Pinia stores — one per feature
│   ├── auth.js
│   ├── section.js
│   └── war.js
├── router/
│   └── index.js      # All routes + role-based guards
└── main.js
```

## Naming Conventions
| Type | Pattern | Example |
|---|---|---|
| View (page) | `<Feature>View.vue` | `SectionListView.vue` |
| Component | PascalCase noun | `TeamCard.vue`, `WAREntryForm.vue` |
| Pinia store | camelCase noun | `useAuthStore`, `useSectionStore` |
| API file | camelCase feature | `sections.js`, `peerEvaluations.vue` |

## Coding Rules
- Use **Composition API** with `<script setup>` — never Options API
- Use **Vuetify components** for all UI: `v-btn`, `v-card`, `v-data-table`, `v-form`, `v-text-field`, etc.
- Never use raw HTML `<button>`, `<input>`, `<table>` — always use Vuetify equivalents
- All API calls go in `src/api/` files — never call `axios` directly inside a component or store
- Use **Pinia** for any state shared across multiple components
- Use `ref()` and `reactive()` for local component state
- Use `computed()` for derived values — never compute in the template

## Axios Setup
Centralize Axios in `src/api/axios.js`:
```js
import axios from 'axios'

const api = axios.create({ baseURL: '/api/v1' })

// Attach JWT to every request
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Redirect to login on 401
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) router.push('/login')
    return Promise.reject(err)
  }
)

export default api
```

Each feature API file imports from this:
```js
// src/api/sections.js
import api from './axios'
export const getSections = (params) => api.get('/sections', { params })
export const createSection = (data) => api.post('/sections', data)
export const getSectionById = (id) => api.get(`/sections/${id}`)
```

## Router & Auth Guards
```js
// src/router/index.js
router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isLoggedIn) return '/login'
  if (to.meta.role && auth.user.role !== to.meta.role) return '/forbidden'
})
```
Add `meta: { requiresAuth: true, role: 'ADMIN' }` to protected routes.

## Vuetify Theme
Use Vuetify's built-in theme system — do not write custom CSS unless absolutely necessary.
Define colors once in `main.js`:
```js
const vuetify = createVuetify({
  theme: {
    themes: {
      light: {
        colors: { primary: '#1565C0', secondary: '#42A5F5' }
      }
    }
  }
})
```

## Forms & Validation
Use Vuetify's `v-form` with `:rules` props — do not use a third-party form library:
```vue
<v-text-field
  v-model="form.email"
  label="Email"
  :rules="[v => !!v || 'Required', v => /.+@.+/.test(v) || 'Invalid email']"
/>
```

## Environment Variables
```
# .env.local (do NOT commit)
VITE_API_BASE_URL=http://localhost:8080
```
Access in code: `import.meta.env.VITE_API_BASE_URL`
