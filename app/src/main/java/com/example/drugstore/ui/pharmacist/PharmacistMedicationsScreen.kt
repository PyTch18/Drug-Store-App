package com.example.drugstore.ui.pharmacist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drugstore.data.model.Medication
import java.math.BigDecimal
import java.math.BigDecimal.ZERO


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacistMedicationsScreen(
    viewModel: PharmacistMedicationsViewModel = viewModel()
) {
    var uiVersion by remember { mutableStateOf(0) }
    var editingMedication by remember { mutableStateOf<Medication?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadMedications { uiVersion++ }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Medications") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingMedication = Medication()
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.medications) { med ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(med.name, style = MaterialTheme.typography.titleMedium)
                        Text("Price: ${med.price}")
                        Text("Quantity: ${med.quantity}")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { editingMedication = med }) {
                                Text("Edit")
                            }
                            TextButton(onClick = {
                                viewModel.deleteMedication(med.id) { uiVersion++ }
                            }) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingMedication != null) {
        MedicationEditDialog(
            initial = editingMedication!!,
            onDismiss = { editingMedication = null },
            onSave = { updated ->
                viewModel.saveMedication(updated) {
                    editingMedication = null
                    uiVersion++
                }
            }
        )
    }
}

@Composable
private fun MedicationEditDialog(
    initial: Medication,
    onDismiss: () -> Unit,
    onSave: (Medication) -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var priceText by remember { mutableStateOf(initial.price.toString()) }
    var quantityText by remember { mutableStateOf(initial.quantity.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val price = priceText.toBigDecimalOrNull() ?: ZERO
                val quantity = quantityText.toIntOrNull() ?: 0

                onSave(
                    initial.copy(
                        name = name,
                        price = price,       // now BigDecimal, matches model
                        quantity = quantity
                    )
                )

            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Medication") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Price") }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantity") }
                )
            }
        }
    )
}
