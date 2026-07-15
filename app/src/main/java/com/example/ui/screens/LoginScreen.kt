package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.User
import com.example.ui.CivicViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: CivicViewModel,
    onLoginSuccess: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val loginError by viewModel.loginError.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        OneUIBluePrimary.copy(alpha = 0.08f),
                        OneUIBackground
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(OneUIBluePrimary)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CI",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Name
            Text(
                text = "Civic Intelligence",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = OneUITextPrimary,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Smart City Governance Portal",
                fontSize = 14.sp,
                color = OneUITextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login Box Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_card"),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = OneUISurface),
                border = BorderStroke(1.dp, Color(0x0DFFFFFF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sign In",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OneUITextPrimary,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input"),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OneUIBluePrimary,
                            focusedLabelColor = OneUIBluePrimary,
                            unfocusedBorderColor = Color(0x1AFFFFFF),
                            unfocusedLabelColor = OneUITextSecondary,
                            unfocusedLeadingIconColor = OneUITextSecondary,
                            focusedLeadingIconColor = OneUIBluePrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OneUIBluePrimary,
                            focusedLabelColor = OneUIBluePrimary,
                            unfocusedBorderColor = Color(0x1AFFFFFF),
                            unfocusedLabelColor = OneUITextSecondary,
                            unfocusedLeadingIconColor = OneUITextSecondary,
                            focusedLeadingIconColor = OneUIBluePrimary,
                            unfocusedTrailingIconColor = OneUITextSecondary,
                            focusedTrailingIconColor = OneUIBluePrimary
                        )
                    )

                    AnimatedVisibility(visible = loginError != null) {
                        Text(
                            text = loginError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .align(Alignment.Start)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sign In Button
                    Button(
                        onClick = {
                            viewModel.login(email.trim(), password.trim(), onLoginSuccess)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OneUIBluePrimary)
                    ) {
                        Text(
                            text = "Sign In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Demo Shortcuts Panel
            Text(
                text = "Demo Accounts (Tap to auto-fill & login)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = OneUITextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Citizen
                Card(
                    onClick = {
                        email = "citizen@civic.com"
                        password = "citizen123"
                        viewModel.login(email, password, onLoginSuccess)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = OneUIBlueLight),
                    border = BorderStroke(1.dp, OneUIBluePrimary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Citizen", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OneUIBlueDark)
                        Text("Demo", fontSize = 10.sp, color = OneUITextSecondary)
                    }
                }

                // Officer
                Card(
                    onClick = {
                        email = "officer@civic.com"
                        password = "officer123"
                        viewModel.login(email, password, onLoginSuccess)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = OneUIBlueLight),
                    border = BorderStroke(1.dp, OneUIBluePrimary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Officer", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OneUIBlueDark)
                        Text("Dave", fontSize = 10.sp, color = OneUITextSecondary)
                    }
                }

                // Admin
                Card(
                    onClick = {
                        email = "admin@civic.com"
                        password = "admin123"
                        viewModel.login(email, password, onLoginSuccess)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = OneUIBlueLight),
                    border = BorderStroke(1.dp, OneUIBluePrimary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Admin", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OneUIBlueDark)
                        Text("Demo", fontSize = 10.sp, color = OneUITextSecondary)
                    }
                }
            }
        }
    }
}
