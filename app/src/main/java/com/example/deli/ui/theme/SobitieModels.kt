package com.example.deli.ui.theme

// модель данных одного события с названием, датой, суммой и участниками
data class Sobitie(
    // название события
    val name: String = "",

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

    // дополнительная сумма сверх равной доли
    val extraAmount: String = ""
)