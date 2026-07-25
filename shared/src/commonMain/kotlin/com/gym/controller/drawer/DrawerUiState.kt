package com.gym.controller.drawer

import com.gym.model.drawer.DrawerMenuItem
import com.gym.model.drawer.UserProfile

/**
 * Immutable State object for Hamburger Side Drawer.
 */
data class DrawerUiState(
    val isOpen: Boolean = false,
    val profile: UserProfile = UserProfile(),
    val menuItems: List<DrawerMenuItem> = emptyList(),
    val selectedMenuItemId: String = "fitness_activity",
    val appVersion: String = "v1.0.0.53",
    val activeSnackbarMessage: String? = null
)
