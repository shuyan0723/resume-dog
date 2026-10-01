import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Alert, Button, Card, Form, Input, Tabs, Typography } from 'antd'
import { ThunderboltOutlined } from '@ant-design/icons'
import { authApi } from '@/api'
import { ApiError } from '@/api/client'
import { useAuthStore } from '@/store/auth'

const { Title, Paragraph, Text } = Typography

export default function LoginPage() {
  const [isRegister, setIsRegister] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const login = useAuthStore((s) => s.login)
  const navigate = useNavigate()

  const handleSubmit = async (values: { email: string; password: string; nickname?: string }) => {
    setLoading(true)
    setError(null)
    try {
      const result = isRegister
        ? await authApi.register(values.email, values.password, values.nickname)
        : await authApi.login(values.email, values.password)
      login(result.token)
      navigate('/')
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '操作失败，请重试')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div
      style={{
        minHeight: '100%',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(135deg, #f0f4ff 0%, #f5f6f8 100%)',
      }}
    >
      <Card style={{ width: 400 }} styles={{ body: { padding: 32 } }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <ThunderboltOutlined style={{ fontSize: 32, color: '#2f6fed' }} />
          <Title level={3} style={{ margin: '8px 0 4px' }}>
            简历狗
          </Title>
          <Paragraph type="secondary" style={{ margin: 0 }}>
            写下经历碎片，AI 帮你写成能投递的简历
          </Paragraph>
        </div>

        {error && <Alert type="error" message={error} showIcon style={{ marginBottom: 16 }} />}

        <Tabs
          activeKey={isRegister ? 'register' : 'login'}
          onChange={(key) => {
            setIsRegister(key === 'register')
            setError(null)
          }}
          items={[
            { key: 'login', label: '登录' },
            { key: 'register', label: '注册' },
          ]}
        />

        <Form layout="vertical" onFinish={handleSubmit} requiredMark={false}>
          <Form.Item
            name="email"
            label="邮箱"
            rules={[
              { required: true, message: '请输入邮箱' },
              { type: 'email', message: '邮箱格式不正确' },
            ]}
          >
            <Input placeholder="you@example.com" autoComplete="email" />
          </Form.Item>

          <Form.Item
            name="password"
            label="密码"
            rules={[
              { required: true, message: '请输入密码' },
              ...(isRegister ? [{ min: 8, message: '密码至少 8 位' }] : []),
            ]}
          >
            <Input.Password
              placeholder={isRegister ? '至少 8 位' : '请输入密码'}
              autoComplete={isRegister ? 'new-password' : 'current-password'}
            />
          </Form.Item>

          {isRegister && (
            <Form.Item name="nickname" label="昵称（选填）">
              <Input placeholder="不填就用邮箱前缀" />
            </Form.Item>
          )}

          <Form.Item style={{ marginBottom: 0 }}>
            <Button type="primary" htmlType="submit" block loading={loading} size="large">
              {isRegister ? '注册并进入' : '登录'}
            </Button>
          </Form.Item>
        </Form>

        <Text type="secondary" style={{ fontSize: 12, display: 'block', marginTop: 16 }}>
          提示：本地开发时把后端的 <code>claude.mock</code> 设为 true，无需 API Key 也能体验完整流程。
        </Text>
      </Card>
    </div>
  )
}
