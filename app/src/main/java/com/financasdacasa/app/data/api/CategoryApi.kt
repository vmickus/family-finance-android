package com.financasdacasa.app.data.api

import com.financasdacasa.app.data.model.Category
import com.financasdacasa.app.data.model.CreateCategoryRequest
import com.financasdacasa.app.data.model.ReorderCategoriesRequest
import com.financasdacasa.app.data.model.UpdateCategoryRequest
import retrofit2.http.*

interface CategoryApi {
    @GET("categories")
    suspend fun list(
        @Query("house_id") houseId: String,
        @Query("type") type: String? = null,
    ): List<Category>

    @POST("categories")
    suspend fun create(@Body body: CreateCategoryRequest): Category

    @PUT("categories/{id}")
    suspend fun update(@Path("id") id: String, @Body body: UpdateCategoryRequest): Category

    @DELETE("categories/{id}")
    suspend fun delete(@Path("id") id: String)

    @PUT("categories/reorder")
    suspend fun reorder(@Body body: ReorderCategoriesRequest)
}
