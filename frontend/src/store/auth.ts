import { create } from 'zustand'

const TOKEN_KEY = 'resume-dog-token'

interface AuthState {
  token: string | null
  login: (token: string) => void
  logout: () => void
}

/**
 * 极简的鉴权状态。
 * 产品里只有一个「当前用户」概念，没必要上 Redux——
 * 简历数据走 react-query，鉴权用 zustand，两者职责清晰。
 */
export const useAuthStore = create<AuthState>((set) => ({
  token: localStorage.getItem(TOKEN_KEY),
  login: (token) => {
    localStorage.setItem(TOKEN_KEY, token)
    set({ token })
  },
  logout: () => {
    localStorage.removeItem(TOKEN_KEY)
    set({ token: null })
  },
}))
