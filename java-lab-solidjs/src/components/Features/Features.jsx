import './Features.css';

const features = [
  {
    icon: '💻',
    title: 'Практические задачи',
    description: 'Более 500 задач по Java от базового синтаксиса до продвинутых концепций. Решайте реальные проблемы и закрепляйте знания на практике.'
  },
  {
    icon: '⚡',
    title: 'Мгновенная проверка',
    description: 'Получайте автоматическую проверку кода сразу после отправки решения. Узнавайте об ошибках и исправляйте их в реальном времени.'
  },
  {
    icon: '📚',
    title: 'Структурированное обучение',
    description: 'Пошаговая программа обучения от простого к сложному. Каждая тема подкрепляется практическими упражнениями и примерами.'
  },
  {
    icon: '🎯',
    title: 'Персональный прогресс',
    description: 'Отслеживайте свой прогресс, получайте достижения и соревнуйтесь с другими студентами в рейтинговой таблице.'
  },
  {
    icon: '🔧',
    title: 'Современные инструменты',
    description: 'Работайте с актуальными версиями Java и популярными фреймворками. Изучайте лучшие практики разработки.'
  },
  {
    icon: '👥',
    title: 'Сообщество',
    description: 'Общайтесь с другими студентами, делитесь решениями и получайте помощь от опытных разработчиков.'
  }
];

export default function Features() {
  return (
    <section class="features">
      <div class="features-container">
        <div class="features-header">
          <h2 class="features-title">Почему JavaLab</h2>
          <p class="features-subtitle">
            Платформа создана для тех, кто хочет начать программировать на Java 
            через практику и реальные задачи
          </p>
        </div>
        <div class="features-grid">
          {features.map((feature, index) => (
            <div class="feature-card" key={index}>
              <div class="feature-icon">{feature.icon}</div>
              <h3 class="feature-title">{feature.title}</h3>
              <p class="feature-description">{feature.description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
