package com.gym.controller.member

import com.gym.model.member.Member
import com.gym.model.member.MemberStatus

/**
 * Immutable State object passed from Controller to View.
 */
data class MemberUiState(
    val selectedFilter: MemberStatus? = null,
    val members: List<Member> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val totalCount: Int = 0,
    val activeCount: Int = 0
)
