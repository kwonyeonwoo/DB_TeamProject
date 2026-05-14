import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import './App.css'
import { GuestOnlyRoute, RequireAuth } from './components/AuthRoutes'
import { Layout } from './components/Layout'
import { AuthProvider } from './contexts/AuthProvider'
import { AdminReportPage } from './pages/AdminReportPage'
import { GroupPage } from './pages/GroupPage'
import { LoginPage } from './pages/LoginPage'
import { MainPage } from './pages/MainPage'
import { MyPage } from './pages/MyPage'
import { PostDetailPage } from './pages/PostDetailPage'
import { PostListPage } from './pages/PostListPage'
import { PostWritePage } from './pages/PostWritePage'
import { SchedulePage } from './pages/SchedulePage'
import { SignupPage } from './pages/SignupPage'

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route element={<Layout />}>
            <Route
              path="/"
              element={
                <RequireAuth>
                  <MainPage />
                </RequireAuth>
              }
            />
            <Route
              path="/login"
              element={
                <GuestOnlyRoute>
                  <LoginPage />
                </GuestOnlyRoute>
              }
            />
            <Route
              path="/signup"
              element={
                <GuestOnlyRoute>
                  <SignupPage />
                </GuestOnlyRoute>
              }
            />
            <Route
              path="/posts"
              element={
                <RequireAuth>
                  <PostListPage />
                </RequireAuth>
              }
            />
            <Route
              path="/posts/write"
              element={
                <RequireAuth>
                  <PostWritePage />
                </RequireAuth>
              }
            />
            <Route
              path="/posts/:postId"
              element={
                <RequireAuth>
                  <PostDetailPage />
                </RequireAuth>
              }
            />
            <Route
              path="/mypage"
              element={
                <RequireAuth>
                  <MyPage />
                </RequireAuth>
              }
            />
            <Route
              path="/schedule"
              element={
                <RequireAuth>
                  <SchedulePage />
                </RequireAuth>
              }
            />
            <Route
              path="/groups"
              element={
                <RequireAuth>
                  <GroupPage />
                </RequireAuth>
              }
            />
            <Route
              path="/admin/reports"
              element={
                <RequireAuth adminOnly>
                  <AdminReportPage />
                </RequireAuth>
              }
            />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}

export default App
