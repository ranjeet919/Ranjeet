package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.CivicViewModel
import com.example.ui.screens.CitizenDashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.OfficerDashboardScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: CivicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentUser by viewModel.currentUser.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (currentUser == null) {
                        LoginScreen(
                            viewModel = viewModel,
                            onLoginSuccess = { /* Routed reactively via Flow */ },
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        val user = currentUser!!
                        if (user.role == "Citizen") {
                            CitizenDashboardScreen(
                                viewModel = viewModel,
                                user = user,
                                onLogout = { viewModel.logout() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        } else {
                            // Officer or Admin roles
                            OfficerDashboardScreen(
                                viewModel = viewModel,
                                user = user,
                                onLogout = { viewModel.logout() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}
