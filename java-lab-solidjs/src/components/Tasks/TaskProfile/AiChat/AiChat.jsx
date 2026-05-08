import { createSignal, For, Show, batch, onCleanup, onMount } from 'solid-js';
import { marked } from 'marked';
import { config } from '../../../../config';
import './AiChat.css';

marked.use({ breaks: true, gfm: true });

function getUserIdFromToken(token) {
  try {
    return JSON.parse(atob(token.split('.')[1])).sub;
  } catch {
    return null;
  }
}

function renderMarkdown(text) {
  if (!text) return '';
  try {
    return marked.parse(text);
  } catch {
    return text;
  }
}

function BotIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
      <rect x="3" y="11" width="18" height="11" rx="2" />
      <path d="M12 2v4M8 11V9a4 4 0 0 1 8 0v2" />
      <circle cx="9" cy="16" r="1" fill="currentColor" stroke="none" />
      <circle cx="15" cy="16" r="1" fill="currentColor" stroke="none" />
    </svg>
  );
}

function SendIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M22 2L11 13" />
      <path d="M22 2L15 22l-4-9-9-4 20-7z" />
    </svg>
  );
}

function StopIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="currentColor">
      <rect x="6" y="6" width="12" height="12" rx="2" />
    </svg>
  );
}

function TrashIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <polyline points="3 6 5 6 21 6" />
      <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
      <path d="M10 11v6M14 11v6" />
      <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" />
    </svg>
  );
}

