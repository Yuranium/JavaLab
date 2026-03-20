import './ProfileActions.css';

export default function ProfileActions(props) {
  const handleDeleteClick = () => {
    if (props.onDeleteAccount) {
      props.onDeleteAccount();
    }
  };

  return (
    <div class="profile-actions">
      <h3 class="profile-actions-title">Опасная зона</h3>

      <div class="profile-actions-item">
        <div class="profile-actions-info">
          <span class="profile-actions-label">Удаление аккаунта</span>
          <span class="profile-actions-description">
            После удаления аккаунта все данные будут безвозвратно удалены
          </span>
        </div>
        <button
          class="profile-actions-delete-btn"
          onClick={handleDeleteClick}
        >
          <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
          </svg>
          Удалить аккаунт
        </button>
      </div>
    </div>
  );
}
