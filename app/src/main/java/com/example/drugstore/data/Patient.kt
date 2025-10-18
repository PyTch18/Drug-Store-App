package com.example.drugstore.data.model

data class Patient(
    val id: String,
    val Name: String,
    val email: String,
    val address: String? = null, // Address might be optional
    val phoneNumber: String? = null
)
