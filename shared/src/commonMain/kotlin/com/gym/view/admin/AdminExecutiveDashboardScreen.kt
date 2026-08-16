package com.gym.view.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.controller.finance.FinanceUiState
import com.gym.controller.member.MemberUiState
import com.gym.controller.timeslot.TimeSlotUiState
import com.gym.model.member.MemberStatus
import com.gym.model.timeslot.SlotCategory
import com.gym.view.theme.*

/**
 * View: Executive Desktop Admin Dashboard & Multi-Tab Analytics matching Google Stitch ("Elite Gym Management Pro").
 * Supports Operational Overview, Member Retention & Analytics, Financial Pulse, and Live Management Controls.
 */
@Composable
fun AdminExecutiveDashboardScreen(
    memberState: MemberUiState,
    financeState: FinanceUiState,
    timeSlotState: TimeSlotUiState,
    isDarkMode: Boolean = true,
    onNavigateToMembers: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToClasses: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onOpenDrawer: () -> Unit,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = getAppThemeColors(isDarkMode)
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Last 30 Days") }
    var activeTab by remember { mutableStateOf(AdminDashboardTab.OVERVIEW) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val netProfit = financeState.summary.netBalance
    val grossRevenue = financeState.summary.totalIncome
    val totalExpenses = financeState.summary.totalExpenses
    val enquireCount = memberState.members.count { it.status == MemberStatus.ENQUIRE }
    val expiredCount = memberState.members.count { it.status == MemberStatus.EXPIRED || it.status == MemberStatus.INACTIVE }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(theme.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        val isCompact = maxWidth < 840.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // 1. Persistent Left Sidebar (Desktop / Tablet Landscape Only)
            if (!isCompact) {
                Surface(
                    color = theme.cardBg,
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight()
                        .border(width = 1.dp, color = theme.cardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            // Logo & Brand Header
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(GymAccentPrimary, GymPrimaryGlow)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "FORGE",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.5.sp
                                        ),
                                        color = GymAccentSecondary
                                    )
                                    Text(
                                        text = "ELITE MANAGEMENT",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = theme.textMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            // Navigation Links
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                SidebarNavLink(
                                    icon = Icons.Default.AccountBalance,
                                    label = "Executive Console",
                                    isActive = true,
                                    theme = theme,
                                    onClick = {}
                                )
                                SidebarNavLink(
                                    icon = Icons.Default.Group,
                                    label = "Members (${memberState.totalCount})",
                                    isActive = false,
                                    theme = theme,
                                    onClick = onNavigateToMembers
                                )
                                SidebarNavLink(
                                    icon = Icons.Default.Payments,
                                    label = "Financials",
                                    isActive = false,
                                    theme = theme,
                                    onClick = onNavigateToFinance
                                )
                                SidebarNavLink(
                                    icon = Icons.AutoMirrored.Filled.EventNote,
                                    label = "Class Schedule",
                                    isActive = false,
                                    theme = theme,
                                    onClick = onNavigateToClasses
                                )
                                SidebarNavLink(
                                    icon = Icons.Default.TrackChanges,
                                    label = "Activity Tracker",
                                    isActive = false,
                                    theme = theme,
                                    onClick = onNavigateToActivity
                                )
                            }
                        }

                        // Sidebar Footer: Quick Action + Sign Out
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onNavigateToMembers,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GymAccentPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Member", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            HorizontalDivider(color = theme.dividerColor)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onLogout() }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = null,
                                    tint = GymAccentDanger,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Sign Out",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = GymAccentDanger
                                )
                            }
                        }
                    }
                }
            }

            // 2. Main Executive Canvas Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Top Bar
                Surface(
                    color = theme.bg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .border(width = 1.dp, color = theme.cardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = if (isCompact) 14.dp else 24.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isCompact) {
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
                                }

                                Text(
                                    text = if (isCompact) "FORGE Executive" else "FORGE Console",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = GymAccentSecondary
                                )
                            }

                            // Right Actions (Dark/Light toggle, Bell, Profile)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = onToggleTheme,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(theme.cardBg)
                                        .border(1.dp, theme.cardBorder, CircleShape)
                                    ) {
                                    Icon(
                                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Toggle Theme",
                                        tint = if (isDarkMode) Color(0xFFFFD54F) else GymAccentPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { snackbarMessage = "All systems operational (0 alerts)" },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(theme.cardBg)
                                        .border(1.dp, theme.cardBorder, CircleShape)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Notifications, contentDescription = null, tint = theme.textMain, modifier = Modifier.size(18.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .align(Alignment.TopEnd)
                                                .offset(x = 2.dp, y = (-2).dp)
                                                .clip(CircleShape)
                                                .background(GymAccentSecondary)
                                        )
                                    }
                                }

                                if (!isCompact) {
                                    VerticalDivider(modifier = Modifier.height(24.dp), color = theme.dividerColor)

                                    // Admin Avatar Chip
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(GymAccentPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("A", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        }
                                        Column {
                                            Text("Administrator", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = theme.textMain)
                                            Text("Super Admin", style = MaterialTheme.typography.labelSmall, color = GymAccentSecondary)
                                        }
                                    }
                                }
                            }
                        }

                        // Search Bar
                        Surface(
                            color = theme.cardBg,
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (searchQuery.isNotBlank()) GymAccentPrimary else theme.cardBorder
                            ),
                            modifier = Modifier.fillMaxWidth().widthIn(max = 440.dp)
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
                                            text = "Search members, classes, or logs...",
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
                    }
                }

                // Scrollable Dashboard Content Canvas
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(androidx.compose.foundation.rememberScrollState())
                        .padding(if (isCompact) 14.dp else 24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Header Row (Overview + Filter Buttons + Export)
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Executive Console",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = theme.textMain
                                    )
                                    Surface(
                                        color = GymAccentPrimary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, GymAccentPrimary.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = "LIVE CLUSTER",
                                            color = GymAccentPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Real-time operations, telemetry & member analytics",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = theme.textMuted
                                )
                            }
                        }

                        // Filter Buttons & Export in a horizontally scrollable row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("Last 7 Days", "Last 30 Days", "This Quarter").forEach { filter ->
                                val isSelected = selectedFilter == filter
                                Surface(
                                    onClick = { selectedFilter = filter },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) GymAccentPrimary.copy(alpha = 0.2f) else theme.cardBg,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) GymAccentPrimary else theme.cardBorder
                                    )
                                ) {
                                    Text(
                                        text = filter,
                                        color = if (isSelected) GymAccentPrimary else theme.textMuted,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Button(
                                onClick = { snackbarMessage = "CSV Export generated successfully for $selectedFilter" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = theme.cardElevated,
                                    contentColor = theme.textMain
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = GymAccentPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export CSV", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Sub-Navigation Tabs: Overview | Member Analytics | Financial Pulse
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AdminDashboardTab.entries.forEach { tab ->
                            val isSelected = activeTab == tab
                            Surface(
                                onClick = { activeTab = tab },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) GymAccentPrimary else theme.cardBg,
                                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        tint = if (isSelected) Color.White else theme.textMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = tab.title,
                                        color = if (isSelected) Color.White else theme.textMuted,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Tab Content Switcher
                    when (activeTab) {
                        AdminDashboardTab.OVERVIEW -> {
                            // Top Section: Financial Hero + Facility Energy Burn
                            if (isCompact) {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    // Financial Hero Card
                                    FinancialHeroCard(theme, netProfit, grossRevenue, totalExpenses, memberState.activeCount)
                                    // Facility Utilization Card
                                    FacilityUtilizationCard(theme, timeSlotState)
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    Box(modifier = Modifier.weight(1.8f)) {
                                        FinancialHeroCard(theme, netProfit, grossRevenue, totalExpenses, memberState.activeCount)
                                    }
                                    Box(modifier = Modifier.weight(1.2f)) {
                                        FacilityUtilizationCard(theme, timeSlotState)
                                    }
                                }
                            }

                            // Lower Section: Live Class Schedule + Recent Member Activity Feed
                            if (isCompact) {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    UpcomingClassesCard(theme, timeSlotState, onNavigateToClasses)
                                    RecentMemberFeedCard(theme, memberState, onNavigateToMembers)
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    Box(modifier = Modifier.weight(1.8f)) {
                                        UpcomingClassesCard(theme, timeSlotState, onNavigateToClasses)
                                    }
                                    Box(modifier = Modifier.weight(1.2f)) {
                                        RecentMemberFeedCard(theme, memberState, onNavigateToMembers)
                                    }
                                }
                            }
                        }

                        AdminDashboardTab.ANALYTICS -> {
                            // 4-Card Analytics Telemetry Grid
                            if (isCompact) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        AnalyticsKpiCard("Total Active", "${memberState.activeCount}", "+5.2% vs last mo", true, Icons.Default.Group, theme, Modifier.weight(1f))
                                        AnalyticsKpiCard("New Enquiries", "$enquireCount", "This month", true, Icons.Default.PersonAdd, theme, Modifier.weight(1f))
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        AnalyticsKpiCard("At-Risk / Expired", "$expiredCount", "Requires action", false, Icons.Default.Warning, theme, Modifier.weight(1f))
                                        AnalyticsKpiCard("Avg Attendance", "3.8 d/wk", "Target: 4.0", true, Icons.Default.EventAvailable, theme, Modifier.weight(1f))
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    AnalyticsKpiCard("Total Active Members", "${memberState.activeCount}", "+5.2% vs last month", true, Icons.Default.Group, theme, Modifier.weight(1f))
                                    AnalyticsKpiCard("New Enquiries", "$enquireCount", "This month", true, Icons.Default.PersonAdd, theme, Modifier.weight(1f))
                                    AnalyticsKpiCard("At-Risk / Expired", "$expiredCount", "Requires retention", false, Icons.Default.Warning, theme, Modifier.weight(1f))
                                    AnalyticsKpiCard("Avg Attendance", "3.8 days/wk", "Target: 4.0", true, Icons.Default.EventAvailable, theme, Modifier.weight(1f))
                                }
                            }

                            // Retention & Tier Breakdown Section
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = theme.cardBg),
                                shape = RoundedCornerShape(18.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = "MEMBERSHIP DISTRIBUTION & RETENTION",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                        color = theme.textMuted
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))

                                    val plans = memberState.members.groupBy { it.membershipPlan }
                                    plans.forEach { (plan, list) ->
                                        val pct = if (memberState.totalCount > 0) list.size.toFloat() / memberState.totalCount else 0f
                                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(plan, fontWeight = FontWeight.SemiBold, color = theme.textMain, fontSize = 13.sp)
                                                Text("${list.size} members (${(pct * 100).toInt()}%)", color = theme.textMuted, fontSize = 12.sp)
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            LinearProgressIndicator(
                                                progress = { pct },
                                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                                color = GymAccentPrimary,
                                                trackColor = theme.cardElevated,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        AdminDashboardTab.FINANCE -> {
                            // Direct Financial Ledger Summary
                            if (isCompact) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    AnalyticsKpiCard("Net Balance", "$${netProfit.toInt()}", "Healthy reserves", true, Icons.Default.AccountBalance, theme, Modifier.fillMaxWidth())
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        AnalyticsKpiCard("Gross Income", "$${grossRevenue.toInt()}", "Memberships & store", true, Icons.AutoMirrored.Filled.TrendingUp, theme, Modifier.weight(1f))
                                        AnalyticsKpiCard("Expenses", "$${totalExpenses.toInt()}", "Payroll & rent", false, Icons.AutoMirrored.Filled.TrendingDown, theme, Modifier.weight(1f))
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    AnalyticsKpiCard("Gross Income", "$${grossRevenue.toInt()}", "Memberships & store", true, Icons.AutoMirrored.Filled.TrendingUp, theme, Modifier.weight(1f))
                                    AnalyticsKpiCard("Operating Expenses", "$${totalExpenses.toInt()}", "Payroll & maintenance", false, Icons.AutoMirrored.Filled.TrendingDown, theme, Modifier.weight(1f))
                                    AnalyticsKpiCard("Net Balance", "$${netProfit.toInt()}", "Healthy reserves", true, Icons.Default.AccountBalance, theme, Modifier.weight(1f))
                                }
                            }

                            // Category Breakdown List
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = theme.cardBg),
                                shape = RoundedCornerShape(18.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "EXPENSE & REVENUE BREAKDOWN",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                            color = theme.textMuted
                                        )
                                        Text(
                                            text = "Full Sheet ➔",
                                            color = GymAccentPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clickable { onNavigateToFinance() }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    financeState.summary.expenseBreakdown.forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(item.category.displayName, fontWeight = FontWeight.SemiBold, color = theme.textMain, fontSize = 13.sp)
                                            Text("$${item.totalAmount.toInt()} (${(item.percentageOfTotalExpenses * 100).toInt()}%)", color = GymAccentDanger, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                        HorizontalDivider(color = theme.dividerColor)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class AdminDashboardTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    OVERVIEW("Overview & Operations", Icons.Default.Dashboard),
    ANALYTICS("Member Analytics & Retention", Icons.Default.Analytics),
    FINANCE("Financial Pulse", Icons.Default.Payments)
}

@Composable
private fun AnalyticsKpiCard(
    title: String,
    value: String,
    trend: String,
    trendPositive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    theme: AppThemeColors,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = theme.cardBg),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 12.sp, color = theme.textMuted, fontWeight = FontWeight.SemiBold)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (trendPositive) GymAccentSecondary.copy(alpha = 0.15f) else GymAccentDanger.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (trendPositive) GymAccentSecondary else GymAccentDanger,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = theme.textMain
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = trend,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (trendPositive) GymAccentSecondary else GymAccentDanger
            )
        }
    }
}

