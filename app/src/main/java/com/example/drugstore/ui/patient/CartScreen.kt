package com.example.drugstore.ui.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drugstore.data.model.CartItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: CartViewModel = viewModel(),
    onBack: () -> Unit
) {
    var uiVersion by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.observeCart { uiVersion++ }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cart") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.items) { item ->
                    CartRow(item = item, onRemove = { viewModel.removeItem(item.id) })
                }
            }

            Divider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total:")
                Text("${viewModel.totalPrice} EGP")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = { viewModel.clearCart() }) {
                    Text("Clear cart")
                }
                Button(onClick = { /* TODO: place order node */ }) {
                    Text("Order")
                }
            }
        }
    }
}

@Composable
private fun CartRow(item: CartItem, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                Text("Qty: ${item.quantity}")
                Text("Price: ${item.price} EGP")
            }
            TextButton(onClick = onRemove) {
                Text("Remove")
            }
        }
    }
}
