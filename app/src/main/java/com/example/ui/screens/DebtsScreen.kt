package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Currency
import com.example.data.model.DebtType
import com.example.data.model.DebtWithDetails
import com.example.data.model.FormatUtils
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.DebtCardItem
import com.example.ui.components.DebtHistoryDialog
import com.example.ui.components.RecordPaymentDialog
import com.example.ui.components.SalimHeader
import com.example.ui.theme.SalimBackground
import com.example.ui.theme.SalimBorder
import com.example.ui.theme.SalimDarkEmerald
import com.example.ui.theme.SalimDebtAmber
import com.example.ui.theme.SalimExpenseBg
import com.example.ui.theme.SalimExpenseRed
import com.example.ui.theme.SalimGold
import com.example.ui.theme.SalimIncomeBg
import com.example.ui.theme.SalimIncomeGreen
import com.example.ui.theme.SalimLightMint
import com.example.ui.theme.SalimSoftMintBg
import com.example.ui.theme.SalimSurface
import com.example.ui.theme.SalimTextMuted
import com.example.ui.theme.SalimTextPrimary
import com.example.ui.theme.SalimTextSecondary

enum class DebtTabFilter(val titleAr: String) {
    ALL("الكل"),
    OWED_TO_ME("لك عند الآخرين"),
    OWED_BY_ME("عليك للآخرين")
}

@Composable
fun DebtsScreen(
    debts: List<DebtWithDetails>,
    onAddDebtClick: () -> Unit,
    onRecordPayment: (debtId: Long, amountCents: Long, paymentDate: Long, notes: String?, updateCash: Boolean) -> Unit,
    onDeleteDebt: (debtId: Long) -> Unit
) {
    var selectedTab by remember { mutableStateOf(DebtTabFilter.ALL) }
    var selectedCurrency by remember { mutableStateOf<Currency?>(null) }

    var debtForPayment by remember { mutableStateOf<DebtWithDetails?>(null) }
    var debtForHistory by remember { mutableStateOf<DebtWithDetails?>(null) }
    var debtToDelete by remember { mutableStateOf<DebtWithDetails?>(null) }

    if (debtForPayment != null) {
        RecordPaymentDialog(
            debtDetails = debtForPayment!!,
            onDismiss = { debtForPayment = null },
            onConfirm = { amountCents, notes, updateCash ->
                onRecordPayment(
                    debtForPayment!!.debt.id,
                    amountCents,
                    System.currentTimeMillis(),
                    notes,
                    updateCash
                )
                debtForPayment = null
            }
        )
    }

    if (debtForHistory != null) {
        DebtHistoryDialog(
            debtDetails = debtForHistory!!,
            onDismiss = { debtForHistory = null },
            onDeleteDebt = {
                val toDelete = debtForHistory
                debtForHistory = null
                debtToDelete = toDelete
            }
        )
    }

    if (debtToDelete != null) {
        ConfirmDeleteDialog(
            title = "حذف الدين",
            message = "هل تريد بالتأكيد حذف دين ${debtToDelete!!.debt.personName} وسجل سداداته بالكامل؟",
            onConfirm = {
                onDeleteDebt(debtToDelete!!.debt.id)
                debtToDelete = null
            },
            onDismiss = { debtToDelete = null }
        )
    }

    val filteredDebts = debts.filter { details ->
        val matchesCurrency = selectedCurrency == null || details.debt.currency == selectedCurrency?.code
        val matchesType = when (selectedTab) {
            DebtTabFilter.ALL -> true
            DebtTabFilter.OWED_TO_ME -> details.type == DebtType.OWED_TO_ME
            DebtTabFilter.OWED_BY_ME -> details.type == DebtType.OWED_BY_ME
        }
        matchesCurrency && matchesType
    }

    // Summary calculations (for selected currency or per primary currency)
    val displayCurrency = selectedCurrency ?: Currency.YER
    val debtsInDisplayCurrency = debts.filter { it.debt.currency == displayCurrency.code }
    val totalOwedToMe = debtsInDisplayCurrency.filter { it.type == DebtType.OWED_TO_ME }.sumOf { it.remainingCents }
    val totalOwedByMe = debtsInDisplayCurrency.filter { it.type == DebtType.OWED_BY_ME }.sumOf { it.remainingCents }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SalimBackground)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SalimHeader(
                    title = "إدارة الديون",
                    subtitle = "سجل دقيق للديون المستحقة والمطلوبة وسداداتها"
                )
            }

            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SalimSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SalimBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ملخص الديون (${displayCurrency.displayNameAr})",
                                style = MaterialTheme.typography.titleMedium,
                                color = SalimDarkEmerald,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = displayCurrency.flagEmoji,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Owed to me (green)
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SalimIncomeBg)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "ديون لك عند الآخرين",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SalimIncomeGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = FormatUtils.formatMoney(totalOwedToMe, displayCurrency),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = SalimIncomeGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Owed by me (red)
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SalimExpenseBg)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "ديون عليك للآخرين",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SalimExpenseRed,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = FormatUtils.formatMoney(totalOwedByMe, displayCurrency),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = SalimExpenseRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Tabs (الكل, لك عند الآخرين, عليك للآخرين)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DebtTabFilter.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedTab = tab },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) SalimDarkEmerald else SalimSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) SalimDarkEmerald else SalimBorder)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab.titleAr,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Color.White else SalimTextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Currency Filter Pills
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isAll = selectedCurrency == null
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedCurrency = null },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isAll) SalimLightMint else SalimSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isAll) SalimDarkEmerald else SalimBorder)
                    ) {
                        Text(
                            text = "جميع العملات",
                            style = MaterialTheme.typography.labelSmall,
                            color = SalimDarkEmerald,
                            fontWeight = if (isAll) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Currency.entries.forEach { curr ->
                        val isSelected = selectedCurrency == curr
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedCurrency = curr },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) SalimLightMint else SalimSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) SalimDarkEmerald else SalimBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(curr.flagEmoji)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = curr.symbolAr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SalimDarkEmerald,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Debts List
            if (filteredDebts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (debts.isEmpty()) "لا توجد ديون مسجلة. اضغط الزر أدناه لإضافة دين." else "لا توجد ديون مطابقة للتصفية.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SalimTextMuted
                        )
                    }
                }
            } else {
                items(filteredDebts, key = { it.debt.id }) { details ->
                    DebtCardItem(
                        debtDetails = details,
                        onRecordPayment = { debtForPayment = details },
                        onViewHistory = { debtForHistory = details },
                        onDelete = { debtToDelete = details }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Floating Action Button to add debt
        FloatingActionButton(
            onClick = onAddDebtClick,
            containerColor = SalimDarkEmerald,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 20.dp)
                .testTag("fab_add_debt")
        ) {
            Icon(Icons.Default.Add, contentDescription = "إضافة دين جديد")
        }
    }
}
