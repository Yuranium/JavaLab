import { Show, createMemo } from 'solid-js';
import { useAuth } from '../../context/AuthContext';
import './Footer.css';

export default function Footer() {
  const auth = useAuth();
  const isAuthenticated = createMemo(() => {
    return !!auth.accessToken() && !auth.isLoading();
  });
  const currentYear = new Date().getFullYear();

  return (
    <footer class="footer">
      <div class="footer-container">
        <div class="footer-content">
          <div class="footer-brand">
            <a href="/" class="footer-logo">
              <img src="/java-logo.png" alt="Java" class="footer-logo-img" />
              <span class="footer-logo-text">JavaLab</span>
            </a>
            <p class="footer-description">
              Интерактивная платформа для обучения программированию на Java.
              Изучайте язык через практику и реальные задачи.
            </p>
          </div>

          <div class="footer-links">
            <div class="footer-column">
              <h4 class="footer-column-title">Платформа</h4>
              <a href="/tasks" class="footer-link">Задачи</a>
              <Show when={!isAuthenticated()}>
                <a href="/register" class="footer-link">Регистрация</a>
                <a href="/login" class="footer-link">Вход</a>
              </Show>
            </div>

            <div class="footer-column">
              <h4 class="footer-column-title">Ресурсы</h4>
              <a href="#" class="footer-link">Документация</a>
              <a href="#" class="footer-link">Блог</a>
              <a href="#" class="footer-link">Сообщество</a>
            </div>

            <div class="footer-column">
              <h4 class="footer-column-title">Информация</h4>
              <a href="#" class="footer-link">О нас</a>
              <a href="#" class="footer-link">Контакты</a>
              <a href="#" class="footer-link">Политика конфиденциальности</a>
            </div>
          </div>
        </div>

        <div class="footer-bottom">
          <p class="footer-copyright">
            © {currentYear} JavaLab. Все права защищены.
          </p>
        </div>
      </div>
    </footer>
  );
}
