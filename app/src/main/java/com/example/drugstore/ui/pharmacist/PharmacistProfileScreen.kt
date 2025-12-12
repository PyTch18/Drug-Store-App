package com.example.drugstore.ui.pharmacist

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drugstore.data.model.Pharmacist

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacistProfileScreen(
    viewModel: PharmacistProfileViewModel = viewModel(),
    onBack: () -> Unit
) {
    val pharmacist = viewModel.pharmacist

    var name by remember { mutableStateOf("") }
    var pharmacyName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    // Update local state when the pharmacist data changes
    LaunchedEffect(pharmacist) {
        if (pharmacist != null) {
            name = pharmacist.name
            pharmacyName = pharmacist.pharmacyName
            phoneNumber = pharmacist.phoneNumber ?: "" // Handle null phone number
        }
    }

    Scaffold(
        topBar = { 
            TopAppBar(title = { Text("My Profile") }) 
        }
    ) { padding ->
        if (pharmacist != null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = pharmacyName,
                    onValueChange = { pharmacyName = it },
                    label = { Text("Pharmacy Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.updatePharmacist(
                            pharmacist.copy(
                                name = name,
                                pharmacyName = pharmacyName,
                                phoneNumber = phoneNumber.ifBlank { null } // Store null if empty
                            )
                        )
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
            }
        } else {
            // Show a loading indicator in the center
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
