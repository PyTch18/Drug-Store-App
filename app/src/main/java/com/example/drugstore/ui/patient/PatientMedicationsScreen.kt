package com.example.drugstore.ui.patient

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drugstore.data.model.Medication
import androidx.compose.material3.ExperimentalMaterial3Api


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientMedicationsScreen(
    viewModel: PatientMedicationsViewModel = viewModel(),
    onCartClick: () -> Unit
) {
    var uiVersion by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.loadMedications { uiVersion++ }
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
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
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
    medications: List<Medication>,
    onAddToCart: (Medication) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
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
