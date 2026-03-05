package com.financasdacasa.app.data.repository

import com.financasdacasa.app.data.api.CategoryApi
import com.financasdacasa.app.data.model.Category
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val api: CategoryApi,
) {
    suspend fun list(houseId: String, type: String? = null): List<Category> =
        api.list(houseId, type)
}
