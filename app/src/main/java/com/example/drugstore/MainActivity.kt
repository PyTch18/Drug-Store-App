package com.example.drugstore // Add the package declaration

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.drugstore.ui.home.HomeScreen
import com.example.drugstore.ui.login.LoginScreen
import com.example.drugstore.register.RegisterScreen
import com.example.drugstore.ui.theme.DrugStoreTheme
import com.example.drugstore.ui.profile.ProfileScreen
import com.example.drugstore.ui.map.MapScreen
import com.example.drugstore.ui.patient.CartScreen
import com.example.drugstore.ui.patient.PatientConsultationScreen
import com.example.drugstore.ui.patient.PatientMedicationsScreen
import com.example.drugstore.ui.pharmacist.PharmacistConsultationScreen
import com.example.drugstore.ui.pharmacist.PharmacistMedicationsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrugStoreTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Pass the padding to the navigation host
                    AppNavigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        composable("login") {
            // FIXED: Pass both required parameters to LoginScreen
            LoginScreen(
                onLoginClick = {
                    // Actual login logic would go here
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = {
                    // Navigate to the new register screen
                    navController.navigate("register")
                }
            )
        }

        // ADDED: The missing destination for the registration screen
        composable("register") {
            RegisterScreen(
                onRegistrationSuccess = {
                    // After successful registration, go to the home screen
                    navController.navigate("home") {
                        // Clear the back stack so the user can't go back
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                onProfileClick = { navController.navigate("profile") },
                onMapClick = { navController.navigate("map") },
                onMedicationsClick = { navController.navigate("patientMeds") },
                onCartClick = { navController.navigate("cart") },
                onConsultClick = { navController.navigate("patientConsult") }
            )
        }
        composable("profile") {
            ProfileScreen(
                onLogout = {
                    // Navigate back to login and clear the entire back stack
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("map") {
            MapScreen()
        }

        composable("patientMeds") {
            PatientMedicationsScreen(
                onCartClick = { navController.navigate("cart") }
            )
        }

        composable("cart") {
            CartScreen(onBack = { navController.popBackStack() })
        }

        composable("patientConsult") {
            PatientConsultationScreen()
        }
        composable("pharmacistMeds") { PharmacistMedicationsScreen() }
        composable("pharmacistConsult") { PharmacistConsultationScreen() }


    }
}
