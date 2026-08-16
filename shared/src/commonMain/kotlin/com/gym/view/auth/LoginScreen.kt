package com.gym.view.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.api.AuthClient
import com.gym.model.auth.LoginRequest
import com.gym.model.auth.LoginResponse
import com.gym.model.auth.UserRole
import com.gym.view.theme.*
import kotlinx.coroutines.launch

/**
 * View: Authentication & Clearance Portal matching Google Stitch ("Elite Gym Management Pro").
 * Supports role switching across Super Admin, Trainer, and Member with dedicated MongoDB RBAC.
 */
@Composable
fun LoginScreen(
    isDarkMode: Boolean,
    onLoginSuccess: (LoginResponse) -> Unit
) {
    val theme = getAppThemeColors(isDarkMode)
    val scope = rememberCoroutineScope()
    val authClient = remember { AuthClient() }

    var selectedRole by remember { mutableStateOf(UserRole.SUPER_ADMIN) }
    var username by remember { mutableStateOf("admin") }
    var password by remember { mutableStateOf("admin123") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun performLogin() {
        if (username.isBlank() || password.isBlank()) return
        isLoading = true
        errorMessage = null
        scope.launch {
            val result = authClient.login(LoginRequest(username.trim(), password.trim()))
            isLoading = false
            result.onSuccess {
                onLoginSuccess(it)
            }.onFailure {
                errorMessage = "Authentication failed: Invalid credentials or clearance mismatch"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(androidx.compose.foundation.rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        // Ambient background glow circles
        Box(
            modifier = Modifier
                .size(500.dp)
                .offset(x = (-140).dp, y = (-140).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GymAccentPrimary.copy(alpha = 0.16f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(500.dp)
                .offset(x = 140.dp, y = 160.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GymAccentSecondary.copy(alpha = 0.12f), Color.Transparent)
                    )
                )
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .shadow(
                    elevation = 28.dp,
                    shape = RoundedCornerShape(22.dp),
                    spotColor = GymAccentPrimary.copy(alpha = 0.3f)
                ),
            colors = CardDefaults.cardColors(containerColor = theme.cardBg),
            shape = RoundedCornerShape(22.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Brand / Logo Header
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(GymAccentPrimary, GymPrimaryGlow)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Forge Logo",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "FORGE",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    ),
                    color = GymAccentSecondary
                )

                Text(
                    text = "ELITE PERFORMANCE MANAGEMENT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                    color = theme.textMuted
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Clearance / Role Selector
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "SELECT CLEARANCE LEVEL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = theme.textMuted,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple(UserRole.SUPER_ADMIN, "Super Admin", Icons.Default.AdminPanelSettings),
                            Triple(UserRole.TRAINER, "Trainer", Icons.Default.SportsMartialArts),
                            Triple(UserRole.MEMBER, "Member", Icons.Default.Person)
                        ).forEach { (role, label, icon) ->
                            val isSelected = selectedRole == role
                            Surface(
                                onClick = {
                                    selectedRole = role
                                    errorMessage = null
                                    when (role) {
                                        UserRole.SUPER_ADMIN -> {
                                            username = "admin"
                                            password = "admin123"
                                        }
                                        UserRole.TRAINER -> {
                                            username = "trainer"
                                            password = "trainer123"
                                        }
                                        UserRole.MEMBER -> {
                                            username = "member"
                                            password = "member123"
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) GymAccentPrimary.copy(alpha = 0.18f) else theme.cardElevated,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) GymAccentPrimary else theme.cardBorder
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) GymAccentPrimary else theme.textMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) GymAccentPrimary else theme.textMuted
                                    )
                                }
                            }
                        }
                    }

                    // Role Description Pill
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (selectedRole) {
                            UserRole.SUPER_ADMIN -> "⚡ Executive KPIs, balance sheet & club settings"
                            UserRole.TRAINER -> "⚡ Class schedules, member workouts & diet plans"
                            UserRole.MEMBER -> "⚡ Attendance, calorie burn metrics & meal logs"
                        },
                        fontSize = 11.sp,
                        color = GymAccentSecondary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Username / Identity Input
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username or Email", color = theme.textMuted, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = GymAccentPrimary, modifier = Modifier.size(20.dp))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.textMain,
                        unfocusedTextColor = theme.textMain,
                        focusedBorderColor = GymAccentPrimary,
                        unfocusedBorderColor = theme.cardBorder,
                        focusedContainerColor = theme.cardElevated,
                        unfocusedContainerColor = theme.cardElevated
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password Input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Access Key / Password", color = theme.textMuted, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = GymAccentPrimary, modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPasswordVisible) "Hide Password" else "Show Password",
                                tint = theme.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.textMain,
                        unfocusedTextColor = theme.textMain,
                        focusedBorderColor = GymAccentPrimary,
                        unfocusedBorderColor = theme.cardBorder,
                        focusedContainerColor = theme.cardElevated,
                        unfocusedContainerColor = theme.cardElevated
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { performLogin() }
                    )
                )

                // Error Message Banner
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = GymAccentDanger.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GymAccentDanger.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = GymAccentDanger, modifier = Modifier.size(18.dp))
                                Text(
                                    text = errorMessage!!,
                                    color = GymAccentDanger,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Sign In Button CTA
                Button(
                    onClick = { performLogin() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(12.dp, RoundedCornerShape(12.dp), spotColor = GymAccentPrimary),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GymAccentPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading && username.isNotBlank() && password.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Access Console",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Auxiliary Action Links
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reset Access Key",
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.textMuted
                    )
                    Text(
                        text = "Request Clearance ➔",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = GymAccentPrimary
                    )
                }
            }
        }
    }
}
