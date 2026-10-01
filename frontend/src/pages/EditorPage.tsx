import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Alert,
  App,
  Button,
  Card,
  Col,
  Divider,
  Input,
  Row,
  Select,
  Space,
  Spin,
  Tag,
  Typography,
} from 'antd'
import { ArrowLeftOutlined, DownloadOutlined, SaveOutlined, ThunderboltOutlined } from '@ant-design/icons'
import { resumeApi, templateApi } from '@/api'
import { ApiError } from '@/api/client'
import ResumePreview from '@/components/ResumePreview'

const { Text, Paragraph } = Typography
const { TextArea } = Input

const PLACEHOLDER = `随便写，不用讲究格式，AI 会帮你理清：

2021.09-2025.06  某某大学  计算机
2025.07 至今      某某科技  后端工程师

实习的时候做过一个订单系统，流量挺大的，压力大的时候数据库老挂，
后来我们做了分库分表又加了一层缓存，现在好多了。

还搞过一个内部工具，接了公司的审批流程，之前大家都在群里问excel，
做完之后基本没人问过了。

熟悉 Java、Spring Boot、MySQL、Redis，k8s 会一点但是不熟。`

export default function EditorPage() {
  const { id } = useParams<{ id: string }>()
  const resumeId = Number(id)
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { message, modal } = App.useApp()

  const [fragments, setFragments] = useState('')
  const [targetPosition, setTargetPosition] = useState('')
  const [templateCode, setTemplateCode] = useState('classic')
  const [dirty, setDirty] = useState(false)

  const { data: resume, isLoading } = useQuery({
    queryKey: ['resume', resumeId],
    queryFn: () => resumeApi.get(resumeId),
  })

  const { data: templates } = useQuery({
    queryKey: ['templates'],
    queryFn: templateApi.list,
    // 模板是静态配置，缓存久一点
    staleTime: 60 * 60 * 1000,
  })

  // 首次拉到数据时回填表单，之后不再覆盖用户的输入
  useEffect(() => {
    if (resume && !dirty) {
      setFragments(resume.rawFragments ?? '')
      setTargetPosition(resume.targetPosition ?? '')
      setTemplateCode(resume.templateCode ?? 'classic')
    }
  }, [resume, dirty])

  const generateMutation = useMutation({
    mutationFn: () => resumeApi.generate(resumeId, fragments, targetPosition),
    onSuccess: (data) => {
      setDirty(false)
      message.success('生成完成')
      queryClient.setQueryData(['resume', resumeId], data)
    },
    onError: (e) => message.error(e instanceof ApiError ? e.message : '生成失败'),
  })

  const saveMutation = useMutation({
    mutationFn: () =>
      resumeApi.update(resumeId, { templateCode, targetPosition, rawFragments: fragments }),
    onSuccess: (data) => {
      setDirty(false)
      message.success('已保存')
      queryClient.setQueryData(['resume', resumeId], data)
    },
    onError: (e) => message.error(e instanceof ApiError ? e.message : '保存失败'),
  })

  const handleExport = () => {
    modal.confirm({
      title: '导出 PDF',
      content: 'PDF 由后端渲染，排版与这里的预览会略有差异（预览只是近似效果）。确认导出？',
      okText: '导出',
      onOk: async () => {
        try {
          if (dirty) await saveMutation.mutateAsync()
          await resumeApi.exportPdf(resumeId, `${resume?.title ?? 'resume'}.pdf`)
          message.success('已开始下载')
        } catch (e) {
          message.error(e instanceof ApiError ? e.message : '导出失败')
          throw e
        }
      },
    })
  }

  if (isLoading || !resume) {
    return (
      <div style={{ textAlign: 'center', paddingTop: 120 }}>
        <Spin size="large" />
      </div>
    )
  }

  return (
    <div style={{ padding: '20px 24px 64px', maxWidth: 1400, margin: '0 auto' }}>
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: 16, gap: 12 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/')}>
          返回
        </Button>
        <Text strong style={{ fontSize: 16 }}>
          {resume.title}
        </Text>
        {dirty && <Tag color="orange">未保存</Tag>}
        <div style={{ flex: 1 }} />
        <Space>
          <Button onClick={handleExport} icon={<DownloadOutlined />} disabled={!resume.content}>
            导出 PDF
          </Button>
          <Button
            onClick={() => saveMutation.mutate()}
            loading={saveMutation.isPending}
            disabled={!dirty}
            icon={<SaveOutlined />}
          >
            保存
          </Button>
        </Space>
      </div>

      <Row gutter={20}>
        {/* -------------------- 左：输入与配置 -------------------- */}
        <Col xs={24} lg={11}>
          <Card
            title="经历碎片"
            extra={
              <Button
                type="primary"
                icon={<ThunderboltOutlined />}
                loading={generateMutation.isPending}
                onClick={() => {
                  if (!fragments.trim()) {
                    message.warning('先写点经历碎片吧')
                    return
                  }
                  generateMutation.mutate()
                }}
              >
                AI 生成
              </Button>
            }
          >
            {generateMutation.isPending && (
              <Alert
                type="info"
                showIcon
                style={{ marginBottom: 12 }}
                message="AI 正在改写，通常需要 20-60 秒"
                description="这段时间可以先看看提示：写得越具体（时间、团队、量级、你的动作），生成的质量越高。"
              />
            )}

            <TextArea
              value={fragments}
              onChange={(e) => {
                setFragments(e.target.value)
                setDirty(true)
              }}
              placeholder={PLACEHOLDER}
              autoSize={{ minRows: 14, maxRows: 26 }}
            />

            <Divider style={{ margin: '18px 0' }} />

            <label style={{ display: 'block', marginBottom: 6 }}>
              <Text>目标岗位</Text>
            </label>
            <Input
              value={targetPosition}
              onChange={(e) => {
                setTargetPosition(e.target.value)
                setDirty(true)
              }}
              placeholder="例如：Java 后端工程师（高并发方向）"
              style={{ marginBottom: 16 }}
            />

            <label style={{ display: 'block', marginBottom: 6 }}>
              <Text>排版模板</Text>
            </label>
            <Select
              value={templateCode}
              onChange={(value) => {
                setTemplateCode(value)
                setDirty(true)
              }}
              style={{ width: '100%' }}
              options={(templates ?? []).map((t) => ({
                value: t.code,
                label: t.name,
                title: t.description,
              }))}
            />
            <Paragraph type="secondary" style={{ fontSize: 12, marginTop: 6, marginBottom: 0 }}>
              {templates?.find((t) => t.code === templateCode)?.description}
            </Paragraph>
          </Card>
        </Col>

        {/* -------------------- 右：预览 -------------------- */}
        <Col xs={24} lg={13}>
          <ResumePreview content={resume.content} />
        </Col>
      </Row>
    </div>
  )
}
