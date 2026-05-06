import { createSignal, createEffect, createMemo, onCleanup, For, Show } from 'solid-js';
import axios from 'axios';
import { useAuth } from '../../context/AuthContext';
import { config } from '../../config';
import './ActivityModal.css';

// ── helpers ──────────────────────────────────────────────────────────────────

/** Форматирует Date в "YYYY-MM-DD" по локальному часовому поясу. */
function localDateStr(date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

function getActivityLevel(count) {
  if (!count) return 0;
  if (count <= 2) return 1;
  if (count <= 4) return 2;
  if (count <= 6) return 3;
  return 4;
}

const monthNames = ['Янв', 'Фев', 'Мар', 'Апр', 'Май', 'Июн',
                    'Июл', 'Авг', 'Сен', 'Окт', 'Ноя', 'Дек'];
const dayNames   = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];

/** Строит матрицу 7×53 для тепловой карты и список меток месяцев. */
function generateHeatmapData(activityMap, year) {
  const yearNum      = parseInt(year, 10);
  const today        = new Date();
  const isCurrentYear = yearNum === today.getFullYear();

  const firstDay  = new Date(yearNum, 0, 1);
  const dowFirst  = firstDay.getDay() || 7;           // 1=Mon … 7=Sun
  const startDate = new Date(firstDay);
  startDate.setDate(firstDay.getDate() - (dowFirst - 1));
  startDate.setHours(0, 0, 0, 0);

  const endDate    = new Date(yearNum, 11, 31, 23, 59, 59);
  const displayEnd = isCurrentYear
    ? new Date(yearNum, 11, 31, 23, 59, 59)
    : endDate;

  const rows       = [[], [], [], [], [], [], []];
  const monthWeeks = {};
  let lastMonth    = -1;
  let weekIndex    = 0;
  let cur          = new Date(startDate);

  while (weekIndex < 53) {
    const dateStr  = localDateStr(cur);
    const rowIdx   = (cur.getDay() || 7) - 1;
    const month    = cur.getMonth();
    const isInYear = cur.getFullYear() === yearNum;
    const isPast   = cur <= today;

    const entry        = (isInYear && isPast) ? (activityMap[dateStr] ?? null) : null;
    const attempts     = entry?.attemptsCount ?? 0;
    const tasksSolved  = entry?.tasksSolved   ?? 0;
    rows[rowIdx][weekIndex] = {
      date : isInYear ? dateStr : '',
      count: attempts,      // used for tooltip
      tasksSolved,
      level: getActivityLevel(attempts),
    };

    if (month !== lastMonth && isInYear && cur >= startDate && cur <= displayEnd) {
      if (!(month in monthWeeks)) monthWeeks[month] = weekIndex;
      lastMonth = month;
    }

    cur.setDate(cur.getDate() + 1);
    if (cur.getDay() === 1) weekIndex++;
    if (isInYear && cur > displayEnd) break;
  }

  const weeksCount   = weekIndex;
  const monthLabels  = Object.entries(monthWeeks)
    .map(([m, w]) => ({ month: +m, weekIndex: w, name: monthNames[+m] }))
    .sort((a, b) => a.month - b.month);

  return { rows, monthLabels, weeksCount };
}

// ── constants ─────────────────────────────────────────────────────────────────

const THIS_YEAR      = new Date().getFullYear();
const START_YEAR     = 2024;
const AVAILABLE_YEARS = Array.from(
  { length: THIS_YEAR - START_YEAR + 1 },
  (_, i) => (THIS_YEAR - i).toString()
); // ['2026','2025','2024'] in 2026

// ── component ─────────────────────────────────────────────────────────────────

