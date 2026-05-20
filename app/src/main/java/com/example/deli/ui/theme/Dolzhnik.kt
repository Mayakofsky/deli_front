package com.example.deli.ui.theme

// модель данных одного должника
data class Dolzhnik(
    // имя должника
    val name: String,

    // сумма долга
    val amount: String,

    // дата дедлайна возврата долга
    val deadline: String,

    // ссылка на фото должника, может быть пустой
    val photoUri: String? = null
)