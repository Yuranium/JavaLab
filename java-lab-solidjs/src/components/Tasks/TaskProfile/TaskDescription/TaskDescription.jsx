import { Show, createSignal } from 'solid-js';
import './TaskDescription.css';

const difficultyColors = {
  EASY: { border: 'var(--difficulty-easy)', bg: 'var(--difficulty-easy-light)', text: 'var(--difficulty-easy)' },
  MEDIUM: { border: 'var(--difficulty-medium)', bg: 'var(--difficulty-medium-light)', text: 'var(--difficulty-medium)' },
  HARD: { border: 'var(--difficulty-hard)', bg: 'var(--difficulty-hard-light)', text: 'var(--difficulty-hard)' },
};

function formatDateToLocal(timestamp) {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  return date.toLocaleString('ru-RU', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function formatDescription(text) {
  if (!text) return [];

  const lines = text.split('\n');
  const raw = [];
  let inCodeBlock = false;
  let codeContent = [];
  let codeLang = '';

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];

    if (line.startsWith('```')) {
      if (inCodeBlock) {
        raw.push({ type: 'code', content: codeContent.join('\n'), lang: codeLang });
        codeContent = [];
        codeLang = '';
        inCodeBlock = false;
      } else {
        codeLang = line.slice(3).trim();
        inCodeBlock = true;
      }
      continue;
    }

    if (inCodeBlock) {
      codeContent.push(line);
      continue;
    }

    if (line.startsWith('### ')) {
      raw.push({ type: 'h3', content: line.slice(4) });
    } else if (line.startsWith('## ')) {
      raw.push({ type: 'h2', content: line.slice(3) });
    } else if (line.startsWith('# ')) {
      raw.push({ type: 'h1', content: line.slice(2) });
    } else if (line.startsWith('- ') || line.startsWith('* ')) {
      raw.push({ type: 'li', content: line.slice(2) });
    } else if (line.trim() === '') {
      raw.push({ type: 'br' });
    } else {
      raw.push({ type: 'p', content: line });
    }
  }

  if (inCodeBlock) {
    raw.push({ type: 'code', content: codeContent.join('\n'), lang: codeLang });
  }

  // Group consecutive li items into ul blocks
  const blocks = [];
  let i = 0;
  while (i < raw.length) {
    if (raw[i].type === 'li') {
      const items = [];
      while (i < raw.length && raw[i].type === 'li') {
        items.push(raw[i]);
        i++;
      }
      blocks.push({ type: 'ul', items });
    } else {
      blocks.push(raw[i]);
      i++;
    }
  }

  return blocks;
}

function formatInlineText(text) {
  if (!text) return '';

  const parts = [];
  let remaining = text;
  let key = 0;

  while (remaining.length > 0) {
    const codeMatch = remaining.match(/`(.+?)`/);
    const boldMatch = remaining.match(/\*\*(.+?)\*\*/);
    const italicMatch = remaining.match(/(?:(?:\*(?!\*)(.+?)\*)|(?:_(.+?)_))/);

    let firstMatch = null;
    let matchType = '';

    const candidates = [];
    if (codeMatch) candidates.push({ m: codeMatch, type: 'code', idx: codeMatch.index });
    if (boldMatch) candidates.push({ m: boldMatch, type: 'bold', idx: boldMatch.index });
    if (italicMatch) candidates.push({ m: italicMatch, type: 'italic', idx: italicMatch.index });
    if (candidates.length > 0) {
      candidates.sort((a, b) => a.idx - b.idx);
      firstMatch = candidates[0].m;
      matchType = candidates[0].type;
    }

    if (firstMatch) {
      if (firstMatch.index > 0) {
        parts.push({ type: 'text', content: remaining.slice(0, firstMatch.index), key: key++ });
      }
      let content = firstMatch[1];
      if (matchType === 'italic' && firstMatch[1] === undefined) content = firstMatch[2];
      parts.push({ type: matchType, content: content, key: key++ });
      remaining = remaining.slice(firstMatch.index + firstMatch[0].length);
    } else {
      parts.push({ type: 'text', content: remaining, key: key++ });
      remaining = '';
    }
  }

  return parts;
}

function InlineContent(props) {
  const parts = formatInlineText(props.text);
  return (
    <>
      {parts.map((part) => {
        if (part.type === 'bold') {
          return <strong>{part.content}</strong>;
        } else if (part.type === 'italic') {
          return <em>{part.content}</em>;
        } else if (part.type === 'code') {
          return <code class="task-description-inline-code">{part.content}</code>;
        }
        return <span>{part.content}</span>;
      })}
    </>
  );
}

