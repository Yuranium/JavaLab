import { createMemo, Show } from 'solid-js';
import { useNavigate } from '@solidjs/router';
import { useLogin } from '../../../context/LoginContext';
import { useAuth } from '../../../context/AuthContext';
import './LoginForm.css';

export default function LoginForm() {
  const login = useLogin();
  const auth = useAuth();
  const navigate = useNavigate();

  const errors = createMemo(() => login.errors());
  const formData = createMemo(() => login.formData());

  const handleSubmit = async (e) => {
    e.preventDefault();
    const success = await login.submitForm(auth);
    
    if (success) {
      navigate('/');
    }
  };

  return (
    <div class="login-form-container">
      <div class="login-form-header">
        <h1 class="login-form-title">Вход</h1>
        <p class="login-form-subtitle">
          Войдите для доступа ко всем возможностям
        </p>
      </div>

      <Show when={errors().general}>
        <div class="login-error-message">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          <span>{errors().general}</span>
        </div>
      </Show>

      <form class="login-form" onSubmit={handleSubmit} novalidate>
          <div class="login-form-row">
          <div class="form-input-wrapper">
            <label class="form-input-label" for="username">
              Имя пользователя
              <span class="form-input-required">*</span>
            </label>
            <input
              class="form-input"
              classList={{ 'form-input--error': !!errors().username }}
              type="text"
              id="username"
              name="username"
              value={formData().username}
              onInput={(e) => login.updateField('username', e.target.value)}
              placeholder="@username"
            />
            <span class="form-input-hint">Вводите имя пользователя без символа '@' вначале</span>
            <Show when={errors().username}>
              <span class="form-input-error">{errors().username}</span>
            </Show>
          </div>
        </div>

        <div class="login-form-row">
          <div class="form-input-wrapper">
            <label class="form-input-label" for="password">
              Пароль
              <span class="form-input-required">*</span>
            </label>
            <input
              class="form-input"
              classList={{ 'form-input--error': !!errors().password }}
              type="password"
              id="password"
              name="password"
              value={formData().password}
              onInput={(e) => login.updateField('password', e.target.value)}
              placeholder="••••••••"
            />
            <Show when={errors().password}>
              <span class="form-input-error">{errors().password}</span>
            </Show>
          </div>
        </div>

        <button
          type="submit"
          class="login-submit-btn"
          disabled={login.isSubmitting()}
        >
          {login.isSubmitting() ? (
            <>
              <span class="login-submit-spinner"></span>
              Вход...
            </>
          ) : (
            'Войти'
          )}
        </button>
      </form>

      <p class="login-register-link">
        Нет аккаунта? <a href="/register">Зарегистрироваться</a>
      </p>
    </div>
  );
}