import { createSignal } from 'solid-js';
import './Profile.css';
import ProfileAvatar from '../ProfileAvatar/ProfileAvatar';
import ProfileInfo from '../ProfileInfo/ProfileInfo';
import ProfileStatus from '../ProfileStatus/ProfileStatus';
import ProfileSettings from '../ProfileSettings/ProfileSettings';
import ProfileActions from '../ProfileActions/ProfileActions';
import ProfileUserDates from '../ProfileUserDates/ProfileUserDates';
import ActivityModal from '../../ProgressActivity/ActivityModal';

// Заглушка - данные будут приходить с сервера
const mockUserData = {
  id: '12345',
  username: 'john_doe',
  firstName: 'Иван',
  lastName: 'Петров',
  email: 'john.doe@example.com',
  avatar: null,
  registrationDate: 1640995200000,
  lastLogin: 1710864000000,
  isVerified: true,
  notificationsEnabled: true,
};

export default function Profile() {
  const [user, setUser] = createSignal({ ...mockUserData });
  const [isActivityModalOpen, setIsActivityModalOpen] = createSignal(false);

  const handleNameUpdate = (field, value) => {
    setUser(prev => ({ ...prev, [field]: value }));
    // TODO: отправить запрос на сервер для обновления
  };

  const handleNotificationsToggle = (enabled) => {
    setUser(prev => ({ ...prev, notificationsEnabled: enabled }));
    // TODO: отправить запрос на сервер для обновления
  };

  const handleDeleteAccount = () => {
    if (confirm('Вы уверены, что хотите удалить аккаунт?')) {
      // TODO: отправить запрос на сервер для удаления
      console.log('Аккаунт удалён');
    }
  };

  const handleAvatarUpload = (file) => {
    // TODO: отправить файл на сервер и получить байты аватара
    console.log('Загрузка аватара:', file);
  };

  return (
    <div class="profile-page">
      <div class="profile-container">
        <h1 class="profile-title">Профиль пользователя</h1>

        <div class="profile-content">
          <div class="profile-sidebar">
            <ProfileAvatar
              avatar={user().avatar}
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
              <h2 class="profile-username">@{user().username}</h2>
            </div>

            <ProfileInfo
              firstName={user().firstName}
              lastName={user().lastName}
              email={user().email}
              onNameUpdate={handleNameUpdate}
            />

            <ProfileSettings
              notificationsEnabled={user().notificationsEnabled}
              onNotificationsToggle={handleNotificationsToggle}
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
  );
}
