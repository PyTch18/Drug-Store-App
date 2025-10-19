package com.example.drugstore.register

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.drugstore.data.model.Patient
import com.example.drugstore.data.model.Pharmacist
import com.example.drugstore.ui.theme.DrugStoreTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

enum class RegisterScreen {
    PATIENT, PHARMACIST
}

@Composable
fun RegisterScreen(onRegistrationSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var pharmacyName by remember { mutableStateOf("") }

    var selectedRole by remember { mutableStateOf(RegisterScreen.PATIENT) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Register", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("I am a:")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = selectedRole == RegisterScreen.PATIENT,
                onClick = { selectedRole = RegisterScreen.PATIENT }
            )
            Text("Patient")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = selectedRole == RegisterScreen.PHARMACIST,
                onClick = { selectedRole = RegisterScreen.PHARMACIST }
            )
            Text("Pharmacist")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (selectedRole == RegisterScreen.PATIENT) {
            // Fields for the Patient
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
                // visualTransformation removed
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), // Correct keyboard type
                singleLine = true
                // visualTransformation removed
            )
        } else {
            // Fields for the Pharmacist
            OutlinedTextField(
                value = pharmacyName,
                onValueChange = { pharmacyName = it },
                label = { Text("Pharmacy Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), // Correct keyboard type
                singleLine = true
            )
        }
        Spacer(modifier = Modifier.height(32.dp))


        Button(
            onClick = {
                val auth = FirebaseAuth.getInstance()
                val database = FirebaseDatabase.getInstance()

                if (email.isNotBlank() && password.isNotBlank() && name.isNotBlank()) {
                    isLoading = true
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = task.result?.user?.uid
                                if (userId != null) {
                                    val userProfile: Any = if (selectedRole == RegisterScreen.PATIENT) {
                                        Patient(
                                            id = userId,
                                            name = name,
                                            email = email,
                                            address = address,
                                            phoneNumber = phoneNumber,
                                        )
                                    } else {
                                        Pharmacist(
                                            id = userId,
                                            name = name,
                                            pharmacyName = pharmacyName,
                                            email = email,
                                            phoneNumber = phoneNumber,

                                            )
                                    }

                                    val dbPath =
                                        if (selectedRole == RegisterScreen.PATIENT) "patients" else "pharmacists"
                                    database.getReference(dbPath).child(userId)
                                        .setValue(userProfile)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                "Registration successful!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            isLoading = false
                                            onRegistrationSuccess()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                context,
                                                "Error saving profile: ${e.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            isLoading = false
                                        }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Registration failed: ${task.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                isLoading = false
                            }
                        }
                } else {
                    Toast.makeText(context, "Please fill all fields.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Register", color = Color.White, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    DrugStoreTheme {
        RegisterScreen(onRegistrationSuccess = {})
    }
}