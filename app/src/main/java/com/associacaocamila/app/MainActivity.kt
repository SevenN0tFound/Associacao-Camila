package com.associacaocamila.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.associacaocamila.app.ui.auth.AuthViewModel
import com.associacaocamila.app.ui.auth.LoginScreen
import com.associacaocamila.app.ui.auth.SessionState
import com.associacaocamila.app.ui.home.HomeScreen
import com.associacaocamila.app.ui.splash.SplashScreen
import com.associacaocamila.app.ui.theme.AssociacaoCamilaTheme
import com.associacaocamila.app.util.NotificationHelper

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializa canais de notificação institucional
        NotificationHelper.createNotificationChannels(this)

        setContent {
            AssociacaoCamilaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val sessionState by authViewModel.sessionState.collectAsState()
                    val currentUser by authViewModel.currentUser.collectAsState()

                    when (val state = sessionState) {
                        is SessionState.Loading -> {
                            SplashScreen()
                        }

                        is SessionState.Authenticated,
                        is SessionState.Unauthenticated -> {
                            val navController = rememberNavController()
                            val startDestination = if (state is SessionState.Authenticated && currentUser != null) {
                                "home"
                            } else {
                                "login"
                            }

                            NavHost(
                                navController = navController,
                                startDestination = startDestination,
                            ) {
                                composable("login") {
                                    LoginScreen(
                                        viewModel = authViewModel,
                                        onLoginSuccess = { user ->
                                            authViewModel.onLoginSuccess(user)
                                            navController.navigate("home") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        },
                                        onNavigateToVolunteer = {
                                            navController.navigate("volunteer_registration")
                                        },
                                    )
                                }

                                composable("volunteer_registration") {
                                    com.associacaocamila.app.ui.volunteer.VolunteerRegistrationScreen(
                                        onNavigateBack = {
                                            navController.popBackStack()
                                        }
                                    )
                                }

                                composable("home") {
                                    val user = currentUser
                                    if (user == null) {
                                        // Fallback de proteção de rota
                                        LaunchedEffect(Unit) {
                                            navController.navigate("login") {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }
                                    } else {
                                        HomeScreen(
                                            currentUser = user,
                                            onUserUpdated = { updated ->
                                                authViewModel.updateCurrentUser(updated)
                                            },
                                            onLogout = {
                                                authViewModel.signOut()
                                                navController.navigate("login") {
                                                    popUpTo(0) { inclusive = true }
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
