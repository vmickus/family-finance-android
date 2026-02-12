package com.financasdacasa.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: String,
    val email: String,
    val name: String,
    @SerializedName("created_at") val createdAt: String
)

data class AuthResponse(
    val token: String,
    val user: User
)

data class Family(
    val id: String,
    val name: String,
    @SerializedName("owner_id") val ownerId: String,
    val owner: User? = null,
    @SerializedName("created_at") val createdAt: String
)

data class FamilyMember(
    val id: String,
    @SerializedName("family_id") val familyId: String,
    @SerializedName("user_id") val userId: String,
    val user: User? = null,
    val role: String,
    @SerializedName("joined_at") val joinedAt: String
)

data class Category(
    val id: String,
    @SerializedName("family_id") val familyId: String,
    val name: String,
    val color: String
)

data class Transaction(
    val id: String,
    @SerializedName("family_id") val familyId: String,
    @SerializedName("user_id") val userId: String,
    val user: User? = null,
    @SerializedName("category_id") val categoryId: String,
    val category: Category? = null,
    val type: String,
    val amount: String,
    val description: String,
    @SerializedName("transaction_date") val transactionDate: String,
    @SerializedName("created_at") val createdAt: String
)

data class TransactionSummary(
    @SerializedName("total_income") val totalIncome: String,
    @SerializedName("total_expense") val totalExpense: String,
    val balance: String,
    val month: Int,
    val year: Int
)

// Request models
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
)

data class CreateFamilyRequest(
    val name: String
)

data class CreateCategoryRequest(
    @SerializedName("family_id") val familyId: String,
    val name: String,
    val color: String = "#6366f1"
)

data class CreateTransactionRequest(
    @SerializedName("family_id") val familyId: String,
    @SerializedName("category_id") val categoryId: String,
    val type: String,
    val amount: Double,
    val description: String = "",
    @SerializedName("transaction_date") val transactionDate: String
)

data class InviteRequest(
    val email: String
)

data class InviteResponse(
    val token: String,
    @SerializedName("expires_at") val expiresAt: String
)

data class JoinRequest(
    val token: String
)

data class ErrorResponse(
    val error: String
)
