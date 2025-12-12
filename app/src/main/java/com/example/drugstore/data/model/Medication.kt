package com.example.drugstore.data.model

data class Medication(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    var quantity: Int = 0,
    val imageUrl: String? = null,
    val pharmacyId: String? = null,
    val pharmacistId: String? = null
)
