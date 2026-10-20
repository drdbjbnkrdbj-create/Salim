package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Currency
import com.example.data.model.CurrencyBalance
import com.example.data.model.DefaultCategories
import com.example.data.model.FormatUtils
import com.example.ui.components.NegativeBalanceDialog
import com.example.ui.components.SalimScreenTopBar
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.SalimBackground
import com.example.ui.theme.SalimBorder
import com.example.ui.theme.SalimDarkEmerald
import com.example.ui.theme.SalimExpenseBg
import com.example.ui.theme.SalimExpenseRed
import com.example.ui.theme.SalimSurface
import com.example.ui.theme.SalimTextMuted
import com.example.ui.theme.SalimTextPrimary
import com.example.ui.theme.SalimTextSecondary

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    initialCurrency: Currency,
    currencyBalances: List<CurrencyBalance>,
    onBack: () -> Unit,
    onSave: (amountCents: Long, currency: Currency, category: String, dateTimestamp: Long, notes: String?) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf(initialCurrency) }
    var selectedCategory by remember { mutableStateOf(DefaultCategories.expenseCategories.first().nameAr) }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var notesText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showNegativeBalanceDialog by remember { mutableStateOf(false) }
    var pendingSaveCents by remember { mutableStateOf(0L) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)

    val currentBalanceForCurrency = currencyBalances.firstOrNull { it.currency == selectedCurrency }?.currentBalanceCents ?: 0L

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                }) {
                    Text("موافق", color = SalimDarkEmerald)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("إلغاء", color = SalimTextMuted)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showNegativeBalanceDialog) {
        NegativeBalanceDialog(
            amountFormatted = FormatUtils.formatMoney(pendingSaveCents, selectedCurrency),
            currentBalanceFormatted = FormatUtils.formatMoney(currentBalanceForCurrency, selectedCurrency),
            onProceed = {
                showNegativeBalanceDialog = false
                onSave(
                    pendingSaveCents,
                    selectedCurrency,
                    selectedCategory,
                    selectedDateMillis,
                    notesText.ifBlank { null }
                )
            },
            onDismiss = { showNegativeBalanceDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SalimBackground)
    ) {
        SalimScreenTopBar(
            title = "إضافة مصروف جديد",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount input card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SalimSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SalimBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "المبلغ المطلوب صرفه",
                        style = MaterialTheme.typography.labelMedium,
                        color = SalimTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it
                            errorMessage = null
                        },
                        placeholder = { Text("مثال: 25000", color = SalimTextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_expense_amount"),
                        shape = RoundedCornerShape(12.dp),
                        isError = errorMessage != null,
                        trailingIcon = {
                            Text(
                                text = selectedCurrency.symbolAr,
                                style = MaterialTheme.typography.titleMedium,
                                color = SalimExpenseRed,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }
                    )
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = SalimExpenseRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "الرصيد المتاح حالياً: ${FormatUtils.formatMoney(currentBalanceForCurrency, selectedCurrency)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (currentBalanceForCurrency <= 0) SalimExpenseRed else SalimDarkEmerald
                    )
                }
            }

            // Currency selector
            Text(
                text = "العملة",
                style = MaterialTheme.typography.titleSmall,
                color = SalimDarkEmerald,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Currency.entries.forEach { currency ->
                    val isSelected = selectedCurrency == currency
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedCurrency = currency }
                            .testTag("select_expense_currency_${currency.code.lowercase()}"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) SalimExpenseBg else SalimSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) SalimExpenseRed else SalimBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(currency.flagEmoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currency.symbolAr,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) SalimExpenseRed else SalimTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Category selector
            Text(
                text = "التصنيف",
                style = MaterialTheme.typography.titleSmall,
                color = SalimDarkEmerald,
                fontWeight = FontWeight.Bold
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DefaultCategories.expenseCategories.forEach { category ->
                    val isSelected = selectedCategory == category.nameAr
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedCategory = category.nameAr },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) SalimExpenseRed else SalimSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) SalimExpenseRed else SalimBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(category.nameAr),
                                contentDescription = null,
                                tint = if (isSelected) Color.White else SalimExpenseRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = category.nameAr,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) Color.White else SalimTextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Date selector
            Text(
                text = "التاريخ",
                style = MaterialTheme.typography.titleSmall,
                color = SalimDarkEmerald,
                fontWeight = FontWeight.Bold
            )
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SalimSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SalimBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showDatePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = FormatUtils.formatDate(selectedDateMillis),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SalimTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "اختيار التاريخ",
                        tint = SalimDarkEmerald
                    )
                }
            }

            // Notes field
            Text(
                text = "ملاحظات (اختيارية)",
                style = MaterialTheme.typography.titleSmall,
                color = SalimDarkEmerald,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                placeholder = { Text("مثال: وجبة غداء أو فاتورة كهرباء", color = SalimTextMuted) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Save Button
            Button(
                onClick = {
                    val amountCents = FormatUtils.parseAmountToCents(amountText)
                    if (amountCents == null || amountCents <= 0L) {
                        errorMessage = "يرجى إدخال مبلغ صحيح أكبر من صفر"
                    } else if (amountCents > currentBalanceForCurrency) {
                        pendingSaveCents = amountCents
                        showNegativeBalanceDialog = true
                    } else {
                        onSave(
                            amountCents,
                            selectedCurrency,
                            selectedCategory,
                            selectedDateMillis,
                            notesText.ifBlank { null }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_expense_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SalimDarkEmerald)
            ) {
                Text(
                    text = "حفظ المصروف",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
