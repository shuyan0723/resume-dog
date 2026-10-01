import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  App,
  Button,
  Card,
  Col,
  Empty,
  Form,
  Input,
  Modal,
  Row,
  Spin,
  Tag,
  Typography,
} from 'antd'
import { DeleteOutlined, FileAddOutlined, LogoutOutlined, ThunderboltOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import { resumeApi } from '@/api'
import { ApiError } from '@/api/client'
import { useAuthStore } from '@/store/auth'
import type { Resume } from '@/types'

const { Title, Text } = Typography

export default function HomePage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { message } = App.useApp()
  const logout = useAuthStore((s) => s.logout)
  const [modalOpen, setModalOpen] = useState(false)
  const [form] = Form.useForm()

  const { data: resumes, isLoading } = useQuery({
    queryKey: ['resumes'],
    queryFn: resumeApi.list,
  })

  const createMutation = useMutation({
    mutationFn: ({ title, targetPosition }: { title?: string; targetPosition?: string }) =>
      resumeApi.create(title, targetPosition),
    onSuccess: (resume) => {
      queryClient.invalidateQueries({ queryKey: ['resumes'] })
      setModalOpen(false)
      form.resetFields()
      navigate(`/resume/${resume.id}`)
    },
    onError: (e) => message.error(e instanceof ApiError ? e.message : '创建失败'),
  })

  const deleteMutation = useMutation({
    mutationFn: resumeApi.remove,
    onSuccess: () => {
      message.success('已删除')
      queryClient.invalidateQueries({ queryKey: ['resumes'] })
    },
    onError: (e) => message.error(e instanceof ApiError ? e.message : '删除失败'),
  })

  return (
    <div style={{ maxWidth: 1000, margin: '0 auto', padding: '32px 24px 64px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <div>
          <Title level={3} style={{ margin: 0 }}>
            <ThunderboltOutlined style={{ color: '#2f6fed' }} /> 简历狗
          </Title>
          <Text type="secondary">把零散的经历，变成一份能投出去的简历</Text>
        </div>
        <div>
          <Button icon={<FileAddOutlined />} type="primary" onClick={() => setModalOpen(true)}>
            新建简历
          </Button>
          <Button style={{ marginLeft: 8 }} icon={<LogoutOutlined />} onClick={logout}>
            退出
          </Button>
        </div>
      </div>

      <Spin spinning={isLoading}>
        {resumes && resumes.length > 0 ? (
          <Row gutter={[16, 16]}>
            {resumes.map((resume: Resume) => (
              <Col key={resume.id} xs={24} sm={12} md={8}>
                <Card
                  hoverable
                  onClick={() => navigate(`/resume/${resume.id}`)}
                  actions={[
                    <DeleteOutlined
                      key="delete"
                      onClick={(e) => {
                        e.stopPropagation()
                        Modal.confirm({
                          title: `删除「${resume.title}」？`,
                          content: '该操作不可撤销。',
                          okType: 'danger',
                          onOk: () => deleteMutation.mutate(resume.id),
                        })
                      }}
                    />,
                  ]}
                >
                  <Title level={5} style={{ margin: 0 }}>
                    {resume.title}
                  </Title>
                  <div style={{ marginTop: 6 }}>
                    {resume.targetPosition ? (
                      <Tag color="blue">{resume.targetPosition}</Tag>
                    ) : (
                      <Tag>未设目标岗位</Tag>
                    )}
                    <Tag bordered={false}>{resume.templateCode}</Tag>
                  </div>
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    {resume.content ? '已生成' : '待生成'} · 更新于{' '}
                    {dayjs(resume.updatedAt).format('YYYY-MM-DD HH:mm')}
                  </Text>
                </Card>
              </Col>
            ))}
          </Row>
        ) : (
          <Card>
            <Empty description="还没有简历">
              <Button type="primary" onClick={() => setModalOpen(true)}>
                新建第一份
              </Button>
            </Empty>
          </Card>
        )}
      </Spin>

      <Modal
        title="新建简历"
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        confirmLoading={createMutation.isPending}
        okText="创建"
      >
        <Form form={form} layout="vertical" onFinish={createMutation.mutate} style={{ marginTop: 16 }}>
          <Form.Item name="title" label="简历名称" initialValue="我的简历">
            <Input placeholder="例如：字节后端 - 2026 秋招" />
          </Form.Item>
          <Form.Item
            name="targetPosition"
            label="目标岗位"
            extra="AI 会按这个岗位决定每段经历的详略取舍，写得越具体效果越好"
          >
            <Input placeholder="例如：Java 后端工程师（高并发方向）" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
