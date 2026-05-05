import { createSignal, For, Show, onMount } from 'solid-js';
import axios from 'axios';
import { config } from '../../../../config';
import './TaskAttempts.css';

const PAGE_SIZE = 10;

export default function TaskAttempts(props) {
  const [loading, setLoading] = createSignal(false);
  const [loadingMore, setLoadingMore] = createSignal(false);
  const [attempts, setAttempts] = createSignal([]);
  const [currentPage, setCurrentPage] = createSignal(0);
  const [hasMore, setHasMore] = createSignal(false);
  const [error, setError] = createSignal(null);

  const fetchAttempts = async (page = 0) => {
    if (page === 0) setLoading(true);
    else setLoadingMore(true);
    setError(null);

    try {
      const token = localStorage.getItem('access_token');
      console.log(`Токен: ${token}`)
      const { data } = await axios.get(`${config.backendUrl}/api/v1/progress/submissions`, {
        params: { taskId: props.taskId, page, size: PAGE_SIZE },
        headers: { 'Authorization': `Bearer ${token}` },
      });

      if (page === 0) {
        setAttempts(data.content);
      } else {
        setAttempts((prev) => [...prev, ...data.content]);
      }
      setCurrentPage(data.number);
      setHasMore(!data.last);
    } catch (err) {
      console.error('Failed to load attempts', err);
      setError('Не удалось загрузить попытки');
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  };

  onMount(() => {
    if (props.taskId) {
      void fetchAttempts(0);
    }
  });

  const loadMore = () => fetchAttempts(currentPage() + 1);

  const formatDate = (iso) => {
    try {
      return new Date(iso).toLocaleDateString('ru-RU');
    } catch (e) {
      return iso;
    }
  };

  function AttemptItem(props) {
    const [expanded, setExpanded] = createSignal(false);
    return (
      <div class="attempt-card">
        <div class="attempt-status">
          <Show when={props.item.isCorrect} fallback={<svg class="status-icon status-fail" viewBox="0 0 24 24"><path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none"/></svg>}>
            <svg class="status-icon status-ok" viewBox="0 0 24 24"><path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none"/></svg>
          </Show>
        </div>

        <div class="attempt-body">
          <div class="attempt-meta">
            <span class="attempt-date">{formatDate(props.item.submittedAt)}</span>
            <span class="attempt-result">{props.item.isCorrect ? 'Успешно' : 'Неудачно'}</span>
            <button class="attempt-expand-btn" onClick={() => setExpanded(!expanded())} aria-label="Показать код">
              <span class={"arrow " + (expanded() ? 'open' : '')}></span>
            </button>
          </div>

          <pre class={"attempt-code " + (expanded() ? 'expanded' : 'collapsed')}><code>{props.item.userCode}</code></pre>
        </div>
      </div>
    );
  }

  return (
    <div class="task-attempts">
      <div class="task-attempts-panel" role="region" aria-label="Мои решения">

        <Show when={loading()} fallback={
          <div>
            <Show when={error()}>
              <div class="task-attempts-error">{error()}</div>
            </Show>

            <div class="task-attempts-list">
              <For each={attempts()}>
                {(item) => <AttemptItem item={item} />}
              </For>
            </div>

            <Show when={hasMore()}>
              <div class="task-attempts-more">
                <button
                  class="attempt-load-more-btn"
                  onClick={loadMore}
                  disabled={loadingMore()}
                >
                  {loadingMore() ? 'Загрузка...' : 'Загрузить ещё'}
                </button>
              </div>
            </Show>
          </div>
        }>
          <div class="task-attempts-loading">Загрузка...</div>
        </Show>
      </div>
    </div>
  );
}