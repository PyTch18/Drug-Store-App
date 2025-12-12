package com.example.drugstore.data.model

data class Patient(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val address: String? = null,
    val phoneNumber: String? = null,
    val gender: String? = null,
    val userType: String = "PATIENT",
    val voipExtension: String? = null,
    val voipPassword: String? = null
)
