import { createSignal, createEffect, onCleanup, For, Show } from 'solid-js';
import { useNavigate } from '@solidjs/router';
import axios from 'axios';
import { Chart } from 'chart.js/auto';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { config } from '../config';
import './ProgressPage.css';

function fillActivityRange(rawData) {
  const today = new Date();
  const start = new Date(today);
  start.setDate(today.getDate() - 29);

  const map = {};
  rawData.forEach(d => { map[d.date] = d; });

  const result = [];
  const cur = new Date(start);
  while (cur <= today) {
    const dateStr = cur.toISOString().split('T')[0];
    result.push(map[dateStr] ?? { date: dateStr, tasksSolved: 0, attemptsCount: 0, loginCount: 0 });
    cur.setDate(cur.getDate() + 1);
  }
  return result;
}

function formatShortDate(dateStr) {
  if (!dateStr) return '';
  const [, m, d] = dateStr.split('-');
  return `${d}.${m}`;
}

function formatFullDate(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleDateString('ru-RU');
}

function ActivityLineChart(props) {
  const { theme } = useTheme();
  let canvasRef;
  let chart = null;

  const cssVar = (name) =>
    getComputedStyle(document.documentElement).getPropertyValue(name).trim();

  const build = () => {
    if (chart) { chart.destroy(); chart = null; }
    if (!canvasRef) return;

    const primary    = cssVar('--primary-color')    || '#6366f1';
    const textSec    = cssVar('--text-secondary')   || '#6e6e73';
    const textMain   = cssVar('--text-primary')     || '#1d1d1f';
    const borderCol  = cssVar('--border-color')     || '#d2d2d7';
    const surfaceCol = cssVar('--surface-color')    || '#ffffff';

    const data = props.data ?? [];
    const labels = data.map(d => formatShortDate(d.date));
    const values = data.map(d => d.tasksSolved ?? 0);

    chart = new Chart(canvasRef, {
      type: 'line',
      data: {
        labels,
        datasets: [{
          label: 'Задач решено',
          data: values,
          borderColor: primary,
          backgroundColor: primary + '28',
          fill: true,
          tension: 0.4,
          pointRadius: 4,
          pointHoverRadius: 7,
          pointBackgroundColor: primary,
          pointBorderColor: surfaceCol,
          pointBorderWidth: 2,
          borderWidth: 2.5,
        }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
        scales: {
          x: {
            grid: { color: borderCol + '55' },
            ticks: {
              color: textSec,
              maxTicksLimit: 10,
              font: { size: 11 },
            },
            border: { color: borderCol },
          },
          y: {
            beginAtZero: true,
            grid: { color: borderCol + '55' },
            ticks: {
              color: textSec,
              precision: 0,
              stepSize: 1,
              font: { size: 11 },
            },
            border: { color: borderCol },
          },
        },
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: surfaceCol,
            titleColor: textMain,
            bodyColor: textMain,
            borderColor: borderCol,
            borderWidth: 1,
            padding: 10,
            cornerRadius: 8,
            callbacks: {
              label: (ctx) => `  Задач решено: ${ctx.raw}`,
            },
          },
        },
      },
    });
  };

  createEffect(() => {
    theme(); // пересобираем при смене темы
    build();
  });

  onCleanup(() => chart?.destroy());

  return (
    <div class="chart-canvas-wrap">
      <canvas ref={canvasRef} />
    </div>
  );
}


function StatCard(props) {
  return (
    <div class="progress-stat-card">
      <Show when={!props.loading} fallback={<div class="stat-skeleton" />}>
        <div class="stat-icon-wrap">{props.icon}</div>
        <div class="progress-stat-value">{props.value}</div>
        <div class="progress-stat-label">{props.label}</div>
      </Show>
    </div>
  );
}

