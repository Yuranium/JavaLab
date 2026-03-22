import { createSignal, createEffect, createMemo, Show } from 'solid-js';
import { useNavigate } from '@solidjs/router';
import axios from 'axios';
import { useAuth } from '../../../context/AuthContext';
import { useProfile } from '../../../context/ProfileContext';
import { config } from '../../../config';
import './Profile.css';
import ProfileAvatar from '../ProfileAvatar/ProfileAvatar';
import ProfileInfo from '../ProfileInfo/ProfileInfo';
import ProfileStatus from '../ProfileStatus/ProfileStatus';
import ProfileSettings from '../ProfileSettings/ProfileSettings';
import ProfileActions from '../ProfileActions/ProfileActions';
import ProfileUserDates from '../ProfileUserDates/ProfileUserDates';
import ActivityModal from '../../ProgressActivity/ActivityModal';

export default function Profile() {
  const auth = useAuth();
  const profile = useProfile();
  const navigate = useNavigate();
  const [user, setUser] = createSignal(null);
  const [error, setError] = createSignal(null);
  const [isActivityModalOpen, setIsActivityModalOpen] = createSignal(false);
  const [hasLoadedProfile, setHasLoadedProfile] = createSignal(false);

  const loadUserProfile = async () => {
    if (hasLoadedProfile()) return;
    setHasLoadedProfile(true);
    
    try {
      const accessToken = auth.accessToken();
      
      console.log('Profile: accessToken =', accessToken);

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
      
      console.log('Profile: полученные данные =', data);

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

      console.log('Profile: setUser вызван');
      setError(null);
    } catch (err) {
      console.error('Ошибка при загрузке профиля:', err);
      setError('Не удалось загрузить данные профиля: ' + (err.message || ''));
    }
  };

  createEffect(() => {
    console.log('Profile: user изменился =', user());
  });

  createEffect(() => {
    const token = auth.accessToken();
    const loading = auth.isLoading();
    console.log('Profile: createEffect сработал, token =', !!token, 'loading =', loading, 'hasLoadedProfile =', hasLoadedProfile());
    
    if (token && !loading && !hasLoadedProfile()) {
      loadUserProfile();
    }
    else if (!token && !loading && !hasLoadedProfile()) {
      console.log('Profile: токена нет, перенаправление на /login');
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

  const handleNameUpdate = async (field, value) => {
    const accessToken = auth.accessToken();
    if (!accessToken) {
      setError('Пользователь не авторизован');
      return;
    }

    try {
      if (field === 'name') {
        const result = await profile.updateName(value, accessToken);
        setUser(prev => ({ ...prev, firstName: result.name }));
      } else if (field === 'lastName') {
        const result = await profile.updateLastName(value, accessToken);
        setUser(prev => ({ ...prev, lastName: result.lastName }));
      }
    } catch (err) {
      console.error(`Ошибка при обновлении ${field}:`, err);
      setError('Ошибка при обновлении: ' + (err.message || ''));
    }
  };

  const handleNotificationsToggle = async (enabled) => {
    const accessToken = auth.accessToken();
    if (!accessToken) {
      setError('Пользователь не авторизован');
      return;
    }

    try {
      await profile.updateNotifications(enabled, accessToken);
      setUser(prev => ({ ...prev, notificationsEnabled: enabled }));
    } catch (err) {
      console.error('Ошибка при обновлении уведомлений:', err);
      setError('Ошибка при обновлении уведомлений: ' + (err.message || ''));
      setUser(prev => ({ ...prev, notificationsEnabled: !enabled }));
    }
  };

  const handleDeleteAccount = () => {
    if (confirm('Вы уверены, что хотите удалить аккаунт?')) {
      // TODO: отправить запрос на сервер для удаления
      console.log('Аккаунт удалён');
    }
  };

  const handleAvatarUpload = async (file) => {
    const accessToken = auth.accessToken();
    if (!accessToken) {
      setError('Пользователь не авторизован');
      return;
    }

    profile.clearAvatarError();
    const result = await profile.updateAvatar(file, accessToken);

    if (result.success) {
      setUser(prev => ({ ...prev, avatar: result.avatar }));
    }
  };

  return (
    <Show when={hasUser()} fallback={
      <Show when={hasError()} fallback={
        <div class="profile-page">
          <div class="profile-container">
            <div class="profile-loading">Загрузка профиля...</div>
          </div>
        </div>
      }>
        <div class="profile-page">
          <div class="profile-container">
            <div class="profile-error">{error()}</div>
          </div>
        </div>
      </Show>
    }>
      <div class="profile-page">
        <div class="profile-container">
          <h1 class="profile-title">Профиль пользователя</h1>

          <div class="profile-content">
            <div class="profile-sidebar">
              <ProfileAvatar
                avatar={user().avatar}
                avatarError={profile.avatarError()}
                isUpdating={profile.isUpdatingAvatar()}
                onUpload={handleAvatarUpload}
                onActivityClick={() => setIsActivityModalOpen(true)}
              />
              <ProfileStatus isVerified={user().isVerified} />
              <ProfileUserDates
                registrationDate={user().registrationDate}
                lastLogin={user().lastLogin}
              />
            </div>

            <div class="profile-main">
              <div class="profile-header-section">
                <h2 class="profile-username">{user().username}</h2>
              </div>

              <ProfileInfo
                firstName={user().firstName}
                lastName={user().lastName}
                email={user().email}
                onNameUpdate={handleNameUpdate}
                isUpdatingName={profile.isUpdatingName()}
                isUpdatingLastName={profile.isUpdatingLastName()}
              />

              <ProfileSettings
                notificationsEnabled={user().notificationsEnabled}
                onNotificationsToggle={handleNotificationsToggle}
                isUpdatingNotifications={profile.isUpdatingNotifications()}
              />

              <ProfileActions onDeleteAccount={handleDeleteAccount} />
            </div>
          </div>
        </div>

        <ActivityModal
          isOpen={isActivityModalOpen()}
          onClose={() => setIsActivityModalOpen(false)}
        />
      </div>
    </Show>
  );
}
