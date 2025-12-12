package com.example.drugstore.ui.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.drugstore.DrugStoreApp
import com.example.drugstore.data.voip.VoipManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

private const val PBX_DOMAIN = "172.20.10.50"

@Composable
fun LoginScreen(
    onLoginSuccess: (isPharmacist: Boolean) -> Unit,
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Get the shared VoipManager from Application
    val app = context.applicationContext as DrugStoreApp
    val voipManager: VoipManager = app.voipManager  // Application-scoped singleton [web:335]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    isLoading = true
                    FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                isLoading = false
                                Toast.makeText(
                                    context,
                                    "Login failed: ${task.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@addOnCompleteListener
                            }

                            val userId = task.result?.user?.uid
                            if (userId == null) {
                                isLoading = false
                                Toast.makeText(
                                    context,
                                    "Login failed: missing user ID",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@addOnCompleteListener
                            }

                            val db = FirebaseDatabase.getInstance()
                            val pharmacistsRef =
                                db.getReference("pharmacists").child(userId)

                            // First check if user is a pharmacist
                            pharmacistsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val isPharmacist = snapshot.exists()
                                    if (isPharmacist) {
                                        // Configure VoIP for pharmacist
                                        val ext = snapshot.child("voipExtension")
                                            .getValue(String::class.java) ?: "2001"
                                        val secret = snapshot.child("voipPassword")
                                            .getValue(String::class.java) ?: "pharmacist_secret"
                                        val name = snapshot.child("name")
                                            .getValue(String::class.java)

                                        voipManager.configureAccount(
                                            ext = ext,
                                            password = secret,
                                            domain = PBX_DOMAIN,
                                            displayName = name
                                        )

                                        isLoading = false
                                        onLoginSuccess(true)
                                    } else {
                                        // Otherwise load patient profile and configure as patient
                                        val patientsRef =
                                            db.getReference("patients").child(userId)
                                        patientsRef.addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(pSnapshot: DataSnapshot) {
                                                isLoading = false
                                                if (!pSnapshot.exists()) {
                                                    Toast.makeText(
                                                        context,
                                                        "User profile not found.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    return
                                                }

                                                val ext = pSnapshot.child("voipExtension")
                                                    .getValue(String::class.java) ?: "1001"
                                                val secret = pSnapshot.child("voipPassword")
                                                    .getValue(String::class.java) ?: "patient_secret"
                                                val name = pSnapshot.child("name")
                                                    .getValue(String::class.java)

                                                voipManager.configureAccount(
                                                    ext = ext,
                                                    password = secret,
                                                    domain = PBX_DOMAIN,
                                                    displayName = name
                                                )

                                                onLoginSuccess(false)
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                isLoading = false
                                                Toast.makeText(
                                                    context,
                                                    "Login failed: ${error.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        })
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    isLoading = false
                                    Toast.makeText(
                                        context,
                                        "Login failed: ${error.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                        }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text("Login", color = Color.White, modifier = Modifier.padding(8.dp))
            }
        }

        TextButton(onClick = onRegisterClick) {
            Text("Don't have an account? Register")
        }
    }
}
