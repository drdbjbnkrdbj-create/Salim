package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppSettingsEntity
import com.example.data.local.DebtEntity
import com.example.data.local.DebtPaymentEntity
import com.example.data.local.OpeningBalanceEntity
import com.example.data.local.SalimDao
import com.example.data.local.SalimDatabase
import com.example.data.local.TransactionEntity
import com.example.data.model.BarChartItem
import com.example.data.model.CategoryShare
import com.example.data.model.Currency
import com.example.data.model.CurrencyBalance
import com.example.data.model.DebtStatus
import com.example.data.model.DebtType
import com.example.data.model.DebtWithDetails
import com.example.data.model.FinancialReport
import com.example.data.model.ReportPeriod
import com.example.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class SalimRepository(
    private val database: SalimDatabase,
    private val dao: SalimDao
) {

    // --- Settings & Onboarding ---
    val appSettings: Flow<AppSettingsEntity?> = dao.getAppSettings()

    suspend fun saveSettings(settings: AppSettingsEntity) = withContext(Dispatchers.IO) {
        dao.saveAppSettings(settings)
    }

    suspend fun completeInitialSetup(
        yerOpeningCents: Long,
        sarOpeningCents: Long,
        usdOpeningCents: Long,
        defaultCurrency: Currency
    ) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val now = System.currentTimeMillis()
            dao.setOpeningBalance(OpeningBalanceEntity("YER", yerOpeningCents, now))
            dao.setOpeningBalance(OpeningBalanceEntity("SAR", sarOpeningCents, now))
            dao.setOpeningBalance(OpeningBalanceEntity("USD", usdOpeningCents, now))
            dao.saveAppSettings(
                AppSettingsEntity(
                    id = 1,
                    isInitialSetupDone = true,
                    defaultCurrency = defaultCurrency.code,
                    appLanguage = "ar"
                )
            )
        }
    }

    // --- Opening Balances ---
    val allOpeningBalances: Flow<List<OpeningBalanceEntity>> = dao.getAllOpeningBalances()

    suspend fun updateOpeningBalance(currency: Currency, amountInCents: Long) = withContext(Dispatchers.IO) {
        dao.setOpeningBalance(
            OpeningBalanceEntity(
                currency = currency.code,
                amountInCents = amountInCents,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // --- Balances Calculation ---
    fun getCurrencyBalances(): Flow<List<CurrencyBalance>> {
        return combine(
            dao.getAllOpeningBalances(),
            dao.getAllTransactions()
        ) { openings, transactions ->
            Currency.entries.map { currency ->
                val opening = openings.firstOrNull { it.currency == currency.code }?.amountInCents ?: 0L
                val currencyTxs = transactions.filter { it.currency == currency.code }

                var runningBalance = opening
                for (tx in currencyTxs) {
                    val type = try {
                        TransactionType.valueOf(tx.type)
                    } catch (_: Exception) {
                        null
                    }
                    if (type != null) {
                        if (type.isPositiveCash) {
                            runningBalance += tx.amountInCents
                        } else if (type.isNegativeCash) {
                            runningBalance -= tx.amountInCents
                        }
                    }
                }

                CurrencyBalance(
                    currency = currency,
                    openingBalanceCents = opening,
                    currentBalanceCents = runningBalance
                )
            }
        }
    }

    suspend fun getCurrentBalanceSync(currency: Currency): Long = withContext(Dispatchers.IO) {
        val opening = dao.getOpeningBalance(currency.code)?.amountInCents ?: 0L
        val txs = dao.getAllTransactionsSync().filter { it.currency == currency.code }
        var running = opening
        for (tx in txs) {
            val type = runCatching { TransactionType.valueOf(tx.type) }.getOrNull()
            if (type?.isPositiveCash == true) running += tx.amountInCents
            if (type?.isNegativeCash == true) running -= tx.amountInCents
        }
        running
    }

    // --- Transactions ---
    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()

    suspend fun addTransaction(transaction: TransactionEntity): Long = withContext(Dispatchers.IO) {
        dao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        dao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transactionId: Long) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val tx = dao.getTransactionById(transactionId)
            if (tx != null) {
                // If this transaction was linked to a debt payment, also remove the payment record
                if (tx.debtId != null) {
                    val allPayments = dao.getAllDebtPaymentsSync()
                    val linkedPayment = allPayments.firstOrNull { it.transactionId == transactionId }
                    if (linkedPayment != null) {
                        dao.deleteDebtPaymentById(linkedPayment.id)
                    }
                }
                dao.deleteTransactionById(transactionId)
            }
        }
    }

    // --- Debts ---
    val allDebtsWithDetails: Flow<List<DebtWithDetails>> = combine(
        dao.getAllDebts(),
        dao.getAllDebtPayments()
    ) { debts, payments ->
        debts.map { debt ->
            val debtPayments = payments.filter { it.debtId == debt.id }
            DebtWithDetails(debt = debt, payments = debtPayments)
        }
    }

    suspend fun addDebt(
        personName: String,
        amountInCents: Long,
        currency: Currency,
        type: DebtType,
        startDate: Long,
        dueDate: Long?,
        notes: String?,
        isCashTransferred: Boolean
    ): Long = withContext(Dispatchers.IO) {
        database.withTransaction {
            val debt = DebtEntity(
                personName = personName.trim(),
                originalAmountInCents = amountInCents,
                currency = currency.code,
                type = type.name,
                startDate = startDate,
                dueDate = dueDate,
                notes = notes?.trim(),
                isCashTransferredInitially = isCashTransferred
            )
            val debtId = dao.insertDebt(debt)

            if (isCashTransferred) {
                val txType = if (type == DebtType.OWED_TO_ME) {
                    TransactionType.DEBT_INIT_LENT
                } else {
                    TransactionType.DEBT_INIT_BORROWED
                }
                val noteText = if (type == DebtType.OWED_TO_ME) {
                    "تسليم مبلغ دين إلى: ${personName.trim()}"
                } else {
                    "استلام مبلغ دين من: ${personName.trim()}"
                }
                dao.insertTransaction(
                    TransactionEntity(
                        type = txType.name,
                        amountInCents = amountInCents,
                        currency = currency.code,
                        category = "دين",
                        dateTimestamp = startDate,
                        notes = notes?.ifBlank { noteText } ?: noteText,
                        debtId = debtId
                    )
                )
            }
            debtId
        }
    }

    suspend fun recordDebtPayment(
        debtId: Long,
        amountInCents: Long,
        paymentDate: Long,
        notes: String?,
        updateCashBalance: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        database.withTransaction {
            val debt = dao.getDebtById(debtId) ?: return@withTransaction false
            val currentPayments = dao.getPaymentsForDebtSync(debtId)
            val alreadyPaid = currentPayments.sumOf { it.amountInCents }
            val remaining = debt.originalAmountInCents - alreadyPaid

            if (amountInCents <= 0L || amountInCents > remaining) {
                return@withTransaction false
            }

            var linkedTxId: Long? = null
            if (updateCashBalance) {
                val debtType = DebtType.fromString(debt.type)
                val txType = if (debtType == DebtType.OWED_TO_ME) {
                    TransactionType.DEBT_PAYMENT_IN
                } else {
                    TransactionType.DEBT_PAYMENT_OUT
                }
                val desc = if (debtType == DebtType.OWED_TO_ME) {
                    "تحصيل سداد دين من: ${debt.personName}"
                } else {
                    "دفع سداد دين إلى: ${debt.personName}"
                }

                linkedTxId = dao.insertTransaction(
                    TransactionEntity(
                        type = txType.name,
                        amountInCents = amountInCents,
                        currency = debt.currency,
                        category = "سداد دين",
                        dateTimestamp = paymentDate,
                        notes = notes?.ifBlank { desc } ?: desc,
                        debtId = debtId
                    )
                )
            }

            dao.insertDebtPayment(
                DebtPaymentEntity(
                    debtId = debtId,
                    amountInCents = amountInCents,
                    paymentDate = paymentDate,
                    notes = notes?.trim(),
                    transactionId = linkedTxId
                )
            )
            true
        }
    }

    suspend fun deleteDebt(debtId: Long) = withContext(Dispatchers.IO) {
        database.withTransaction {
            dao.deletePaymentsByDebtId(debtId)
            dao.deleteTransactionsByDebtId(debtId)
            dao.deleteDebtById(debtId)
        }
    }

    // --- Reports Calculation ---
    fun getFinancialReport(
        currency: Currency,
        period: ReportPeriod
    ): Flow<FinancialReport> {
        return combine(
            dao.getTransactionsByCurrency(currency.code),
            dao.getAllDebts(),
            dao.getAllDebtPayments()
        ) { transactions, debts, payments ->
            calculateReport(currency, period, transactions, debts, payments)
        }
    }

    private fun calculateReport(
        currency: Currency,
        period: ReportPeriod,
        transactions: List<TransactionEntity>,
        debts: List<DebtEntity>,
        payments: List<DebtPaymentEntity>
    ): FinancialReport {
        val (startTime, endTime) = getPeriodRange(period)

        val periodTransactions = transactions.filter {
            it.dateTimestamp in startTime..endTime
        }

        var totalIncome = 0L
        var totalExpense = 0L
        val categoryExpenses = mutableMapOf<String, Long>()

        for (tx in periodTransactions) {
            when (tx.type) {
                TransactionType.INCOME.name -> {
                    totalIncome += tx.amountInCents
                }
                TransactionType.EXPENSE.name -> {
                    totalExpense += tx.amountInCents
                    val current = categoryExpenses.getOrDefault(tx.category, 0L)
                    categoryExpenses[tx.category] = current + tx.amountInCents
                }
            }
        }

        val netSavings = totalIncome - totalExpense

        // Breakdown percentages
        val categoryBreakdown = if (totalExpense > 0L) {
            categoryExpenses.map { (cat, amt) ->
                CategoryShare(
                    categoryName = cat,
                    totalCents = amt,
                    percentage = (amt.toFloat() / totalExpense.toFloat()) * 100f
                )
            }.sortedByDescending { it.totalCents }
        } else {
            emptyList()
        }

        // Timeline Bar Chart Data
        val chartData = buildTimelineChartData(period, periodTransactions)

        // Debts calculations for this currency
        val currencyDebts = debts.filter { it.currency == currency.code }
        var owedToMeRemaining = 0L
        var owedByMeRemaining = 0L

        for (debt in currencyDebts) {
            val paid = payments.filter { it.debtId == debt.id }.sumOf { it.amountInCents }
            val remaining = kotlin.math.max(0L, debt.originalAmountInCents - paid)
            if (debt.type == DebtType.OWED_TO_ME.name) {
                owedToMeRemaining += remaining
            } else {
                owedByMeRemaining += remaining
            }
        }

        // Payments repaid during this period for this currency
        val currencyDebtIds = currencyDebts.map { it.id }.toSet()
        val debtsRepaidDuringPeriod = payments
            .filter { it.debtId in currencyDebtIds && it.paymentDate in startTime..endTime }
            .sumOf { it.amountInCents }

        return FinancialReport(
            currency = currency,
            period = period,
            totalIncomeCents = totalIncome,
            totalExpenseCents = totalExpense,
            netCashFlowCents = netSavings,
            transactionCount = periodTransactions.size,
            categoryBreakdown = categoryBreakdown,
            chartData = chartData,
            debtsOwedToMeTotalRemaining = owedToMeRemaining,
            debtsOwedByMeTotalRemaining = owedByMeRemaining,
            debtsRepaidDuringPeriod = debtsRepaidDuringPeriod
        )
    }

    private fun getPeriodRange(period: ReportPeriod): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val endTime = cal.timeInMillis

        when (period) {
            ReportPeriod.DAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
            ReportPeriod.WEEK -> {
                cal.firstDayOfWeek = Calendar.SATURDAY
                cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
            ReportPeriod.MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
            ReportPeriod.YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
        }
        return Pair(cal.timeInMillis, endTime)
    }

    private fun buildTimelineChartData(
        period: ReportPeriod,
        transactions: List<TransactionEntity>
    ): List<BarChartItem> {
        // Group transactions into 4-7 buckets depending on period
        return when (period) {
            ReportPeriod.DAY -> {
                listOf(
                    BarChartItem(
                        label = "اليوم",
                        incomeCents = transactions.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amountInCents },
                        expenseCents = transactions.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amountInCents }
                    )
                )
            }
            ReportPeriod.WEEK -> {
                val daysAr = listOf("السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")
                val buckets = daysAr.map { BarChartItem(it, 0L, 0L) }.toMutableList()
                val cal = Calendar.getInstance()
                for (tx in transactions) {
                    cal.timeInMillis = tx.dateTimestamp
                    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                    val idx = when (dayOfWeek) {
                        Calendar.SATURDAY -> 0
                        Calendar.SUNDAY -> 1
                        Calendar.MONDAY -> 2
                        Calendar.TUESDAY -> 3
                        Calendar.WEDNESDAY -> 4
                        Calendar.THURSDAY -> 5
                        Calendar.FRIDAY -> 6
                        else -> 0
                    }
                    val item = buckets[idx]
                    if (tx.type == TransactionType.INCOME.name) {
                        buckets[idx] = item.copy(incomeCents = item.incomeCents + tx.amountInCents)
                    } else if (tx.type == TransactionType.EXPENSE.name) {
                        buckets[idx] = item.copy(expenseCents = item.expenseCents + tx.amountInCents)
                    }
                }
                buckets
            }
            ReportPeriod.MONTH -> {
                val weeks = listOf("أسبوع 1", "أسبوع 2", "أسبوع 3", "أسبوع 4")
                val buckets = weeks.map { BarChartItem(it, 0L, 0L) }.toMutableList()
                val cal = Calendar.getInstance()
                for (tx in transactions) {
                    cal.timeInMillis = tx.dateTimestamp
                    val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
                    val weekIdx = ((dayOfMonth - 1) / 7).coerceIn(0, 3)
                    val item = buckets[weekIdx]
                    if (tx.type == TransactionType.INCOME.name) {
                        buckets[weekIdx] = item.copy(incomeCents = item.incomeCents + tx.amountInCents)
                    } else if (tx.type == TransactionType.EXPENSE.name) {
                        buckets[weekIdx] = item.copy(expenseCents = item.expenseCents + tx.amountInCents)
                    }
                }
                buckets
            }
            ReportPeriod.YEAR -> {
                val quarters = listOf("الربع 1", "الربع 2", "الربع 3", "الربع 4")
                val buckets = quarters.map { BarChartItem(it, 0L, 0L) }.toMutableList()
                val cal = Calendar.getInstance()
                for (tx in transactions) {
                    cal.timeInMillis = tx.dateTimestamp
                    val month = cal.get(Calendar.MONTH) // 0-11
                    val qIdx = (month / 3).coerceIn(0, 3)
                    val item = buckets[qIdx]
                    if (tx.type == TransactionType.INCOME.name) {
                        buckets[qIdx] = item.copy(incomeCents = item.incomeCents + tx.amountInCents)
                    } else if (tx.type == TransactionType.EXPENSE.name) {
                        buckets[qIdx] = item.copy(expenseCents = item.expenseCents + tx.amountInCents)
                    }
                }
                buckets
            }
        }
    }

    // --- Backup & Restore (JSON Format) ---
    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "Salim")
        root.put("schema_version", 1)
        root.put("exported_at", System.currentTimeMillis())

        // Settings
        val settings = dao.getAppSettingsSync()
        val settingsObj = JSONObject().apply {
            put("is_initial_setup_done", settings?.isInitialSetupDone ?: false)
            put("default_currency", settings?.defaultCurrency ?: "YER")
            put("app_language", settings?.appLanguage ?: "ar")
        }
        root.put("settings", settingsObj)

        // Opening Balances
        val openingsArray = JSONArray()
        dao.getAllOpeningBalancesSync().forEach { ob ->
            openingsArray.put(
                JSONObject().apply {
                    put("currency", ob.currency)
                    put("amount_cents", ob.amountInCents)
                    put("updated_at", ob.updatedAt)
                }
            )
        }
        root.put("opening_balances", openingsArray)

        // Transactions
        val txsArray = JSONArray()
        dao.getAllTransactionsSync().forEach { tx ->
            txsArray.put(
                JSONObject().apply {
                    put("id", tx.id)
                    put("type", tx.type)
                    put("amount_cents", tx.amountInCents)
                    put("currency", tx.currency)
                    put("category", tx.category)
                    put("date_timestamp", tx.dateTimestamp)
                    put("notes", tx.notes ?: "")
                    if (tx.debtId != null) put("debt_id", tx.debtId)
                    put("created_at", tx.createdAt)
                }
            )
        }
        root.put("transactions", txsArray)

        // Debts
        val debtsArray = JSONArray()
        dao.getAllDebtsSync().forEach { d ->
            debtsArray.put(
                JSONObject().apply {
                    put("id", d.id)
                    put("person_name", d.personName)
                    put("original_amount_cents", d.originalAmountInCents)
                    put("currency", d.currency)
                    put("type", d.type)
                    put("start_date", d.startDate)
                    if (d.dueDate != null) put("due_date", d.dueDate)
                    put("notes", d.notes ?: "")
                    put("is_cash_transferred", d.isCashTransferredInitially)
                    put("created_at", d.createdAt)
                }
            )
        }
        root.put("debts", debtsArray)

        // Debt Payments
        val paymentsArray = JSONArray()
        dao.getAllDebtPaymentsSync().forEach { p ->
            paymentsArray.put(
                JSONObject().apply {
                    put("id", p.id)
                    put("debt_id", p.debtId)
                    put("amount_cents", p.amountInCents)
                    put("payment_date", p.paymentDate)
                    put("notes", p.notes ?: "")
                    if (p.transactionId != null) put("transaction_id", p.transactionId)
                    put("created_at", p.createdAt)
                }
            )
        }
        root.put("debt_payments", paymentsArray)

        root.toString(2)
    }

    suspend fun restoreBackupJson(jsonString: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val app = root.optString("app")
            if (app != "Salim") {
                return@withContext Result.failure(IllegalArgumentException("الملف المحدد ليس نسخة احتياطية صالحة لتطبيق سليم."))
            }
            val schemaVersion = root.optInt("schema_version", 0)
            if (schemaVersion < 1) {
                return@withContext Result.failure(IllegalArgumentException("إصدار المخطط غير مدعوم في هذا الملف."))
            }

            // Parse and validate all sections before touching DB
            val restoredOpenings = mutableListOf<OpeningBalanceEntity>()
            val openingsArr = root.optJSONArray("opening_balances") ?: JSONArray()
            for (i in 0 until openingsArr.length()) {
                val item = openingsArr.getJSONObject(i)
                val currency = item.getString("currency")
                val amount = item.getLong("amount_cents")
                val updated = item.optLong("updated_at", System.currentTimeMillis())
                restoredOpenings.add(OpeningBalanceEntity(currency, amount, updated))
            }

            val restoredTxs = mutableListOf<TransactionEntity>()
            val txArr = root.optJSONArray("transactions") ?: JSONArray()
            for (i in 0 until txArr.length()) {
                val item = txArr.getJSONObject(i)
                restoredTxs.add(
                    TransactionEntity(
                        id = item.optLong("id", 0L),
                        type = item.getString("type"),
                        amountInCents = item.getLong("amount_cents"),
                        currency = item.getString("currency"),
                        category = item.getString("category"),
                        dateTimestamp = item.getLong("date_timestamp"),
                        notes = item.optString("notes").ifEmpty { null },
                        debtId = if (item.has("debt_id")) item.getLong("debt_id") else null,
                        createdAt = item.optLong("created_at", System.currentTimeMillis())
                    )
                )
            }

            val restoredDebts = mutableListOf<DebtEntity>()
            val debtsArr = root.optJSONArray("debts") ?: JSONArray()
            for (i in 0 until debtsArr.length()) {
                val item = debtsArr.getJSONObject(i)
                restoredDebts.add(
                    DebtEntity(
                        id = item.optLong("id", 0L),
                        personName = item.getString("person_name"),
                        originalAmountInCents = item.getLong("original_amount_cents"),
                        currency = item.getString("currency"),
                        type = item.getString("type"),
                        startDate = item.getLong("start_date"),
                        dueDate = if (item.has("due_date")) item.getLong("due_date") else null,
                        notes = item.optString("notes").ifEmpty { null },
                        isCashTransferredInitially = item.optBoolean("is_cash_transferred", false),
                        createdAt = item.optLong("created_at", System.currentTimeMillis())
                    )
                )
            }

            val restoredPayments = mutableListOf<DebtPaymentEntity>()
            val paymentsArr = root.optJSONArray("debt_payments") ?: JSONArray()
            for (i in 0 until paymentsArr.length()) {
                val item = paymentsArr.getJSONObject(i)
                restoredPayments.add(
                    DebtPaymentEntity(
                        id = item.optLong("id", 0L),
                        debtId = item.getLong("debt_id"),
                        amountInCents = item.getLong("amount_cents"),
                        paymentDate = item.getLong("payment_date"),
                        notes = item.optString("notes").ifEmpty { null },
                        transactionId = if (item.has("transaction_id")) item.getLong("transaction_id") else null,
                        createdAt = item.optLong("created_at", System.currentTimeMillis())
                    )
                )
            }

            val settingsObj = root.optJSONObject("settings")
            val restoredSettings = AppSettingsEntity(
                id = 1,
                isInitialSetupDone = settingsObj?.optBoolean("is_initial_setup_done", true) ?: true,
                defaultCurrency = settingsObj?.optString("default_currency", "YER") ?: "YER",
                appLanguage = settingsObj?.optString("app_language", "ar") ?: "ar"
            )

            // Perform atomic database replacement
            database.withTransaction {
                dao.clearAllOpeningBalances()
                dao.clearAllTransactions()
                dao.clearAllDebtPayments()
                dao.clearAllDebts()

                dao.insertOpeningBalances(restoredOpenings)
                dao.insertDebts(restoredDebts)
                dao.insertDebtPayments(restoredPayments)
                dao.insertTransactions(restoredTxs)
                dao.saveAppSettings(restoredSettings)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("فشل قراءة ملف النسخة الاحتياطية: ${e.localizedMessage ?: "تنسيق غير سليم"}", e))
        }
    }
}
