import { createSignal, createEffect, onCleanup, createMemo } from 'solid-js';
import './ActivityModal.css';

// Заглушка - данные будут приходить с сервера в формате { "YYYY-MM-DD": count }
const mockActivityData = {
  "2025-12-20": 3,
  "2025-12-22": 5,
  "2025-12-24": 2,
  "2025-12-26": 7,
  "2025-12-28": 4,
  "2025-12-30": 1,
  "2026-01-01": 6,
  "2026-01-03": 3,
  "2026-01-05": 8,
  "2026-01-07": 2,
  "2026-01-09": 5,
  "2026-01-11": 4,
  "2026-01-13": 9,
  "2026-01-15": 3,
  "2026-01-17": 6,
  "2026-01-19": 2,
  "2026-01-21": 7,
  "2026-01-23": 4,
  "2026-01-25": 5,
  "2026-01-27": 3,
  "2026-01-29": 8,
  "2026-01-31": 1,
  "2026-02-02": 6,
  "2026-02-04": 4,
  "2026-02-06": 7,
  "2026-02-08": 2,
  "2026-02-10": 5,
  "2026-02-12": 3,
  "2026-02-14": 9,
  "2026-02-16": 4,
  "2026-02-18": 6,
  "2026-02-20": 2,
  "2026-02-22": 8,
  "2026-02-24": 3,
  "2026-02-26": 5,
  "2026-02-28": 7,
  "2026-03-02": 1,
  "2026-03-04": 4,
  "2026-03-06": 6,
  "2026-03-08": 3,
  "2026-03-10": 8,
  "2026-03-12": 2,
  "2026-03-14": 5,
  "2026-03-16": 7,
  "2026-03-17": 4,
};

function getActivityLevel(count) {
  if (count === 0 || count === undefined) return 0;
  if (count <= 2) return 1;
  if (count <= 4) return 2;
  if (count <= 6) return 3;
  return 4;
}

const monthNames = ['Янв', 'Фев', 'Мар', 'Апр', 'Май', 'Июн', 'Июл', 'Авг', 'Сен', 'Окт', 'Ноя', 'Дек'];
const dayNames = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];

function generateHeatmapData(activityData, year) {
  const yearNum = parseInt(year);
  const today = new Date();
  const isCurrentYear = yearNum === today.getFullYear();

  // Находим первый понедельник года
  const firstDay = new Date(yearNum, 0, 1);
  const dayOfWeek = firstDay.getDay() || 7;
  const startDate = new Date(firstDay);
  startDate.setDate(firstDay.getDate() - (dayOfWeek - 1));
  startDate.setHours(0, 0, 0, 0);

  // Конец: для текущего года - конец года, для прошлых - конец года
  const endDate = new Date(yearNum, 11, 31, 23, 59, 59);
  const displayEnd = isCurrentYear ? new Date(today.getFullYear(), 11, 31, 23, 59, 59) : endDate;

  const rows = [[], [], [], [], [], [], []];
  const monthWeeks = {};
  let lastMonth = -1;
  let currentDate = new Date(startDate);
  let weekIndex = 0;

  while (weekIndex < 53) {
    const dateStr = currentDate.toISOString().split('T')[0];
    const rowIdx = (currentDate.getDay() || 7) - 1;
    const month = currentDate.getMonth();
    const isInYear = currentDate.getFullYear() === yearNum;
    const isPast = currentDate <= today;
    const isInRange = currentDate <= endDate;

    // Для текущего года показываем все дни до конца года, но активность только за прошедшие
    const count = (isInYear && isInRange && isPast) ? (activityData[dateStr] || 0) : 0;
    rows[rowIdx][weekIndex] = {
      date: isInYear ? dateStr : '',
      count,
      level: getActivityLevel(count)
    };

    // Запоминаем неделю, когда начинается новый месяц
    if (month !== lastMonth && isInYear && currentDate >= startDate && currentDate <= displayEnd) {
      if (!(month in monthWeeks)) {
        monthWeeks[month] = weekIndex;
      }
      lastMonth = month;
    }

    currentDate.setDate(currentDate.getDate() + 1);
    if (currentDate.getDay() === 1) weekIndex++;
    
    // Останавливаемся в конце года
    if (isInYear && currentDate > displayEnd) break;
  }

  const weeksCount = weekIndex;
  
  // Формируем список месяцев с их позициями
  const monthLabels = Object.entries(monthWeeks)
    .map(([month, week]) => ({
      month: parseInt(month),
      weekIndex: week,
      name: monthNames[parseInt(month)]
    }))
    .sort((a, b) => a.month - b.month);

  return { rows, monthLabels, weeksCount };
}

