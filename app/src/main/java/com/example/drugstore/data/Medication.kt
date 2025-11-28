package com.example.drugstore.data.model

import java.math.BigDecimal

data class Medication(
    val id: String = "",
    val name: String = "",
    val type: String = "",        // e.g. tablet, syrup, etc.
    val price: BigDecimal = BigDecimal.ZERO,
    val imageUrl: String? = null,
    val quantity: Int = 0,
    val pharmacyId: String? = null
)
