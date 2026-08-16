package com.gym.view.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.gym.model.auth.LoginResponse
import com.gym.model.auth.LoginRequest
import com.gym.api.AuthClient
import com.gym.view.theme.getAppThemeColors
import com.gym.view.theme.GymAccentPrimary
import com.gym.view.theme.GymAccentDanger
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    isDarkMode: Boolean,
    onLoginSuccess: (LoginResponse) -> Unit
) {
    val theme = getAppThemeColors(isDarkMode)
    val scope = rememberCoroutineScope()
    val authClient = remember { AuthClient() }
    
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.bg),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.width(400.dp),
            colors = CardDefaults.cardColors(containerColor = theme.cardBg),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to Antigravity Gym",
                    style = MaterialTheme.typography.headlineSmall,
                    color = theme.textMain
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username", color = theme.textMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.textMain,
                        unfocusedTextColor = theme.textMain,
                        focusedBorderColor = GymAccentPrimary,
                        unfocusedBorderColor = theme.textMuted.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = theme.textMuted) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.textMain,
                        unfocusedTextColor = theme.textMain,
                        focusedBorderColor = GymAccentPrimary,
                        unfocusedBorderColor = theme.textMuted.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )
                
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage!!,
                        color = GymAccentDanger,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            val result = authClient.login(LoginRequest(username, password))
                            isLoading = false
                            result.onSuccess {
                                onLoginSuccess(it)
                            }.onFailure {
                                errorMessage = "Login failed: ${it.message}"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GymAccentPrimary),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading && username.isNotBlank() && password.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = theme.textMain, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Log In", color = theme.textMain)
                    }
                }
            }
        }
    }
}
