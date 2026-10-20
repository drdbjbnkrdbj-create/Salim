package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SalimDao {

    // --- Opening Balances ---
    @Query("SELECT * FROM opening_balances")
    fun getAllOpeningBalances(): Flow<List<OpeningBalanceEntity>>

    @Query("SELECT * FROM opening_balances")
    suspend fun getAllOpeningBalancesSync(): List<OpeningBalanceEntity>

    @Query("SELECT * FROM opening_balances WHERE currency = :currency LIMIT 1")
    suspend fun getOpeningBalance(currency: String): OpeningBalanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setOpeningBalance(balance: OpeningBalanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpeningBalances(balances: List<OpeningBalanceEntity>)

    // --- Transactions ---
    @Query("SELECT * FROM transactions ORDER BY dateTimestamp DESC, id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY dateTimestamp DESC, id DESC")
    suspend fun getAllTransactionsSync(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE currency = :currency ORDER BY dateTimestamp DESC, id DESC")
    fun getTransactionsByCurrency(currency: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE debtId = :debtId")
    suspend fun getTransactionsByDebtId(debtId: Long): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions WHERE debtId = :debtId")
    suspend fun deleteTransactionsByDebtId(debtId: Long)

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()

    // --- Debts ---
    @Query("SELECT * FROM debts ORDER BY createdAt DESC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts ORDER BY createdAt DESC")
    suspend fun getAllDebtsSync(): List<DebtEntity>

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    suspend fun getDebtById(id: Long): DebtEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebts(debts: List<DebtEntity>)

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun deleteDebtById(id: Long)

    @Query("DELETE FROM debts")
    suspend fun clearAllDebts()

    // --- Debt Payments ---
    @Query("SELECT * FROM debt_payments ORDER BY paymentDate DESC")
    fun getAllDebtPayments(): Flow<List<DebtPaymentEntity>>

    @Query("SELECT * FROM debt_payments ORDER BY paymentDate DESC")
    suspend fun getAllDebtPaymentsSync(): List<DebtPaymentEntity>

    @Query("SELECT * FROM debt_payments WHERE debtId = :debtId ORDER BY paymentDate DESC")
    fun getPaymentsForDebt(debtId: Long): Flow<List<DebtPaymentEntity>>

    @Query("SELECT * FROM debt_payments WHERE debtId = :debtId ORDER BY paymentDate DESC")
    suspend fun getPaymentsForDebtSync(debtId: Long): List<DebtPaymentEntity>

    @Query("SELECT * FROM debt_payments WHERE id = :id LIMIT 1")
    suspend fun getDebtPaymentById(id: Long): DebtPaymentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtPayment(payment: DebtPaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtPayments(payments: List<DebtPaymentEntity>)

    @Query("DELETE FROM debt_payments WHERE id = :id")
    suspend fun deleteDebtPaymentById(id: Long)

    @Query("DELETE FROM debt_payments WHERE debtId = :debtId")
    suspend fun deletePaymentsByDebtId(debtId: Long)

    @Query("DELETE FROM debt_payments")
    suspend fun clearAllDebtPayments()

    // --- Settings ---
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getAppSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getAppSettingsSync(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAppSettings(settings: AppSettingsEntity)

    @Query("DELETE FROM opening_balances")
    suspend fun clearAllOpeningBalances()
}
