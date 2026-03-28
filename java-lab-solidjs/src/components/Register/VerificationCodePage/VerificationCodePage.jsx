import { createSignal, createEffect, Show } from 'solid-js';
import { useNavigate } from '@solidjs/router';
import { useRegister } from '../../../context/RegisterContext';
import { useAuth } from '../../../context/AuthContext';
import './VerificationCodePage.css';

export default function VerificationCodePage() {
  const register = useRegister();
  const auth = useAuth();
  const navigate = useNavigate();
  const [code, setCode] = createSignal(['', '', '', '', '', '']);
  const [isSubmitting, setIsSubmitting] = createSignal(false);
  const [submitError, setSubmitError] = createSignal('');
  const [submitSuccess, setSubmitSuccess] = createSignal(false);

  const handleInputChange = (index, value) => {
    if (!/^\d*$/.test(value)) {
      return;
    }

    const newCode = [...code()];
    newCode[index] = value.slice(-1);
    setCode(newCode);
    setSubmitError('');

    if (value && index < 5) {
      const nextInput = document.getElementById(`code-input-${index + 1}`);
      if (nextInput) {
        nextInput.focus();
      }
    }

    if (newCode.every(digit => digit !== '')) {
      const fullCode = newCode.join('');
      register.updateVerificationCode(fullCode);
    }
  };

  const handleKeyDown = (index, e) => {
    if (e.key === 'Backspace' && !code()[index] && index > 0) {
      const prevInput = document.getElementById(`code-input-${index - 1}`);
      if (prevInput) {
        prevInput.focus();
      }
    }
  };

  const handlePaste = (e) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData('text').slice(0, 6);
    
    if (!/^\d+$/.test(pastedData)) {
      return;
    }

    const newCode = [...code()];
    for (let i = 0; i < pastedData.length && i < 6; i++) {
      newCode[i] = pastedData[i];
    }
    setCode(newCode);

    const nextEmptyIndex = newCode.findIndex(digit => digit === '');
    const focusIndex = nextEmptyIndex !== -1 ? nextEmptyIndex : 5;
    const inputToFocus = document.getElementById(`code-input-${focusIndex}`);
    if (inputToFocus) {
      inputToFocus.focus();
    }

    const fullCode = newCode.join('');
    if (fullCode.length === 6) {
      register.updateVerificationCode(fullCode);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const fullCode = code().join('');
    if (fullCode.length !== 6) {
      setSubmitError('Введите 6-значный код');
      return;
    }

    setIsSubmitting(true);
    setSubmitError('');

    const success = await register.submitVerificationCode(fullCode);

    if (success) {
      setSubmitSuccess(true);
      
      const credentials = register.getUserCredentials();
      
      if (!credentials.username || !credentials.password) {
        setSubmitError('Учётные данные не найдены. Попробуйте зарегистрироваться заново.');
        setIsSubmitting(false);
        return;
      }
      
      const result = await auth.login(credentials.username, credentials.password);

      if (result.success) {
        localStorage.removeItem('username');
        navigate('/');
      } else {
        setSubmitError(result.error || 'Ошибка при получении токенов');
      }
    } else {
      setSubmitError(register.verificationError() || 'Ошибка при проверке кода');
    }

    setIsSubmitting(false);
  };

  const handleResendCode = async () => {
    if (register.resendTimer() > 0) {
      return;
    }

    const success = await register.resendVerificationCode();
    
    if (success) {
      setCode(['', '', '', '', '', '']);
      setSubmitError('');
      setSubmitSuccess(false);
      const firstInput = document.getElementById('code-input-0');
      if (firstInput) {
        firstInput.focus();
      }
    }
  };

  createEffect(() => {
    const fullCode = code().join('');
    if (fullCode.length === 6) {
      register.updateVerificationCode(fullCode);
    }
  });

  return (
    <div class="verification-page">
      <div class="verification-container">
        <div class="verification-header">
          <div class="verification-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
              <line x1="16" y1="2" x2="16" y2="6" />
              <line x1="8" y1="2" x2="8" y2="6" />
              <line x1="3" y1="10" x2="21" y2="10" />
            </svg>
          </div>
          <h1 class="verification-title">Подтверждение кода</h1>
          <p class="verification-subtitle">
            Введите 6-значный код, отправленный на вашу почту
          </p>
        </div>

        <Show when={submitSuccess()}>
          <div class="verification-success">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
              <polyline points="22 4 12 14.01 9 11.01" />
            </svg>
            <span>Код успешно подтверждён!</span>
          </div>
        </Show>

        <Show when={submitError() || register.verificationError()}>
          <div class="verification-error">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
            <span>{submitError() || register.verificationError()}</span>
          </div>
        </Show>

        <form class="verification-form" onSubmit={handleSubmit}>
          <div class="code-inputs-container" onPaste={handlePaste}>
            {Array.from({ length: 6 }).map((_, index) => (
              <input
                id={`code-input-${index}`}
                type="text"
                inputMode="numeric"
                pattern="[0-9]*"
                maxlength="1"
                class="code-input"
                value={code()[index]}
                onInput={(e) => handleInputChange(index, e.target.value)}
                onKeyDown={(e) => handleKeyDown(index, e)}
                disabled={submitSuccess()}
              />
            ))}
          </div>

          <button
            type="submit"
            class="verification-submit-btn"
            disabled={isSubmitting() || submitSuccess() || code().some(digit => digit === '')}
          >
            {isSubmitting() ? (
              <>
                <span class="verification-spinner"></span>
                Проверка...
              </>
            ) : (
              'Подтвердить'
            )}
          </button>
        </form>

        <div class="resend-container">
          <p class="resend-text">Не получили код?</p>
          <button
            class="resend-btn"
            onClick={handleResendCode}
            disabled={register.resendTimer() > 0 || submitSuccess()}
          >
            {register.resendTimer() > 0 
              ? `Отправить повторно через ${register.resendTimer()} сек`
              : 'Отправить код повторно'}
          </button>
        </div>

        <div class="verification-back-link">
          <a href="/register">← Назад к регистрации</a>
        </div>
      </div>
    </div>
  );
}