package com.financasdacasa.app.data.repository

import com.financasdacasa.app.data.api.HouseApi
import com.financasdacasa.app.data.local.dao.HouseDao
import com.financasdacasa.app.data.local.mapper.toDomain
import com.financasdacasa.app.data.local.mapper.toEntity
import com.financasdacasa.app.data.model.House
import com.financasdacasa.app.data.model.HouseMember
import com.financasdacasa.app.data.model.InviteDetails
import com.financasdacasa.app.data.model.InviteResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HouseRepository @Inject constructor(
    private val houseApi: HouseApi,
    private val houseDao: HouseDao,
) {
    suspend fun list(): List<House> {
        return try {
            val fresh = houseApi.list()
            houseDao.replaceAll(fresh.map { it.toEntity() })
            fresh
        } catch (e: Exception) {
            val cached = houseDao.getAll()
            if (cached.isNotEmpty()) cached.map { it.toDomain() }
            else throw e
        }
    }

    suspend fun create(name: String): House {
        return houseApi.create(mapOf("name" to name, "category_tier" to 2))
    }

    suspend fun acceptInvite(token: String): House {
        return houseApi.acceptInvite(token)
    }

    suspend fun getMembers(houseId: String): List<HouseMember> = houseApi.getMembers(houseId)

    suspend fun invite(houseId: String): InviteResponse {
        return houseApi.invite(houseId, mapOf("email" to ""))
    }

    suspend fun removeMember(houseId: String, userId: String) {
        houseApi.removeMember(houseId, userId)
    }

    suspend fun rename(houseId: String, name: String): House {
        return houseApi.rename(houseId, mapOf("name" to name))
    }

    suspend fun getInviteDetails(token: String): InviteDetails {
        return houseApi.getInviteDetails(token)
    }
}