export default function ActivityModal(props) {
  const [isVisible, setIsVisible] = createSignal(false);
  const [selectedYear, setSelectedYear] = createSignal(null);
  const activityData = mockActivityData;
  
  // Доступные годы (вычисляем один раз)
  const availableYears = [...new Set(Object.keys(activityData).map(d => d.split('-')[0]))].sort().reverse();

  createEffect(() => {
    if (props.isOpen) {
      setIsVisible(true);
      const currentYear = new Date().getFullYear().toString();
      setSelectedYear(availableYears.includes(currentYear) ? currentYear : availableYears[0]);
      document.body.style.overflow = 'hidden';
    } else {
      setIsVisible(false);
      document.body.style.overflow = '';
    }
  });

  onCleanup(() => { document.body.style.overflow = ''; });

  const handleClose = () => props.onClose();
  const handleBackdropClick = (e) => { if (e.target === e.currentTarget) handleClose(); };
  const handleKeyDown = (e) => { if (e.key === 'Escape') handleClose(); };
  const handleYearChange = (e) => setSelectedYear(e.target.value);

  // Данные heatmap и статистика
  const heatmapData = createMemo(() => {
    const year = selectedYear();
    return year ? generateHeatmapData(activityData, year) : { rows: [], monthLabels: [], weeksCount: 0 };
  });

  const yearData = createMemo(() => {
    const year = selectedYear();
    return year ? Object.entries(activityData).filter(([d]) => d.startsWith(year)).reduce((s, [_, c]) => s + c, 0) : 0;
  });

  return (
    <div
      class={`activity-modal-backdrop ${isVisible() ? 'visible' : ''}`}
      onClick={handleBackdropClick}
      onKeyDown={handleKeyDown}
      tabindex="-1"
    >
      <div class={`activity-modal ${isVisible() ? 'visible' : ''}`}>
        <button class="activity-modal-close" onClick={handleClose} aria-label="Закрыть">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
            <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
          </svg>
        </button>

        <div class="activity-modal-header">
          <h2 class="activity-modal-title">Активность</h2>
          {availableYears.length > 1 && (
            <select
              class="activity-year-select"
              value={selectedYear()}
              onChange={handleYearChange}
            >
              {availableYears.map(year => (
                <option value={year}>{year}</option>
              ))}
            </select>
          )}
        </div>

        <div class="activity-stats">
          <div class="activity-stat-item">
            <span class="activity-stat-value">{yearData()}</span>
            <span class="activity-stat-label">Решено задач в {selectedYear()} году</span>
          </div>
          <div class="activity-stat-item">
            <span class="activity-stat-value">—</span>
            <span class="activity-stat-label">Решено сегодня</span>
          </div>
          <div class="activity-stat-item">
            <span class="activity-stat-value">—</span>
            <span class="activity-stat-label">Решено на неделе</span>
          </div>
        </div>

        <div class="activity-heatmap-container">
          <div class="activity-months-header" style={{ width: `${heatmapData().weeksCount * 13}px` }}>
            {heatmapData().monthLabels.map((m, idx) => {
              const next = heatmapData().monthLabels[idx + 1];
              const w = next ? next.weekIndex - m.weekIndex : heatmapData().weeksCount - m.weekIndex;
              return (
                <div class="activity-month-label" style={{ width: `${w * 13}px` }}>
                  {m.name}
                </div>
              );
            })}
          </div>
          
          <div class="activity-heatmap-wrapper">
            <div class="activity-heatmap">
              {heatmapData().rows.map((row, dayIndex) => (
                <div class="activity-heatmap-row">
                  <div class="activity-day-label">{dayNames[dayIndex]}</div>
                  <div class="activity-cells">
                    {row.map((day) => (
                      <div
                        class={`activity-cell activity-level-${day.level}`}
                        title={day.date ? `${day.date}: ${day.count} задач` : ''}
                      />
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div class="activity-legend">
            <span>Меньше</span>
            <div class="activity-legend-cells">
              <div class="activity-cell activity-level-0"></div>
              <div class="activity-cell activity-level-1"></div>
              <div class="activity-cell activity-level-2"></div>
              <div class="activity-cell activity-level-3"></div>
              <div class="activity-cell activity-level-4"></div>
            </div>
            <span>Больше</span>
          </div>
        </div>
      </div>
    </div>
  );
}
