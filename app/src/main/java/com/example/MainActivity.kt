package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Currency
import com.example.ui.components.SalimBottomBar
import com.example.ui.screens.AddDebtScreen
import com.example.ui.screens.AddExpenseScreen
import com.example.ui.screens.AddIncomeScreen
import com.example.ui.screens.DebtsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InitialSetupScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TransactionsListScreen
import com.example.ui.theme.SalimDarkEmerald
import com.example.ui.theme.SalimExpenseRed
import com.example.ui.theme.SalimSurface
import com.example.ui.theme.SalimTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.SalimViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: SalimViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SalimTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    SalimApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun SalimApp(viewModel: SalimViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val currencyBalances by viewModel.currencyBalances.collectAsStateWithLifecycle()
    val recentTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val debts by viewModel.allDebts.collectAsStateWithLifecycle()
    val heroCurrency by viewModel.selectedHeroCurrency.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val reportCurrency by viewModel.reportCurrency.collectAsStateWithLifecycle()
    val reportPeriod by viewModel.reportPeriod.collectAsStateWithLifecycle()
    val financialReport by viewModel.financialReport.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    var pendingRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }

    // SAF Activity Launchers for Backup & Restore
    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackup(context.contentResolver, uri) { success, msg ->
                coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
            }
        }
    }

    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingRestoreUri = uri
        }
    }

    // Confirmation dialog before overriding database with restore
    if (pendingRestoreUri != null) {
        AlertDialog(
            onDismissRequest = { pendingRestoreUri = null },
            containerColor = SalimSurface,
            title = {
                Text(
                    text = "تأكيد استعادة النسخة الاحتياطية",
                    color = SalimDarkEmerald
                )
            },
            text = {
                Text(
                    text = "تنبيه: ستؤدي عملية الاستعادة إلى استبدال جميع البيانات الحالية في التطبيق بالبيانات الموجودة في ملف النسخة الاحتياطية. سيتم فحص الملف والتحقق منه أولاً قبل الاستبدال. هل تود المتابعة؟"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = pendingRestoreUri!!
                        pendingRestoreUri = null
                        viewModel.importBackup(context.contentResolver, uri) { success, msg ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SalimDarkEmerald)
                ) {
                    Text("نعم، استعادة")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingRestoreUri = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Show Snackbars
    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    // Predictive / system Back Handling
    BackHandler(enabled = currentScreen != AppScreen.MAIN && currentScreen != AppScreen.SPLASH) {
        viewModel.navigateBack()
    }

    BackHandler(enabled = currentScreen == AppScreen.MAIN && currentTab != MainTab.HOME) {
        viewModel.switchTab(MainTab.HOME)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (currentScreen == AppScreen.MAIN) {
                SalimBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.switchTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    AppScreen.SPLASH -> {
                        SplashScreen()
                    }
                    AppScreen.INITIAL_SETUP -> {
                        InitialSetupScreen(
                            onComplete = { yer, sar, usd, defCurr ->
                                viewModel.completeInitialSetup(yer, sar, usd, defCurr)
                            }
                        )
                    }
                    AppScreen.MAIN -> {
                        when (currentTab) {
                            MainTab.HOME -> {
                                HomeScreen(
                                    selectedHeroCurrency = heroCurrency,
                                    currencyBalances = currencyBalances,
                                    recentTransactions = recentTransactions,
                                    onSelectHeroCurrency = { viewModel.setHeroCurrency(it) },
                                    onAddIncomeClick = { viewModel.navigateTo(AppScreen.ADD_INCOME) },
                                    onAddExpenseClick = { viewModel.navigateTo(AppScreen.ADD_EXPENSE) },
                                    onAddDebtClick = { viewModel.navigateTo(AppScreen.ADD_DEBT) },
                                    onViewAllTransactionsClick = { viewModel.switchTab(MainTab.TRANSACTIONS) },
                                    onOpenSettingsClick = { viewModel.switchTab(MainTab.SETTINGS) }
                                )
                            }
                            MainTab.TRANSACTIONS -> {
                                TransactionsListScreen(
                                    transactions = recentTransactions,
                                    onDeleteTransaction = { viewModel.deleteTransaction(it) }
                                )
                            }
                            MainTab.DEBTS -> {
                                DebtsScreen(
                                    debts = debts,
                                    onAddDebtClick = { viewModel.navigateTo(AppScreen.ADD_DEBT) },
                                    onRecordPayment = { id, amt, date, notes, updateCash ->
                                        viewModel.recordDebtPayment(id, amt, date, notes, updateCash)
                                    },
                                    onDeleteDebt = { viewModel.deleteDebt(it) }
                                )
                            }
                            MainTab.REPORTS -> {
                                ReportsScreen(
                                    report = financialReport,
                                    selectedCurrency = reportCurrency,
                                    selectedPeriod = reportPeriod,
                                    onSelectCurrency = { viewModel.setReportCurrency(it) },
                                    onSelectPeriod = { viewModel.setReportPeriod(it) }
                                )
                            }
                            MainTab.SETTINGS -> {
                                val currentDef = Currency.fromCode(appSettings?.defaultCurrency)
                                SettingsScreen(
                                    currentDefaultCurrency = currentDef,
                                    currencyBalances = currencyBalances,
                                    onSetDefaultCurrency = { viewModel.setDefaultCurrency(it) },
                                    onUpdateOpeningBalance = { curr, amt -> viewModel.updateOpeningBalance(curr, amt) },
                                    onExportBackupClick = {
                                        val fileName = "salim_backup_${System.currentTimeMillis()}.json"
                                        exportBackupLauncher.launch(fileName)
                                    },
                                    onImportBackupClick = {
                                        importBackupLauncher.launch(arrayOf("application/json", "*/*"))
                                    }
                                )
                            }
                        }
                    }
                    AppScreen.ADD_INCOME -> {
                        AddIncomeScreen(
                            initialCurrency = heroCurrency,
                            onBack = { viewModel.navigateBack() },
                            onSave = { amt, curr, cat, date, notes ->
                                viewModel.addIncome(amt, curr, cat, date, notes)
                            }
                        )
                    }
                    AppScreen.ADD_EXPENSE -> {
                        AddExpenseScreen(
                            initialCurrency = heroCurrency,
                            currencyBalances = currencyBalances,
                            onBack = { viewModel.navigateBack() },
                            onSave = { amt, curr, cat, date, notes ->
                                viewModel.addExpense(amt, curr, cat, date, notes)
                            }
                        )
                    }
                    AppScreen.ADD_DEBT -> {
                        AddDebtScreen(
                            initialCurrency = heroCurrency,
                            onBack = { viewModel.navigateBack() },
                            onSave = { name, amt, curr, type, start, due, notes, isCash ->
                                viewModel.addDebt(name, amt, curr, type, start, due, notes, isCash)
                            }
                        )
                    }
                }
            }
        }
    }
}
