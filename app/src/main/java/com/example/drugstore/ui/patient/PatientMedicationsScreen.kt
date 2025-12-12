package com.example.drugstore.ui.patient

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drugstore.data.model.Medication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientMedicationsScreen(
    viewModel: PatientMedicationsViewModel = viewModel(),
    onCartClick: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadMedications()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medications") },
                actions = {
                    TextButton(onClick = onCartClick) { Text("Cart") }
                }
            )
        }
    ) { padding ->
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            MedicationList(
                modifier = Modifier.padding(padding),
                medications = viewModel.medications,
                onAddToCart = { med -> viewModel.addToCart(med) }
            )
        }
    }
}

@Composable
private fun MedicationList(
    modifier: Modifier = Modifier,
    medications: List<Medication>,
    onAddToCart: (Medication) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(medications) { med ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(med.name, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("Price: ${med.price} EGP")
                    Spacer(Modifier.height(4.dp))
                    if (med.quantity > 0) {
                        Text("Quantity: ${med.quantity}") // Corrected label
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { onAddToCart(med) }) {
                            Text("Add to cart")
                        }
                    } else {
                        Text("Sold Out", color = Color.Red)
                    }
                }
            }
        }
    }
}
