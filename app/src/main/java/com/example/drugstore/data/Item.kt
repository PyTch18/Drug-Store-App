package com.example.drugstore.data.model

import java.math.BigDecimal

data class Item(
    val id: String,
    val name: String,
    val type: String,
    val price: Double,
    val imageUrl: String? = null
)
