import './ProfileUserDates.css';

function formatDate(timestamp) {
  if (!timestamp) return '—';
  return new Date(timestamp).toLocaleDateString('ru-RU', {
    day: 'numeric',
    month: 'short'
  });
}

export default function ProfileUserDates(props) {
  return (
    <div class="profile-user-dates">
      <div class="profile-user-date-item">
        <span class="profile-user-date-label">Дата регистрации:</span>
        <span class="profile-user-date-value">{formatDate(props.registrationDate)}</span>
      </div>
      <div class="profile-user-date-item">
        <span class="profile-user-date-label">Последний вход:</span>
        <span class="profile-user-date-value">{formatDate(props.lastLogin)}</span>
      </div>
    </div>
  );
}
