package com.gym.model.member

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Room KMP Data Access Object (DAO) for Member database operations.
 */
@Dao
interface MemberDao {

    @Query("SELECT * FROM members ORDER BY fullName ASC")
    suspend fun getAllMembers(): List<MemberEntity>

    @Query("SELECT * FROM members WHERE status = :statusName ORDER BY fullName ASC")
    suspend fun getMembersByStatus(statusName: String): List<MemberEntity>

    @Query("UPDATE members SET status = :newStatus WHERE id = :memberId")
    suspend fun updateMemberStatus(memberId: String, newStatus: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMember(member: MemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<MemberEntity>)

    @Query("DELETE FROM members WHERE id = :memberId")
    suspend fun deleteMember(memberId: String)
}
