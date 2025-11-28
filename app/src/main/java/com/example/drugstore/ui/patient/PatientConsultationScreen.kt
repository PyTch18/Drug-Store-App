package com.example.drugstore.ui.patient

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drugstore.data.model.Pharmacist
import com.example.drugstore.data.repository.ConsultationRepository

class PatientConsultationViewModel(
    private val repo: ConsultationRepository = ConsultationRepository()
) : androidx.lifecycle.ViewModel() {

    var onlinePharmacists: List<Pharmacist> = emptyList()
        private set

    fun observeOnline(onStateChanged: () -> Unit) {
        repo.getOnlinePharmacists { list ->
            onlinePharmacists = list
            onStateChanged()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientConsultationScreen(
    viewModel: PatientConsultationViewModel = viewModel()
) {
    val context = LocalContext.current
    var uiVersion by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.observeOnline { uiVersion++ }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Consult a Pharmacist") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.onlinePharmacists) { pharmacist ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(pharmacist.name)
                            Text(pharmacist.pharmacyName ?: "")
                            Text(pharmacist.phoneNumber ?: "")
                        }
                        Button(
                            onClick = {
                                pharmacist.phoneNumber?.let { phone ->
                                    val intent = Intent(
                                        Intent.ACTION_DIAL,
                                        Uri.parse("tel:$phone")
                                    )
                                    context.startActivity(intent)
                                }
                            }
                        ) {
                            Text("Call")
                        }
                    }
                }
            }
        }
    }
}
