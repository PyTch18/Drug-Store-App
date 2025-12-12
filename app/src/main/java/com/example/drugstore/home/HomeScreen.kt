package com.example.drugstore.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onProfileClick: () -> Unit,
    onMapClick: () -> Unit,
    onMedicationsClick: () -> Unit,
    onCartClick: () -> Unit,
    onConsultClick: () -> Unit,
    onLogoutClick: () -> Unit // Added callback
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome, Patient!", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        // Profile Button
        HomeButton(icon = Icons.Default.Person, text = "My Profile", onClick = onProfileClick)
        Spacer(Modifier.height(16.dp))

        // Map Button
        HomeButton(icon = Icons.Default.LocationOn, text = "Map", onClick = onMapClick)
        Spacer(Modifier.height(16.dp))

        // Medications Button
        HomeButton(icon = Icons.Default.LocalPharmacy, text = "Order Medications", onClick = onMedicationsClick)
        Spacer(Modifier.height(16.dp))

        // Cart Button
        HomeButton(icon = Icons.Default.ShoppingCart, text = "My Cart", onClick = onCartClick)
        Spacer(Modifier.height(16.dp))

        // Consult Button
        HomeButton(icon = Icons.Default.Call, text = "Consult a Pharmacist", onClick = onConsultClick)
        Spacer(Modifier.height(16.dp))
        
        // Logout Button
        HomeButton(icon = Icons.Default.Logout, text = "Logout", onClick = onLogoutClick)
    }
}

@Composable
private fun HomeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
    ) {
        Icon(icon, contentDescription = text, tint = Color.White)
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
    }
}
