import { Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from './components/ProtectedRoute'
import { AppLayout } from './components/AppLayout'
import { LoginPage } from './pages/LoginPage'
import { CallbackPage } from './pages/CallbackPage'
import { FeedPage } from './pages/FeedPage'
import { ProfilePage } from './pages/ProfilePage'
import { ChatsPage } from './pages/ChatsPage'
import { ChatConversation } from './pages/ChatConversation'
import { NotificationsPage } from './pages/NotificationsPage'
import { WellbeingPage } from './pages/WellbeingPage'
import { UserProfilePage } from './pages/UserProfilePage'
import { PeoplePage } from './pages/PeoplePage'

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/callback" element={<CallbackPage />} />
      <Route
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/" element={<FeedPage />} />
        <Route path="/chats" element={<ChatsPage />} />
        <Route path="/chats/:id" element={<ChatConversation />} />
        <Route path="/notifications" element={<NotificationsPage />} />
        <Route path="/wellbeing" element={<WellbeingPage />} />
        <Route path="/people" element={<PeoplePage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/profiles/:id" element={<UserProfilePage />} />
      </Route>
    </Routes>
  )
}
