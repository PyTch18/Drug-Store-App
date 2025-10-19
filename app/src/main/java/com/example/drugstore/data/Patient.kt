package com.example.drugstore.data.model

// Add default values to all properties
data class Patient(
    val id: String = "",
    val Name: String = "",
    val email: String = "",
    val address: String? = null,
    val phoneNumber: String? = null
)
