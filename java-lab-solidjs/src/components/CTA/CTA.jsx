import { A } from '@solidjs/router';
import './CTA.css';

export default function CTA() {
  return (
    <section class="cta">
      <div class="cta-container">
        <div class="cta-content">
          <h2 class="cta-title">Готовы начать изучение Java?</h2>
          <p class="cta-subtitle">
            Присоединяйтесь к сообществу JavaLab и начните свой путь в мир 
            Java прямо сейчас
          </p>
          <div class="cta-actions">
            <A href="/register" class="btn btn--primary btn--large">
              Зарегистрироваться бесплатно
            </A>
            <A href="/tasks" class="btn btn--outline btn--large">
              Посмотреть задачи
            </A>
          </div>
        </div>
      </div>
    </section>
  );
}