@Composable
private fun SidebarNavLink(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    theme: AppThemeColors,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isActive) GymAccentPrimary.copy(alpha = 0.15f) else Color.Transparent,
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, GymAccentPrimary.copy(alpha = 0.4f)) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) GymAccentPrimary else theme.textMuted,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isActive) GymAccentPrimary else theme.textMain
            )
        }
    }
}

@Composable
private fun FinancialHeroCard(
    theme: AppThemeColors,
    netProfit: Double,
    grossRevenue: Double,
    totalExpenses: Double,
    activeMembers: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(18.dp), spotColor = GymAccentPrimary.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = theme.cardBg),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MONTHLY NET REVENUE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = theme.textMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$${netProfit.toInt().toString().reversed().chunked(3).joinToString(",").reversed()}",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = GymAccentPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GymAccentPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = GymAccentPrimary, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = theme.dividerColor)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Gross Revenue", fontSize = 11.sp, color = theme.textMuted)
                    Text("$${grossRevenue.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GymAccentSecondary)
                }
                Column {
                    Text("Total Expenses", fontSize = 11.sp, color = theme.textMuted)
                    Text("$${totalExpenses.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GymAccentDanger)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Active Members", fontSize = 11.sp, color = theme.textMuted)
                    Text("$activeMembers", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = theme.textMain)
                }
            }
        }
    }
}

