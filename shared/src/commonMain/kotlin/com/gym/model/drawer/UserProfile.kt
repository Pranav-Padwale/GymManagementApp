package com.gym.model.drawer

/**
 * Domain model representing member profile details in the Hamburger Side Drawer.
 */
data class UserProfile(
    val name: String = "pradnya",
    val phone: String = "+91 8010129371",
    val isDarkMode: Boolean = true
)

/**
 * Drawer Menu Item representation.
 */
data class DrawerMenuItem(
    val id: String,
    val title: String,
    val iconName: String
)
