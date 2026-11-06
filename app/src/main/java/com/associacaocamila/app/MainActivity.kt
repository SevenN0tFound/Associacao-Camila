package com.associacaocamila.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.ui.auth.AuthViewModel
import com.associacaocamila.app.ui.auth.LoginScreen
import com.associacaocamila.app.ui.home.HomeScreen
import com.associacaocamila.app.ui.theme.AssociacaoCamilaTheme

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AssociacaoCamilaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var loggedUser by remember { mutableStateOf<User?>(null) }

                    if (loggedUser == null) {
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = { user ->
                                loggedUser = user
                            }
                        )
                    } else {
                        HomeScreen(
                            currentUser = loggedUser!!,
                            onLogout = {
                                authViewModel.signOut()
                                loggedUser = null
                            }
                        )
                    }
                }
            }
        }
    }
}