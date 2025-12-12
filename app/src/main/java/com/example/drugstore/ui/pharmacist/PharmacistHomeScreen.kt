package com.example.drugstore.ui.pharmacist

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PharmacistHomeScreen(
    onMedicationsClick: () -> Unit,
    onConsultationClick: () -> Unit,
    onMapClick: () -> Unit,
    onVoipCallCenterClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Pharmacist Home", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onProfileClick, // Added this button
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("My Profile", color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onMedicationsClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Icon(Icons.Default.LocalPharmacy, contentDescription = "Medications", tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("My Medications", color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onConsultationClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Icon(Icons.Default.Call, contentDescription = "Consultation", tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text(
                "Consultation Status",
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onMapClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = "Map", tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text(
                "Pharmacy Location / Map",
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onVoipCallCenterClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Icon(Icons.Default.Call, contentDescription = "VoIP", tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("VoIP Call Center", color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}
