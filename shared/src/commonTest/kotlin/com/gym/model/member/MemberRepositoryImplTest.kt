package com.gym.model.member

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MemberRepositoryImplTest {

    @Test
    fun getMembers_noFilter_returnsAllSeededMembers() = runTest {
        val repo = MemberRepositoryImpl()

        val members = repo.getMembers(null).getOrThrow()

        assertEquals(7, members.size)
    }

    @Test
    fun getMembers_filteredByStatus_onlyReturnsMatchingMembers() = runTest {
        val repo = MemberRepositoryImpl()

        val active = repo.getMembers(MemberStatus.ACTIVE).getOrThrow()

        assertTrue(active.isNotEmpty())
        assertTrue(active.all { it.status == MemberStatus.ACTIVE })
    }

    @Test
    fun getMembers_eachStatusFilter_isMutuallyExclusiveAndCoversAllMembers() = runTest {
        val repo = MemberRepositoryImpl()
        val all = repo.getMembers(null).getOrThrow()

        val byStatus = MemberStatus.entries.associateWith { status ->
            repo.getMembers(status).getOrThrow()
        }

        val totalAcrossFilters = byStatus.values.sumOf { it.size }
        assertEquals(all.size, totalAcrossFilters, "Every member must fall into exactly one status bucket")
    }

    @Test
    fun updateMemberStatus_existingMember_updatesAndIsVisibleOnNextFetch() = runTest {
        val repo = MemberRepositoryImpl()

        val result = repo.updateMemberStatus("M-102", MemberStatus.ACTIVE)

        assertTrue(result.isSuccess)
        val updated = repo.getMembers(null).getOrThrow().first { it.id == "M-102" }
        assertEquals(MemberStatus.ACTIVE, updated.status)
    }

    @Test
    fun updateMemberStatus_unknownMemberId_returnsFailureInsteadOfSilentlySucceeding() = runTest {
        // Regression test for MEMBER-1 (see QA report): updateMemberStatus used to return
        // Result.success(Unit) even when no member matched memberId, silently discarding
        // the write. It must now surface a failure so callers/UI can report the error.
        val repo = MemberRepositoryImpl()

        val result = repo.updateMemberStatus("M-DOES-NOT-EXIST", MemberStatus.EXPIRED)

        assertTrue(result.isFailure)
    }

    @Test
    fun addMember_newMember_isReturnedByLaterGetMembersCall() = runTest {
        val repo = MemberRepositoryImpl()
        val newMember = Member(
            id = "M-NEW-1", fullName = "Taylor QA", email = "taylor@example.com",
            phone = "+1 555-0000", status = MemberStatus.ENQUIRE,
            membershipPlan = "Trial", joinDate = "2026-09-01", expiryDate = "2026-09-08"
        )

        val result = repo.addMember(newMember)

        assertTrue(result.isSuccess)
        val all = repo.getMembers(null).getOrThrow()
        assertTrue(all.any { it.id == "M-NEW-1" })
    }
}
