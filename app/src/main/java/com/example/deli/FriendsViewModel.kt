package com.example.deli

import androidx.lifecycle.ViewModel
import com.example.deli.ui.theme.FriendRecord
import com.example.deli.ui.theme.FriendRequestStatus
import com.example.deli.ui.theme.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FriendsViewModel : ViewModel() {

    // локальная имитация базы всех пользователей
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

    // публичный поток всех пользователей только для чтения
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    // хранит список текущих друзей
    private val _friends = MutableStateFlow<List<FriendRecord>>(emptyList())

    // публичный поток друзей только для чтения
    val friends: StateFlow<List<FriendRecord>> = _friends.asStateFlow()

    // хранит отправленные заявки в друзья которые ждут ответа
    private val _sentRequests = MutableStateFlow<List<FriendRecord>>(emptyList())

    // публичный поток отправленных заявок только для чтения
    val sentRequests: StateFlow<List<FriendRecord>> = _sentRequests.asStateFlow()

    // хранит входящие заявки которые ждут моего ответа
    private val _incomingRequests = MutableStateFlow<List<FriendRecord>>(emptyList())

    // публичный поток входящих заявок только для чтения
    val incomingRequests: StateFlow<List<FriendRecord>> = _incomingRequests.asStateFlow()

    // ищет пользователей по имени, фамилии, телефону или email
    fun searchUsers(query: String): List<User> {

        // возвращает пустой список если запрос пустой
        if (query.isBlank()) return emptyList()

        // приводит запрос к нижнему регистру для сравнения
        val lowerQuery = query.lowercase().trim()

        return _allUsers.value.filter { user ->
            // проверяет совпадение по любому из полей пользователя
            user.firstName.lowercase().contains(lowerQuery) ||
                    user.lastName.lowercase().contains(lowerQuery) ||
                    user.phone.contains(lowerQuery) ||
                    user.email.lowercase().contains(lowerQuery) ||
                    "${user.firstName} ${user.lastName}".lowercase().contains(lowerQuery)
        }.filter { user ->
            // исключает пользователей которые уже есть в друзьях или заявках
            _friends.value.none { it.user.id == user.id } &&
                    _sentRequests.value.none { it.user.id == user.id } &&
                    _incomingRequests.value.none { it.user.id == user.id }
        }
    }

    // добавляет пользователя в список отправленных заявок
    fun sendFriendRequest(user: User) {
        val newRequest = FriendRecord(user, FriendRequestStatus.SENT)
        _sentRequests.value = _sentRequests.value + newRequest
    }

    // удаляет пользователя из списка отправленных заявок
    fun cancelSentRequest(user: User) {
        _sentRequests.value = _sentRequests.value.filter { it.user.id != user.id }
    }

    // переносит пользователя из входящих заявок в список друзей
    fun acceptIncomingRequest(user: User) {
        _incomingRequests.value = _incomingRequests.value.filter { it.user.id != user.id }
        _friends.value = _friends.value + FriendRecord(user, FriendRequestStatus.ACCEPTED)
    }

    // удаляет пользователя из списка входящих заявок без добавления в друзья
    fun declineIncomingRequest(user: User) {
        _incomingRequests.value = _incomingRequests.value.filter { it.user.id != user.id }
    }

    // удаляет пользователя из списка друзей
    fun removeFriend(user: User) {
        _friends.value = _friends.value.filter { it.user.id != user.id }
    }

    // симулирует входящую заявку от случайного пользователя
    fun simulateIncomingRequest() {

        // выбирает случайного пользователя из базы
        val testUser = _allUsers.value.random()

        // добавляет заявку только если пользователь не задействован нигде
        if (_friends.value.none { it.user.id == testUser.id } &&
            _sentRequests.value.none { it.user.id == testUser.id } &&
            _incomingRequests.value.none { it.user.id == testUser.id }
        ) {
            // добавляет входящую заявку от выбранного пользователя
            _incomingRequests.value = _incomingRequests.value +
                    FriendRecord(testUser, FriendRequestStatus.PENDING)
        }
    }
}