import { Show } from 'solid-js';
import { Navigate } from '@solidjs/router';
import VerificationCodePage from '../components/Register/VerificationCodePage/VerificationCodePage';
import { useRegister } from '../context/RegisterContext';
import './RegisterPage.css';

function VerificationContent() {
  const register = useRegister();

  return (
    <Show when={register.isVerificationSent()} fallback={<Navigate href="/" />}>
      <VerificationCodePage />
    </Show>
  );
}

export default function VerificationPage() {
  return (
    <div class="register-page">
      <VerificationContent />
    </div>
  );
}