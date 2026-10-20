package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import com.example.data.model.DebtType
import com.example.data.model.FormatUtils
import com.example.ui.components.SalimScreenTopBar
import com.example.ui.theme.SalimBackground
import com.example.ui.theme.SalimBorder
import com.example.ui.theme.SalimDarkEmerald
import com.example.ui.theme.SalimDebtAmber
import com.example.ui.theme.SalimDebtBg
import com.example.ui.theme.SalimExpenseRed
import com.example.ui.theme.SalimIncomeBg
import com.example.ui.theme.SalimIncomeGreen
import com.example.ui.theme.SalimSurface
import com.example.ui.theme.SalimTextMuted
import com.example.ui.theme.SalimTextPrimary
import com.example.ui.theme.SalimTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtScreen(
    initialCurrency: Currency,
    onBack: () -> Unit,
    onSave: (
        personName: String,
        amountCents: Long,
        currency: Currency,
        type: DebtType,
        startDate: Long,
        dueDate: Long?,
        notes: String?,
        isCashTransferred: Boolean
    ) -> Unit
) {
    var personName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf(initialCurrency) }
    var selectedType by remember { mutableStateOf(DebtType.OWED_TO_ME) }
    var startDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var dueDateMillis by remember { mutableStateOf<Long?>(null) }
    var notesText by remember { mutableStateOf("") }
    var isCashTransferred by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }

    val startDatePickerState = rememberDatePickerState(initialSelectedDateMillis = startDateMillis)
    val dueDatePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis() + 86400000L * 30L)

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let { startDateMillis = it }
                    showStartDatePicker = false
                }) {
                    Text("موافق", color = SalimDarkEmerald)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("إلغاء", color = SalimTextMuted)
                }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    if (showDueDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDueDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDateMillis = dueDatePickerState.selectedDateMillis
                    showDueDatePicker = false
                }) {
                    Text("موافق", color = SalimDarkEmerald)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDueDatePicker = false }) {
                    Text("إلغاء", color = SalimTextMuted)
                }
            }
        ) {
            DatePicker(state = dueDatePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SalimBackground)
    ) {
        SalimScreenTopBar(
            title = "إضافة دين جديد",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Person Name
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SalimSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SalimBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "اسم الشخص أو الطرف الآخر",
                        style = MaterialTheme.typography.labelMedium,
                        color = SalimTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = personName,
                        onValueChange = {
                            personName = it
                            errorMessage = null
                        },
                        placeholder = { Text("مثال: أحمد محمد", color = SalimTextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_debt_person_name"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            // Amount input card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SalimSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SalimBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "مبلغ الدين الأصلي",
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
                        placeholder = { Text("مثال: 10000", color = SalimTextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_debt_amount"),
                        shape = RoundedCornerShape(12.dp),
                        isError = errorMessage != null,
                        trailingIcon = {
                            Text(
                                text = selectedCurrency.symbolAr,
                                style = MaterialTheme.typography.titleMedium,
                                color = SalimDebtAmber,
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
                            .testTag("select_debt_currency_${currency.code.lowercase()}"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) SalimDebtBg else SalimSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) SalimDebtAmber else SalimBorder
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
                                color = if (isSelected) SalimDebtAmber else SalimTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Debt Type selector
            Text(
                text = "نوع الدين",
                style = MaterialTheme.typography.titleSmall,
                color = SalimDarkEmerald,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DebtType.entries.forEach { type ->
                    val isSelected = selectedType == type
                    val activeColor = if (type == DebtType.OWED_TO_ME) SalimIncomeGreen else SalimExpenseRed
                    val activeBg = if (type == DebtType.OWED_TO_ME) SalimIncomeBg else SalimExpenseRed.copy(alpha = 0.1f)

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedType = type }
                            .testTag("select_debt_type_${type.name.lowercase()}"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) activeBg else SalimSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.8.dp else 1.dp,
                            color = if (isSelected) activeColor else SalimBorder
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = type.titleAr,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isSelected) activeColor else SalimTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "(${type.shortLabelAr})",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) activeColor else SalimTextMuted
                            )
                        }
                    }
                }
            }

            // Start Date
            Text(
                text = "تاريخ تسجيل الدين",
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
                    .clickable { showStartDatePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = FormatUtils.formatDate(startDateMillis),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SalimTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = SalimDarkEmerald)
                }
            }

            // Due Date (Optional)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تاريخ الاستحقاق (اختياري)",
                    style = MaterialTheme.typography.titleSmall,
                    color = SalimDarkEmerald,
                    fontWeight = FontWeight.Bold
                )
                if (dueDateMillis != null) {
                    TextButton(onClick = { dueDateMillis = null }) {
                        Text("إلغاء التحديد", color = SalimExpenseRed, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SalimSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SalimBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showDueDatePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (dueDateMillis != null) FormatUtils.formatDate(dueDateMillis!!) else "بدون موعد استحقاق محدد",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (dueDateMillis != null) SalimTextPrimary else SalimTextMuted
                    )
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = SalimDarkEmerald)
                }
            }

            // Cash movement checkbox
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SalimSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SalimBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = isCashTransferred,
                        onCheckedChange = { isCashTransferred = it },
                        colors = CheckboxDefaults.colors(checkedColor = SalimDarkEmerald)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "تسجيل حركة نقدية فورية في المحفظة",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SalimTextPrimary
                        )
                        Text(
                            text = if (selectedType == DebtType.OWED_TO_ME) {
                                "سيتم خصم هذا المبلغ فوراً من الرصيد النقدي كمال سُلم باليد للشخص."
                            } else {
                                "سيتم إضافة هذا المبلغ فوراً إلى الرصيد النقدي كمال استلمته باليد من الشخص."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = SalimTextSecondary
                        )
                    }
                }
            }

            // Notes
            Text(
                text = "ملاحظات (اختيارية)",
                style = MaterialTheme.typography.titleSmall,
                color = SalimDarkEmerald,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                placeholder = { Text("مثال: قرض شخصي أو ثمن بضاعة مؤجلة", color = SalimTextMuted) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Save Button
            Button(
                onClick = {
                    val amountCents = FormatUtils.parseAmountToCents(amountText)
                    if (personName.trim().isEmpty()) {
                        errorMessage = "يرجى كتابة اسم الشخص أو الطرف الآخر"
                    } else if (amountCents == null || amountCents <= 0L) {
                        errorMessage = "يرجى إدخال مبلغ دين صالح أكبر من صفر"
                    } else {
                        onSave(
                            personName.trim(),
                            amountCents,
                            selectedCurrency,
                            selectedType,
                            startDateMillis,
                            dueDateMillis,
                            notesText.ifBlank { null },
                            isCashTransferred
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_debt_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SalimDarkEmerald)
            ) {
                Text(
                    text = "حفظ الدين",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
