package com.example.drugstore.ui.calling

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.drugstore.data.voip.VoipManager
import org.linphone.core.Call

@Composable
fun CallingScreen(
    voipManager: VoipManager,
    modifier: Modifier = Modifier
) {
    val call = voipManager.call
    val callState = voipManager.callState

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            // Call status and remote user
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Status: ${callState?.name ?: "--"}",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    call?.remoteAddress?.displayName ?: "Unknown Contact",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            // Only Hang Up button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = { voipManager.hangUp() },
                    modifier = Modifier.size(64.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.CallEnd,
                        contentDescription = "Hang Up",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
