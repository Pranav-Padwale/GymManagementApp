package com.gym.model.drawer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * Model Layer: DrawerRepository contract.
 */
interface DrawerRepository {
    suspend fun getUserProfile(): Result<UserProfile>
    suspend fun getMenuItems(): Result<List<DrawerMenuItem>>
    suspend fun toggleDarkMode(): Result<Boolean>
}

/**
 * Model Layer: DrawerRepository implementation.
 */
class DrawerRepositoryImpl : DrawerRepository {

    private var profile = UserProfile()

    private val menuItems = listOf(
        // Primary Core Modules
        DrawerMenuItem("fitness_activity", "Fitness Activity", "TrackChanges"),
        DrawerMenuItem("member_status", "Member Status Directory", "People"),
        DrawerMenuItem("balance_sheet", "Income & Expense Sheet", "AccountBalance"),

        // Additional App Sections
        DrawerMenuItem("gym_plan", "Gym Plan", "Shield"),
        DrawerMenuItem("health_data", "Health Data", "ShowChart"),
        DrawerMenuItem("active_x", "ActiveX", "FitnessCenter"),
        DrawerMenuItem("review_rating", "Review/Rating", "RateReview"),
        DrawerMenuItem("store", "Store", "Storefront"),
        DrawerMenuItem("time_slot", "Time Slot", "Schedule"),
        DrawerMenuItem("meetings", "Upcoming Meetings", "EventAvailable"),
        DrawerMenuItem("invite", "Invite a Friend", "Share"),
        DrawerMenuItem("logout", "Logout", "Logout")
    )

    override suspend fun getUserProfile(): Result<UserProfile> = withContext(Dispatchers.IO) {
        Result.success(profile)
    }

    override suspend fun getMenuItems(): Result<List<DrawerMenuItem>> = withContext(Dispatchers.IO) {
        Result.success(menuItems)
    }

    override suspend fun toggleDarkMode(): Result<Boolean> = withContext(Dispatchers.IO) {
        profile = profile.copy(isDarkMode = !profile.isDarkMode)
        Result.success(profile.isDarkMode)
    }
}
