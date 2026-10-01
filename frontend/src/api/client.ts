import axios, { type AxiosError } from 'axios'
import type { ApiResponse } from '@/types'
import { useAuthStore } from '@/store/auth'

/**
 * 统一的 HTTP 客户端。
 *
 * 两个约定：
 * 1. 业务成功码是 code === 0，不是 HTTP 2xx。直接解包 data 抛给调用方。
 * 2. 401 统一跳登录页，避免每个页面各写一遍。
 */
export const http = axios.create({
  baseURL: '/api',
  timeout: 120_000, // AI 生成是长耗时请求，超时给足
})

http.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiResponse<unknown>>) => {
    const status = error.response?.status
    if (status === 401) {
      useAuthStore.getState().logout()
      // 避免在登录页自身触发循环跳转
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    }
    return Promise.reject(new ApiError(error))
  },
)

export class ApiError extends Error {
  readonly code: number
  readonly httpStatus?: number

  constructor(error: AxiosError<ApiResponse<unknown>>) {
    super(error.response?.data?.message || error.message || '请求失败')
    this.code = error.response?.data?.code ?? -1
    this.httpStatus = error.response?.status
  }
}

/** 取出后端统一响应里的 data */
export async function unwrap<T>(promise: Promise<{ data: ApiResponse<T> }>): Promise<T> {
  const response = await promise
  if (response.data.code !== 0) {
    throw new ApiError({
      message: response.data.message,
    } as AxiosError<ApiResponse<unknown>>)
  }
  return response.data.data
}
