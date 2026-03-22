import { createSignal, createEffect, createMemo, Show } from 'solid-js';
import { useParams, useNavigate } from '@solidjs/router';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import { config, getS3Url } from '../config';
import './UserProfilePage.css';
import ProfileStatus from '../components/UserProfile/ProfileStatus/ProfileStatus';
import ProfileUserDates from '../components/UserProfile/ProfileUserDates/ProfileUserDates';
import ProfileInfo from '../components/UserProfile/ProfileInfo/ProfileInfo';
import ProfileSettings from '../components/UserProfile/ProfileSettings/ProfileSettings';

export default function UserProfilePage() {
  const params = useParams();
  const auth = useAuth();
  const navigate = useNavigate();
  const [user, setUser] = createSignal(null);
  const [error, setError] = createSignal(null);
  const [isLoading, setIsLoading] = createSignal(true);
  const [isOwnProfile, setIsOwnProfile] = createSignal(false);

  const loadUserProfile = async () => {
    setIsLoading(true);
    setError(null);

    try {
      const accessToken = auth.accessToken();

      if (!accessToken) {
        setError('Пользователь не авторизован');
        return;
      }

      const userId = params.id;
      const response = await axios.get(`${config.backendUrl}/api/v1/user/${userId}`, {
        headers: {
          'Authorization': `Bearer ${accessToken}`,
        },
      });

      const data = response.data;

      const currentUserId = auth.user()?.id;
      setIsOwnProfile(currentUserId === data.id);

      setUser({
        id: data.id,
        username: data.username,
        firstName: data.name,
        lastName: data.lastName,
        email: data.email || '',
        avatar: data.avatar,
        registrationDate: new Date(data.dateRegistration).getTime(),
        lastLogin: data.lastLogin ? new Date(data.lastLogin).getTime() : null,
        isVerified: data.activity,
        notificationsEnabled: data.notifyEnabled,
        timezone: data.timezone,
      });

      setError(null);
    } catch (err) {
      console.error('Ошибка при загрузке профиля пользователя:', err);
      const errorMessage = err.response?.data?.message || err.message || 'Не удалось загрузить данные профиля';
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  createEffect(() => {
    const token = auth.accessToken();
    const loading = auth.isLoading();

    if (token && !loading && params.id) {
      loadUserProfile();
    } else if (!token && !loading) {
      navigate('/login');
    }
  });

  const hasError = createMemo(() => {
    return !!error();
  });

  const hasUser = createMemo(() => {
    return !!user();
  });

  const getBackUrl = () => {
    if (isOwnProfile() || !auth.hasRole(auth.ROLES.ADMIN)) {
      return '/profile';
    }
    return '/admin/users';
  };

  const handleBackClick = () => {
    navigate(getBackUrl());
  };

  const getAvatarUrl = (avatarPath) => {
    if (!avatarPath) return null;
    if (avatarPath.startsWith('http://') || avatarPath.startsWith('https://')) {
      return avatarPath;
    }
    return getS3Url(avatarPath);
  };

  return (
    <Show when={hasUser()} fallback={
      <Show when={hasError()} fallback={
        <div class="user-profile-page">
          <div class="user-profile-container">
            <div class="user-profile-loading">Загрузка профиля...</div>
          </div>
        </div>
      }>
        <div class="user-profile-page">
          <div class="user-profile-container">
            <div class="user-profile-error-box">
              <svg class="user-profile-error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12"/>
                <line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
              <p class="user-profile-error-message">{error()}</p>
            </div>
            <button class="user-profile-back-btn" onClick={handleBackClick}>
              ← Назад
            </button>
          </div>
        </div>
      </Show>
    }>
      <div class="user-profile-page">
        <div class="user-profile-container">
          <button class="user-profile-back-btn" onClick={handleBackClick}>
            ← Назад
          </button>

          <h1 class="user-profile-title">Профиль пользователя</h1>

          <div class="user-profile-content">
            <div class="user-profile-sidebar">
              <div class="user-profile-avatar-container">
                {getAvatarUrl(user().avatar) ? (
                  <img src={getAvatarUrl(user().avatar)} alt="Аватар" class="user-profile-avatar" />
                ) : (
                  <div class="user-profile-avatar-placeholder">
                    <svg viewBox="0 0 24 24" width="48" height="48" fill="currentColor">
                      <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                    </svg>
                  </div>
                )}
              </div>
              <ProfileStatus isVerified={user().isVerified} />
              <ProfileUserDates
                registrationDate={user().registrationDate}
                lastLogin={user().lastLogin}
              />
            </div>

            <div class="user-profile-main">
              <div class="user-profile-header-section">
                <h2 class="user-profile-username">{user().username}</h2>
              </div>

              <ProfileInfo
                firstName={user().firstName}
                lastName={user().lastName}
                email={user().email}
                isReadOnly
              />

              <ProfileSettings
                notificationsEnabled={user().notificationsEnabled}
                isReadOnly
              />
            </div>
          </div>
        </div>
      </div>
    </Show>
  );
}
