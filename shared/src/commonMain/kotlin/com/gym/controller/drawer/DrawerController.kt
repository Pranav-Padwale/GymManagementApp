package com.gym.controller.drawer

import com.gym.model.drawer.DrawerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private fun loadDrawerData() {
        scope.launch {
            val pResult = repository.getUserProfile()
            val mResult = repository.getMenuItems()

            _uiState.update {
                it.copy(
                    profile = pResult.getOrDefault(it.profile),
                    menuItems = mResult.getOrDefault(emptyList())
                )
            }
        }
    }
}
