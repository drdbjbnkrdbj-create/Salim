package com.example.ui.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppSettingsEntity
import com.example.data.local.SalimDatabase
import com.example.data.local.TransactionEntity
import com.example.data.model.Currency
import com.example.data.model.CurrencyBalance
import com.example.data.model.DebtType
import com.example.data.model.DebtWithDetails
import com.example.data.model.FinancialReport
import com.example.data.model.ReportPeriod
import com.example.data.model.TransactionType
import com.example.data.repository.SalimRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

enum class AppScreen {
    SPLASH,
    INITIAL_SETUP,
    MAIN,
    ADD_INCOME,
    ADD_EXPENSE,
    ADD_DEBT
}

enum class MainTab(val titleAr: String) {
    HOME("الرئيسية"),
    TRANSACTIONS("العمليات"),
    DEBTS("الديون"),
    REPORTS("التقارير"),
    SETTINGS("الإعدادات")
}

class SalimViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SalimDatabase.getDatabase(application)
    private val repository = SalimRepository(database, database.salimDao())

    // App Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Screen navigation stack for back-press support
    private val screenStack = ArrayDeque<AppScreen>()

    // Messages & Alerts
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Settings & Balances
    val appSettings: StateFlow<AppSettingsEntity?> = repository.appSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currencyBalances: StateFlow<List<CurrencyBalance>> = repository.getCurrencyBalances()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            Currency.entries.map { CurrencyBalance(it, 0L, 0L) }
        )

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDebts: StateFlow<List<DebtWithDetails>> = repository.allDebtsWithDetails
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Primary Currency for Hero Display
    private val _selectedHeroCurrency = MutableStateFlow(Currency.YER)
    val selectedHeroCurrency: StateFlow<Currency> = _selectedHeroCurrency.asStateFlow()

    // Reports Filter State
    private val _reportPeriod = MutableStateFlow(ReportPeriod.MONTH)
    val reportPeriod: StateFlow<ReportPeriod> = _reportPeriod.asStateFlow()

    private val _reportCurrency = MutableStateFlow(Currency.YER)
    val reportCurrency: StateFlow<Currency> = _reportCurrency.asStateFlow()

    val financialReport: StateFlow<FinancialReport?> = combine(
        _reportCurrency,
        _reportPeriod
    ) { curr, period ->
        Pair(curr, period)
    }.flatMapLatest { (curr, period) ->
        repository.getFinancialReport(curr, period)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Evaluate initial routing when settings load
        viewModelScope.launch {
            repository.appSettings.collect { settings ->
                if (_currentScreen.value == AppScreen.SPLASH) {
                    // Small delay for branding splash
                    kotlinx.coroutines.delay(1200)
                    if (settings == null || !settings.isInitialSetupDone) {
                        _currentScreen.value = AppScreen.INITIAL_SETUP
                    } else {
                        _selectedHeroCurrency.value = Currency.fromCode(settings.defaultCurrency)
                        _reportCurrency.value = Currency.fromCode(settings.defaultCurrency)
                        _currentScreen.value = AppScreen.MAIN
                    }
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            screenStack.addLast(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        return if (screenStack.isNotEmpty()) {
            _currentScreen.value = screenStack.removeLast()
            true
        } else {
            false
        }
    }

    fun switchTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun setHeroCurrency(currency: Currency) {
        _selectedHeroCurrency.value = currency
    }

    fun setReportPeriod(period: ReportPeriod) {
        _reportPeriod.value = period
    }

    fun setReportCurrency(currency: Currency) {
        _reportCurrency.value = currency
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    // --- Onboarding & Opening Balances ---
    fun completeInitialSetup(
        yerCents: Long,
        sarCents: Long,
        usdCents: Long,
        defaultCurrency: Currency
    ) {
        viewModelScope.launch {
            repository.completeInitialSetup(yerCents, sarCents, usdCents, defaultCurrency)
            _selectedHeroCurrency.value = defaultCurrency
            _reportCurrency.value = defaultCurrency
            screenStack.clear()
            _currentScreen.value = AppScreen.MAIN
            _userMessage.value = "مرحباً بك في سليم! تم حفظ الأرصدة الافتتاحية بنجاح."
        }
    }

    fun updateOpeningBalance(currency: Currency, amountInCents: Long) {
        viewModelScope.launch {
            repository.updateOpeningBalance(currency, amountInCents)
            _userMessage.value = "تم تحديث الرصيد الافتتاحي لـ ${currency.displayNameAr}"
        }
    }

    fun setDefaultCurrency(currency: Currency) {
        viewModelScope.launch {
            val currentSettings = repository.appSettings
            repository.saveSettings(
                AppSettingsEntity(
                    id = 1,
                    isInitialSetupDone = true,
                    defaultCurrency = currency.code,
                    appLanguage = "ar"
                )
            )
            _selectedHeroCurrency.value = currency
            _userMessage.value = "تم تعيين ${currency.displayNameAr} كعملة افتراضية"
        }
    }

    // --- Transactions Management ---
    fun addIncome(
        amountInCents: Long,
        currency: Currency,
        category: String,
        dateTimestamp: Long,
        notes: String?
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                TransactionEntity(
                    type = TransactionType.INCOME.name,
                    amountInCents = amountInCents,
                    currency = currency.code,
                    category = category,
                    dateTimestamp = dateTimestamp,
                    notes = notes?.trim()?.ifEmpty { null }
                )
            )
            navigateBack()
            _userMessage.value = "تم تسجيل الدخل بنجاح"
        }
    }

    suspend fun checkExpenseBalance(amountInCents: Long, currency: Currency): Boolean {
        val current = repository.getCurrentBalanceSync(currency)
        return amountInCents > current
    }

    fun addExpense(
        amountInCents: Long,
        currency: Currency,
        category: String,
        dateTimestamp: Long,
        notes: String?
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                TransactionEntity(
                    type = TransactionType.EXPENSE.name,
                    amountInCents = amountInCents,
                    currency = currency.code,
                    category = category,
                    dateTimestamp = dateTimestamp,
                    notes = notes?.trim()?.ifEmpty { null }
                )
            )
            navigateBack()
            _userMessage.value = "تم تسجيل المصروف بنجاح"
        }
    }

    fun deleteTransaction(txId: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(txId)
            _userMessage.value = "تم حذف العملية بنجاح"
        }
    }

    fun updateTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(tx)
            _userMessage.value = "تم تعديل العملية بنجاح"
        }
    }

    // --- Debts Management ---
    fun addDebt(
        personName: String,
        amountInCents: Long,
        currency: Currency,
        type: DebtType,
        startDate: Long,
        dueDate: Long?,
        notes: String?,
        isCashTransferred: Boolean
    ) {
        viewModelScope.launch {
            repository.addDebt(
                personName = personName,
                amountInCents = amountInCents,
                currency = currency,
                type = type,
                startDate = startDate,
                dueDate = dueDate,
                notes = notes,
                isCashTransferred = isCashTransferred
            )
            navigateBack()
            _userMessage.value = "تم تسجيل الدين بنجاح"
        }
    }

    fun recordDebtPayment(
        debtId: Long,
        amountInCents: Long,
        paymentDate: Long,
        notes: String?,
        updateCashBalance: Boolean
    ) {
        viewModelScope.launch {
            val success = repository.recordDebtPayment(
                debtId = debtId,
                amountInCents = amountInCents,
                paymentDate = paymentDate,
                notes = notes,
                updateCashBalance = updateCashBalance
            )
            if (success) {
                _userMessage.value = "تم تسجيل السداد بنجاح"
            } else {
                _userMessage.value = "خطأ: تأكد من أن مبلغ السداد لا يتجاوز المتبقي"
            }
        }
    }

    fun deleteDebt(debtId: Long) {
        viewModelScope.launch {
            repository.deleteDebt(debtId)
            _userMessage.value = "تم حذف الدين وسجلاته بنجاح"
        }
    }

    // --- Backup & Restore via SAF ---
    fun exportBackup(contentResolver: ContentResolver, uri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val jsonString = repository.exportBackupJson()
                contentResolver.openOutputStream(uri)?.use { stream ->
                    OutputStreamWriter(stream, Charsets.UTF_8).use { writer ->
                        writer.write(jsonString)
                    }
                }
                onResult(true, "تم إنشاء وتصدير النسخة الاحتياطية بنجاح!")
            } catch (e: Exception) {
                onResult(false, "فشل إنشاء النسخة الاحتياطية: ${e.localizedMessage}")
            }
        }
    }

    fun importBackup(contentResolver: ContentResolver, uri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val sb = StringBuilder()
                contentResolver.openInputStream(uri)?.use { stream ->
                    BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
                        var line = reader.readLine()
                        while (line != null) {
                            sb.append(line).append('\n')
                            line = reader.readLine()
                        }
                    }
                }
                val jsonString = sb.toString()
                val restoreResult = repository.restoreBackupJson(jsonString)
                if (restoreResult.isSuccess) {
                    onResult(true, "تمت استعادة البيانات بنجاح تام!")
                } else {
                    onResult(false, restoreResult.exceptionOrNull()?.localizedMessage ?: "الملف غير صالح")
                }
            } catch (e: Exception) {
                onResult(false, "تعذر قراءة ملف النسخة: ${e.localizedMessage}")
            }
        }
    }
}
