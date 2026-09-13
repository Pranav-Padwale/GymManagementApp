package com.gym

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.gym.controller.activity.ActivityController
import com.gym.controller.diet.DietPlanController
import com.gym.controller.drawer.DrawerController
import com.gym.controller.finance.FinanceController
import com.gym.controller.goal.GoalController
import com.gym.controller.member.MemberManagementController
import com.gym.controller.timeslot.TimeSlotController
import com.gym.model.activity.ActivityRepositoryImpl
import com.gym.model.diet.DietPlanRepositoryImpl
import com.gym.model.drawer.DrawerRepositoryImpl
import com.gym.api.FinancialRepositoryHttp
import com.gym.api.MemberRepositoryHttp
import com.gym.api.TimeSlotRepositoryHttp
import com.gym.model.goal.GoalRepositoryImpl
import com.gym.view.activity.ActivityDashboardScreen
import com.gym.view.diet.DietPlanScreen
import com.gym.view.drawer.HamburgerSideDrawer
import com.gym.view.finance.FinanceDashboardScreen
import com.gym.view.goal.EditGoalScreen
import com.gym.view.member.MemberManagementScreen
import com.gym.view.timeslot.TimeSlotScreen
import com.gym.view.theme.GymTheme
import com.gym.view.theme.getAppThemeColors

/**
 * Main Application Entry Point binding Model, Controller, and View components.
 * Supports dynamic Dark / Light mode switching across all screens.
 */
