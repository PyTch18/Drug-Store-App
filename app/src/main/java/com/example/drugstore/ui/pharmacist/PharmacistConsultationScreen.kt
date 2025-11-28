package com.example.drugstore.ui.pharmacist

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drugstore.data.repository.ConsultationRepository
import com.google.firebase.auth.FirebaseAuth

class PharmacistConsultationViewModel(
    private val repo: ConsultationRepository = ConsultationRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : androidx.lifecycle.ViewModel() {

    var isOnline by mutableStateOf(false)
        private set

    fun toggleOnline(newValue: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        isOnline = newValue
        repo.setPharmacistOnline(uid, newValue)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacistConsultationScreen(
    viewModel: PharmacistConsultationViewModel = viewModel()
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Medical Consultation") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Be available to receive consultation calls from patients.")
            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (viewModel.isOnline) "Status: Online" else "Status: Offline")
                Switch(
                    checked = viewModel.isOnline,
                    onCheckedChange = { viewModel.toggleOnline(it) }
                )
            }

            // TODO: handle incoming call requests here (left blank as requested)
        }
    }
}