export default function AiChat() {
  // Completed messages — stable array for <For>, never mutated during streaming
  const [messages, setMessages] = createSignal([]);
  // Streaming text lives in its own signal: only the streaming bubble subscribes to it
  const [streamingText, setStreamingText] = createSignal('');
  const [streamingError, setStreamingError] = createSignal(false);

  const [input, setInput] = createSignal('');
  const [isStreaming, setIsStreaming] = createSignal(false);
  const [isLoadingHistory, setIsLoadingHistory] = createSignal(false);
  const [error, setError] = createSignal('');

  let abortControllerRef;
  let textareaRef;

  onMount(async () => {
    await loadHistory();
  });

  onCleanup(() => {
    abortControllerRef?.abort();
  });

  const loadHistory = async () => {
    const token = localStorage.getItem('access_token');
    if (!token) return;

    const userId = getUserIdFromToken(token);
    if (!userId) return;

    setIsLoadingHistory(true);
    setError('');
    try {
      const response = await fetch(`${config.backendUrl}/api/v1/ai/history/${userId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (response.ok) {
        const history = await response.json();
        const mapped = history
          .filter(msg => msg.messageType !== 'SYSTEM')
          .map(msg => ({
            role: msg.messageType === 'USER' ? 'user' : 'ai',
            content: msg.text ?? msg.content ?? '',
          }));
        setMessages(mapped);
      }
    } catch {
      // History not critical — silently skip
    } finally {
      setIsLoadingHistory(false);
    }
  };

  const sendMessage = async () => {
    const text = input().trim();
    if (!text || isStreaming()) return;

    // Batch initial state changes so both user bubble and streaming state appear atomically
    batch(() => {
      setError('');
      setStreamingError(false);
      setStreamingText('');
      setIsStreaming(true);
      setMessages(prev => [...prev, { role: 'user', content: text }]);
    });

    setInput('');
    if (textareaRef) textareaRef.style.height = 'auto';

    abortControllerRef = new AbortController();

    try {
      const token = localStorage.getItem('access_token');
      const response = await fetch(`${config.backendUrl}/api/v1/ai/chat/stream`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
          Accept: 'text/event-stream',
        },
        body: JSON.stringify({ message: text }),
        signal: abortControllerRef.signal,
      });

      if (!response.ok) throw new Error(`HTTP ${response.status}: ${response.statusText}`);

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });

        // Split on SSE event boundaries (\n\n).
        // Within each event, multiple data: lines are joined with \n per spec —
        // this is how Spring encodes newline characters inside a token.
        let boundary;
        while ((boundary = buffer.indexOf('\n\n')) !== -1) {
          const rawEvent = buffer.slice(0, boundary);
          buffer = buffer.slice(boundary + 2);

          if (!rawEvent) continue;

          const dataLines = rawEvent
            .split('\n')
            .filter(l => l.startsWith('data:'))
            .map(l => {
              const rest = l.slice(5); // remove 'data:'
              return rest.startsWith(' ') ? rest.slice(1) : rest; // strip optional leading space
            });

          if (!dataLines.length) continue;

          const eventData = dataLines.join('\n'); // restore newlines between data: lines
          if (eventData !== '[DONE]') {
            setStreamingText(prev => prev + eventData);
          }
        }
      }
    } catch (err) {
      if (err.name !== 'AbortError') {
        setError('Не удалось получить ответ от AI. Попробуйте ещё раз.');
        setStreamingError(true);
      }
    } finally {
      // Commit the streamed response into the stable messages array atomically:
      // streaming bubble disappears and committed message appears in the same frame
      const finalContent = streamingText();
      const hadError = streamingError();
      batch(() => {
        setMessages(prev => [...prev, {
          role: 'ai',
          content: hadError && !finalContent
            ? 'Произошла ошибка при получении ответа.'
            : finalContent,
          error: hadError,
        }]);
        setStreamingText('');
        setIsStreaming(false);
        setStreamingError(false);
      });
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const handleTextareaInput = (e) => {
    setInput(e.target.value);
    e.target.style.height = 'auto';
    e.target.style.height = Math.min(e.target.scrollHeight, 120) + 'px';
  };

  const stopStreaming = () => {
    abortControllerRef?.abort();
  };

  const clearChat = async () => {
    if (isStreaming()) return;

    const token = localStorage.getItem('access_token');
    if (!token) return;

    const userId = getUserIdFromToken(token);
    if (!userId) return;

    try {
      await fetch(`${config.backendUrl}/api/v1/ai/history/${userId}/clear`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token}` },
      });
      batch(() => {
        setMessages([]);
        setError('');
      });
    } catch {
      setError('Не удалось очистить историю.');
    }
  };

  return (
    <div class="ai-chat">
      <div class="ai-chat-header">
        <div class="ai-chat-header-left">
          <div class="ai-chat-header-avatar">
            <BotIcon />
          </div>
          <div class="ai-chat-header-meta">
            <span class="ai-chat-header-title">AI-помощник</span>
            <span class="ai-chat-header-status">
              <span class={`ai-chat-status-dot ${isStreaming() ? 'ai-chat-status-dot--typing' : 'ai-chat-status-dot--online'}`} />
              {isStreaming() ? 'Печатает...' : 'Онлайн'}
            </span>
          </div>
        </div>
        <button
          class="ai-chat-icon-btn ai-chat-clear-btn"
          onClick={clearChat}
          disabled={isStreaming() || messages().length === 0}
          title="Очистить историю чата"
        >
          <TrashIcon />
        </button>
      </div>

      <div class="ai-chat-messages">
        <Show when={isLoadingHistory()}>
          <div class="ai-chat-loader-wrap">
            <div class="ai-chat-spinner" />
          </div>
        </Show>

        <Show when={!isLoadingHistory() && messages().length === 0 && !isStreaming()}>
          <div class="ai-chat-empty">
            <div class="ai-chat-empty-icon">
              <BotIcon />
            </div>
            <p class="ai-chat-empty-title">AI-помощник готов помочь</p>
            <p class="ai-chat-empty-hint">
              Задайте вопрос по задаче — объяснение алгоритма,<br />
              подсказка, разбор ошибки или что-то ещё.
            </p>
          </div>
        </Show>

        <Show when={!isLoadingHistory()}>
          {/* Completed messages: For only re-evaluates when messages() changes, never on token updates */}
          <For each={messages()}>
            {(msg) => (
              <div class={`ai-chat-row ${msg.role === 'user' ? 'ai-chat-row--user' : 'ai-chat-row--ai'}`}>
                <Show when={msg.role === 'ai'}>
                  <div class="ai-chat-msg-avatar"><BotIcon /></div>
                </Show>
                <div class={`ai-chat-bubble ${msg.role === 'user' ? 'ai-chat-bubble--user' : 'ai-chat-bubble--ai'} ${msg.error ? 'ai-chat-bubble--error' : ''}`}>
                  <Show
                    when={msg.role === 'ai'}
                    fallback={<span class="ai-chat-bubble-text">{msg.content}</span>}
                  >
                    <div class="ai-chat-markdown" innerHTML={renderMarkdown(msg.content)} />
                  </Show>
                </div>
              </div>
            )}
          </For>

          {/* Streaming bubble: plain text with pre-wrap — newlines visible, no markdown mid-stream artifacts */}
          <Show when={isStreaming()}>
            <div class="ai-chat-row ai-chat-row--ai ai-chat-row--streaming">
              <div class="ai-chat-msg-avatar"><BotIcon /></div>
              <div class="ai-chat-bubble ai-chat-bubble--ai">
                <Show
                  when={streamingText() !== ''}
                  fallback={
                    <span class="ai-chat-typing">
                      <span /><span /><span />
                    </span>
                  }
                >
                  <div class="ai-chat-streaming-text">{streamingText()}<span class="ai-chat-cursor" /></div>
                </Show>
              </div>
            </div>
          </Show>
        </Show>
      </div>

      <Show when={error()}>
        <div class="ai-chat-error-bar">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          {error()}
        </div>
      </Show>

      <div class="ai-chat-input-area">
        <textarea
          ref={textareaRef}
          class="ai-chat-textarea"
          value={input()}
          onInput={handleTextareaInput}
          onKeyDown={handleKeyDown}
          placeholder="Задайте вопрос... (Enter — отправить, Shift+Enter — перенос строки)"
          disabled={isStreaming()}
          rows="1"
        />
        <button
          class={`ai-chat-send-btn ${isStreaming() ? 'ai-chat-send-btn--stop' : ''}`}
          onClick={isStreaming() ? stopStreaming : sendMessage}
          disabled={!isStreaming() && !input().trim()}
          title={isStreaming() ? 'Остановить генерацию' : 'Отправить сообщение'}
        >
          <Show when={isStreaming()} fallback={<SendIcon />}>
            <StopIcon />
          </Show>
        </button>
      </div>
    </div>
  );
}