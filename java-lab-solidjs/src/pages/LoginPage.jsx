import LoginForm from '../components/Login/LoginForm/LoginForm';
import { LoginProvider } from '../context/LoginContext';
import './RegisterPage.css';

export default function LoginPage() {
  return (
    <LoginProvider>
      <div class="register-page">
        <LoginForm />
      </div>
    </LoginProvider>
  );
}