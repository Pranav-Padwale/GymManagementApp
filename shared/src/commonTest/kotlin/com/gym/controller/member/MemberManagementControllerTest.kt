package com.gym.controller.member

import com.gym.model.member.Member
import com.gym.model.member.MemberRepository
import com.gym.model.member.MemberStatus
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeMemberRepository(seed: List<Member> = emptyList()) : MemberRepository {
    var shouldFail = false
    val members = seed.toMutableList()

    override suspend fun getMembers(statusFilter: MemberStatus?): Result<List<Member>> =
        if (shouldFail) Result.failure(Exception("simulated connection drop"))
        else Result.success(if (statusFilter == null) members.toList() else members.filter { it.status == statusFilter })

    override suspend fun updateMemberStatus(memberId: String, newStatus: MemberStatus): Result<Unit> {
        if (shouldFail) return Result.failure(Exception("simulated write failure"))
        val idx = members.indexOfFirst { it.id == memberId }
        if (idx == -1) return Result.failure(NoSuchElementException("No member: $memberId"))
        members[idx] = members[idx].copy(status = newStatus)
        return Result.success(Unit)
    }

    override suspend fun addMember(member: Member): Result<Unit> {
        if (shouldFail) return Result.failure(Exception("simulated write failure"))
        members.add(member)
        return Result.success(Unit)
    }
}

private fun sampleMember(id: String, status: MemberStatus) = Member(
    id = id, fullName = "Member $id", email = "$id@example.com", phone = "+1 555-0000",
    status = status, membershipPlan = "Gold", joinDate = "2026-01-01", expiryDate = "2027-01-01"
)

class MemberManagementControllerTest {

    private fun newController(repo: MemberRepository): MemberManagementController {
        val scope = TestScope(UnconfinedTestDispatcher())
        return MemberManagementController(repository = repo, scope = scope)
    }

    @Test
    fun refreshMembers_onInit_populatesTotalAndActiveCounts() = runTest {
        val repo = FakeMemberRepository(
            listOf(
                sampleMember("M-1", MemberStatus.ACTIVE),
                sampleMember("M-2", MemberStatus.ACTIVE),
                sampleMember("M-3", MemberStatus.EXPIRED)
            )
        )
        val controller = newController(repo)

        val state = controller.uiState.value
        assertEquals(3, state.totalCount)
        assertEquals(2, state.activeCount)
        assertTrue(!state.isLoading)
    }

    @Test
    fun onFilterStatusChanged_narrowsVisibleMembersButKeepsGlobalCountsFromFullSet() = runTest {
        val repo = FakeMemberRepository(
            listOf(
                sampleMember("M-1", MemberStatus.ACTIVE),
                sampleMember("M-2", MemberStatus.EXPIRED)
            )
        )
        val controller = newController(repo)

        controller.onFilterStatusChanged(MemberStatus.EXPIRED)

        val state = controller.uiState.value
        assertEquals(1, state.members.size)
        assertEquals(MemberStatus.EXPIRED, state.members.first().status)
        // Global totals must still reflect the whole roster, not just the filtered view.
        assertEquals(2, state.totalCount)
        assertEquals(1, state.activeCount)
    }

    @Test
    fun onUpdateMemberStatus_success_refreshesListWithNewStatus() = runTest {
        val repo = FakeMemberRepository(listOf(sampleMember("M-1", MemberStatus.ENQUIRE)))
        val controller = newController(repo)

        controller.onUpdateMemberStatus("M-1", MemberStatus.ACTIVE)

        val updated = controller.uiState.value.members.first { it.id == "M-1" }
        assertEquals(MemberStatus.ACTIVE, updated.status)
        assertNull(controller.uiState.value.errorMessage)
    }

    @Test
    fun onUpdateMemberStatus_unknownMemberId_surfacesErrorMessageInsteadOfSilentlySucceeding() = runTest {
        val repo = FakeMemberRepository(listOf(sampleMember("M-1", MemberStatus.ENQUIRE)))
        val controller = newController(repo)

        controller.onUpdateMemberStatus("M-DOES-NOT-EXIST", MemberStatus.ACTIVE)

        assertNotNull(controller.uiState.value.errorMessage)
    }

    @Test
    fun refreshMembers_repositoryFailure_surfacesErrorAndStopsLoading() = runTest {
        val repo = FakeMemberRepository().apply { shouldFail = true }
        val controller = newController(repo)

        val state = controller.uiState.value
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage!!.contains("simulated connection drop"))
        assertTrue(!state.isLoading)
    }

    @Test
    fun onAddMember_success_appearsInSubsequentMemberList() = runTest {
        val repo = FakeMemberRepository()
        val controller = newController(repo)

        controller.onAddMember(sampleMember("M-NEW", MemberStatus.ENQUIRE))

        assertTrue(controller.uiState.value.members.any { it.id == "M-NEW" })
    }
}
