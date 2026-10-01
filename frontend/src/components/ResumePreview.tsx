import { Empty, Tag } from 'antd'
import type { ResumeContent } from '@/types'

/**
 * 简历预览。
 *
 * 注意：这只是给用户看的「所见即所得」近似效果，**不是最终产物**。
 * 真正的排版由后端的 Thymeleaf + OpenHTMLtoPDF 渲染（见 backend 的 resume-templates/），
 * 因为只有服务端渲染才能保证投递出去的 PDF 跨端一致且正确嵌入中文字体。
 * 两者共用同一份 ResumeContent 数据，改模板时两边都要看一眼。
 */
export default function ResumePreview({ content }: { content: ResumeContent | null }) {
  if (!content) {
    return (
      <div className="resume-paper">
        <Empty description="还没有内容。在左侧写下经历碎片，然后点「AI 生成」" />
      </div>
    )
  }

  const { basicInfo, education, work, projects, skills, honors } = content

  return (
    <div className="resume-paper">
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-end',
          borderBottom: '2px solid #1a1a1a',
          paddingBottom: 12,
          marginBottom: 20,
        }}
      >
        <div>
          <div style={{ fontSize: 26, fontWeight: 700, letterSpacing: 2 }}>
            {basicInfo.name}
          </div>
          <div style={{ color: '#555', marginTop: 2 }}>{basicInfo.targetPosition}</div>
        </div>
        <div style={{ textAlign: 'right', color: '#333', fontSize: 13, lineHeight: 1.8 }}>
          {basicInfo.phone && <div>{basicInfo.phone}</div>}
          {basicInfo.email && <div>{basicInfo.email}</div>}
          {basicInfo.city && <div>{basicInfo.city}</div>}
          {basicInfo.homepage && <div>{basicInfo.homepage}</div>}
        </div>
      </div>

      {basicInfo.summary && (
        <Section title="个人概述">
          <p style={{ margin: 0 }}>{basicInfo.summary}</p>
        </Section>
      )}

      {education.length > 0 && (
        <Section title="教育背景">
          {education.map((edu, i) => (
            <div className="resume-entry" key={i}>
              <div className="resume-entry-head">
                <span className="resume-entry-org">{edu.school}</span>
                <span className="resume-entry-period">{edu.period}</span>
              </div>
              <div className="resume-entry-meta">
                {edu.major} · {edu.degree}
                {edu.gpa ? ` · ${edu.gpa}` : ''}
              </div>
              <BulletList items={edu.highlights} />
            </div>
          ))}
        </Section>
      )}

      {work.length > 0 && (
        <Section title="工作经历">
          {work.map((w, i) => (
            <div className="resume-entry" key={i}>
              <div className="resume-entry-head">
                <span className="resume-entry-org">
                  {w.company}
                  <span style={{ fontWeight: 400, marginLeft: 8 }}>{w.position}</span>
                </span>
                <span className="resume-entry-period">{w.period}</span>
              </div>
              <BulletList items={w.highlights} />
            </div>
          ))}
        </Section>
      )}

      {projects.length > 0 && (
        <Section title="项目经历">
          {projects.map((p, i) => (
            <div className="resume-entry" key={i}>
              <div className="resume-entry-head">
                <span className="resume-entry-org">
                  {p.name}
                  <span style={{ fontWeight: 400, marginLeft: 8 }}>{p.role}</span>
                </span>
                <span className="resume-entry-period">{p.period}</span>
              </div>
              {p.techStack && (
                <div className="resume-entry-meta">
                  技术栈：{p.techStack}
                </div>
              )}
              {p.description && <div className="resume-entry-meta">{p.description}</div>}
              <BulletList items={p.highlights} />
            </div>
          ))}
        </Section>
      )}

      {skills.length > 0 && (
        <Section title="专业技能">
          {skills.map((g, i) => (
            <div key={i} style={{ marginBottom: 4 }}>
              <strong>{g.category}</strong>：{g.items.join('、')}
            </div>
          ))}
        </Section>
      )}

      {honors.length > 0 && (
        <Section title="荣誉奖项">
          <BulletList items={honors} />
        </Section>
      )}
    </div>
  )
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="resume-section">
      <div className="resume-section-title">{title}</div>
      {children}
    </div>
  )
}

function BulletList({ items }: { items: string[] }) {
  if (!items || items.length === 0) return null
  return (
    <ul style={{ margin: '4px 0 0', paddingLeft: 18 }}>
      {items.map((text, i) => (
        <li key={i} style={{ marginBottom: 2 }}>
          {text}
        </li>
      ))}
    </ul>
  )
}
