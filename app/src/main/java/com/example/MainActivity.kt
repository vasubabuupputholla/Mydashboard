package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.model.AuthState
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.TechnicianDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ServiceFlowViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ServiceFlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val authState by viewModel.authState.collectAsState()

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                ) {
                    AnimatedContent(
                        targetState = authState,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "AuthNav"
                    ) { targetAuth ->
                        when (targetAuth) {
                            is AuthState.LoggedOut -> {
                                LoginScreen(viewModel = viewModel)
                            }
                            is AuthState.Admin -> {
                                BackHandler {
                                    viewModel.logout()
                                }
                                AdminDashboardScreen(viewModel = viewModel)
                            }
                            is AuthState.TechnicianUser -> {
                                BackHandler {
                                    viewModel.logout()
                                }
                                TechnicianDashboardScreen(
                                    technician = targetAuth.technician,
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}
