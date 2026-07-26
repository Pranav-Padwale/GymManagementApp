package com.gym.view.member

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.controller.member.MemberUiState
import com.gym.model.member.Member
import com.gym.model.member.MemberStatus
import com.gym.view.theme.*

/**
 * View (Passive Compose Multiplatform Screen): Member Status Management.
 * Dynamically adapts colors between Dark Mode and Light Mode.
 */
@Composable
fun MemberManagementScreen(
    state: MemberUiState,
    isDarkMode: Boolean = true,
    onOpenDrawer: () -> Unit,
    onNavigateBack: () -> Unit,
    onFilterStatusChanged: (MemberStatus?) -> Unit,
    onUpdateMemberStatus: (memberId: String, newStatus: MemberStatus) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = getAppThemeColors(isDarkMode)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.bg)
            .padding(16.dp)
    ) {
        // Navigation Header Row (Hamburger ☰ & Back Arrow ⬅️)
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Hamburger Menu Trigger
                IconButton(onClick = onOpenDrawer, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Menu, contentDescription = "Open Hamburger Menu", tint = theme.textMain)
                }

                // Back Arrow Button
                IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Main Dashboard", tint = theme.textMain)
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(
                        text = "Member Directory",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textMain
                    )
                    Text(
                        text = "Total: ${state.totalCount}  •  Active: ${state.activeCount}",
                        fontSize = 12.sp,
                        color = theme.textMuted
                    )
                }
            }

            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(theme.cardBg)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Directory",
                    tint = GymAccentPrimary
                )
            }
        }

        // Error Banner (if present)
        AnimatedVisibility(
            visible = state.errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            state.errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = GymAccentDanger.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = GymAccentDanger)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = error, color = GymAccentDanger, fontSize = 13.sp)
                    }
                }
            }
        }

        // Filter Chips Row
        Text(
            text = "Filter by Status:",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = theme.textMuted,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            item {
                FilterChipItem(
                    label = "All Members (${state.totalCount})",
                    isSelected = state.selectedFilter == null,
                    badgeColor = GymAccentPrimary,
                    theme = theme,
                    onClick = { onFilterStatusChanged(null) }
                )
            }
            items(MemberStatus.entries) { status ->
                val badgeColor = when (status) {
                    MemberStatus.ACTIVE -> GymAccentSecondary
                    MemberStatus.ENQUIRE -> GymAccentWarning
                    MemberStatus.INACTIVE -> theme.textMuted
                    MemberStatus.EXPIRED -> GymAccentDanger
                }
                FilterChipItem(
                    label = status.displayName,
                    isSelected = state.selectedFilter == status,
                    badgeColor = badgeColor,
                    theme = theme,
                    onClick = { onFilterStatusChanged(status) }
                )
            }
        }

        // Loading indicator or Member List
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GymAccentPrimary)
            }
        } else if (state.members.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No members found for selected status filter.",
                    color = theme.textMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(state.members, key = { it.id }) { member ->
                    MemberCardItem(
                        member = member,
                        theme = theme,
                        onUpdateStatus = { newStatus ->
                            onUpdateMemberStatus(member.id, newStatus)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    badgeColor: Color,
    theme: AppThemeColors,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) badgeColor.copy(alpha = 0.25f) else theme.cardBg,
        modifier = Modifier.border(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) badgeColor else theme.cardBorder,
            shape = RoundedCornerShape(20.dp)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) theme.textMain else theme.textMuted
            )
        }
    }
}

@Composable
private fun MemberCardItem(
    member: Member,
    theme: AppThemeColors,
    onUpdateStatus: (MemberStatus) -> Unit
) {
    var isStatusMenuExpanded by remember { mutableStateOf(false) }

    val statusColor = when (member.status) {
        MemberStatus.ACTIVE -> GymAccentSecondary
        MemberStatus.ENQUIRE -> GymAccentWarning
        MemberStatus.INACTIVE -> theme.textMuted
        MemberStatus.EXPIRED -> GymAccentDanger
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = theme.cardBg),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, theme.cardBorder, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = statusColor
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = member.fullName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textMain
                        )
                        Text(
                            text = "Plan: ${member.membershipPlan}  •  ${member.email}",
                            fontSize = 12.sp,
                            color = theme.textMuted
                        )
                    }
                }

                // Interactive Status Change Dropdown
                Box {
                    Surface(
                        onClick = { isStatusMenuExpanded = true },
                        shape = RoundedCornerShape(16.dp),
                        color = statusColor.copy(alpha = 0.15f),
                        modifier = Modifier.border(1.dp, statusColor, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = member.status.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isStatusMenuExpanded,
                        onDismissRequest = { isStatusMenuExpanded = false },
                        modifier = Modifier.background(theme.cardBg)
                    ) {
                        MemberStatus.entries.forEach { statusOption ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (statusOption == member.status) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = GymAccentPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(statusOption.displayName, color = theme.textMain, fontSize = 13.sp)
                                    }
                                },
                                onClick = {
                                    isStatusMenuExpanded = false
                                    if (statusOption != member.status) {
                                        onUpdateStatus(statusOption)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = theme.dividerColor)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Joined: ${member.joinDate}", fontSize = 11.sp, color = theme.textMuted)
                Text(text = "Expires: ${member.expiryDate}", fontSize = 11.sp, color = theme.textMuted)
            }
        }
    }
}
