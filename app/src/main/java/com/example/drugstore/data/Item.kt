package com.example.drugstore.data.model

import java.math.BigDecimal

data class Item(
    val id: String,
    val name: String,
    val type: String,
    val price: BigDecimal,
    val imageUrl: String? = null
)
