import { createContext, useContext, createSignal, createMemo } from 'solid-js';

const UsersContext = createContext();

// Моковые данные пользователей
const mockUsers = [
  {
    id: '1',
    username: 'john_doe',
    firstName: 'Иван',
    lastName: 'Петров',
    registrationDate: 1640995200000,
    lastLogin: 1710864000000,
    isVerified: true,
  },
  {
    id: '2',
    username: 'jane_smith',
    firstName: 'Анна',
    lastName: 'Смирнова',
    registrationDate: 1672531200000,
    lastLogin: 1710950400000,
    isVerified: true,
  },
  {
    id: '3',
    username: 'alex_wilson',
    firstName: null,
    lastName: null,
    registrationDate: 1680307200000,
    lastLogin: null,
    isVerified: false,
  },
  {
    id: '4',
    username: 'maria_garcia',
    firstName: 'Мария',
    lastName: 'Гарсия',
    registrationDate: 1688169600000,
    lastLogin: 1710777600000,
    isVerified: true,
  },
  {
    id: '5',
    username: 'david_brown',
    firstName: 'Дэвид',
    lastName: 'Браун',
    registrationDate: 1696118400000,
    lastLogin: 1710691200000,
    isVerified: false,
  },
  {
    id: '6',
    username: 'emma_davis',
    firstName: 'Эмма',
    lastName: 'Дэвис',
    registrationDate: 1704067200000,
    lastLogin: 1710604800000,
    isVerified: true,
  },
  {
    id: '7',
    username: 'michael_johnson',
    firstName: 'Майкл',
    lastName: 'Джонсон',
    registrationDate: 1704153600000,
    lastLogin: 1710518400000,
    isVerified: true,
  },
  {
    id: '8',
    username: 'sarah_williams',
    firstName: null,
    lastName: 'Уильямс',
    registrationDate: 1704240000000,
    lastLogin: 1710432000000,
    isVerified: false,
  },
  {
    id: '9',
    username: 'robert_miller',
    firstName: 'Роберт',
    lastName: 'Миллер',
    registrationDate: 1704326400000,
    lastLogin: 1710345600000,
    isVerified: true,
  },
  {
    id: '10',
    username: 'lisa_anderson',
    firstName: 'Лиза',
    lastName: 'Андерсон',
    registrationDate: 1704412800000,
    lastLogin: null,
    isVerified: true,
  },
  {
    id: '11',
    username: 'james_taylor',
    firstName: 'Джеймс',
    lastName: 'Тейлор',
    registrationDate: 1704499200000,
    lastLogin: 1710172800000,
    isVerified: false,
  },
  {
    id: '12',
    username: 'olivia_thomas',
    firstName: 'Оливия',
    lastName: 'Томас',
    registrationDate: 1704585600000,
    lastLogin: 1710086400000,
    isVerified: true,
  },
];

export function UsersProvider(props) {
  const [users] = createSignal(mockUsers);
  const [blockedUsers, setBlockedUsers] = createSignal(new Set());

  // Функция блокировки пользователя
  const blockUser = (userId, reason, duration) => {
    console.log(`Блокировка пользователя ${userId}: ${reason}, срок: ${duration}`);
    setBlockedUsers(prev => new Set([...prev, userId]));
    // TODO: отправить запрос на сервер для блокировки
  };

  // Функция разблокировки пользователя
  const unblockUser = (userId) => {
    setBlockedUsers(prev => {
      const newSet = new Set(prev);
      newSet.delete(userId);
      return newSet;
    });
    // TODO: отправить запрос на сервер для разблокировки
  };

  // Проверка, заблокирован ли пользователь
  const isUserBlocked = (userId) => {
    return blockedUsers().has(userId);
  };

  const value = {
    users,
    blockUser,
    unblockUser,
    isUserBlocked,
  };

  return (
    <UsersContext.Provider value={value}>
      {props.children}
    </UsersContext.Provider>
  );
}

export function useUsers() {
  return useContext(UsersContext);
}
