import './HowItWorks.css';

const steps = [
  {
    number: '01',
    title: 'Зарегистрируйтесь',
    description: 'Создайте аккаунт за пару минут и получите доступ ко всем материалам платформы'
  },
  {
    number: '02',
    title: 'Выберите тему',
    description: 'Начните с выбора интересующей вас задачи из каталога'
  },
  {
    number: '03',
    title: 'Решайте задачи',
    description: 'Читайте условие, пишите код прямо в браузере и отправляйте на проверку'
  },
  {
    number: '04',
    title: 'Получайте результат',
    description: 'Система автоматически проверит код и покажет ошибки, если они есть'
  }
];

export default function HowItWorks() {
  return (
    <section class="how-it-works">
      <div class="how-it-works-container">
        <div class="how-it-works-header">
          <h2 class="how-it-works-title">Как это работает</h2>
          <p class="how-it-works-subtitle">
            Четыре простых шага отделяют вас от начала обучения Java
          </p>
        </div>
        <div class="steps-container">
          {steps.map((step, index) => (
            <div class="step" key={index}>
              <div class="step-number">{step.number}</div>
              <div class="step-content">
                <h3 class="step-title">{step.title}</h3>
                <p class="step-description">{step.description}</p>
              </div>
              {index < steps.length - 1 && <div class="step-connector" />}
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
