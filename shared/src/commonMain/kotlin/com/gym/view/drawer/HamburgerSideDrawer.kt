package com.gym.view.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.controller.drawer.DrawerUiState
import com.gym.model.drawer.DrawerMenuItem

// Dynamic Palette Tokens for Dark / Light Modes
private val OrangeAvatarBg = Color(0xFFFF6D25)

/**
 * View (Passive Compose Multiplatform Side Drawer): Hamburger Navigation Drawer matching mockup image.
 * Features real-time interactive Dark Mode 🌙 / Light Mode ☀️ theme toggle.
 */
@Composable
fun HamburgerSideDrawer(
    state: DrawerUiState,
    onCloseDrawer: () -> Unit,
    onMenuItemClick: (itemId: String, title: String) -> Unit,
    onToggleTheme: () -> Unit,
    content: @Composable () -> Unit
) {
    val isDark = state.profile.isDarkMode

    // Dynamic Colors depending on Dark / Light Mode state
    val drawerBg = if (isDark) Color(0xFF1E1E22) else Color(0xFFF8FAFC)
    val cardBg = if (isDark) Color(0xFF27272A) else Color(0xFFE2E8F0)
    val dividerColor = if (isDark) Color(0xFF3F3F46) else Color(0xFFCBD5E1)
    val textMain = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val textMuted = if (isDark) Color(0xFFA1A1AA) else Color(0xFF64748B)

    val drawerState = rememberDrawerState(
        initialValue = if (state.isOpen) DrawerValue.Open else DrawerValue.Closed
    )

    LaunchedEffect(state.isOpen) {
        if (state.isOpen) {
            drawerState.open()
        } else {
            drawerState.close()
        }
    }

    LaunchedEffect(drawerState.isOpen) {
        if (!drawerState.isOpen && state.isOpen) {
            onCloseDrawer()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = drawerBg,
                drawerShape = RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp),
                modifier = Modifier.width(300.dp).fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(drawerBg)
                        .padding(20.dp)
                ) {
                    // 1. User Profile Header Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Large Orange Avatar Circle
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(OrangeAvatarBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = state.profile.name.take(1).uppercase(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = state.profile.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textMain
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = state.profile.phone,
                                    fontSize = 12.sp,
                                    color = textMuted
                                )
                            }
                        }

                        // Real-Time Interactive Dark/Light Mode Toggle Button 🌙 / ☀️
                        Surface(
                            onClick = onToggleTheme,
                            shape = CircleShape,
                            color = if (isDark) Color(0xFF27272A) else Color(0xFFE2E8F0),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = "Toggle Theme Mode",
                                    tint = if (isDark) Color(0xFFFFD54F) else OrangeAvatarBg,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = dividerColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Menu Items List
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(state.menuItems) { item ->
                            DrawerItemRow(
                                item = item,
                                isSelected = item.id == state.selectedMenuItemId,
                                textMain = textMain,
                                cardBg = cardBg,
                                onClick = { onMenuItemClick(item.id, item.title) }
                            )
                        }
                    }

                    // 3. Footer App Version Text
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.appVersion,
                            fontSize = 11.sp,
                            color = textMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        content = content
    )
}

@Composable
private fun DrawerItemRow(
    item: DrawerMenuItem,
    isSelected: Boolean,
    textMain: Color,
    cardBg: Color,
    onClick: () -> Unit
) {
    val icon = getIconForMenuItem(item.id)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) cardBg else Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = item.title,
                tint = if (isSelected) OrangeAvatarBg else textMain,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = item.title,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) OrangeAvatarBg else textMain
            )
        }
    }
}

private fun getIconForMenuItem(itemId: String): ImageVector {
    return when (itemId) {
        "fitness_activity" -> Icons.Default.TrackChanges
        "member_status" -> Icons.Default.People
        "balance_sheet" -> Icons.Default.AccountBalance
        "gym_plan" -> Icons.Default.Shield
        "health_data" -> Icons.AutoMirrored.Filled.ShowChart
        "active_x" -> Icons.Default.FitnessCenter
        "review_rating" -> Icons.Default.RateReview
        "store" -> Icons.Default.Storefront
        "time_slot" -> Icons.Default.Schedule
        "meetings" -> Icons.Default.EventAvailable
        "invite" -> Icons.Default.Share
        "logout" -> Icons.AutoMirrored.Filled.Logout
        else -> Icons.Default.Shield
    }
}
