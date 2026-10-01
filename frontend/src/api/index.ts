import { http, unwrap } from './client'
import type { Resume, ResumeContent, TemplateInfo } from '@/types'

// ---------------------------------------------------------------- 认证

export const authApi = {
  register: (email: string, password: string, nickname?: string) =>
    unwrap<{ token: string }>(http.post('/auth/register', { email, password, nickname })),

  login: (email: string, password: string) =>
    unwrap<{ token: string }>(http.post('/auth/login', { email, password })),
}

// ---------------------------------------------------------------- 简历

export interface SaveResumePayload {
  title?: string
  targetPosition?: string
  templateCode?: string
  rawFragments?: string
  content?: ResumeContent
}

export const resumeApi = {
  list: () => unwrap<Resume[]>(http.get('/resumes')),

  get: (id: number) => unwrap<Resume>(http.get(`/resumes/${id}`)),

  create: (title?: string, targetPosition?: string) =>
    unwrap<Resume>(http.post('/resumes', { title, targetPosition })),

  update: (id: number, payload: SaveResumePayload) =>
    unwrap<Resume>(http.put(`/resumes/${id}`, payload)),

  remove: (id: number) => unwrap<void>(http.delete(`/resumes/${id}`)),

  /**
   * AI 生成。耗时通常 20-60 秒（取决于模型和简历长度），
   * 所以 client.ts 里的 timeout 给了 120s。
   */
  generate: (id: number, fragments: string, targetPosition?: string) =>
    unwrap<Resume>(http.post(`/resumes/${id}/generate`, { fragments, targetPosition })),

  /**
   * 导出 PDF。
   *
   * 不能直接 window.open(url) —— 那样浏览器不会带上 Authorization 头。
   * 所以走 blob 下载，用 fetch + 手动加 header。
   */
  async exportPdf(id: number, fileName = 'resume.pdf'): Promise<void> {
    const token = localStorage.getItem('resume-dog-token')
    const response = await fetch(`/api/resumes/${id}/export`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    })
    if (!response.ok) {
      throw new Error(`导出失败：HTTP ${response.status}`)
    }
    const blob = await response.blob()
    const url = URL.createObjectURL(blob)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = fileName
    document.body.appendChild(anchor)
    anchor.click()
    anchor.remove()
    URL.revokeObjectURL(url)
  },
}

// ---------------------------------------------------------------- 模板

export const templateApi = {
  list: () => unwrap<TemplateInfo[]>(http.get('/templates')),
}