export default function ActivityModal(props) {
  const auth = useAuth();

  const [isVisible,    setIsVisible]    = createSignal(false);
  const [selectedYear, setSelectedYear] = createSignal(THIS_YEAR.toString());
  const [activityMap,  setActivityMap]  = createSignal({});
  const [loading,      setLoading]      = createSignal(false);
  const [fetchError,   setFetchError]   = createSignal(null);

  // ── data fetching ────────────────────────────────────────────────────────────

  const fetchActivity = (year) => {
    const token = auth.accessToken();
    if (!token) return;

    setLoading(true);
    setFetchError(null);

    axios.get(`${config.backendUrl}/api/v1/progress/activity`, {
      headers: { Authorization: `Bearer ${token}` },
      params : {
        from: `${year}-01-01`,
        to  : `${year}-12-31`,
        size: 400,
        sort: 'activityDate,asc',
      },
    })
      .then(({ data }) => {
        const content = data.content ?? [];
        const map = {};
        content.forEach(({ date, tasksSolved, attemptsCount }) => {
          map[String(date)] = {
            tasksSolved  : tasksSolved   ?? 0,
            attemptsCount: attemptsCount ?? 0,
          };
        });
        setActivityMap(map);
      })
      .catch((e) => {
        console.error('Failed to fetch activity', e);
        setFetchError('Не удалось загрузить данные активности');
      })
      .finally(() => setLoading(false));
  };

  // ── effects ──────────────────────────────────────────────────────────────────

  // Visibility + body scroll lock
  createEffect(() => {
    if (props.isOpen) {
      setIsVisible(true);
      document.body.style.overflow = 'hidden';
    } else {
      setIsVisible(false);
      document.body.style.overflow = '';
    }
  });

  // Fetch when modal opens or selected year changes
  createEffect(() => {
    const year = selectedYear();
    if (props.isOpen && year) fetchActivity(year);
  });

  onCleanup(() => { document.body.style.overflow = ''; });

  // ── event handlers ───────────────────────────────────────────────────────────

  const handleClose         = () => props.onClose();
  const handleBackdropClick = (e) => { if (e.target === e.currentTarget) handleClose(); };
  const handleKeyDown       = (e) => { if (e.key === 'Escape') handleClose(); };

  // ── derived data ──────────────────────────────────────────────────────────────

  const heatmapData = createMemo(() =>
    generateHeatmapData(activityMap(), selectedYear())
  );

  /** Сумма решённых задач за выбранный год. */
  const yearTotal = createMemo(() =>
    Object.values(activityMap()).reduce((s, e) => s + (e?.tasksSolved ?? 0), 0)
  );

  /** Решено сегодня. */
  const todayCount = createMemo(() =>
    activityMap()[localDateStr(new Date())]?.tasksSolved ?? 0
  );

  /** Решено на текущей неделе (пн–вс). */
  const weekCount = createMemo(() => {
    const map = activityMap();
    const now = new Date();
    const dow = now.getDay() || 7;
    const monday = new Date(now);
    monday.setDate(now.getDate() - (dow - 1));
    monday.setHours(0, 0, 0, 0);
    let total = 0;
    const cur = new Date(monday);
    for (let i = 0; i < 7; i++) {
      total += map[localDateStr(cur)]?.tasksSolved ?? 0;
      cur.setDate(cur.getDate() + 1);
    }
    return total;
  });

  // ── render ────────────────────────────────────────────────────────────────────

  return (
    <div
      class={`activity-modal-backdrop${isVisible() ? ' visible' : ''}`}
      onClick={handleBackdropClick}
      onKeyDown={handleKeyDown}
      tabindex="-1"
    >
      <div class={`activity-modal${isVisible() ? ' visible' : ''}`}>

        {/* Close button */}
        <button class="activity-modal-close" onClick={handleClose} aria-label="Закрыть">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
            <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
          </svg>
        </button>

        {/* Header */}
        <div class="activity-modal-header">
          <h2 class="activity-modal-title">Активность</h2>
          <Show when={AVAILABLE_YEARS.length > 1}>
            <select
              class="activity-year-select"
              value={selectedYear()}
              onChange={(e) => setSelectedYear(e.target.value)}
            >
              <For each={AVAILABLE_YEARS}>
                {(year) => <option value={year}>{year}</option>}
              </For>
            </select>
          </Show>
        </div>

        {/* Stats */}
        <div class="activity-stats">
          <div class="activity-stat-item">
            <span class="activity-stat-value">
              <Show when={!loading()} fallback={<span class="activity-stat-skeleton" />}>
                {yearTotal()}
              </Show>
            </span>
            <span class="activity-stat-label">Решено задач в {selectedYear()} году</span>
          </div>
          <div class="activity-stat-item">
            <span class="activity-stat-value">
              <Show when={!loading()} fallback={<span class="activity-stat-skeleton" />}>
                {todayCount()}
              </Show>
            </span>
            <span class="activity-stat-label">Решено сегодня</span>
          </div>
          <div class="activity-stat-item">
            <span class="activity-stat-value">
              <Show when={!loading()} fallback={<span class="activity-stat-skeleton" />}>
                {weekCount()}
              </Show>
            </span>
            <span class="activity-stat-label">Решено на неделе</span>
          </div>
        </div>

        {/* Error */}
        <Show when={fetchError()}>
          <div class="activity-error">{fetchError()}</div>
        </Show>

        {/* Heatmap */}
        <div class="activity-heatmap-container">
          <Show when={!loading()} fallback={
            <div class="activity-heatmap-skeleton">
              <For each={Array(7).fill(null)}>
                {() => <div class="activity-heatmap-skeleton-row" />}
              </For>
            </div>
          }>
            {/* Month labels */}
            <div class="activity-months-header">
              <For each={heatmapData().monthLabels}>
                {(m, idx) => {
                  const next = heatmapData().monthLabels[idx() + 1];
                  const w = next
                    ? next.weekIndex - m.weekIndex
                    : heatmapData().weeksCount - m.weekIndex;
                  return (
                    <div class="activity-month-label" style={{ width: `${w * 15}px` }}>
                      {m.name}
                    </div>
                  );
                }}
              </For>
            </div>

            {/* Grid */}
            <div class="activity-heatmap-wrapper">
              <div class="activity-heatmap">
                <For each={heatmapData().rows}>
                  {(row, dayIndex) => (
                    <div class="activity-heatmap-row">
                      <div class="activity-day-label">{dayNames[dayIndex()]}</div>
                      <div class="activity-cells">
                        <For each={row}>
                          {(day) => (
                            <div
                              class={`activity-cell activity-level-${day.level}`}
                              title={day.date ? `${day.date}: попыток ${day.count}, решено ${day.tasksSolved}` : ''}
                            />
                          )}
                        </For>
                      </div>
                    </div>
                  )}
                </For>
              </div>
            </div>
          </Show>

          {/* Legend (always visible) */}
          <div class="activity-legend">
            <span>Меньше</span>
            <div class="activity-legend-cells">
              <div class="activity-cell activity-level-0" />
              <div class="activity-cell activity-level-1" />
              <div class="activity-cell activity-level-2" />
              <div class="activity-cell activity-level-3" />
              <div class="activity-cell activity-level-4" />
            </div>
            <span>Больше</span>
          </div>
        </div>

      </div>
    </div>
  );
}