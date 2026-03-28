import { formatDistanceToNow } from './utils';
import { getS3Url } from '../../../config';
import './UserListItem.css';

export default function UserListItem(props) {
  const { user } = props;

  const parseDate = (dateString) => {
    if (!dateString) return null;
    return new Date(dateString).getTime();
  };

  const formatDate = (dateString) => {
    const timestamp = parseDate(dateString);
    if (!timestamp) return '—';
    return formatDistanceToNow(timestamp);
  };

  const formatFullDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const avatarUrl = user.avatar ? getS3Url(user.avatar) : null;

  return (
    <div class="user-list-item" classList={{ 'user-list-item--blocked': props.isBlocked }}>
      <a href={`/profile/@${user.username}`} class="user-list-item-link">
        <div class="user-list-item-avatar">
          {avatarUrl ? (
            <img 
              src={avatarUrl} 
              alt="Аватар" 
              onError={(e) => {
                e.target.style.display = 'none';
                e.target.nextElementSibling.style.display = 'flex';
              }}
            />
          ) : null}
          <span style={avatarUrl ? 'display: none;' : ''}>
            {user.username.charAt(0).toUpperCase()}
          </span>
        </div>

        <div class="user-list-item-content">
          <div class="user-list-item-header">
            <span class="user-list-item-username">{user.username}</span>
            <span
              class="user-list-item-status"
              classList={{
                'user-list-item-status--verified': user.activity,
                'user-list-item-status--unverified': !user.activity
              }}
              title={user.activity ? 'Активен' : 'Неактивен'}
            >
              {user.activity ? (
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
            {(user.name || user.lastName) && (
              <span class="user-list-item-name">
                {user.name}{user.name && user.lastName ? ' ' : ''}{user.lastName}
              </span>
            )}
          </div>

          <div class="user-list-item-dates">
            <div class="user-list-item-date-row">
              <span class="user-list-item-date-label">Регистрация:</span>
              <span class="user-list-item-date-value" title={formatFullDate(user.dateRegistration)}>
                {formatDate(user.dateRegistration)}
              </span>
            </div>
            <div class="user-list-item-date-row">
              <span class="user-list-item-date-label">Последний вход:</span>
              <span class="user-list-item-date-value" title={formatFullDate(user.lastLogin)}>
                {formatDate(user.lastLogin)}
              </span>
            </div>
            <div class="user-list-item-date-row">
              <span class="user-list-item-date-label">Уведомления:</span>
              <span class="user-list-item-date-value">
                {user.notifyEnabled ? 'Включены' : 'Отключены'}
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
          props.onBlockClick(user);
        }}
        disabled={props.isBlocked}
        classList={{ 'user-list-item-block-btn--blocked': props.isBlocked }}
      >
        {props.isBlocked ? 'Заблокирован' : 'Заблокировать'}
      </button>
    </div>
  );
}