@Composable
fun App() {
    // Model Initialization
    val drawerRepository = remember { DrawerRepositoryImpl() }
    val activityRepository = remember { ActivityRepositoryImpl() }
    val dietPlanRepository = remember { DietPlanRepositoryImpl() }
    val goalRepository = remember { GoalRepositoryImpl() }
    val memberRepository = remember { MemberRepositoryHttp() }
    val financialRepository = remember { FinancialRepositoryHttp() }
    val timeSlotRepository = remember { TimeSlotRepositoryHttp() }

    // Controller Initialization (Standard Kotlin Controllers)
    val drawerController = remember { DrawerController(repository = drawerRepository) }
    val activityController = remember { ActivityController(repository = activityRepository) }
    val dietPlanController = remember { DietPlanController(repository = dietPlanRepository) }
    val goalController = remember { GoalController(repository = goalRepository) }
    val memberController = remember { MemberManagementController(repository = memberRepository) }
    val financeController = remember { FinanceController(repository = financialRepository) }
    val timeSlotController = remember { TimeSlotController(repository = timeSlotRepository) }

    // State Collectors
    val drawerState by drawerController.uiState.collectAsState()
    val activityState by activityController.uiState.collectAsState()
    val dietPlanState by dietPlanController.uiState.collectAsState()
    val goalState by goalController.uiState.collectAsState()
    val memberState by memberController.uiState.collectAsState()
    val financeState by financeController.uiState.collectAsState()
    val timeSlotState by timeSlotController.uiState.collectAsState()

    val isDarkMode = drawerState.profile.isDarkMode
    val appTheme = getAppThemeColors(isDarkMode)

    // Auth Session State
    var session by remember { mutableStateOf<com.gym.model.auth.LoginResponse?>(null) }

    GymTheme(darkTheme = isDarkMode) {
        if (session == null) {
            com.gym.view.auth.LoginScreen(
                isDarkMode = isDarkMode,
                onLoginSuccess = { loginResponse ->
                    session = loginResponse
                    // Pass token to HTTP repositories
                    memberRepository.authToken = loginResponse.token
                    financialRepository.authToken = loginResponse.token
                    timeSlotRepository.authToken = loginResponse.token
                    
                    // Filter menu based on role
                    drawerController.setRole(loginResponse.role)
                    drawerController.updateProfileName(loginResponse.username)
                }
            )
        } else {
            HamburgerSideDrawer(
                state = drawerState,
                onCloseDrawer = { drawerController.onToggleDrawer(false) },
                onMenuItemClick = { id, title -> drawerController.onMenuItemClicked(id, title) },
                onToggleTheme = { drawerController.onToggleTheme() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(appTheme.bg)
                ) {
                    when (drawerState.selectedMenuItemId) {
                        "admin_dashboard" -> {
                            com.gym.view.admin.AdminExecutiveDashboardScreen(
                                memberState = memberState,
                                financeState = financeState,
                                timeSlotState = timeSlotState,
                                isDarkMode = isDarkMode,
                                onNavigateToMembers = { drawerController.onMenuItemClicked("member_status", "Member Status Directory") },
                                onNavigateToFinance = { drawerController.onMenuItemClicked("balance_sheet", "Income & Expense Sheet") },
                                onNavigateToClasses = { drawerController.onMenuItemClicked("time_slot", "Time Slot") },
                                onNavigateToActivity = { drawerController.onMenuItemClicked("fitness_activity", "Fitness Activity") },
                                onOpenDrawer = { drawerController.onToggleDrawer(true) },
                                onToggleTheme = { drawerController.onToggleTheme() },
                                onLogout = { session = null }
                            )
                        }
                        "fitness_activity" -> {
                            ActivityDashboardScreen(
                                state = activityState,
                                isDarkMode = isDarkMode,
                                onOpenDrawer = {
                                    drawerController.onToggleDrawer(true)
                                },
                                onToggleCalendarDialog = { isOpen ->
                                    activityController.onToggleCalendarDialog(isOpen)
                                },
                                onCalendarDateSelected = { day, month, year ->
                                    activityController.onCalendarDateSelected(day, month, year)
                                },
                                onCalendarMonthNav = { deltaMonth ->
                                    activityController.onCalendarMonthNav(deltaMonth)
                                },
                                onSelectToday = {
                                    activityController.onSelectToday()
                                },
                                onGrantHealthPermission = {
                                    activityController.onGrantHealthPermission()
                                },
                                onGraphTimeRangeChanged = { range ->
                                    activityController.onGraphTimeRangeChanged(range)
                                },
                                onToggleSeries = { showCal, showEaten, showBurn ->
                                    activityController.onToggleSeries(showCal, showEaten, showBurn)
                                },
                                onBottomTabSelected = { tabName ->
                                    when (tabName) {
                                        "Goal" -> drawerController.onMenuItemClicked("edit_goal", "Edit Goal")
                                        "Diet Plan" -> drawerController.onMenuItemClicked("diet_plan", "Diet Plan")
                                        else -> activityController.onBottomTabSelected(tabName)
                                    }
                                }
                            )
                        }
                        "diet_plan" -> {
                            DietPlanScreen(
                                state = dietPlanState,
                                isDarkMode = isDarkMode,
                                onOpenDrawer = {
                                    drawerController.onToggleDrawer(true)
                                },
                                onNavigateBack = {
                                    drawerController.onMenuItemClicked("fitness_activity", "Fitness Activity")
                                },
                                onTrainerSelected = { trainerName ->
                                    dietPlanController.onTrainerSelected(trainerName)
                                },
                                onToggleTrainerDropdown = { expanded ->
                                    dietPlanController.onToggleTrainerDropdown(expanded)
                                },
                                onCameraIconClicked = { category ->
                                    dietPlanController.onCameraIconClicked(category)
                                },
                                onAddFoodItem = { name, grams, cal, cat ->
                                    dietPlanController.onAddFoodItem(name, grams, cal, cat)
                                },
                                onRemoveFoodItem = { foodId ->
                                    dietPlanController.onRemoveFoodItem(foodId)
                                }
                            )
                        }
                        "edit_goal", "gym_plan" -> {
                            EditGoalScreen(
                                state = goalState,
                                isDarkMode = isDarkMode,
                                onOpenDrawer = {
                                    drawerController.onToggleDrawer(true)
                                },
                                onNavigateBack = {
                                    drawerController.onMenuItemClicked("fitness_activity", "Fitness Activity")
                                },
                                onGenderSelected = { gender ->
                                    goalController.onGenderSelected(gender)
                                },
                                onAgeChanged = { ageStr ->
                                    goalController.onAgeChanged(ageStr)
                                },
                                onHeightChanged = { heightStr ->
                                    goalController.onHeightChanged(heightStr)
                                },
                                onWeightChanged = { weightStr ->
                                    goalController.onWeightChanged(weightStr)
                                },
                                onGoalWeightChanged = { goalWeightStr ->
                                    goalController.onGoalWeightChanged(goalWeightStr)
                                },
                                onWeightGoalSelected = { goal ->
                                    goalController.onWeightGoalSelected(goal)
                                },
                                onActivityLevelSelected = { activity ->
                                    goalController.onActivityLevelSelected(activity)
                                },
                                onViewResultClicked = {
                                    goalController.onViewResultClicked()
                                }
                            )
                        }
                        "member_status" -> {
                            MemberManagementScreen(
                                state = memberState,
                                isDarkMode = isDarkMode,
                                onOpenDrawer = {
                                    drawerController.onToggleDrawer(true)
                                },
                                onNavigateBack = {
                                    drawerController.onMenuItemClicked("fitness_activity", "Fitness Activity")
                                },
                                onFilterStatusChanged = { status ->
                                    memberController.onFilterStatusChanged(status)
                                },
                                onUpdateMemberStatus = { memberId, newStatus ->
                                    memberController.onUpdateMemberStatus(memberId, newStatus)
                                },
                                onAddMember = { newMember ->
                                    memberController.onAddMember(newMember)
                                },
                                onDismissNewMemberCredentials = {
                                    memberController.onDismissNewMemberCredentials()
                                },
                                onRefresh = {
                                    memberController.refreshMembers()
                                }
                            )
                        }
                        "balance_sheet" -> {
                            FinanceDashboardScreen(
                                state = financeState,
                                isDarkMode = isDarkMode,
                                onOpenDrawer = {
                                    drawerController.onToggleDrawer(true)
                                },
                                onNavigateBack = {
                                    drawerController.onMenuItemClicked("fitness_activity", "Fitness Activity")
                                },
                                onMonthRangeChanged = { month, year ->
                                    financeController.onMonthRangeChanged(month, year)
                                },
                                onAddExpense = { title, amount, category ->
                                    financeController.onAddExpense(title, amount, category)
                                },
                                onClearError = {
                                    financeController.onClearError()
                                },
                                onRefresh = {
                                    financeController.refreshBalanceSheet()
                                }
                            )
                        }
                        "time_slot" -> {
                            TimeSlotScreen(
                                state = timeSlotState,
                                isDarkMode = isDarkMode,
                                onOpenDrawer = {
                                    drawerController.onToggleDrawer(true)
                                },
                                onNavigateBack = {
                                    drawerController.onMenuItemClicked("fitness_activity", "Fitness Activity")
                                },
                                onDaySelected = { day ->
                                    timeSlotController.onDaySelected(day)
                                },
                                onSlotSelected = { slot ->
                                    timeSlotController.onSlotSelected(slot)
                                },
                                onToggleCreateDialog = { open ->
                                    timeSlotController.onToggleCreateDialog(open)
                                },
                                onCreateSlot = {
                                    timeSlotController.onCreateSlot()
                                },
                                onDeleteSlot = { slotId ->
                                    timeSlotController.onDeleteSlot(slotId)
                                },
                                onToggleSlotActive = { slotId ->
                                    timeSlotController.onToggleSlotActive(slotId)
                                },
                                onCheckInMember = { slotId, memberId ->
                                    timeSlotController.onCheckInMember(slotId, memberId)
                                },
                                onCheckOutMember = { attendanceId ->
                                    timeSlotController.onCheckOutMember(attendanceId)
                                },
                                onBookSlot = { slotId ->
                                    timeSlotController.onBookSlot(slotId, "M-101", "Alex Rivers")
                                },
                                onNewSlotTitleChanged = { value ->
                                    timeSlotController.onNewSlotTitleChanged(value)
                                },
                                onNewSlotTrainerChanged = { value ->
                                    timeSlotController.onNewSlotTrainerChanged(value)
                                },
                                onNewSlotDayChanged = { day ->
                                    timeSlotController.onNewSlotDayChanged(day)
                                },
                                onNewSlotStartTimeChanged = { value ->
                                    timeSlotController.onNewSlotStartTimeChanged(value)
                                },
                                onNewSlotEndTimeChanged = { value ->
                                    timeSlotController.onNewSlotEndTimeChanged(value)
                                },
                                onNewSlotCapacityChanged = { value ->
                                    timeSlotController.onNewSlotCapacityChanged(value)
                                },
                                onNewSlotCategoryChanged = { category ->
                                    timeSlotController.onNewSlotCategoryChanged(category)
                                },
                                onClearMessages = {
                                    timeSlotController.onClearMessages()
                                }
                            )
                        }
                        else -> {
                            ActivityDashboardScreen(
                                state = activityState,
                                isDarkMode = isDarkMode,
                                onOpenDrawer = {
                                    drawerController.onToggleDrawer(true)
                                },
                                onToggleCalendarDialog = { isOpen ->
                                    activityController.onToggleCalendarDialog(isOpen)
                                },
                                onCalendarDateSelected = { day, month, year ->
                                    activityController.onCalendarDateSelected(day, month, year)
                                },
                                onCalendarMonthNav = { deltaMonth ->
                                    activityController.onCalendarMonthNav(deltaMonth)
                                },
                                onSelectToday = {
                                    activityController.onSelectToday()
                                },
                                onGrantHealthPermission = {
                                    activityController.onGrantHealthPermission()
                                },
                                onGraphTimeRangeChanged = { range ->
                                    activityController.onGraphTimeRangeChanged(range)
                                },
                                onToggleSeries = { showCal, showEaten, showBurn ->
                                    activityController.onToggleSeries(showCal, showEaten, showBurn)
                                },
                                onBottomTabSelected = { tabName ->
                                    when (tabName) {
                                        "Goal" -> drawerController.onMenuItemClicked("edit_goal", "Edit Goal")
                                        "Diet Plan" -> drawerController.onMenuItemClicked("diet_plan", "Diet Plan")
                                        else -> activityController.onBottomTabSelected(tabName)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