@Composable
private fun FacilityUtilizationCard(
    theme: AppThemeColors,
    timeSlotState: TimeSlotUiState
) {
    val totalCapacity = timeSlotState.daySlots.sumOf { it.maxCapacity }
    val totalBookings = timeSlotState.daySlots.sumOf { it.currentBookings }
    val occupancyRatio = if (totalCapacity > 0) (totalBookings.toFloat() / totalCapacity).coerceIn(0f, 1f) else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = theme.cardBg),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FACILITY OCCUPANCY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = theme.textMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(occupancyRatio * 100).toInt()}% Capacity",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (occupancyRatio > 0.85f) GymAccentWarning else GymAccentSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GymAccentSecondary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = GymAccentSecondary, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { occupancyRatio },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = if (occupancyRatio > 0.85f) GymAccentWarning else GymAccentSecondary,
                trackColor = theme.cardElevated
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${timeSlotState.daySlots.size} Sessions Today", fontSize = 11.sp, color = theme.textMuted)
                Text("$totalBookings / $totalCapacity Booked", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = theme.textMain)
            }
        }
    }
}

@Composable
private fun UpcomingClassesCard(
    theme: AppThemeColors,
    timeSlotState: TimeSlotUiState,
    onNavigateToClasses: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = theme.cardBg),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S SCHEDULE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = theme.textMuted
                )
                Text(
                    text = "View All ➔",
                    color = GymAccentPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToClasses() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (timeSlotState.daySlots.isEmpty()) {
                Text("No classes scheduled for today.", fontSize = 13.sp, color = theme.textMuted)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    timeSlotState.daySlots.take(3).forEach { slot ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.cardElevated)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(slot.category.emoji, fontSize = 20.sp)
                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text(slot.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = theme.textMain, maxLines = 1)
                                    Text("${slot.trainerName} • ${slot.startTime}", fontSize = 11.sp, color = theme.textMuted, maxLines = 1)
                                }
                            }

                            Surface(
                                color = GymAccentPrimary.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${slot.currentBookings}/${slot.maxCapacity}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GymAccentPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentMemberFeedCard(
    theme: AppThemeColors,
    memberState: MemberUiState,
    onNavigateToMembers: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = theme.cardBg),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT MEMBER REGISTRATIONS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = theme.textMuted
                )
                Text(
                    text = "Directory ➔",
                    color = GymAccentPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToMembers() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (memberState.members.isEmpty()) {
                Text("No registered members found.", fontSize = 13.sp, color = theme.textMuted)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    memberState.members.take(3).forEach { member ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.cardElevated)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(GymAccentPrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = member.fullName.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = GymAccentPrimary
                                    )
                                }

                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text(member.fullName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = theme.textMain, maxLines = 1)
                                    Text(member.membershipPlan, fontSize = 11.sp, color = theme.textMuted, maxLines = 1)
                                }
                            }

                            Surface(
                                color = when (member.status) {
                                    MemberStatus.ACTIVE -> GymAccentSecondary.copy(alpha = 0.15f)
                                    MemberStatus.ENQUIRE -> GymAccentWarning.copy(alpha = 0.15f)
                                    MemberStatus.INACTIVE -> theme.textMuted.copy(alpha = 0.15f)
                                    MemberStatus.EXPIRED -> GymAccentDanger.copy(alpha = 0.15f)
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = member.status.displayName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (member.status) {
                                        MemberStatus.ACTIVE -> GymAccentSecondary
                                        MemberStatus.ENQUIRE -> GymAccentWarning
                                        MemberStatus.INACTIVE -> theme.textMuted
                                        MemberStatus.EXPIRED -> GymAccentDanger
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
