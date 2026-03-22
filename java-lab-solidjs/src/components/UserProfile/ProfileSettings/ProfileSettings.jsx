import { Show } from 'solid-js';
import './ProfileSettings.css';

export default function ProfileSettings(props) {
  const isReadOnly = props.isReadOnly || false;

  return (
    <div class="profile-settings">
      <h3 class="profile-settings-title">Настройки</h3>

      <div class="profile-settings-item">
        <div class="profile-settings-content">
          <div class="profile-settings-info">
            <span class="profile-settings-label">Уведомления</span>
            <span class="profile-settings-description">
              Получать уведомления о новых задачах и достижениях
            </span>
          </div>
          {!isReadOnly ? (
            <>
              <label class="profile-settings-toggle">
                <input
                  type="checkbox"
                  checked={props.notificationsEnabled}
                  onChange={(e) => props.onNotificationsToggle(e.target.checked)}
                  class="profile-settings-checkbox"
                  disabled={props.isUpdatingNotifications}
                />
                <span class="profile-settings-slider"></span>
              </label>
              <Show when={props.isUpdatingNotifications}>
                <span class="profile-settings-loading">Обновление...</span>
              </Show>
            </>
          ) : (
            <span class="profile-settings-value">
              {props.notificationsEnabled ? 'Включены' : 'Отключены'}
            </span>
          )}
        </div>
      </div>
    </div>
  );
}
