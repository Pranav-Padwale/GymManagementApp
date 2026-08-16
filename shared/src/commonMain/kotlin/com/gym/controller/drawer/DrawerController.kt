package com.gym.controller.drawer

import com.gym.model.drawer.DrawerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.gym.model.auth.UserRole
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Controller: Dedicated Kotlin class for Hamburger Side Menu Drawer.
 * Manages drawer open/close state, profile theme toggles, and item click callbacks.
 */
class DrawerController(
    private val repository: DrawerRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {

    private val _uiState = MutableStateFlow(DrawerUiState())
    val uiState: StateFlow<DrawerUiState> = _uiState.asStateFlow()

    private var currentRole: UserRole? = null
    private var allMenuItems: List<com.gym.model.drawer.DrawerMenuItem> = emptyList()

    init {
        loadDrawerData()
    }

    /**
     * Open or close drawer drawer state.
     */
    fun onToggleDrawer(isOpen: Boolean) {
        _uiState.update { it.copy(isOpen = isOpen) }
    }

    /**
     * User clicks a menu item inside drawer.
     */
    fun onMenuItemClicked(itemId: String, itemTitle: String) {
        _uiState.update {
            it.copy(
                selectedMenuItemId = itemId,
                isOpen = false,
                activeSnackbarMessage = "Navigated to $itemTitle"
            )
        }
    }

    /**
     * Toggle dark/light theme icon.
     */
    fun onToggleTheme() {
        scope.launch {
            val result = repository.toggleDarkMode()
            result.onSuccess { isDark ->
                _uiState.update {
                    it.copy(profile = it.profile.copy(isDarkMode = isDark))
                }
            }
        }
    }

    /**
     * Clear snackbar notification message.
     */
    fun onClearSnackbar() {
        _uiState.update { it.copy(activeSnackbarMessage = null) }
    }

    fun setRole(role: UserRole) {
        currentRole = role
        filterMenuItems()
        
        // Also route them to the best default page based on role
        if (role == UserRole.MEMBER) {
            onMenuItemClicked("time_slot", "Time Slots")
        }
    }

    fun updateProfileName(username: String) {
        _uiState.update {
            it.copy(profile = it.profile.copy(name = username))
        }
    }

    private fun filterMenuItems() {
        val role = currentRole ?: return
        
        val filtered = allMenuItems.filter { item ->
            when (role) {
                UserRole.SUPER_ADMIN -> true // Admin sees everything
                UserRole.TRAINER -> {
                    // Trainer cannot see finances
                    item.id != "balance_sheet"
                }
                UserRole.MEMBER -> {
                    // Member can only see profile/dashboard, time slots, and diet
                    item.id in listOf("fitness_activity", "diet_plan", "time_slot")
                }
            }
        }
        
        _uiState.update { it.copy(menuItems = filtered) }
    }

    private fun loadDrawerData() {
        scope.launch {
            val pResult = repository.getUserProfile()
            val mResult = repository.getMenuItems()

            allMenuItems = mResult.getOrDefault(emptyList())

            _uiState.update {
                it.copy(
                    profile = pResult.getOrDefault(it.profile),
                    menuItems = allMenuItems // initially show all, will be filtered after login
                )
            }
        }
    }
}
