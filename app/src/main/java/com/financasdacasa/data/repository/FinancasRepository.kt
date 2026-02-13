package com.financasdacasa.data.repository

import com.financasdacasa.data.api.ApiService
import com.financasdacasa.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinancasRepository @Inject constructor(
    private val apiService: ApiService
) {
    // Families
    suspend fun listFamilies(): Result<List<Family>> {
        return try {
            val response = apiService.listFamilies()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Erro ao carregar famílias"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createFamily(name: String): Result<Family> {
        return try {
            val response = apiService.createFamily(CreateFamilyRequest(name))
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Resposta vazia"))
            } else {
                Result.failure(Exception("Erro ao criar família"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFamily(id: String): Result<Family> {
        return try {
            val response = apiService.getFamily(id)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Resposta vazia"))
            } else {
                Result.failure(Exception("Família não encontrada"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinFamily(familyId: String, token: String): Result<Unit> {
        return try {
            val response = apiService.joinFamily(familyId, JoinRequest(token))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Token inválido ou expirado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Categories
    suspend fun getCategories(familyId: String): Result<List<Category>> {
        return try {
            val response = apiService.getCategories(familyId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Erro ao carregar categorias"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCategory(familyId: String, name: String, color: String): Result<Category> {
        return try {
            val response = apiService.createCategory(CreateCategoryRequest(familyId, name, color))
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Resposta vazia"))
            } else {
                Result.failure(Exception("Erro ao criar categoria"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Transactions
    suspend fun getTransactions(familyId: String, month: Int, year: Int): Result<List<Transaction>> {
        return try {
            val response = apiService.getTransactions(familyId, month, year)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Erro ao carregar transações"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTransaction(
        familyId: String,
        categoryId: String,
        type: String,
        amount: Double,
        description: String,
        transactionDate: String
    ): Result<Transaction> {
        return try {
            val response = apiService.createTransaction(
                CreateTransactionRequest(
                    familyId = familyId,
                    categoryId = categoryId,
                    type = type,
                    amount = amount,
                    description = description,
                    transactionDate = transactionDate
                )
            )
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Resposta vazia"))
            } else {
                Result.failure(Exception("Erro ao criar transação"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteTransaction(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erro ao excluir transação"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransactionSummary(familyId: String, month: Int, year: Int): Result<TransactionSummary> {
        return try {
            val response = apiService.getTransactionSummary(familyId, month, year)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Resposta vazia"))
            } else {
                Result.failure(Exception("Erro ao carregar resumo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
