package com.gym.model.member

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * Model Layer: MemberRepository Contract
 */
interface MemberRepository {
    suspend fun getMembers(statusFilter: MemberStatus?): Result<List<Member>>
    suspend fun updateMemberStatus(memberId: String, newStatus: MemberStatus): Result<Unit>
    suspend fun addMember(member: Member): Result<Unit>
}

/**
 * Model Layer: Room KMP implementation of MemberRepository
 */
class MemberRepositoryImpl(
    private val memberDao: MemberDao? = null
) : MemberRepository {

    // In-memory backing fallback for demonstration / testing when DB is initializing
    private val mockDb = mutableListOf(
        Member("M-101", "Alex Rivers", "alex@example.com", "+1 555-0192", MemberStatus.ACTIVE, "Gold Annual", "2025-01-15", "2026-01-15"),
        Member("M-102", "Sarah Jenkins", "sarah.j@example.com", "+1 555-0183", MemberStatus.ENQUIRE, "Trial Week", "2026-07-20", "2026-07-27"),
        Member("M-103", "Marcus Vance", "marcus@vance.io", "+1 555-0174", MemberStatus.EXPIRED, "Silver Monthly", "2025-05-10", "2026-06-10"),
        Member("M-104", "Elena Rostova", "elena.r@example.com", "+1 555-0165", MemberStatus.ACTIVE, "Platinum VIP", "2024-11-01", "2026-11-01"),
        Member("M-105", "David Kim", "dkim@tech.com", "+1 555-0156", MemberStatus.INACTIVE, "Basic Monthly", "2025-02-14", "2025-08-14"),
        Member("M-106", "Chloe Bennett", "chloe.b@example.com", "+1 555-0147", MemberStatus.ENQUIRE, "Lead / Consultation", "2026-07-24", "2026-07-31"),
        Member("M-107", "James Wilson", "jwilson@fitnet.com", "+1 555-0138", MemberStatus.EXPIRED, "Gold Quarterly", "2025-10-05", "2026-01-05")
    )

    override suspend fun getMembers(statusFilter: MemberStatus?): Result<List<Member>> = withContext(Dispatchers.IO) {
        try {
            if (memberDao != null) {
                val entities = if (statusFilter == null) {
                    memberDao.getAllMembers()
                } else {
                    memberDao.getMembersByStatus(statusFilter.name)
                }
                if (entities.isEmpty() && statusFilter == null) {
                    // Seed initial demo data
                    memberDao.insertAll(mockDb.map { MemberEntity.fromDomain(it) })
                    Result.success(mockDb)
                } else {
                    Result.success(entities.map { it.toDomain() })
                }
            } else {
                // Fallback in-memory behavior
                val resultList = if (statusFilter == null) {
                    mockDb.toList()
                } else {
                    mockDb.filter { it.status == statusFilter }
                }
                Result.success(resultList)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMemberStatus(memberId: String, newStatus: MemberStatus): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (memberDao != null) {
                memberDao.updateMemberStatus(memberId, newStatus.name)
            }
            // Update in-memory cache as well
            val index = mockDb.indexOfFirst { it.id == memberId }
            if (index != -1) {
                mockDb[index] = mockDb[index].copy(status = newStatus)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addMember(member: Member): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (memberDao != null) {
                memberDao.insertOrUpdateMember(MemberEntity.fromDomain(member))
            }
            mockDb.add(member)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