function AchievementCard(props) {
  const a = props.achievement;
  return (
    <div class={`achievement-card${a.unlocked ? ' achievement-unlocked' : ' achievement-locked'}`}>
      <div class="achievement-icon-wrap">
        <Show when={a.iconUrl} fallback={
          <div class="achievement-icon-placeholder">
            <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor">
              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
            </svg>
          </div>
        }>
          <img src={a.iconUrl} alt={a.name} class="achievement-icon" />
        </Show>
        <Show when={a.unlocked}>
          <span class="achievement-check" aria-label="Получено">✓</span>
        </Show>
        <Show when={!a.unlocked}>
          <span class="achievement-lock" aria-label="Заблокировано">
            <svg viewBox="0 0 24 24" width="12" height="12" fill="currentColor">
              <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"/>
            </svg>
          </span>
        </Show>
      </div>
      <div class="achievement-info">
        <span class="achievement-name">{a.name}</span>
        <span class="achievement-desc">{a.description}</span>
        <Show when={a.unlocked && a.unlockedAt}>
          <span class="achievement-date">Получено {formatFullDate(a.unlockedAt)}</span>
        </Show>
      </div>
    </div>
  );
}

const icons = {
  check: (
    <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
      <polyline points="20 6 9 17 4 12" />
    </svg>
  ),
  send: (
    <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/>
    </svg>
  ),
  fire: (
    <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
      <path d="M13.5 0.67s.74 2.65.74 4.8c0 2.06-1.35 3.73-3.41 3.73-2.07 0-3.63-1.67-3.63-3.73l.03-.36C5.21 7.51 4 10.62 4 14c0 4.42 3.58 8 8 8s8-3.58 8-8C20 8.61 17.41 3.8 13.5.67zM11.71 19c-1.78 0-3.22-1.4-3.22-3.14 0-1.62 1.05-2.76 2.81-3.12 1.77-.36 3.6-1.21 4.62-2.58.39 1.29.59 2.65.59 4.04 0 2.65-2.15 4.8-4.8 4.8z"/>
    </svg>
  ),
  trophy: (
    <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
      <path d="M19 5h-2V3H7v2H5c-1.1 0-2 .9-2 2v1c0 2.55 1.92 4.63 4.39 4.94A5.01 5.01 0 0011 15.9V18H9v2h6v-2h-2v-2.1a5.01 5.01 0 003.61-2.96C19.08 12.63 21 10.55 21 8V7c0-1.1-.9-2-2-2zM5 8V7h2v3.82C5.84 10.4 5 9.3 5 8zm14 0c0 1.3-.84 2.4-2 2.82V7h2v1z"/>
    </svg>
  ),
};

