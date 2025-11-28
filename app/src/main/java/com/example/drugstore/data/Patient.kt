package com.example.drugstore.data.model

data class Patient(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val address: String? = null,
    val phoneNumber: String? = null,
    val gender: String? = null,          // "Male", "Female", etc.
    val userType: String = "PATIENT"     // used in profile / navigation
)
