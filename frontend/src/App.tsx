import { Navigate, Route, Routes } from 'react-router-dom'
import { Result, Button } from 'antd'
import { useAuthStore } from '@/store/auth'
import LoginPage from '@/pages/LoginPage'
import HomePage from '@/pages/HomePage'
import EditorPage from '@/pages/EditorPage'

function RequireAuth({ children }: { children: JSX.Element }) {
  const token = useAuthStore((s) => s.token)
  if (!token) {
    return <Navigate to="/login" replace />
  }
  return children
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/"
        element={
          <RequireAuth>
            <HomePage />
          </RequireAuth>
        }
      />
      <Route
        path="/resume/:id"
        element={
          <RequireAuth>
            <EditorPage />
          </RequireAuth>
        }
      />
      <Route
        path="*"
        element={
          <Result
            status="404"
            title="404"
            subTitle="页面不存在"
            extra={
              <Button type="primary" href="/">
                回到首页
              </Button>
            }
          />
        }
      />
    </Routes>
  )
}
