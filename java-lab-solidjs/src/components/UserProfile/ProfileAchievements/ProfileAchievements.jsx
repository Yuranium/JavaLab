import { createSignal, createEffect, For, Show } from 'solid-js';
import { A } from '@solidjs/router';
import axios from 'axios';
import { useAuth } from '../../../context/AuthContext';
import { config } from '../../../config';
import './ProfileAchievements.css';

const MAX_SHOWN = 5;

function AchievementBadge(props) {
  const a = props.achievement;
  return (
    <div class="profile-ach-badge" data-tooltip={a.name} aria-label={a.name}>
      <Show when={a.iconUrl} fallback={
        <div class="profile-ach-icon-placeholder">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor" aria-hidden="true">
            <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
          </svg>
        </div>
      }>
        <img src={a.iconUrl} alt={a.name} class="profile-ach-icon" />
      </Show>
    </div>
  );
}

export default function ProfileAchievements() {
  const auth = useAuth();
  const [achievements, setAchievements] = createSignal([]);
  const [loading, setLoading] = createSignal(true);
  let fetched = false;

  createEffect(() => {
    const token = auth.accessToken();
    const isLoading = auth.isLoading();
    if (!token || isLoading || fetched) return;
    fetched = true;

    axios
      .get(`${config.backendUrl}/api/v1/progress/achievements/unlocked`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then(({ data }) => {
        setAchievements(Array.isArray(data) ? data : (data.content ?? []));
      })
      .catch((e) => console.error('Failed to load profile achievements', e))
      .finally(() => setLoading(false));
  });

  const displayed = () => achievements().slice(0, MAX_SHOWN);
  const hasMore = () => achievements().length > MAX_SHOWN;

  return (
    <Show when={!loading() && achievements().length > 0}>
      <div class="profile-achievements">
        <span class="profile-achievements-label">Достижения</span>
        <div class="profile-achievements-row">
          <For each={displayed()}>
            {(ach) => <AchievementBadge achievement={ach} />}
          </For>
          <Show when={hasMore()}>
            <A href="/progress" class="profile-achievements-more">
              Подробнее
            </A>
          </Show>
        </div>
      </div>
    </Show>
  );
}