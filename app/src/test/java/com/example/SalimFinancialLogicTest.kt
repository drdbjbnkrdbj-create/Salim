package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SalimDatabase
import com.example.data.local.TransactionEntity
import com.example.data.model.Currency
import com.example.data.model.DebtStatus
import com.example.data.model.DebtType
import com.example.data.model.FormatUtils
import com.example.data.model.TransactionType
import com.example.data.repository.SalimRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SalimFinancialLogicTest {

    private lateinit var database: SalimDatabase
    private lateinit var repository: SalimRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, SalimDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SalimRepository(database, database.salimDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testAppNameStringResource() {
        val appName = context.getString(R.string.app_name)
        assertEquals("سليم", appName)
    }

    @Test
    fun testInitialSetupAndThreeIndependentCurrencies() = runBlocking {
        // YER: 250,000 (25000000 cents), SAR: 1,500 (150000 cents), USD: 300 (30000 cents)
        repository.completeInitialSetup(
            yerOpeningCents = 25000000L,
            sarOpeningCents = 150000L,
            usdOpeningCents = 30000L,
            defaultCurrency = Currency.YER
        )

        val balances = repository.getCurrencyBalances().first()
        val yer = balances.first { it.currency == Currency.YER }
        val sar = balances.first { it.currency == Currency.SAR }
        val usd = balances.first { it.currency == Currency.USD }

        assertEquals(25000000L, yer.currentBalanceCents)
        assertEquals(150000L, sar.currentBalanceCents)
        assertEquals(30000L, usd.currentBalanceCents)

        // Adding income in YER must NOT affect SAR or USD
        repository.addTransaction(
            TransactionEntity(
                type = TransactionType.INCOME.name,
                amountInCents = 5000000L,
                currency = "YER",
                category = "راتب",
                dateTimestamp = System.currentTimeMillis()
            )
        )

        val updatedBalances = repository.getCurrencyBalances().first()
        val updatedYer = updatedBalances.first { it.currency == Currency.YER }
        val updatedSar = updatedBalances.first { it.currency == Currency.SAR }
        val updatedUsd = updatedBalances.first { it.currency == Currency.USD }

        assertEquals(30000000L, updatedYer.currentBalanceCents)
        assertEquals(150000L, updatedSar.currentBalanceCents)
        assertEquals(30000L, updatedUsd.currentBalanceCents)
    }

    @Test
    fun testExpenseDeduction() = runBlocking {
        repository.completeInitialSetup(100000L, 0L, 0L, Currency.YER)

        // Add expense of 40,000 cents
        repository.addTransaction(
            TransactionEntity(
                type = TransactionType.EXPENSE.name,
                amountInCents = 40000L,
                currency = "YER",
                category = "طعام",
                dateTimestamp = System.currentTimeMillis()
            )
        )

        val currentYer = repository.getCurrentBalanceSync(Currency.YER)
        assertEquals(60000L, currentYer)
    }

    @Test
    fun testDebtCreationWithoutAndWithCashMovement() = runBlocking {
        repository.completeInitialSetup(100000L, 0L, 0L, Currency.YER)

        // 1. Debt without cash movement: Cash balance must stay 100,000
        val debtId1 = repository.addDebt(
            personName = "صالح",
            amountInCents = 20000L,
            currency = Currency.YER,
            type = DebtType.OWED_TO_ME,
            startDate = System.currentTimeMillis(),
            dueDate = null,
            notes = "بدون حركة نقدية",
            isCashTransferred = false
        )
        assertEquals(100000L, repository.getCurrentBalanceSync(Currency.YER))

        // 2. Debt with cash movement (lent out to someone): Cash must drop by 30,000
        val debtId2 = repository.addDebt(
            personName = "خالد",
            amountInCents = 30000L,
            currency = Currency.YER,
            type = DebtType.OWED_TO_ME,
            startDate = System.currentTimeMillis(),
            dueDate = null,
            notes = "تم تسليم المبلغ نقداً",
            isCashTransferred = true
        )
        assertEquals(70000L, repository.getCurrentBalanceSync(Currency.YER))
    }

    @Test
    fun testPartialAndFullDebtRepayment() = runBlocking {
        repository.completeInitialSetup(50000L, 0L, 0L, Currency.YER)

        // Someone owes me 20,000
        val debtId = repository.addDebt(
            personName = "أحمد",
            amountInCents = 20000L,
            currency = Currency.YER,
            type = DebtType.OWED_TO_ME,
            startDate = System.currentTimeMillis(),
            dueDate = null,
            notes = null,
            isCashTransferred = false
        )

        // Partial repayment of 8,000
        val pay1Success = repository.recordDebtPayment(
            debtId = debtId,
            amountInCents = 8000L,
            paymentDate = System.currentTimeMillis(),
            notes = "دفعة أولى",
            updateCashBalance = true
        )
        assertTrue(pay1Success)

        // Debt status should be PARTIALLY_PAID and remaining 12,000
        val debtsAfterPay1 = repository.allDebtsWithDetails.first()
        val debtDetails1 = debtsAfterPay1.first { it.debt.id == debtId }
        assertEquals(12000L, debtDetails1.remainingCents)
        assertEquals(8000L, debtDetails1.totalPaidCents)
        assertEquals(DebtStatus.PARTIALLY_PAID, debtDetails1.status)
        // Cash increased by 8,000 (from 50,000 to 58,000)
        assertEquals(58000L, repository.getCurrentBalanceSync(Currency.YER))

        // Full remaining repayment of 12,000
        val pay2Success = repository.recordDebtPayment(
            debtId = debtId,
            amountInCents = 12000L,
            paymentDate = System.currentTimeMillis(),
            notes = "سداد نهائي",
            updateCashBalance = true
        )
        assertTrue(pay2Success)

        val debtsAfterPay2 = repository.allDebtsWithDetails.first()
        val debtDetails2 = debtsAfterPay2.first { it.debt.id == debtId }
        assertEquals(0L, debtDetails2.remainingCents)
        assertEquals(20000L, debtDetails2.totalPaidCents)
        assertEquals(DebtStatus.FULLY_PAID, debtDetails2.status)
        assertEquals(70000L, repository.getCurrentBalanceSync(Currency.YER))

        // Attempting to pay when remaining is 0 must fail
        val excessPay = repository.recordDebtPayment(
            debtId = debtId,
            amountInCents = 1000L,
            paymentDate = System.currentTimeMillis(),
            notes = null,
            updateCashBalance = true
        )
        assertFalse(excessPay)
    }

    @Test
    fun testBackupAndRestoreIntegrity() = runBlocking {
        repository.completeInitialSetup(100000L, 50000L, 20000L, Currency.SAR)
        repository.addTransaction(
            TransactionEntity(
                type = TransactionType.INCOME.name,
                amountInCents = 15000L,
                currency = "SAR",
                category = "استثمار",
                dateTimestamp = System.currentTimeMillis()
            )
        )

        // Export backup
        val jsonBackup = repository.exportBackupJson()
        assertNotNull(jsonBackup)
        assertTrue(jsonBackup.contains("\"app\": \"Salim\""))
        assertTrue(jsonBackup.contains("\"schema_version\": 1"))

        // Modify state
        repository.addTransaction(
            TransactionEntity(
                type = TransactionType.EXPENSE.name,
                amountInCents = 999999L,
                currency = "SAR",
                category = "أخرى",
                dateTimestamp = System.currentTimeMillis()
            )
        )

        // Restore backup
        val restoreResult = repository.restoreBackupJson(jsonBackup)
        assertTrue(restoreResult.isSuccess)

        val restoredBalance = repository.getCurrentBalanceSync(Currency.SAR)
        assertEquals(65000L, restoredBalance) // 50,000 opening + 15,000 income
    }

    @Test
    fun testCorruptBackupRejectionLeavesDbIntact() = runBlocking {
        repository.completeInitialSetup(100000L, 0L, 0L, Currency.YER)

        val corruptJson = "{\"app\":\"FakeApp\",\"schema_version\":0}"
        val result = repository.restoreBackupJson(corruptJson)

        assertFalse(result.isSuccess)
        // Data must remain intact
        assertEquals(100000L, repository.getCurrentBalanceSync(Currency.YER))
    }
}
