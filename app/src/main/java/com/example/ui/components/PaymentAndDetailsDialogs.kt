package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.DebtWithDetails
import com.example.data.model.FormatUtils
import com.example.ui.theme.SalimBorder
import com.example.ui.theme.SalimDarkEmerald
import com.example.ui.theme.SalimDebtAmber
import com.example.ui.theme.SalimExpenseRed
import com.example.ui.theme.SalimMidGreen
import com.example.ui.theme.SalimSurface
import com.example.ui.theme.SalimTextMuted
import com.example.ui.theme.SalimTextPrimary
import com.example.ui.theme.SalimTextSecondary

@Composable
fun RecordPaymentDialog(
    debtDetails: DebtWithDetails,
    onDismiss: () -> Unit,
    onConfirm: (amountCents: Long, notes: String?, updateCash: Boolean) -> Unit
) {
    var amountInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var updateCash by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currency = debtDetails.currency
    val remainingCents = debtDetails.remainingCents

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SalimSurface,
        title = {
            Text(
                text = "تسجيل سداد دين",
                style = MaterialTheme.typography.titleLarge,
                color = SalimDarkEmerald,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "الطرف: ${debtDetails.debt.personName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SalimTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "المبلغ المتبقي: ${FormatUtils.formatMoney(remainingCents, currency)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SalimTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = {
                        amountInput = it
                        errorMessage = null
                    },
                    label = { Text("مبلغ السداد (${currency.symbolAr})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = errorMessage != null
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = SalimExpenseRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("ملاحظة اختيارية (مثال: دفعة أولى)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = updateCash,
                        onCheckedChange = { updateCash = it },
                        colors = CheckboxDefaults.colors(checkedColor = SalimDarkEmerald)
                    )
                    Text(
                        text = "تسجيل حركة نقدية في رصيد المحفظة",
                        style = MaterialTheme.typography.bodySmall,
                        color = SalimTextPrimary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = FormatUtils.parseAmountToCents(amountInput)
                    if (parsed == null || parsed <= 0L) {
                        errorMessage = "يرجى إدخال مبلغ سداد صالح أكبر من صفر"
                    } else if (parsed > remainingCents) {
                        errorMessage = "مبلغ السداد لا يمكن أن يتجاوز المتبقي (${FormatUtils.formatMoney(remainingCents, currency)})"
                    } else {
                        onConfirm(parsed, notesInput.ifBlank { null }, updateCash)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SalimDarkEmerald)
            ) {
                Text("حفظ السداد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = SalimTextMuted)
            }
        }
    )
}

@Composable
fun DebtHistoryDialog(
    debtDetails: DebtWithDetails,
    onDismiss: () -> Unit,
    onDeleteDebt: () -> Unit
) {
    val debt = debtDetails.debt
    val currency = debtDetails.currency

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SalimSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "تفاصيل وسجل الدين",
                        style = MaterialTheme.typography.titleLarge,
                        color = SalimDarkEmerald,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = debt.personName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SalimTextSecondary
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SalimBorder.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المبلغ الأصلي:", style = MaterialTheme.typography.bodySmall, color = SalimTextMuted)
                            Text(FormatUtils.formatMoney(debt.originalAmountInCents, currency), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("إجمالي المسدد:", style = MaterialTheme.typography.bodySmall, color = SalimTextMuted)
                            Text(FormatUtils.formatMoney(debtDetails.totalPaidCents, currency), color = SalimMidGreen, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المتبقي:", style = MaterialTheme.typography.bodySmall, color = SalimTextMuted)
                            Text(FormatUtils.formatMoney(debtDetails.remainingCents, currency), color = SalimExpenseRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "سجل عمليات السداد (${debtDetails.payments.size}):",
                    style = MaterialTheme.typography.titleSmall,
                    color = SalimDarkEmerald,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (debtDetails.payments.isEmpty()) {
                    Text(
                        text = "لا توجد عمليات سداد مسجلة حتى الآن.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SalimTextMuted,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        items(debtDetails.payments) { payment ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = FormatUtils.formatDate(payment.paymentDate),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SalimTextMuted
                                    )
                                    if (!payment.notes.isNullOrBlank()) {
                                        Text(
                                            text = payment.notes,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SalimTextSecondary
                                        )
                                    }
                                }
                                Text(
                                    text = FormatUtils.formatMoney(payment.amountInCents, currency),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SalimMidGreen
                                )
                            }
                            HorizontalDivider(color = SalimBorder)
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = onDeleteDebt,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SalimExpenseRed)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(4.dp))
                Text("حذف الدين")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = SalimDarkEmerald)
            }
        }
    )
}

@Composable
fun NegativeBalanceDialog(
    amountFormatted: String,
    currentBalanceFormatted: String,
    onProceed: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SalimSurface,
        icon = {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = SalimDebtAmber,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "تنبيه: المصروف يتجاوز الرصيد المتاح",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SalimDarkEmerald
            )
        },
        text = {
            Column {
                Text(
                    text = "المبلغ المطلوب صرفه ($amountFormatted) أكبر من الرصيد الحالي المتوفر في المحفظة ($currentBalanceFormatted).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SalimTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "إذا كنت قد صرفت هذا المبلغ فعلاً وتريد تسجيله، يمكنك المتابعة وسيصبح الرصيد سالباً لحين تغذيته.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SalimTextSecondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onProceed,
                colors = ButtonDefaults.buttonColors(containerColor = SalimDarkEmerald)
            ) {
                Text("متابعة وحفظ")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("تراجع")
            }
        }
    )
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SalimSurface,
        title = {
            Text(text = title, fontWeight = FontWeight.Bold, color = SalimExpenseRed)
        },
        text = {
            Text(text = message, color = SalimTextPrimary)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = SalimExpenseRed)
            ) {
                Text("تأكيد الحذف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = SalimTextMuted)
            }
        }
    )
}
