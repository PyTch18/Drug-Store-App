package com.example.drugstore.ui.pharmacist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.drugstore.data.model.Medication


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacistMedicationsScreen(
    viewModel: PharmacistMedicationsViewModel = viewModel()
) {
    var editingMedication by remember { mutableStateOf<Medication?>(null) }

    // Load medications when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadMedications()
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
                    Row(modifier = Modifier.padding(16.dp)) {
                        if (med.imageUrl != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(med.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = med.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(100.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
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
                                    viewModel.deleteMedication(med.id) {}
                                }) {
                                    Text("Delete")
                                }
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
    var imageUrl by remember { mutableStateOf(initial.imageUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val price = priceText.toDoubleOrNull() ?: 0.0
                val quantity = quantityText.toIntOrNull() ?: 0

                onSave(
                    initial.copy(
                        name = name,
                        price = price,
                        quantity = quantity,
                        imageUrl = imageUrl.ifBlank { null } // store null if empty
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
                    label = { Text("Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL (Optional)") }
                )
            }
        }
    )
}
