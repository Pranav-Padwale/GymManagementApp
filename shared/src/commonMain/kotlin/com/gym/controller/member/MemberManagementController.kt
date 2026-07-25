package com.gym.controller.member

import com.gym.model.member.MemberRepository
import com.gym.model.member.MemberStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Controller: Dedicated Kotlin class for Member Management MVC.
 * Acts as glue between the View and Model layer without Android ViewModel or MVI pipelines.
 */
class MemberManagementController(
    private val repository: MemberRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {

    private val _uiState = MutableStateFlow(MemberUiState())
    val uiState: StateFlow<MemberUiState> = _uiState.asStateFlow()

    init {
        // Initial data fetch
        refreshMembers()
    }

    /**
     * User action handler: Filter members by status.
     * Null status signifies 'ALL'.
     */
    fun onFilterStatusChanged(status: MemberStatus?) {
        _uiState.update { it.copy(selectedFilter = status, isLoading = true) }
        fetchMembersForFilter(status)
    }

    /**
     * User action handler: Update member status in database and refresh state.
     */
    fun onUpdateMemberStatus(memberId: String, newStatus: MemberStatus) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val updateResult = repository.updateMemberStatus(memberId, newStatus)
            updateResult.fold(
                onSuccess = {
                    // Refresh state after successful update
                    fetchMembersForFilter(_uiState.value.selectedFilter)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to update status: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    /**
     * Refreshes member list based on current active filter.
     */
    fun refreshMembers() {
        fetchMembersForFilter(_uiState.value.selectedFilter)
    }

    private fun fetchMembersForFilter(statusFilter: MemberStatus?) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.getMembers(statusFilter)
            result.fold(
                onSuccess = { memberList ->
                    // Calculate summary counts from full set if needed
                    val allMembersResult = if (statusFilter == null) result else repository.getMembers(null)
                    val allMembers = allMembersResult.getOrDefault(memberList)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            members = memberList,
                            totalCount = allMembers.size,
                            activeCount = allMembers.count { m -> m.status == MemberStatus.ACTIVE },
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Database fetch error: ${error.message}"
                        )
                    }
                }
            )
        }
    }
}
