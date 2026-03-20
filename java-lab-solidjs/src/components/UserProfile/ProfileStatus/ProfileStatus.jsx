import './ProfileStatus.css';

export default function ProfileStatus(props) {
  return (
    <div class={`profile-status ${props.isVerified ? 'profile-status--verified' : 'profile-status--unverified'}`}>
      <svg class="profile-status-icon" viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
        {props.isVerified ? (
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
        ) : (
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
        )}
      </svg>
      <span class="profile-status-text">
        {props.isVerified ? 'Аккаунт подтверждён' : 'Аккаунт не подтверждён'}
      </span>
    </div>
  );
}
