package com.financasdacasa.app.data.repository

import com.financasdacasa.app.data.api.CategoryApi
import com.financasdacasa.app.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val api: CategoryApi,
) {
    suspend fun list(houseId: String, type: String? = null): List<Category> =
        api.list(houseId, type)

    suspend fun create(request: CreateCategoryRequest): Category =
        api.create(request)

    suspend fun update(id: String, request: UpdateCategoryRequest): Category =
        api.update(id, request)

    suspend fun delete(id: String) = api.delete(id)

    suspend fun reorder(request: ReorderCategoriesRequest) = api.reorder(request)
}
