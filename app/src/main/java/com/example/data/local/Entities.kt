package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "opening_balances")
data class OpeningBalanceEntity(
    @PrimaryKey
    val currency: String,
    val amountInCents: Long,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val type: String, // TransactionType.name
    val amountInCents: Long,
    val currency: String,
    val category: String,
    val dateTimestamp: Long,
    val notes: String? = null,
    val debtId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val personName: String,
    val originalAmountInCents: Long,
    val currency: String,
    val type: String, // DebtType.name
    val startDate: Long,
    val dueDate: Long? = null,
    val notes: String? = null,
    val isCashTransferredInitially: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "debt_payments")
data class DebtPaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val debtId: Long,
    val amountInCents: Long,
    val paymentDate: Long,
    val notes: String? = null,
    val transactionId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val isInitialSetupDone: Boolean = false,
    val defaultCurrency: String = "YER",
    val appLanguage: String = "ar"
)
