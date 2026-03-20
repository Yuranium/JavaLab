import RegisterForm from '../components/Register/RegisterForm/RegisterForm';
import { RegisterProvider } from '../context/RegisterContext';
import './RegisterPage.css';

export default function RegisterPage() {
  return (
    <RegisterProvider>
      <div class="register-page">
        <RegisterForm />
      </div>
    </RegisterProvider>
  );
}
