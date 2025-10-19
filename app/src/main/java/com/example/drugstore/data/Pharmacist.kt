package com.example.drugstore.data.model

// Add default values to all properties
data class Pharmacist(
    val id: String = "",
    val name: String = "",
    val pharmacyName: String = "",
    val email: String = "",
    val phoneNumber: String? = null
)
