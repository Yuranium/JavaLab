import { Router, Route } from '@solidjs/router';
import { ThemeProvider } from './context/ThemeContext';
import { UsersProvider } from './context/UsersContext';
import { RegisterProvider } from './context/RegisterContext';
import { AuthProvider } from './context/AuthContext';
import { ProfileProvider } from './context/ProfileContext';
import Header from './components/Header/Header';
import Footer from './components/Footer/Footer';
import Hero from './components/Hero/Hero';
import Features from './components/Features/Features';
import HowItWorks from './components/HowItWorks/HowItWorks';
import CTA from './components/CTA/CTA';
import Profile from './components/UserProfile/Profile/Profile';
import AdminUsersPage from './pages/AdminUsersPage';
import RegisterPage from './pages/RegisterPage';
import VerificationPage from './pages/VerificationPage';
import LoginPage from './pages/LoginPage';
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

function Tasks() {
  return (
    <div style="padding: 80px 24px; text-align: center;">
      <h1 style="font-size: 36px; margin-bottom: 16px;">Задачи</h1>
      <p style="color: var(--text-secondary);">Раздел с задачами в разработке</p>
    </div>
  );
}

export default function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <UsersProvider>
          <RegisterProvider>
            <ProfileProvider>
              <div class="app">
                <Header />
                <main class="main-content">
                  <Router>
                    <Route path="/" component={Home} />
                    <Route path="/tasks" component={Tasks} />
                    <Route path="/login" component={LoginPage} />
                    <Route path="/register" component={RegisterPage} />
                    <Route path="/register/verify" component={VerificationPage} />
                    <Route path="/profile" component={Profile} />
                    <Route path="/admin/users" component={AdminUsersPage} />
                  </Router>
                </main>
                <Footer />
              </div>
            </ProfileProvider>
          </RegisterProvider>
        </UsersProvider>
      </AuthProvider>
    </ThemeProvider>
  );
}