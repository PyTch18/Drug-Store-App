package com.example.drugstore.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.drugstore.ui.theme.DrugStoreTheme

@Composable
fun HomeScreen(
    onProfileClick: () -> Unit,
    onMapClick: () -> Unit,
    onMedicationsClick: () -> Unit,
    onCartClick: () -> Unit,
    onConsultClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Home", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onProfileClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Profile Info", color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onMapClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = "Map", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Nearby Drug Stores", color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onMedicationsClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Icon(Icons.Default.LocalPharmacy, contentDescription = "Medications", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Medications", color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCartClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cart", color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onConsultClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Icon(Icons.Default.Call, contentDescription = "Consult", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Consult a Pharmacist", color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    DrugStoreTheme {
        HomeScreen(
            onProfileClick = {},
            onMapClick = {},
            onMedicationsClick = {},
            onCartClick = {},
            onConsultClick = {}
        )
    }
}
