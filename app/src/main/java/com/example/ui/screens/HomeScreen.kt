package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.TransactionEntity
import com.example.data.model.Currency
import com.example.data.model.CurrencyBalance
import com.example.ui.components.MiniCurrencyCard
import com.example.ui.components.SalimActionButton
import com.example.ui.components.SalimHeader
import com.example.ui.components.SalimHeroBalanceCard
import com.example.ui.components.TransactionRowItem
import com.example.ui.theme.SalimBackground
import com.example.ui.theme.SalimDarkEmerald
import com.example.ui.theme.SalimDebtAmber
import com.example.ui.theme.SalimDebtBg
import com.example.ui.theme.SalimExpenseBg
import com.example.ui.theme.SalimExpenseRed
import com.example.ui.theme.SalimIncomeBg
import com.example.ui.theme.SalimIncomeGreen
import com.example.ui.theme.SalimTextMuted

@Composable
fun HomeScreen(
    selectedHeroCurrency: Currency,
    currencyBalances: List<CurrencyBalance>,
    recentTransactions: List<TransactionEntity>,
    onSelectHeroCurrency: (Currency) -> Unit,
    onAddIncomeClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddDebtClick: () -> Unit,
    onViewAllTransactionsClick: () -> Unit,
    onOpenSettingsClick: () -> Unit
) {
    val heroBalance = currencyBalances.firstOrNull { it.currency == selectedHeroCurrency }?.currentBalanceCents ?: 0L

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SalimBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SalimHeader(
                onSettingsClick = onOpenSettingsClick
            )
        }

        // Hero Balance Card
        item {
            SalimHeroBalanceCard(
                currency = selectedHeroCurrency,
                balanceCents = heroBalance,
                onSwitchCurrencyClick = {
                    val nextIndex = (selectedHeroCurrency.ordinal + 1) % Currency.entries.size
                    onSelectHeroCurrency(Currency.entries[nextIndex])
                }
            )
        }

        // 3 Independent Currency Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Currency.entries.forEach { currency ->
                    val bal = currencyBalances.firstOrNull { it.currency == currency }?.currentBalanceCents ?: 0L
                    MiniCurrencyCard(
                        currency = currency,
                        balanceCents = bal,
                        isSelected = selectedHeroCurrency == currency,
                        onClick = { onSelectHeroCurrency(currency) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SalimActionButton(
                    title = "إضافة دخل",
                    icon = Icons.Default.ArrowUpward,
                    containerColor = SalimIncomeBg,
                    contentColor = SalimIncomeGreen,
                    onClick = onAddIncomeClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_add_income_btn")
                )

                SalimActionButton(
                    title = "إضافة مصروف",
                    icon = Icons.Default.ArrowDownward,
                    containerColor = SalimExpenseBg,
                    contentColor = SalimExpenseRed,
                    onClick = onAddExpenseClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_add_expense_btn")
                )

                SalimActionButton(
                    title = "إضافة دين",
                    icon = Icons.Default.Handshake,
                    containerColor = SalimDebtBg,
                    contentColor = SalimDebtAmber,
                    onClick = onAddDebtClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_add_debt_btn")
                )
            }
        }

        // Recent Transactions Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "أحدث العمليات",
                    style = MaterialTheme.typography.titleMedium,
                    color = SalimDarkEmerald,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = onViewAllTransactionsClick,
                    modifier = Modifier.testTag("home_view_all_transactions_btn")
                ) {
                    Text(
                        text = "عرض الكل",
                        style = MaterialTheme.typography.labelMedium,
                        color = SalimDarkEmerald,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (recentTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد عمليات مسجلة بعد. استخدم الأزرار أعلاه لبدء التسجيل.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SalimTextMuted
                    )
                }
            }
        } else {
            items(recentTransactions.take(5)) { tx ->
                TransactionRowItem(transaction = tx)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
