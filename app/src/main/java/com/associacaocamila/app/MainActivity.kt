package com.associacaocamila.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.associacaocamila.app.ui.auth.LoginScreen
import com.associacaocamila.app.ui.theme.AssociacaoCamilaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AssociacaoCamilaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LoginScreen(
                        onLoginSuccess = { user ->
                            Toast.makeText(
                                this,
                                "Bem-vindo(a), ${user.name}!",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Próxima etapa: Navegar para HomeScreen
                        }
                    )
                }
            }
        }
    }
}