import { createSignal, Show } from 'solid-js';
import './ProfileAvatar.css';

export default function ProfileAvatar(props) {
  const [isHovered, setIsHovered] = createSignal(false);
  let fileInputRef;

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file && props.onUpload) {
      props.onUpload(file);
    }
    e.target.value = '';
  };

  const triggerFileInput = () => {
    if (fileInputRef) {
      fileInputRef.click();
    }
  };

  return (
    <div class="profile-avatar-container">
      <div
        class="profile-avatar-wrapper"
        onMouseEnter={() => setIsHovered(true)}
        onMouseLeave={() => setIsHovered(false)}
      >
        {props.avatar ? (
          <img src={props.avatar} alt="Аватар" class="profile-avatar" />
        ) : (
          <div class="profile-avatar-placeholder">
            <svg viewBox="0 0 24 24" width="48" height="48" fill="currentColor">
              <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
            </svg>
          </div>
        )}

        <button
          class={`profile-avatar-upload-btn ${isHovered() ? 'visible' : ''}`}
          onClick={triggerFileInput}
          aria-label="Изменить аватар"
          disabled={props.isUpdating}
        >
          <Show when={!props.isUpdating} fallback={
            <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor" class="profile-avatar-spinning">
              <path d="M12 4V1L8 5l4 4V6c3.31 0 6 2.69 6 6 0 1.01-.25 1.97-.7 2.8l1.46 1.46A7.93 7.93 0 0 0 20 12c0-4.42-3.58-8-8-8zm0 14c-3.31 0-6-2.69-6-6 0-1.01.25-1.97.7-2.8L5.24 7.74A7.93 7.93 0 0 0 4 12c0 4.42 3.58 8 8 8v3l4-4-4-4v3z"/>
            </svg>
          }>
            <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
              <path d="M9 16h6v-6h4l-7-7-7 7h4zm-4 2h14v2H5z"/>
            </svg>
          </Show>
        </button>
      </div>

      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        style="display: none;"
        onChange={handleFileChange}
        disabled={props.isUpdating}
      />

      <p class="profile-avatar-hint">Нажмите для загрузки</p>

      <Show when={props.avatarError}>
        <p class="profile-avatar-error">{props.avatarError}</p>
      </Show>

      <button
        class="profile-activity-btn"
        onClick={props.onActivityClick}
        aria-label="Показать активность"
        title="Прогресс"
      >
        <div class="activity-bars">
          <div class="activity-bar activity-bar-1"></div>
          <div class="activity-bar activity-bar-2"></div>
          <div class="activity-bar activity-bar-3"></div>
        </div>
      </button>
    </div>
  );
}