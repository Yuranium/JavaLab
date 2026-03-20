import { onMount, createMemo, Show } from 'solid-js';
import { useRegister } from '../../../context/RegisterContext';
import FormFileInput from '../FormFileInput/FormFileInput';
import FormCheckbox from '../FormCheckbox/FormCheckbox';
import OAuthButtons from '../OAuthButtons/OAuthButtons';
import './RegisterForm.css';

export default function RegisterForm() {
  const register = useRegister();
  
  const errors = createMemo(() => register.errors());
  const formData = createMemo(() => register.formData());

  onMount(() => {
    register.initTimezone();
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    register.submitForm();
  };

  return (
    <div class="register-form-container">
      <div class="register-form-header">
        <h1 class="register-form-title">Регистрация</h1>
        <p class="register-form-subtitle">
          Создайте аккаунт, чтобы получить доступ ко всем возможностям
        </p>
      </div>

      <Show when={register.isSuccess()}>
        <div class="register-success-message">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
            <polyline points="22 4 12 14.01 9 11.01" />
          </svg>
          <span>Регистрация успешна! Теперь вы можете войти.</span>
        </div>
      </Show>

      <Show when={errors().general}>
        <div class="register-error-message">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          <span>{errors().general}</span>
        </div>
      </Show>

      <form class="register-form" onSubmit={handleSubmit} novalidate>
        <div class="register-form-row">
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
              onInput={(e) => register.updateField('username', e.target.value)}
              placeholder="@username"
            />
            <Show when={errors().username}>
              <span class="form-input-error">{errors().username}</span>
            </Show>
          </div>
        </div>

        <div class="register-form-row">
          <div class="form-input-wrapper">
            <label class="form-input-label" for="firstName">
              Имя
            </label>
            <input
              class="form-input"
              classList={{ 'form-input--error': !!errors().firstName }}
              type="text"
              id="firstName"
              name="firstName"
              value={formData().firstName}
              onInput={(e) => register.updateField('firstName', e.target.value)}
            />
            <Show when={errors().firstName}>
              <span class="form-input-error">{errors().firstName}</span>
            </Show>
          </div>
        </div>

        <div class="register-form-row">
          <div class="form-input-wrapper">
            <label class="form-input-label" for="lastName">
              Фамилия
            </label>
            <input
              class="form-input"
              classList={{ 'form-input--error': !!errors().lastName }}
              type="text"
              id="lastName"
              name="lastName"
              value={formData().lastName}
              onInput={(e) => register.updateField('lastName', e.target.value)}
            />
            <Show when={errors().lastName}>
              <span class="form-input-error">{errors().lastName}</span>
            </Show>
          </div>
        </div>

        <div class="register-form-row">
          <div class="form-input-wrapper">
            <label class="form-input-label" for="email">
              Email
              <span class="form-input-required">*</span>
            </label>
            <input
              class="form-input"
              classList={{ 'form-input--error': !!errors().email }}
              type="email"
              id="email"
              name="email"
              value={formData().email}
              onInput={(e) => register.updateField('email', e.target.value)}
              placeholder="example@email.com"
            />
            <Show when={errors().email}>
              <span class="form-input-error">{errors().email}</span>
            </Show>
          </div>
        </div>

        <div class="register-form-row">
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
              onInput={(e) => register.updateField('password', e.target.value)}
              placeholder="••••••••"
            />
            <Show when={errors().password}>
              <span class="form-input-error">{errors().password}</span>
            </Show>
          </div>
        </div>

        <div class="register-form-row">
          <div class="form-input-wrapper">
            <label class="form-input-label" for="confirmPassword">
              Подтверждение пароля
              <span class="form-input-required">*</span>
            </label>
            <input
              class="form-input"
              classList={{ 'form-input--error': !!errors().confirmPassword }}
              type="password"
              id="confirmPassword"
              name="confirmPassword"
              value={formData().confirmPassword}
              onInput={(e) => register.updateField('confirmPassword', e.target.value)}
              placeholder="••••••••"
            />
            <Show when={errors().confirmPassword}>
              <span class="form-input-error">{errors().confirmPassword}</span>
            </Show>
          </div>
        </div>

        <div class="register-form-row">
          <FormFileInput
            label="Аватар (необязательно)"
            name="avatar"
            value={formData().avatar}
            onChange={(file) => register.updateAvatar(file)}
            error={errors().avatar}
            accept="image/*"
            maxSize={10}
          />
        </div>

        <div class="register-form-row">
          <FormCheckbox
            label="Включить уведомления о новых задачах и активностях"
            name="notificationsEnabled"
            checked={formData().notificationsEnabled}
            onChange={(checked) => register.toggleNotifications(checked)}
          />
        </div>

        <input type="hidden" name="timezone" value={formData().timezone} />

        <button
          type="submit"
          class="register-submit-btn"
          disabled={register.isSubmitting()}
        >
          {register.isSubmitting() ? (
            <>
              <span class="register-submit-spinner"></span>
              Регистрация...
            </>
          ) : (
            'Зарегистрироваться'
          )}
        </button>
      </form>

      <OAuthButtons />

      <p class="register-login-link">
        Уже есть аккаунт? <a href="/login">Войти</a>
      </p>
    </div>
  );
}
