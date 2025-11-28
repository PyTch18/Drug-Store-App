package com.example.drugstore.data.model

data class Pharmacist(
    val id: String = "",
    val name: String = "",
    val pharmacyName: String = "",
    val email: String = "",
    val phoneNumber: String? = null,
    val gender: String? = null,
    val userType: String = "PHARMACIST",
    val isOnline: Boolean = false        // for consultation availability
)
