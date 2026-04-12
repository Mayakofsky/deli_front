package com.example.deli.ui.theme

// Модель участника события
data class Participant(
    val name: String = "",
    val phone: String = "",
    val extraAmount: String = ""
)

// Модель события
data class Sobitie(
    val date: String,
    val totalAmount: Double,
    val participants: List<Participant>
)