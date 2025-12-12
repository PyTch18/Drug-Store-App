package com.example.drugstore.ui.pharmacist

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.linphone.core.Call

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacistConsultationScreen(
    viewModel: PharmacistConsultationViewModel = viewModel(),
    voipManager: com.example.drugstore.data.voip.VoipManager
) {
    val call = voipManager.call
    val callState = voipManager.callState

    // Safely load the initial online status
    LaunchedEffect(viewModel) {
        viewModel.loadStatus()
    }

    if (call != null && callState == Call.State.IncomingReceived) {
        IncomingCallDialog(
            from = call.remoteAddress.displayName ?: "Unknown",
            onAccept = { voipManager.acceptCall() },
            onDecline = { voipManager.hangUp() }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Medical Consultation") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Be available to receive consultation calls from patients.")
            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (viewModel.isOnline) "Status: Online" else "Status: Offline")
                Switch(
                    checked = viewModel.isOnline,
                    onCheckedChange = { viewModel.toggleOnline(it) }
                )
            }

            Spacer(Modifier.height(32.dp))

            if (call != null && callState != null) {
                Text("Call Status: $callState", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                if (callState != Call.State.End && callState != Call.State.Error) {
                    Button(onClick = { voipManager.hangUp() }) {
                        Text("Hang Up")
                    }
                }
            } else {
                Text("No active call.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun IncomingCallDialog(
    from: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDecline,
        title = { Text("Incoming Call") },
        text = { Text("Call from: $from") },
        confirmButton = {
            Button(onClick = onAccept) { Text("Accept") }
        },
        dismissButton = {
            Button(onClick = onDecline) { Text("Decline") }
        }
    )
}
