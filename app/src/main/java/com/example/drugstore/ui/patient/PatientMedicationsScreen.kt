package com.example.drugstore.ui.patient

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

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
                onAddToCart = { med ->
                    viewModel.addToCart(med) { /* could show snackbar */ }
                }
            )
        }
    }
}

@Composable
private fun MedicationList(
    modifier: Modifier = Modifier,
    medications: List<com.example.drugstore.data.model.Medication>,
    onAddToCart: (com.example.drugstore.data.model.Medication) -> Unit
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
                    Text("Available: ${med.quantity}")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { onAddToCart(med) }) {
                        Text("Add to cart")
                    }
                }
            }
        }
    }
}
