package com.example.drugstore.data.model

import com.google.firebase.database.Exclude
import java.util.UUID

data class Medication(
    var id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val imageUrl: String? = null,
    val pharmacyId: String? = null,
    val pharmacistId: String? = null
) {
    @Exclude
    fun isNew() = id.isBlank()
}
