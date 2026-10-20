package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionEntity
import com.example.data.model.Currency
import com.example.data.model.DebtStatus
import com.example.data.model.DebtType
import com.example.data.model.DebtWithDetails
import com.example.data.model.FormatUtils
import com.example.data.model.TransactionType
import com.example.ui.theme.SalimBackground
import com.example.ui.theme.SalimBorder
import com.example.ui.theme.SalimDarkEmerald
import com.example.ui.theme.SalimDebtAmber
import com.example.ui.theme.SalimDebtBg
import com.example.ui.theme.SalimDeepForest
import com.example.ui.theme.SalimExpenseBg
import com.example.ui.theme.SalimExpenseRed
import com.example.ui.theme.SalimGold
import com.example.ui.theme.SalimIncomeBg
import com.example.ui.theme.SalimIncomeGreen
import com.example.ui.theme.SalimLightMint
import com.example.ui.theme.SalimMidGreen
import com.example.ui.theme.SalimSoftMintBg
import com.example.ui.theme.SalimSurface
import com.example.ui.theme.SalimTextMuted
import com.example.ui.theme.SalimTextPrimary
import com.example.ui.theme.SalimTextSecondary
import com.example.ui.viewmodel.MainTab

@Composable
fun SalimHeader(
    title: String = "سليم",
    subtitle: String? = "حساباتك، بكل بساطة",
    onSettingsClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(SalimDarkEmerald, SalimMidGreen)
                        )
                    )
                    .border(1.dp, SalimGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = SalimGold,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = SalimDarkEmerald,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = SalimTextMuted
                    )
                }
            }
        }

        if (onSettingsClick != null) {
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SalimSurface)
                    .border(1.dp, SalimBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "الإعدادات",
                    tint = SalimDarkEmerald
                )
            }
        }
    }
}

@Composable
fun SalimScreenTopBar(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(SalimSurface)
                .border(1.dp, SalimBorder, CircleShape)
                .testTag("screen_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "رجوع",
                tint = SalimDarkEmerald
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = SalimDarkEmerald,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SalimBottomBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar(
        containerColor = SalimSurface,
        tonalElevation = 6.dp,
        modifier = Modifier.border(width = 1.dp, color = SalimBorder)
    ) {
        MainTab.entries.forEach { tab ->
            val isSelected = currentTab == tab
            val icon = when (tab) {
                MainTab.HOME -> Icons.Default.Home
                MainTab.TRANSACTIONS -> Icons.Default.SwapHoriz
                MainTab.DEBTS -> Icons.Default.Handshake
                MainTab.REPORTS -> Icons.Default.Assessment
                MainTab.SETTINGS -> Icons.Default.Settings
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.titleAr,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.titleAr,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SalimDarkEmerald,
                    selectedTextColor = SalimDarkEmerald,
                    indicatorColor = SalimLightMint,
                    unselectedIconColor = SalimTextMuted,
                    unselectedTextColor = SalimTextMuted
                ),
                modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
            )
        }
    }
}

@Composable
fun SalimHeroBalanceCard(
    currency: Currency,
    balanceCents: Long,
    onSwitchCurrencyClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable { onSwitchCurrencyClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SalimDarkEmerald),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            SalimMidGreen.copy(alpha = 0.5f),
                            SalimDeepForest
                        ),
                        radius = 800f
                    )
                )
                .padding(22.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "الرصيد المتاح حالياً",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SalimLightMint.copy(alpha = 0.85f)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = currency.flagEmoji,
                                fontSize = 16.sp
                            )
                            Text(
                                text = currency.displayNameAr,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(SalimGold.copy(alpha = 0.15f))
                            .border(1.dp, SalimGold.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = SalimGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = FormatUtils.formatMoney(balanceCents, currency, includeSymbol = true),
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "اضغط للتبديل بين العملات",
                    style = MaterialTheme.typography.labelSmall,
                    color = SalimLightMint.copy(alpha = 0.65f)
                )
            }
        }
    }
}

