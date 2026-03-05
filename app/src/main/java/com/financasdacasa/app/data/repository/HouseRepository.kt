package com.financasdacasa.app.data.repository

import com.financasdacasa.app.data.api.HouseApi
import com.financasdacasa.app.data.model.House
import com.financasdacasa.app.data.model.HouseMember
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HouseRepository @Inject constructor(
    private val houseApi: HouseApi,
) {
    suspend fun list(): List<House> = houseApi.list()

    suspend fun create(name: String): House {
        return houseApi.create(mapOf("name" to name, "category_tier" to 2))
    }

    suspend fun acceptInvite(token: String): House {
        return houseApi.acceptInvite(token)
    }

    suspend fun getMembers(houseId: String): List<HouseMember> = houseApi.getMembers(houseId)
}
