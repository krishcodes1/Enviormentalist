package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.dashboard.DashboardScreen
import com.example.myapplication.ui.dashboard.Event
import com.example.myapplication.LoginScreen
import com.example.myapplication.RegisterScreen
import com.example.myapplication.ui.login.CredentialsScreen
import com.example.myapplication.ui.login.OnboardingScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.DatabaseSeeder
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch

sealed class Screen {
    object LoginInitial : Screen()
    object Credentials  : Screen()
    object Register     : Screen()
    object Onboarding   : Screen()
    object Dashboard    : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            MyApplicationTheme {
                var screen by remember { mutableStateOf<Screen>(Screen.LoginInitial) }
                var showSeedButton by remember { mutableStateOf(true) }
                var isSeeding by remember { mutableStateOf(false) }
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                // sample events; swap in your DB fetch later
                val sampleEvents = listOf(
                    Event(1, "Feed the Homeless", "Oct 10 – Oct 20", 4.5, "Dr.Krish", R.drawable.event1),
                    Event(2, "Clean Central Park", "Oct 05 – Oct 15", 4.2, "Dr.Krish", R.drawable.event2),
                    Event(3, "Community Garden",   "Nov 01 – Nov 10", 4.8, "Dr.Krish", R.drawable.event3)
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    when (screen) {
                        Screen.LoginInitial -> LoginScreen(
                            onLoginClick        = { screen = Screen.Dashboard },
                            onCreateAccountClick= { screen = Screen.Register }
                        )

                        Screen.Register    -> RegisterScreen(
                            onBack     = { screen = Screen.LoginInitial },
                            onRegister = { screen = Screen.Onboarding },
                            onLogin    = { screen = Screen.LoginInitial },
                            onFacebook = { /* TODO */ },
                            onGoogle   = { /* TODO */ },
                            onApple    = { /* TODO */ }
                        )

                        Screen.Onboarding  -> OnboardingScreen(
                            onFinished = { screen = Screen.Dashboard }
                        )

                        Screen.Dashboard   -> DashboardScreen(
                            events       = sampleEvents,
                            onEventClick = { /* TODO: show details */ }
                        )

                        Screen.Credentials -> TODO()
                    }

                    // Seed Database Button (only visible in development)
                    if (showSeedButton) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                        ) {
                            if (isSeeding) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Button(
                                    onClick = {
                                        isSeeding = true
                                        scope.launch {
                                            try {
                                                val seeder = DatabaseSeeder()
                                                seeder.seedDatabase()
                                                showSeedButton = false
                                                snackbarHostState.showSnackbar("Database seeded successfully!")
                                            } catch (e: Exception) {
                                                snackbarHostState.showSnackbar("Error seeding database: ${e.message}")
                                            } finally {
                                                isSeeding = false
                                            }
                                        }
                                    }
                                ) {
                                    Text("Seed Database")
                                }
                            }
                        }
                    }

                    // Snackbar for feedback
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}
