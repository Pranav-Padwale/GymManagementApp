package com.gym.api

import com.gym.model.member.Member
import com.gym.model.member.MemberRepository
import com.gym.model.member.MemberStatus
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MemberRepositoryHttp : MemberRepository {
    var authToken: String? = null

    override suspend fun getMembers(statusFilter: MemberStatus?): Result<List<Member>> = withContext(Dispatchers.Default) {
        try {
            val response = HttpClientProvider.client.get("${HttpClientProvider.BASE_URL}/api/members") {
                authToken?.let { header("Authorization", "Bearer $it") }
                if (statusFilter != null) {
                    parameter("status", statusFilter.name)
                }
            }
            if (response.status.isSuccess()) {
                val allMembers: List<Member> = response.body()
                val filtered = if (statusFilter == null) allMembers else allMembers.filter { it.status == statusFilter }
                Result.success(filtered)
            } else {
                Result.failure(Exception("HTTP ${response.status}"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateMemberStatus(memberId: String, newStatus: MemberStatus): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val response = HttpClientProvider.client.patch("${HttpClientProvider.BASE_URL}/api/members/$memberId/status") {
                authToken?.let { header("Authorization", "Bearer $it") }
                contentType(ContentType.Text.Plain)
                setBody(newStatus.name)
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("HTTP ${response.status}"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun addMember(member: Member): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val response = HttpClientProvider.client.post("${HttpClientProvider.BASE_URL}/api/members") {
                contentType(ContentType.Application.Json)
                authToken?.let { header("Authorization", "Bearer $it") }
                setBody(member)
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("HTTP ${response.status}"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
