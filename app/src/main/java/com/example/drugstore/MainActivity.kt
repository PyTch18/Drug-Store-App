package com.example.drugstore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.drugstore.data.voip.VoipManager
import com.example.drugstore.register.RegisterScreen
import com.example.drugstore.ui.calling.CallingScreen
import com.example.drugstore.ui.calling.IncomingCallScreen
import com.example.drugstore.ui.home.HomeScreen
import com.example.drugstore.ui.login.LoginScreen
import com.example.drugstore.ui.map.MapScreen
import com.example.drugstore.ui.patient.CartScreen
import com.example.drugstore.ui.patient.PatientConsultationScreen
import com.example.drugstore.ui.patient.PatientMedicationsScreen
import com.example.drugstore.ui.pharmacist.PharmacistConsultationScreen
import com.example.drugstore.ui.pharmacist.PharmacistHomeScreen
import com.example.drugstore.ui.pharmacist.PharmacistMedicationsScreen
import com.example.drugstore.ui.pharmacist.PharmacistProfileScreen
import com.example.drugstore.ui.profile.ProfileScreen
import com.example.drugstore.ui.theme.DrugStoreTheme
import com.google.firebase.auth.FirebaseAuth
import org.linphone.core.Call

class MainActivity : ComponentActivity() {

    private val voipManager: VoipManager by lazy {
        (application as DrugStoreApp).voipManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DrugStoreTheme {
                val callState = voipManager.callState

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        AppNavigation(
                            modifier = Modifier.padding(innerPadding),
                            voipManager = voipManager
                        )
                    }

                    if (callState != null) {
                        when (callState) {
                            Call.State.IncomingReceived -> {
                                IncomingCallScreen(voipManager = voipManager)
                            }
                            Call.State.Connected, Call.State.OutgoingProgress -> {
                                CallingScreen(voipManager = voipManager)
                            }
                            else -> { /* No UI for other states */ }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    voipManager: VoipManager
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { isPharmacist ->
                    navController.navigate(if (isPharmacist) "pharmacistHome" else "home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegistrationSuccess = { isPharmacist ->
                    navController.navigate(if (isPharmacist) "pharmacistHome" else "home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        val onLogout = {
            FirebaseAuth.getInstance().signOut()
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }

        composable("home") {
            HomeScreen(
                onProfileClick = { navController.navigate("profile") },
                onMapClick = { navController.navigate("map") },
                onMedicationsClick = { navController.navigate("patientMeds") },
                onCartClick = { navController.navigate("cart") },
                onConsultClick = { navController.navigate("patientConsult") },
                onLogoutClick = onLogout
            )
        }

        composable("pharmacistHome") {
            PharmacistHomeScreen(
                onMedicationsClick = { navController.navigate("pharmacistMeds") },
                onConsultationClick = { navController.navigate("pharmacistConsult") },
                onMapClick = { navController.navigate("map") },
                onVoipCallCenterClick = { navController.navigate("pharmacistConsult") },
                onProfileClick = { navController.navigate("pharmacistProfile") },
                onLogoutClick = onLogout
            )
        }

        composable("pharmacistProfile") {
            PharmacistProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("profile") {
            ProfileScreen(
                onLogout = onLogout
            )
        }

        composable("map") { MapScreen() }

        composable("patientMeds") {
            PatientMedicationsScreen(onCartClick = { navController.navigate("cart") })
        }

        composable("cart") {
            CartScreen(onBack = { navController.popBackStack() })
        }

        composable("patientConsult") {
            PatientConsultationScreen(voipManager = voipManager)
        }

        composable("pharmacistMeds") {
            PharmacistMedicationsScreen()
        }

        composable("pharmacistConsult") {
            PharmacistConsultationScreen(voipManager = voipManager)
        }
    }
}
