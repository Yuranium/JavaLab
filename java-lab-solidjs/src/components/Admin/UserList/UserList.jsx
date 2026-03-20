import { createMemo, For } from 'solid-js';
import { useUsers } from '../../../context/UsersContext';
import UserListItem from './UserListItem';
import './UserList.css';

const ITEMS_PER_PAGE = 5;

export default function UserList(props) {
  const { users, blockUser, isUserBlocked } = useUsers();
  const { onBlockClick } = props;

  const paginatedUsers = createMemo(() => {
    const endIndex = props.visibleCount * ITEMS_PER_PAGE;
    return users().slice(0, endIndex);
  });

  const hasMore = createMemo(() => {
    return props.visibleCount * ITEMS_PER_PAGE < users().length;
  });

  return (
    <div class="user-list">
      <div class="user-list-container">
        <For each={paginatedUsers()}>
          {(user) => (
            <UserListItem
              user={user}
              onBlockClick={onBlockClick}
              isBlocked={isUserBlocked(user.id)}
            />
          )}
        </For>
      </div>

      {hasMore() && (
        <div class="user-list-load-more">
          <button 
            class="user-list-load-more-btn"
            onClick={() => props.onLoadMore()}
          >
            Загрузить ещё
          </button>
        </div>
      )}
    </div>
  );
}
