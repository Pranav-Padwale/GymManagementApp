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
import com.gym.model.activity.ActivityRepositoryImpl
import com.gym.model.diet.DietPlanRepositoryImpl
import com.gym.model.drawer.DrawerRepositoryImpl
import com.gym.model.finance.FinancialRepositoryImpl
import com.gym.model.goal.GoalRepositoryImpl
import com.gym.model.member.MemberRepositoryImpl
import com.gym.view.activity.ActivityDashboardScreen
import com.gym.view.diet.DietPlanScreen
import com.gym.view.drawer.HamburgerSideDrawer
import com.gym.view.finance.FinanceDashboardScreen
import com.gym.view.goal.EditGoalScreen
import com.gym.view.member.MemberManagementScreen
import com.gym.view.theme.GymDarkBackground
import com.gym.view.theme.GymTheme

/**
 * Main Application Entry Point binding Model, Controller, and View components.
 * Supports navigation between Fitness Activity, Diet Plan, Edit Goal, Member Status, and Balance Sheet.
 */
@Composable
fun App() {
    // Model Initialization
    val drawerRepository = remember { DrawerRepositoryImpl() }
    val activityRepository = remember { ActivityRepositoryImpl() }
    val dietPlanRepository = remember { DietPlanRepositoryImpl() }
    val goalRepository = remember { GoalRepositoryImpl() }
    val memberRepository = remember { MemberRepositoryImpl() }
    val financialRepository = remember { FinancialRepositoryImpl() }

    // Controller Initialization (Standard Kotlin Controllers)
    val drawerController = remember { DrawerController(repository = drawerRepository) }
    val activityController = remember { ActivityController(repository = activityRepository) }
    val dietPlanController = remember { DietPlanController(repository = dietPlanRepository) }
    val goalController = remember { GoalController(repository = goalRepository) }
    val memberController = remember { MemberManagementController(repository = memberRepository) }
    val financeController = remember { FinanceController(repository = financialRepository) }

    // State Collectors
    val drawerState by drawerController.uiState.collectAsState()
    val activityState by activityController.uiState.collectAsState()
    val dietPlanState by dietPlanController.uiState.collectAsState()
    val goalState by goalController.uiState.collectAsState()
    val memberState by memberController.uiState.collectAsState()
    val financeState by financeController.uiState.collectAsState()

    GymTheme(darkTheme = drawerState.profile.isDarkMode) {
        HamburgerSideDrawer(
            state = drawerState,
            onCloseDrawer = { drawerController.onToggleDrawer(false) },
            onMenuItemClick = { id, title -> drawerController.onMenuItemClicked(id, title) },
            onToggleTheme = { drawerController.onToggleTheme() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GymDarkBackground)
            ) {
                when (drawerState.selectedMenuItemId) {
                    "fitness_activity" -> {
                        ActivityDashboardScreen(
                            state = activityState,
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
                            onRemoveFoodItem = { foodId ->
                                dietPlanController.onRemoveFoodItem(foodId)
                            }
                        )
                    }
                    "edit_goal", "gym_plan" -> {
                        // Passive View receiving state & emitting events to GoalController
                        EditGoalScreen(
                            state = goalState,
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
                            onRefresh = {
                                memberController.refreshMembers()
                            }
                        )
                    }
                    "balance_sheet" -> {
                        FinanceDashboardScreen(
                            state = financeState,
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
                    else -> {
                        ActivityDashboardScreen(
                            state = activityState,
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
