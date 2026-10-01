/**
 * 与后端 ResumeContent / Resume 实体一一对应的类型。
 * 字段名保持与 Java record 完全一致，避免额外的映射层。
 */

export interface BasicInfo {
  name: string
  targetPosition: string
  phone: string
  email: string
  city: string
  homepage: string
  summary: string
}

export interface EducationItem {
  school: string
  major: string
  degree: string
  period: string
  gpa: string
  highlights: string[]
}

export interface WorkItem {
  company: string
  position: string
  period: string
  highlights: string[]
}

export interface ProjectItem {
  name: string
  role: string
  period: string
  description: string
  techStack: string
  highlights: string[]
}

export interface SkillGroup {
  category: string
  items: string[]
}

export interface ResumeContent {
  basicInfo: BasicInfo
  education: EducationItem[]
  work: WorkItem[]
  projects: ProjectItem[]
  skills: SkillGroup[]
  honors: string[]
}

export interface Resume {
  id: number
  userId: number
  title: string
  targetPosition: string | null
  templateCode: string
  rawFragments: string | null
  content: ResumeContent | null
  createdAt: string
  updatedAt: string
}

export interface TemplateInfo {
  code: string
  name: string
  description: string
  file: string
}

/** 后端统一响应包装：code === 0 表示成功 */
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: string
}
