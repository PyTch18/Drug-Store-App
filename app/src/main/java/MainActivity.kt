import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.drugstore.ui.homeimport.HomeScreen
import com.example.drugstore.ui.login.LoginScreen
import com.example.drugstore.ui.theme.DrugStoreTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrugStoreTheme {
                // A surface container using the 'background' color from the theme
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // We don't need the padding from Scaffold for this setup
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current // For showing toasts

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(onLoginClick = {
                // Here you would add your actual login logic
                navController.navigate("home") {
                    // Prevent going back to login screen with back button
                    popUpTo("login") { inclusive = true }
                }
            })
        }
        composable("home") {
            HomeScreen(
                onProfileClick = {
                    // For now, just show a toast message
                    Toast.makeText(context, "Profile Clicked!", Toast.LENGTH_SHORT).show()
                },
                onMapClick = {
                    // For now, just show a toast message
                    Toast.makeText(context, "Map Clicked!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}