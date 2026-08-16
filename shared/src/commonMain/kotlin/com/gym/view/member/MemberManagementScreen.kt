package com.gym.view.member

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.controller.member.MemberUiState
import com.gym.model.member.Member
import com.gym.model.member.MemberStatus
import com.gym.view.theme.*

/**
 * View: Member Directory & Status Management matching Google Stitch ("Elite Gym Management Pro").
 * Supports live searching, status filtering, member card actions, and member registration.
 */
@Composable
fun MemberManagementScreen(
    state: MemberUiState,
    isDarkMode: Boolean = true,
    onOpenDrawer: () -> Unit,
    onNavigateBack: () -> Unit,
    onFilterStatusChanged: (MemberStatus?) -> Unit,
    onUpdateMemberStatus: (memberId: String, newStatus: MemberStatus) -> Unit,
    onAddMember: (Member) -> Unit = {},
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = getAppThemeColors(isDarkMode)
    var searchQuery by remember { mutableStateOf("") }
    var isAddMemberDialogOpen by remember { mutableStateOf(false) }

    // Filter members by search query and active status filter
    val filteredMembers = remember(state.members, searchQuery) {
        if (searchQuery.isBlank()) {
            state.members
        } else {
            state.members.filter {
                it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true) ||
                it.membershipPlan.contains(searchQuery, ignoreCase = true) ||
                it.id.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Navigation Header Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(theme.cardBg)
                            .border(1.dp, theme.cardBorder, CircleShape)
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = theme.textMain)
                    }

                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(theme.cardBg)
                            .border(1.dp, theme.cardBorder, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = theme.textMain)
                    }

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Member Directory",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = theme.textMain,
                                maxLines = 1
                            )
                            Surface(
                                color = GymAccentSecondary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${state.totalCount}",
                                    color = GymAccentSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Tier status & directory records",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.textMuted,
                            maxLines = 1
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(theme.cardBg)
                            .border(1.dp, theme.cardBorder, CircleShape)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = GymAccentPrimary)
                    }

                    Button(
                        onClick = { isAddMemberDialogOpen = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GymAccentPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // 1. Search Bar (Full Width)
            Surface(
                color = theme.cardBg,
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (searchQuery.isNotBlank()) GymAccentPrimary else theme.cardBorder
                ),
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = theme.textMuted,
                        modifier = Modifier.size(18.dp)
                    )

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search by name, email, or ID...",
                                color = theme.textMuted,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = theme.textMain,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(GymAccentPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = theme.textMuted,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { searchQuery = "" }
                        )
                    }
                }
            }

            // 2. Horizontally Scrollable Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipItem(
                    label = "All (${state.totalCount})",
                    isSelected = state.selectedFilter == null,
                    dotColor = GymAccentPrimary,
                    theme = theme,
                    onClick = { onFilterStatusChanged(null) }
                )
                MemberStatus.entries.forEach { status ->
                    val dotColor = when (status) {
                        MemberStatus.ACTIVE -> GymAccentSecondary
                        MemberStatus.ENQUIRE -> GymAccentWarning
                        MemberStatus.INACTIVE -> theme.textMuted
                        MemberStatus.EXPIRED -> GymAccentDanger
                    }
                    FilterChipItem(
                        label = status.displayName,
                        isSelected = state.selectedFilter == status,
                        dotColor = dotColor,
                        theme = theme,
                        onClick = { onFilterStatusChanged(status) }
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
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GymAccentDanger.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = GymAccentDanger)
                            Text(text = error, color = GymAccentDanger, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Member Grid List
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GymAccentPrimary)
                }
            } else if (filteredMembers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = theme.textMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No members match '$searchQuery'" else "No members in this category",
                            color = theme.textMuted,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 280.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredMembers, key = { it.id }) { member ->
                        MemberBentoCard(
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

        // Add Member Modal Dialog
        if (isAddMemberDialogOpen) {
            AddMemberDialog(
                theme = theme,
                onDismiss = { isAddMemberDialogOpen = false },
                onAdd = { newMember ->
                    isAddMemberDialogOpen = false
                    onAddMember(newMember)
                }
            )
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    dotColor: Color,
    theme: AppThemeColors,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) dotColor.copy(alpha = 0.2f) else theme.cardBg,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) dotColor else theme.cardBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) theme.textMain else theme.textMuted
            )
        }
    }
}

@Composable
private fun MemberBentoCard(
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
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top Row: Avatar + Name + Plan + Status Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(GymAccentPrimary.copy(alpha = 0.3f), GymPrimaryGlow.copy(alpha = 0.15f))
                                )
                            )
                            .border(1.5.dp, GymAccentPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.fullName.take(1).uppercase(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GymAccentPrimary
                        )
                    }

                    Column {
                        Text(
                            text = member.fullName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = theme.textMain
                        )
                        Surface(
                            color = GymAccentPrimary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = member.membershipPlan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GymAccentPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Interactive Status Pill Dropdown
                Box {
                    Surface(
                        onClick = { isStatusMenuExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        color = statusColor.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(statusColor))
                            Text(
                                text = member.status.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (statusOption == member.status) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GymAccentPrimary, modifier = Modifier.size(14.dp))
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

            Spacer(modifier = Modifier.height(14.dp))

            // Contact Info Row
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = theme.textMuted, modifier = Modifier.size(14.dp))
                    Text(text = member.email, fontSize = 12.sp, color = theme.textMuted)
                }
                if (member.phone.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = theme.textMuted, modifier = Modifier.size(14.dp))
                        Text(text = member.phone, fontSize = 12.sp, color = theme.textMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = theme.dividerColor)
            Spacer(modifier = Modifier.height(10.dp))

            // Dates & Validity Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Joined", fontSize = 10.sp, color = theme.textMuted)
                    Text(member.joinDate, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = theme.textMain)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Expires", fontSize = 10.sp, color = theme.textMuted)
                    Text(member.expiryDate, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (member.status == MemberStatus.EXPIRED) GymAccentDanger else theme.textMain)
                }
            }
        }
    }
}

@Composable
private fun AddMemberDialog(
    theme: AppThemeColors,
    onDismiss: () -> Unit,
    onAdd: (Member) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var plan by remember { mutableStateOf("Gold Annual") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Register New Member", fontWeight = FontWeight.Bold, color = theme.textMain)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = plan,
                    onValueChange = { plan = it },
                    label = { Text("Membership Plan (e.g. Gold Annual, Platinum VIP)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isNotBlank() && email.isNotBlank()) {
                        val newId = "M-${(100..999).random()}"
                        onAdd(
                            Member(
                                id = newId,
                                fullName = fullName.trim(),
                                email = email.trim(),
                                phone = phone.trim(),
                                status = MemberStatus.ACTIVE,
                                membershipPlan = plan.trim(),
                                joinDate = "2026-08-16",
                                expiryDate = "2027-08-16"
                            )
                        )
                    }
                },
                enabled = fullName.isNotBlank() && email.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GymAccentPrimary)
            ) {
                Text("Register Member", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = theme.textMuted)
            }
        },
        containerColor = theme.cardBg
    )
}
