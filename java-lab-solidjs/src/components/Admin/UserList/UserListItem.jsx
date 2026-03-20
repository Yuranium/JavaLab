import { formatDistanceToNow } from './utils';
import './UserListItem.css';

export default function UserListItem(props) {
  const { user, onBlockClick } = props;

  const formatDate = (timestamp) => {
    if (!timestamp) return '—';
    return formatDistanceToNow(timestamp);
  };

  const formatFullDate = (timestamp) => {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    return date.toLocaleDateString('ru-RU', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div class="user-list-item" classList={{ 'user-list-item--blocked': props.isBlocked }}>
      <a href={`/profile/${user.id}`} class="user-list-item-link">
        <div class="user-list-item-avatar">
          <span>{user.username.charAt(0).toUpperCase()}</span>
        </div>

        <div class="user-list-item-content">
          <div class="user-list-item-header">
            <span class="user-list-item-username">@{user.username}</span>
            <span 
              class="user-list-item-status" 
              classList={{ 
                'user-list-item-status--verified': user.isVerified,
                'user-list-item-status--unverified': !user.isVerified 
              }}
              title={user.isVerified ? 'Аккаунт подтверждён' : 'Аккаунт не подтверждён'}
            >
              {user.isVerified ? (
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                  <polyline points="20 6 9 17 4 12" />
                </svg>
              ) : (
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                  <line x1="5" y1="12" x2="19" y2="12" />
                </svg>
              )}
            </span>
          </div>

          <div class="user-list-item-info">
            {(user.firstName || user.lastName) && (
              <span class="user-list-item-name">
                {user.firstName}{user.firstName && user.lastName ? ' ' : ''}{user.lastName}
              </span>
            )}
          </div>

          <div class="user-list-item-dates">
            <div class="user-list-item-date-row">
              <span class="user-list-item-date-label">Регистрация:</span>
              <span class="user-list-item-date-value" title={formatFullDate(user.registrationDate)}>
                {formatDate(user.registrationDate)}
              </span>
            </div>
            <div class="user-list-item-date-row">
              <span class="user-list-item-date-label">Последний вход:</span>
              <span class="user-list-item-date-value" title={formatFullDate(user.lastLogin)}>
                {formatDate(user.lastLogin)}
              </span>
            </div>
          </div>
        </div>
      </a>

      <button
        class="user-list-item-block-btn"
        onClick={(e) => {
          e.preventDefault();
          e.stopPropagation();
          onBlockClick(user);
        }}
        disabled={props.isBlocked}
        classList={{ 'user-list-item-block-btn--blocked': props.isBlocked }}
      >
        {props.isBlocked ? 'Заблокирован' : 'Заблокировать'}
      </button>
    </div>
  );
}
