import { Show, createMemo } from 'solid-js';
import { useAuth } from '../../context/AuthContext';
import './Hero.css';

export default function Hero() {
  const auth = useAuth();
  const isAuthenticated = createMemo(() => {
    return !!auth.accessToken() && !auth.isLoading();
  });

  return (
    <section class="hero">
      <div class="hero-container">
        <div class="hero-content">
          <h1 class="hero-title">
            Изучай <span class="highlight">Java</span> на практике
          </h1>
          <p class="hero-subtitle">
            Интерактивная платформа для обучения программированию на Java.
            Решай задачи, получай мгновенную проверку кода и прокачивай свои навыки.
          </p>
          <div class="hero-actions">
            <Show when={!isAuthenticated()} fallback={null}>
              <a href="/register" class="btn btn--primary btn--large">Начать обучение</a>
            </Show>
            <a href="/tasks" class="btn btn--secondary btn--large">Смотреть задачи</a>
          </div>
          <div class="hero-stats">
            <div class="stat">
              <span class="stat-value">25+</span>
              <span class="stat-label">Задач</span>
            </div>
            <div class="stat">
              <span class="stat-value">95%</span>
              <span class="stat-label">Успешных</span>
            </div>
          </div>
        </div>
        <div class="hero-visual">
          <div class="code-block">
            <div class="code-header">
              <span class="code-dot code-dot--red"></span>
              <span class="code-dot code-dot--yellow"></span>
              <span class="code-dot code-dot--green"></span>
            </div>
            <pre class="code-content"><code innerHTML={`
<span class="keyword">public class</span> <span class="class-name">HelloWorld</span> {
    <span class="keyword">public static void</span> <span class="function">main</span>(String[] args) {
        System.<span class="function">out</span>.<span class="function">println</span>(<span class="string">"Hello, Java!"</span>);
    }
}
`}></code></pre>
          </div>
        </div>
      </div>
    </section>
  );
}
