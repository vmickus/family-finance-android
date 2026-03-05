package com.financasdacasa.app.data.api

import com.financasdacasa.app.data.model.Category
import retrofit2.http.GET
import retrofit2.http.Query

interface CategoryApi {
    @GET("categories")
    suspend fun list(
        @Query("house_id") houseId: String,
        @Query("type") type: String? = null,
    ): List<Category>
}
