package com.example.deli

import androidx.lifecycle.ViewModel
import com.example.deli.ui.theme.FriendRecord
import com.example.deli.ui.theme.FriendRequestStatus
import com.example.deli.ui.theme.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FriendsViewModel : ViewModel() {

    // Все пользователи (имитация базы — потом заменить на сервер)
    private val _allUsers = MutableStateFlow(
        listOf(
            User("1", "Иван", "Петров", "+79111111111", "ivan@mail.ru"),
            User("2", "Мария", "Сидорова", "+79222222222", "maria@mail.ru"),
            User("3", "Алексей", "Иванов", "+79333333333", "alex@mail.ru"),
            User("4", "Ольга", "Кузнецова", "+79444444444", "olga@mail.ru"),
            User("5", "Дмитрий", "Смирнов", "+79555555555", "dmitry@mail.ru"),
            User("6", "Анна", "Попова", "+79666666666", "anna@mail.ru"),
        )
    )
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    // Друзья
    private val _friends = MutableStateFlow<List<FriendRecord>>(emptyList())
    val friends: StateFlow<List<FriendRecord>> = _friends.asStateFlow()

    // Отправленные заявки (ожидают принятия)
    private val _sentRequests = MutableStateFlow<List<FriendRecord>>(emptyList())
    val sentRequests: StateFlow<List<FriendRecord>> = _sentRequests.asStateFlow()

    // Входящие заявки (ждут моего ответа)
    private val _incomingRequests = MutableStateFlow<List<FriendRecord>>(emptyList())
    val incomingRequests: StateFlow<List<FriendRecord>> = _incomingRequests.asStateFlow()

    /**
     * Поиск пользователей по имени, фамилии, телефону или почте
     */
    fun searchUsers(query: String): List<User> {
        if (query.isBlank()) return emptyList()

        val lowerQuery = query.lowercase().trim()

        return _allUsers.value.filter { user ->
            user.firstName.lowercase().contains(lowerQuery) ||
                    user.lastName.lowercase().contains(lowerQuery) ||
                    user.phone.contains(lowerQuery) ||
                    user.email.lowercase().contains(lowerQuery) ||
                    "${user.firstName} ${user.lastName}".lowercase().contains(lowerQuery)
        }.filter { user ->
            // Исключаем тех, кто уже в друзьях или с активной заявкой
            _friends.value.none { it.user.id == user.id } &&
                    _sentRequests.value.none { it.user.id == user.id } &&
                    _incomingRequests.value.none { it.user.id == user.id }
        }
    }

    /**
     * Отправить заявку в друзья
     */
    fun sendFriendRequest(user: User) {
        val newRequest = FriendRecord(user, FriendRequestStatus.SENT)
        _sentRequests.value = _sentRequests.value + newRequest

        // ИМИТАЦИЯ: создаём входящую заявку с этого же пользователя для теста
        // В реальности это происходит на сервере
    }

    /**
     * Отменить отправленную заявку
     */
    fun cancelSentRequest(user: User) {
        _sentRequests.value = _sentRequests.value.filter { it.user.id != user.id }
    }

    /**
     * Принять входящую заявку
     */
    fun acceptIncomingRequest(user: User) {
        _incomingRequests.value = _incomingRequests.value.filter { it.user.id != user.id }
        _friends.value = _friends.value + FriendRecord(user, FriendRequestStatus.ACCEPTED)
    }

    /**
     * Отклонить входящую заявку
     */
    fun declineIncomingRequest(user: User) {
        _incomingRequests.value = _incomingRequests.value.filter { it.user.id != user.id }
    }

    /**
     * Удалить из друзей
     */
    fun removeFriend(user: User) {
        _friends.value = _friends.value.filter { it.user.id != user.id }
    }

    /**
     * ТЕСТ: имитировать входящую заявку
     */
    fun simulateIncomingRequest() {
        val testUser = _allUsers.value.random()

        // Проверяем, что его ещё нет нигде
        if (_friends.value.none { it.user.id == testUser.id } &&
            _sentRequests.value.none { it.user.id == testUser.id } &&
            _incomingRequests.value.none { it.user.id == testUser.id }
        ) {
            _incomingRequests.value = _incomingRequests.value +
                    FriendRecord(testUser, FriendRequestStatus.PENDING)
        }
    }
}