@Composable
fun MiniCurrencyCard(
    currency: Currency,
    balanceCents: Long,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("currency_card_${currency.code}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SalimSoftMintBg else SalimSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.8.dp else 1.dp,
            color = if (isSelected) SalimMidGreen else SalimBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currency.flagEmoji,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currency.displayNameAr,
                style = MaterialTheme.typography.labelSmall,
                color = SalimTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = FormatUtils.formatMoney(balanceCents, currency, includeSymbol = true),
                style = MaterialTheme.typography.titleSmall,
                color = if (balanceCents < 0) SalimExpenseRed else SalimDarkEmerald,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SalimActionButton(
    title: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TransactionRowItem(
    transaction: TransactionEntity,
    onClick: () -> Unit = {},
    onDelete: (() -> Unit)? = null
) {
    val currency = Currency.fromCode(transaction.currency)
    val type = runCatching { TransactionType.valueOf(transaction.type) }.getOrNull() ?: TransactionType.EXPENSE

    val isPositive = type.isPositiveCash
    val badgeColor = if (isPositive) SalimIncomeGreen else SalimExpenseRed
    val badgeBg = if (isPositive) SalimIncomeBg else SalimExpenseBg
    val prefix = if (isPositive) "+" else "-"

    val categoryIcon = getCategoryIcon(transaction.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SalimSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, SalimBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.titleMedium,
                        color = SalimTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = FormatUtils.formatDate(transaction.dateTimestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = SalimTextMuted
                        )
                        if (!transaction.notes.isNullOrBlank()) {
                            Text(
                                text = "•  ${transaction.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SalimTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "$prefix${FormatUtils.formatMoney(transaction.amountInCents, currency)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = badgeColor,
                    fontWeight = FontWeight.Bold
                )

                if (onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف العملية",
                            tint = SalimTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DebtCardItem(
    debtDetails: DebtWithDetails,
    onRecordPayment: () -> Unit,
    onViewHistory: () -> Unit,
    onDelete: () -> Unit
) {
    val debt = debtDetails.debt
    val currency = debtDetails.currency
    val type = debtDetails.type

    val isOwedToMe = type == DebtType.OWED_TO_ME
    val typeColor = if (isOwedToMe) SalimIncomeGreen else SalimExpenseRed
    val typeBg = if (isOwedToMe) SalimIncomeBg else SalimExpenseBg

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onViewHistory() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SalimSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, SalimBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(typeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = typeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = debt.personName,
                            style = MaterialTheme.typography.titleMedium,
                            color = SalimTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = typeBg,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = type.shortLabelAr,
                                style = MaterialTheme.typography.labelSmall,
                                color = typeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Debt status badge
                val statusText = when (debtDetails.status) {
                    DebtStatus.OPEN -> "مفتوح"
                    DebtStatus.PARTIALLY_PAID -> "مسدد جزئياً"
                    DebtStatus.FULLY_PAID -> "مسدد بالكامل"
                }
                val statusColor = when (debtDetails.status) {
                    DebtStatus.OPEN -> SalimDebtAmber
                    DebtStatus.PARTIALLY_PAID -> SalimMidGreen
                    DebtStatus.FULLY_PAID -> SalimDarkEmerald
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Amounts row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "المبلغ الأصلي",
                        style = MaterialTheme.typography.labelSmall,
                        color = SalimTextMuted
                    )
                    Text(
                        text = FormatUtils.formatMoney(debt.originalAmountInCents, currency),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SalimTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "المبلغ المتبقي",
                        style = MaterialTheme.typography.labelSmall,
                        color = SalimTextMuted
                    )
                    Text(
                        text = FormatUtils.formatMoney(debtDetails.remainingCents, currency),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (debtDetails.remainingCents > 0) typeColor else SalimTextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (debtDetails.remainingCents > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onRecordPayment,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SalimDarkEmerald
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تسجيل سداد",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onViewHistory,
                        modifier = Modifier.height(42.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "السجل",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

fun getCategoryIcon(categoryName: String): ImageVector {
    return when (categoryName) {
        "راتب" -> Icons.Default.Payments
        "تجارة" -> Icons.Default.ShoppingBag
        "دخل إضافي" -> Icons.Default.AccountBalanceWallet
        "بيع" -> Icons.Default.ReceiptLong
        "هدية" -> Icons.Default.AccountBalanceWallet
        "استثمار" -> Icons.AutoMirrored.Filled.TrendingUp
        "طعام ومشروبات" -> Icons.Default.Restaurant
        "مواصلات" -> Icons.Default.DirectionsCar
        "فواتير" -> Icons.Default.ReceiptLong
        "الصحة" -> Icons.Default.MedicalServices
        "المنزل" -> Icons.Default.Home
        "التعليم" -> Icons.Default.School
        "المشتريات" -> Icons.Default.ShoppingBag
        "مصروفات شخصية" -> Icons.Default.Person
        "دين", "سداد دين" -> Icons.Default.Handshake
        else -> Icons.Default.Category
    }
}