function CategoryTooltip(props) {
  const [visible, setVisible] = createSignal(false);
  const [position, setPosition] = createSignal({ top: 0, left: 0, arrowLeft: 0 });
  let triggerRef;
  let tooltipRef;
  const TOOLTIP_WIDTH = 240;

  const showTooltip = () => {
    if (!triggerRef) return;
    const rect = triggerRef.getBoundingClientRect();
    const viewportWidth = window.innerWidth;

    let left = rect.left + rect.width / 2 - TOOLTIP_WIDTH / 2;
    let arrowLeft = TOOLTIP_WIDTH / 2;

    if (left < 8) {
      arrowLeft = rect.left + rect.width / 2 - 8;
      left = 8;
    } else if (left + TOOLTIP_WIDTH > viewportWidth - 8) {
      arrowLeft = TOOLTIP_WIDTH - (viewportWidth - 8 - left);
      left = viewportWidth - TOOLTIP_WIDTH - 8;
    }

    setPosition({
      top: rect.bottom + 8,
      left,
      arrowLeft,
    });
    setVisible(true);
  };

  const hideTooltip = () => {
    setVisible(false);
  };

  return (
    <div
      class="task-description-tooltip-container"
      ref={triggerRef}
      onMouseEnter={showTooltip}
      onMouseLeave={hideTooltip}
    >
      <span class="task-description-category">{props.category.title}</span>
      <div
        ref={tooltipRef}
        class="task-description-tooltip"
        classList={{ 'task-description-tooltip--visible': visible() }}
        style={{
          top: `${position().top}px`,
          left: `${position().left}px`,
        }}
      >
        <div
          class="task-description-tooltip-arrow"
          style={{ left: `${position().arrowLeft}px` }}
        ></div>
        <div class="task-description-tooltip-content">
          <strong>{props.category.title}</strong>
          <p>{props.category.description || 'Описание категории'}</p>
        </div>
      </div>
    </div>
  );
}

export default function TaskDescription(props) {
  const task = () => props.task;
  const formattedContent = () => formatDescription(task()?.description || '');

  return (
    <div class="task-description">
      <h2 class="task-description-title">{task()?.title}</h2>

      <div class="task-description-meta">
        <div class="task-description-meta-top">
          <span
            class="task-description-badge task-description-badge--difficulty"
            style={{
              'border-color': difficultyColors[task()?.difficulty]?.border,
              'background-color': difficultyColors[task()?.difficulty]?.bg,
              'color': difficultyColors[task()?.difficulty]?.text,
            }}
          >
            {task()?.difficulty}
          </span>
        </div>

        <div class="task-description-categories">
          <Show when={task()?.categories && task()?.categories.length > 0}>
            {task().categories.map((category) => (
              <CategoryTooltip category={category} />
            ))}
          </Show>
        </div>
      </div>

      <div class="task-description-dates">
        <div class="task-description-date-item">
          <span class="task-description-date-label">Создана:</span>
          <span class="task-description-date-value">{formatDateToLocal(task()?.createdAt)}</span>
        </div>
        <div class="task-description-date-item">
          <span class="task-description-date-label">Обновлена:</span>
          <span class="task-description-date-value">{formatDateToLocal(task()?.updatedAt)}</span>
        </div>
      </div>

      <div class="task-description-content">
        <Show
          when={formattedContent().length > 0}
          fallback={
            <p class="task-description-placeholder">
              Описание задачи отсутствует
            </p>
          }
        >
          {formattedContent().map((block) => {
            if (block.type === 'h1') {
              return <h1><InlineContent text={block.content} /></h1>;
            } else if (block.type === 'h2') {
              return <h2><InlineContent text={block.content} /></h2>;
            } else if (block.type === 'h3') {
              return <h3><InlineContent text={block.content} /></h3>;
            } else if (block.type === 'p') {
              return <p><InlineContent text={block.content} /></p>;
            } else if (block.type === 'ul') {
              return (
                <ul class="task-description-list">
                  {block.items.map((item) => (
                    <li><InlineContent text={item.content} /></li>
                  ))}
                </ul>
              );
            } else if (block.type === 'code') {
              return (
                <pre class="task-description-code">
                  <code>{block.content}</code>
                </pre>
              );
            } else if (block.type === 'br') {
              return <div class="task-description-br" />;
            }
            return null;
          })}
        </Show>
      </div>
    </div>
  );
}
