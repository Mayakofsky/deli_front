package com.example.deli.ui.theme

// модель данных одного события с датой, суммой и участниками
data class Sobitie(
    // дата события
    val date: String,

    // общая сумма события
    val totalAmount: Double,

    // список участников события
    val participants: List<Participant>
)

// модель данных одного участника события
data class Participant(
    // имя участника
    val name: String = "",

    // номер телефона участника
    val phone: String = "",

    // дополнительная сумма сверх равной доли
    val extraAmount: String = ""
)