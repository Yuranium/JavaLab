export function formatDistanceToNow(timestamp) {
  const now = Date.now();
  const diff = now - timestamp;

  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);
  const months = Math.floor(days / 30);
  const years = Math.floor(days / 365);

  if (seconds < 60) {
    return 'только что';
  } else if (minutes < 60) {
    return `${minutes} мин. назад`;
  } else if (hours < 24) {
    return `${hours} ч. назад`;
  } else if (days < 30) {
    return `${days} дн. назад`;
  } else if (years < 1) {
    return `${months} мес. назад`;
  } else {
    return `${years} г. назад`;
  }
}
