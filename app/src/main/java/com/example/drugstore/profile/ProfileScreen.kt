package com.example.drugstore.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drugstore.data.model.Pharmacist // Corrected import
import com.example.drugstore.ui.theme.DrugStoreTheme

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val userProfileState by profileViewModel.userProfile.observeAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val state = userProfileState) {
            is UserProfile.Loading -> {
                CircularProgressIndicator()
            }

            is UserProfile.PatientProfile -> {
                val patient = state.patient
                ProfileContent(
                    name = patient.name,
                    email = patient.email,
                    role = "Patient",
                    details = listOf(
                        Triple(
                            Icons.Default.Person,
                            "Gender",
                            patient.gender ?: "Not specified"
                        ),
                        Triple(
                            Icons.Default.Home,
                            "Address",
                            patient.address ?: "No address provided"
                        ),
                        Triple(
                            Icons.Default.Phone,
                            "Phone",
                            patient.phoneNumber ?: "No phone provided"
                        )
                    ),
                    onLogout = {
                        profileViewModel.logout()
                        onLogout()
                    }
                )
            }

            is UserProfile.PharmacistProfile -> {
                val pharmacist = state.pharmacist
                ProfileContent(
                    name = pharmacist.name,
                    email = pharmacist.email,
                    role = "Pharmacist",
                    details = listOf(
                        Triple(
                            Icons.Default.Home,
                            "Pharmacy Name",
                            pharmacist.pharmacyName
                        ),
                        Triple(
                            Icons.Default.Person,
                            "Gender",
                            pharmacist.gender ?: "Not specified"
                        ),
                        Triple(
                            Icons.Default.Phone,
                            "Phone",
                            pharmacist.phoneNumber ?: "No phone provided"
                        )
                    ),
                    onLogout = {
                        profileViewModel.logout()
                        onLogout()
                    }
                )
            }

            is UserProfile.Error, null -> {
                Text("Failed to load profile. Please try again.")
            }
        }
    }
}

@Composable
private fun ProfileContent(
    name: String,
    email: String,
    role: String,
    details: List<Triple<ImageVector, String, String>>,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = role,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(32.dp))

        InfoRow(icon = Icons.Default.Person, label = "Name", value = name)
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        InfoRow(icon = Icons.Default.Email, label = "Email", value = email)
        Divider(modifier = Modifier.padding(vertical = 16.dp))

        details.forEach { (icon, label, value) ->
            InfoRow(icon = icon, label = label, value = value)
            Divider(modifier = Modifier.padding(vertical = 16.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Logout")
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.bodySmall)
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    DrugStoreTheme {
        ProfileContent(
            name = "Amr Azouz",
            email = "amr@example.com",
            role = "Patient",
            details = listOf(
                Triple(Icons.Default.Person, "Gender", "Male"),
                Triple(Icons.Default.Home, "Address", "123 Main St"),
                Triple(Icons.Default.Phone, "Phone", "555-1234")
            ),
            onLogout = {}
        )
    }
}
