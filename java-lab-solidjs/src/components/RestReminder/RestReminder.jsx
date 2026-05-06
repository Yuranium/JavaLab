import { createSignal, onCleanup, Show } from 'solid-js';
import './RestReminder.css';

const TIPS = [
  {
    title: 'Время отдохнуть!',
    text: 'Встаньте и потянитесь — 2–3 минуты восстановят концентрацию.',
  },
  {
    title: 'Правило 20-20-20',
    text: 'Посмотрите на объект в 6 метрах от вас в течение 20 секунд — глаза скажут спасибо.',
  },
  {
    title: 'Выпейте воды',
    text: 'Лёгкое обезвоживание снижает внимательность. Сделайте пару глотков.',
  },
  {
    title: 'Глубокий вдох',
    text: 'Три медленных вдоха и выдоха прямо сейчас снизят уровень стресса.',
  },
  {
    title: 'Разомните руки',
    text: 'Дайте пальцам и запястьям отдохнуть от клавиатуры хотя бы на минуту.',
  },
];

const INTERVAL_MS  = 1_800_000;
const SHOW_CLOSE_MS = 5_000;
const EXIT_DURATION = 320;

export default function RestReminder() {
  const [visible,   setVisible]   = createSignal(false);
  const [exiting,   setExiting]   = createSignal(false);
  const [showClose, setShowClose] = createSignal(false);
  const [tipIndex,  setTipIndex]  = createSignal(0);

  let showCloseTimer = null;

  const doExit = (onDone) => {
    clearTimeout(showCloseTimer);
    setExiting(true);
    setTimeout(() => {
      setVisible(false);
      setExiting(false);
      setShowClose(false);
      onDone?.();
    }, EXIT_DURATION);
  };

  const dismiss = () => {
    if (!visible() || exiting()) return;
    doExit();
  };

  const trigger = () => {
    const nextTip = () => {
      setTipIndex(i => (i + 1) % TIPS.length);
      setShowClose(false);
      setExiting(false);
      setVisible(true);
      showCloseTimer = setTimeout(() => setShowClose(true), SHOW_CLOSE_MS);
    };

    if (visible() && !exiting()) {
      doExit(nextTip);
    } else {
      nextTip();
    }
  };

  const interval = setInterval(trigger, INTERVAL_MS);

  onCleanup(() => {
    clearInterval(interval);
    clearTimeout(showCloseTimer);
  });

  const tip = () => TIPS[tipIndex()];

  return (
    <Show when={visible()}>
      <div
        class={`rest-reminder${exiting() ? ' rest-reminder--out' : ' rest-reminder--in'}`}
        role="alert"
        aria-live="polite"
      >
        {/* accent bar */}
        <div class="rest-reminder-accent" />

        {/* progress bar: counts down until X appears */}
        <Show when={!showClose()}>
          <div class="rest-reminder-progress" />
        </Show>

        <div class="rest-reminder-icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
            <path d="M18.5 3H6c-1.1 0-2 .9-2 2v5.71c0 3.83 2.95 7.18 6.78 7.29 3.96.12 7.22-3.06 7.22-7v-1h.5c1.93 0 3.5-1.57 3.5-3.5S20.43 3 18.5 3zM16 5v3H6V5h10zm2.5 3H18V5h.5c.83 0 1.5.67 1.5 1.5S19.33 8 18.5 8zM4 19h16v2H4z" />
          </svg>
        </div>

        <div class="rest-reminder-body">
          <p class="rest-reminder-title">{tip().title}</p>
          <p class="rest-reminder-text">{tip().text}</p>
        </div>

        <Show when={showClose()}>
          <button
            class="rest-reminder-close"
            onClick={dismiss}
            aria-label="Закрыть уведомление"
            type="button"
          >
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor" aria-hidden="true">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z" />
            </svg>
          </button>
        </Show>
      </div>
    </Show>
  );
}