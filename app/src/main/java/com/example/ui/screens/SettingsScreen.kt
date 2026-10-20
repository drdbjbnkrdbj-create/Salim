package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Currency
import com.example.data.model.CurrencyBalance
import com.example.data.model.FormatUtils
import com.example.ui.components.SalimHeader
import com.example.ui.theme.SalimBackground
import com.example.ui.theme.SalimBorder
import com.example.ui.theme.SalimDarkEmerald
import com.example.ui.theme.SalimExpenseRed
import com.example.ui.theme.SalimGold
import com.example.ui.theme.SalimLightMint
import com.example.ui.theme.SalimMidGreen
import com.example.ui.theme.SalimSoftMintBg
import com.example.ui.theme.SalimSurface
import com.example.ui.theme.SalimTextMuted
import com.example.ui.theme.SalimTextPrimary
import com.example.ui.theme.SalimTextSecondary

@Composable
fun SettingsScreen(
    currentDefaultCurrency: Currency,
    currencyBalances: List<CurrencyBalance>,
    onSetDefaultCurrency: (Currency) -> Unit,
    onUpdateOpeningBalance: (Currency, Long) -> Unit,
    onExportBackupClick: () -> Unit,
    onImportBackupClick: () -> Unit
) {
    var showOpeningBalancesDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    if (showOpeningBalancesDialog) {
        ManageOpeningBalancesDialog(
            currencyBalances = currencyBalances,
            onDismiss = { showOpeningBalancesDialog = false },
            onSave = { currency, amountCents ->
                onUpdateOpeningBalance(currency, amountCents)
                showOpeningBalancesDialog = false
            }
        )
    }

    if (showCurrencyDialog) {
        SelectDefaultCurrencyDialog(
            current = currentDefaultCurrency,
            onDismiss = { showCurrencyDialog = false },
            onSelect = {
                onSetDefaultCurrency(it)
                showCurrencyDialog = false
            }
        )
    }

    if (showAboutDialog) {
        AboutSalimDialog(onDismiss = { showAboutDialog = false })
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SalimBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SalimHeader(
                title = "الإعدادات",
                subtitle = "إدارة التفضيلات والنسخ الاحتياطي وحساباتك"
            )
        }

        // Profile / Brand Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SalimSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SalimBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.5.dp, SalimGold, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.salim_logo),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "سليم",
                            style = MaterialTheme.typography.titleLarge,
                            color = SalimDarkEmerald,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "حساباتك، بكل بساطة",
                            style = MaterialTheme.typography.bodySmall,
                            color = SalimTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SalimSoftMintBg
                        ) {
                            Text(
                                text = "الإصدار 1.0.0 • محلي وآمن 100%",
                                style = MaterialTheme.typography.labelSmall,
                                color = SalimMidGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Financial Settings Group
        item {
            SettingsGroup(title = "الحسابات والعملات") {
                SettingsRow(
                    icon = Icons.Default.Payments,
                    title = "العملة الأساسية",
                    subtitle = "${currentDefaultCurrency.displayNameAr} (${currentDefaultCurrency.symbolAr})",
                    onClick = { showCurrencyDialog = true }
                )

                HorizontalDivider(color = SalimBorder)

                SettingsRow(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "إدارة الأرصدة الافتتاحية",
                    subtitle = "عرض أو تعديل رصيد البداية للعملات الثلاث",
                    onClick = { showOpeningBalancesDialog = true }
                )
            }
        }

        // Backup & Restore Group
        item {
            SettingsGroup(title = "النسخ الاحتياطي واستعادة البيانات") {
                SettingsRow(
                    icon = Icons.Default.CloudUpload,
                    title = "إنشاء نسخة احتياطية",
                    subtitle = "تصدير جميع الحسابات والديون كملف يمكن نقله إلى هاتف آخر",
                    onClick = onExportBackupClick
                )

                HorizontalDivider(color = SalimBorder)

                SettingsRow(
                    icon = Icons.Default.CloudDownload,
                    title = "استعادة البيانات من نسخة احتياطية",
                    subtitle = "استرجاع الحسابات بأمان من ملف نسخة احتياطية محلي",
                    onClick = onImportBackupClick
                )
            }
        }

        // App Information Group
        item {
            SettingsGroup(title = "حول وسرية البيانات") {
                SettingsRow(
                    icon = Icons.Default.Language,
                    title = "اللغة",
                    subtitle = "العربية (RTL)",
                    onClick = {}
                )

                HorizontalDivider(color = SalimBorder)

                SettingsRow(
                    icon = Icons.Default.Shield,
                    title = "الخصوصية والأمان",
                    subtitle = "تطبيق محلي تماماً لا يتصل بالإنترنت ولا يجمع بياناتك",
                    onClick = {}
                )

                HorizontalDivider(color = SalimBorder)

                SettingsRow(
                    icon = Icons.Default.Info,
                    title = "حول تطبيق سليم",
                    subtitle = "معلومات التطوير وحقوق الملكية",
                    onClick = { showAboutDialog = true }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = SalimDarkEmerald,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SalimSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, SalimBorder)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SalimSoftMintBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SalimDarkEmerald,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SalimTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SalimTextSecondary
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = SalimTextMuted,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun ManageOpeningBalancesDialog(
    currencyBalances: List<CurrencyBalance>,
    onDismiss: () -> Unit,
    onSave: (Currency, Long) -> Unit
) {
    var selectedCurrency by remember { mutableStateOf(Currency.YER) }
    val initialCents = currencyBalances.firstOrNull { it.currency == selectedCurrency }?.openingBalanceCents ?: 0L
    var inputAmount by remember { mutableStateOf(FormatUtils.formatMoney(initialCents, selectedCurrency, includeSymbol = false)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SalimSurface,
        title = {
            Text(
                text = "تعديل الرصيد الافتتاحي",
                style = MaterialTheme.typography.titleLarge,
                color = SalimDarkEmerald,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "اختر العملة التي تريد تحديث رصيدها الافتتاحي:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SalimTextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Currency.entries.forEach { curr ->
                        val isSel = selectedCurrency == curr
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedCurrency = curr
                                    val currCents = currencyBalances.firstOrNull { it.currency == curr }?.openingBalanceCents ?: 0L
                                    inputAmount = FormatUtils.formatMoney(currCents, curr, includeSymbol = false)
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) SalimDarkEmerald else SalimSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) SalimDarkEmerald else SalimBorder)
                        ) {
                            Text(
                                text = "${curr.flagEmoji} ${curr.symbolAr}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSel) Color.White else SalimTextPrimary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = inputAmount,
                    onValueChange = { inputAmount = it },
                    label = { Text("الرصيد الافتتاحي (${selectedCurrency.symbolAr})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ملاحظة: هذا هو الرصيد الأساسي عند بدء استخدام المحفظة ولا يؤثر على دخل أو مصروفات الفترة.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SalimTextMuted
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = FormatUtils.parseAmountToCents(inputAmount) ?: 0L
                    onSave(selectedCurrency, parsed)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SalimDarkEmerald)
            ) {
                Text("حفظ التعديل")
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
private fun SelectDefaultCurrencyDialog(
    current: Currency,
    onDismiss: () -> Unit,
    onSelect: (Currency) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SalimSurface,
        title = {
            Text(
                text = "العملة الأساسية الافتراضية",
                style = MaterialTheme.typography.titleLarge,
                color = SalimDarkEmerald,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Currency.entries.forEach { currency ->
                    val isSelected = current == currency
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(currency) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) SalimSoftMintBg else SalimSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) SalimDarkEmerald else SalimBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(currency.flagEmoji, fontSize = 20.sp)
                            Column {
                                Text(
                                    text = currency.displayNameAr,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SalimTextPrimary
                                )
                                Text(
                                    text = "الرمز: ${currency.symbolAr}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SalimTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = SalimTextMuted)
            }
        }
    )
}

@Composable
private fun AboutSalimDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SalimSurface,
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.5.dp, SalimGold, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.salim_logo),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "سليم (Salim)",
                    style = MaterialTheme.typography.titleLarge,
                    color = SalimDarkEmerald,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "حساباتك، بكل بساطة",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SalimTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SalimSoftMintBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SalimLightMint)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المطوّر:", style = MaterialTheme.typography.bodySmall, color = SalimTextSecondary)
                            Text("محمد سليم مدرك", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SalimDarkEmerald)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("الاسم المستعار:", style = MaterialTheme.typography.bodySmall, color = SalimTextSecondary)
                            Text("q>dim", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SalimDarkEmerald)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("حقوق النشر:", style = MaterialTheme.typography.bodySmall, color = SalimTextSecondary)
                            Text("© 2026 جميع الحقوق محفوظة", style = MaterialTheme.typography.bodySmall, color = SalimTextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "صُمم هذا التطبيق خصيصاً ليكون وسيلة بسيطة، أمينة ومريحة لإدارة الأموال والمصروفات والديون بالريال اليمني والسعودي والدولار الأمريكي دون تعقيد أو حاجة للإنترنت.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SalimTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SalimDarkEmerald),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إغلاق")
            }
        }
    )
}
