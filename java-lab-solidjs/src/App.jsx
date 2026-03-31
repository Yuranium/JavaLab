import { Router, Route } from '@solidjs/router';
import { ThemeProvider } from './context/ThemeContext';
import { AuthProvider } from './context/AuthContext';
import Layout from './components/Layout/Layout';
import Hero from './components/Hero/Hero';
import Features from './components/Features/Features';
import HowItWorks from './components/HowItWorks/HowItWorks';
import CTA from './components/CTA/CTA';
import Profile from './components/UserProfile/Profile/Profile';
import RegisterPage from './pages/RegisterPage';
import VerificationPage from './pages/VerificationPage';
import LoginPage from './pages/LoginPage';
import ProgressPage from './pages/ProgressPage';
import UserProfilePage from './pages/UserProfilePage';
import CallbackPage from './pages/CallbackPage';
import TasksPage from './pages/TasksPage';
import { UsersProvider } from './context/UsersContext';
import { RegisterProvider } from './context/RegisterContext';
import { ProfileProvider } from './context/ProfileContext';
import AdminUsersPage from './pages/AdminUsersPage';
import './App.css';

function Home() {
  return (
    <div class="home-page">
      <Hero />
      <Features />
      <HowItWorks />
      <CTA />
    </div>
  );
}

function AdminUsersRoute() {
  return (
    <UsersProvider>
      <AdminUsersPage />
    </UsersProvider>
  );
}

export default function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <RegisterProvider>
          <ProfileProvider>
            <Router root={Layout}>
              <Route path="/" component={Home} />
              <Route path="/tasks" component={TasksPage} />
              <Route path="/login" component={LoginPage} />
              <Route path="/register" component={RegisterPage} />
              <Route path="/register/verify" component={VerificationPage} />
              <Route path="/callback" component={CallbackPage} />
              <Route path="/profile" component={Profile} />
              <Route path="/profile/:id" component={UserProfilePage} />
              <Route path="/progress" component={ProgressPage} />
              <Route path="/admin/users" component={AdminUsersRoute} />
            </Router>
          </ProfileProvider>
        </RegisterProvider>
      </AuthProvider>
    </ThemeProvider>
  );
}