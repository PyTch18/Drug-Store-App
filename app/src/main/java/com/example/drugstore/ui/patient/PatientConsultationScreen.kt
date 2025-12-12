package com.example.drugstore.ui.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drugstore.data.model.Pharmacist
import com.example.drugstore.data.repository.ConsultationRepository
import com.example.drugstore.data.voip.VoipManager

class PatientConsultationViewModel(
    private val repo: ConsultationRepository = ConsultationRepository()
) : androidx.lifecycle.ViewModel() {

    var onlinePharmacists by mutableStateOf<List<Pharmacist>>(emptyList())
        private set

    init {
        repo.getOnlinePharmacists { list ->
            onlinePharmacists = list
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientConsultationScreen(
    voipManager: VoipManager? = null, // Corrected: made nullable
    viewModel: PatientConsultationViewModel = viewModel()
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Consult a Pharmacist") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (viewModel.onlinePharmacists.isEmpty()) {
                Text("No pharmacists online right now.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(viewModel.onlinePharmacists) { pharm ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(pharm.name, style = MaterialTheme.typography.titleMedium)
                                if (!pharm.pharmacyName.isNullOrBlank()) {
                                    Text(pharm.pharmacyName)
                                }
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        pharm.voipExtension?.let { ext ->
                                            val sipAddress = "sip:$ext@192.168.56.1"
                                            voipManager?.startCall(sipAddress)
                                        }
                                    },
                                    enabled = pharm.voipExtension != null && voipManager != null
                                ) {
                                    Text("Start VoIP Call")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
