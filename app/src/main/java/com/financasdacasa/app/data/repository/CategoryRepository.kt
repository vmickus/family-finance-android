package com.financasdacasa.app.data.repository

import com.financasdacasa.app.data.api.CategoryApi
import com.financasdacasa.app.data.local.dao.CategoryDao
import com.financasdacasa.app.data.local.mapper.toDomain
import com.financasdacasa.app.data.local.mapper.toEntity
import com.financasdacasa.app.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val api: CategoryApi,
    private val categoryDao: CategoryDao,
) {
    suspend fun list(houseId: String, type: String? = null): List<Category> {
        return try {
            val fresh = api.list(houseId, type)
            if (type != null) {
                categoryDao.replaceByHouseAndType(houseId, type, fresh.map { it.toEntity() })
            } else {
                categoryDao.replaceByHouse(houseId, fresh.map { it.toEntity() })
            }
            fresh
        } catch (e: Exception) {
            val cached = if (type != null) {
                categoryDao.getByHouseAndType(houseId, type)
            } else {
                categoryDao.getByHouse(houseId)
            }
            if (cached.isNotEmpty()) cached.map { it.toDomain() }
            else throw e
        }
    }

    suspend fun create(request: CreateCategoryRequest): Category =
        api.create(request)

    suspend fun update(id: String, request: UpdateCategoryRequest): Category =
        api.update(id, request)

    suspend fun delete(id: String) = api.delete(id)

    suspend fun reorder(request: ReorderCategoriesRequest) = api.reorder(request)
}
