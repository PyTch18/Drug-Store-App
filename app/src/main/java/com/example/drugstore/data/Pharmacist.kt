package com.example.drugstore.data.model

import com.google.firebase.database.PropertyName

data class Pharmacist(
    val id: String = "",
    val name: String = "",
    val pharmacyName: String = "",
    val email: String = "",
    val phoneNumber: String? = null,
    val gender: String? = null,
    val userType: String = "PHARMACIST",

    @JvmField
    val isOnline: Boolean = false,
    
    val voipExtension: String? = null,
    val voipPassword: String? = null
)
