package com.example.deli.ui.theme

data class Sobitie(
    val date: String,
    val totalAmount: Double,
    val participants: List<Participant>
)

data class Participant(
    val name: String = "",
    val phone: String = "",
    val extraAmount: String = ""
)