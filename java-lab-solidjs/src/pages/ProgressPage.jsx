import { createSignal, createEffect, createMemo, Show } from 'solid-js';
import { useNavigate } from '@solidjs/router';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import { config } from '../config';
import './ProgressPage.css';

export default function ProgressPage() {
  const auth = useAuth();
  const navigate = useNavigate();
  const [user, setUser] = createSignal(null);
  const [error, setError] = createSignal(null);
  const [hasLoadedProfile, setHasLoadedProfile] = createSignal(false);

  const loadUserProfile = async () => {
    if (hasLoadedProfile()) return;
    setHasLoadedProfile(true);

    try {
      const accessToken = auth.accessToken();

      if (!accessToken) {
        setError('Пользователь не авторизован');
        return;
      }

      const response = await axios.get(`${config.backendUrl}/api/v1/auth`, {
        headers: {
          'Authorization': `Bearer ${accessToken}`,
        },
      });

      const data = response.data;

      setUser({
        id: data.id,
        username: data.username,
        firstName: data.name,
        lastName: data.lastName,
        email: data.email || '',
        avatar: data.avatar,
        registrationDate: new Date(data.dateRegistration).getTime(),
        lastLogin: new Date(data.lastLogin).getTime(),
        isVerified: data.activity,
        notificationsEnabled: data.notifyEnabled,
        timezone: data.timezone,
      });

      setError(null);
    } catch (err) {
      console.error('Ошибка при загрузке профиля:', err);
      setError('Не удалось загрузить данные профиля: ' + (err.message || ''));
    }
  };

  createEffect(() => {
    const token = auth.accessToken();
    const loading = auth.isLoading();

    if (token && !loading && !hasLoadedProfile()) {
      loadUserProfile();
    } else if (!token && !loading && !hasLoadedProfile()) {
      navigate('/login');
    }
  });

  const isLoading = createMemo(() => {
    return !user() && !error() && !hasLoadedProfile();
  });

  const hasError = createMemo(() => {
    return !!error();
  });

  const hasUser = createMemo(() => {
    return !!user();
  });

  return (
    <Show when={hasUser()} fallback={
      <Show when={hasError()} fallback={
        <div class="progress-page">
          <div class="progress-container">
            <div class="progress-loading">Загрузка...</div>
          </div>
        </div>
      }>
        <div class="progress-page">
          <div class="progress-container">
            <div class="progress-error">{error()}</div>
          </div>
        </div>
      </Show>
    }>
      <div class="progress-page">
        <div class="progress-container">
          <h1 class="progress-title">Прогресс пользователя</h1>
          
          <div class="progress-content">
            <div class="progress-stats">
              <div class="progress-stat-card">
                <div class="progress-stat-value">0</div>
                <div class="progress-stat-label">Выполнено задач</div>
              </div>
              
              <div class="progress-stat-card">
                <div class="progress-stat-value">0</div>
                <div class="progress-stat-label">Текущая серия</div>
              </div>
              
              <div class="progress-stat-card">
                <div class="progress-stat-value">0</div>
                <div class="progress-stat-label">Всего выполнено задач</div>
              </div>
            </div>

            <div class="progress-chart">
              <h2 class="progress-chart-title">Активность по дням</h2>
              <div class="progress-chart-placeholder">
                <p>График активности будет доступен в ближайшее время</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Show>
  );
}