export default function ProgressPage() {
  const auth = useAuth();
  const navigate = useNavigate();

  const [progress, setProgress] = createSignal(null);
  const [activity, setActivity] = createSignal([]);
  const [achievements, setAchievements] = createSignal([]);
  const [showingUnlocked, setShowingUnlocked] = createSignal(false);

  const [loadingProgress, setLoadingProgress] = createSignal(true);
  const [loadingActivity, setLoadingActivity] = createSignal(true);
  const [loadingAchievements, setLoadingAchievements] = createSignal(true);
  const [activityEmpty, setActivityEmpty] = createSignal(false);
  const [error, setError] = createSignal(null);

  let loaded = false;

  const authHeader = () => ({
    Authorization: `Bearer ${localStorage.getItem('access_token')}`,
  });

  const loadAll = async () => {
    const base = config.backendUrl;
    const h = authHeader();

    setLoadingProgress(true);
    setLoadingActivity(true);
    setLoadingAchievements(true);

    const [progRes, actRes, achRes] = await Promise.allSettled([
      axios.get(`${base}/api/v1/progress`, { headers: h }),
      axios.get(`${base}/api/v1/progress/activity`, {
        headers: h,
        params: { size: 31, sort: 'activityDate,asc' },
      }),
      axios.get(`${base}/api/v1/progress/achievements`, {
        headers: h,
        params: { size: 50 },
      }),
    ]);

    if (progRes.status === 'fulfilled') {
      setProgress(progRes.value.data);
    } else {
      setError('Не удалось загрузить прогресс');
    }
    setLoadingProgress(false);

    if (actRes.status === 'fulfilled') {
      const raw = actRes.value.data.content ?? [];
      setActivityEmpty(raw.length === 0);
      setActivity(fillActivityRange(raw));
    }
    setLoadingActivity(false);

    if (achRes.status === 'fulfilled') {
      setAchievements(achRes.value.data.content ?? []);
    }
    setLoadingAchievements(false);
  };

  const toggleAchievements = async () => {
    const next = !showingUnlocked();
    setShowingUnlocked(next);
    setLoadingAchievements(true);

    try {
      const base = config.backendUrl;
      const h = authHeader();

      if (next) {
        const { data } = await axios.get(`${base}/api/v1/progress/achievements/unlocked`, { headers: h });
        setAchievements(Array.isArray(data) ? data : (data.content ?? []));
      } else {
        const { data } = await axios.get(`${base}/api/v1/progress/achievements`, {
          headers: h,
          params: { size: 50 },
        });
        setAchievements(data.content ?? []);
      }
    } catch (e) {
      console.error('Failed to load achievements', e);
    } finally {
      setLoadingAchievements(false);
    }
  };

  createEffect(() => {
    const t = auth.accessToken();
    const loading = auth.isLoading();
    if (!loading && !loaded) {
      if (t) {
        loaded = true;
        void loadAll();
      } else {
        navigate('/login');
      }
    }
  });


  return (
    <div class="progress-page">
      <div class="progress-container">
        <h1 class="progress-title">Мой прогресс</h1>

        <Show when={error()}>
          <div class="progress-error">{error()}</div>
        </Show>

        {/* ── Stats cards ── */}
        <div class="progress-stats">
          <StatCard label="Задач решено"   value={progress()?.totalTasksSolved ?? '—'} loading={loadingProgress()} icon={icons.check}  />
          <StatCard label="Всего попыток"  value={progress()?.totalAttempts    ?? '—'} loading={loadingProgress()} icon={icons.send}   />
          <StatCard label="Текущий стрик"  value={progress() ? `${progress().currentStreak} д.`  : '—'} loading={loadingProgress()} icon={icons.fire}  />
          <StatCard label="Лучший стрик"   value={progress() ? `${progress().longestStreak} д.`  : '—'} loading={loadingProgress()} icon={icons.trophy} />
        </div>

        {/* ── Activity chart ── */}
        <div class="progress-chart">
          <h2 class="progress-chart-title">Активность за 30 дней</h2>
          <Show when={!loadingActivity()} fallback={<div class="chart-placeholder-text">Загрузка...</div>}>
            <Show
              when={!activityEmpty()}
              fallback={<div class="chart-placeholder-text">Нет данных об активности за последние 30 дней</div>}
            >
              <ActivityLineChart data={activity()} />
            </Show>
          </Show>
        </div>

        {/* ── Achievements ── */}
        <div class="achievements-section">
          <div class="achievements-header">
            <h2 class="achievements-title">Достижения</h2>
            <button
              class={`achievements-toggle${showingUnlocked() ? ' active' : ''}`}
              onClick={toggleAchievements}
              disabled={loadingAchievements()}
            >
              {showingUnlocked() ? 'Все достижения' : 'Только полученные'}
            </button>
          </div>

          <Show when={!loadingAchievements()} fallback={
            <div class="achievements-placeholder">Загрузка...</div>
          }>
            <Show when={achievements().length > 0} fallback={
              <div class="achievements-placeholder">
                {showingUnlocked() ? 'Пока нет полученных достижений' : 'Достижения не найдены'}
              </div>
            }>
              <div class="achievement-grid">
                <For each={achievements()}>
                  {(ach) => <AchievementCard achievement={ach} />}
                </For>
              </div>
            </Show>
          </Show>
        </div>

      </div>
    </div>
  );
}