package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BarChartItem
import com.example.data.model.CategoryShare
import com.example.data.model.Currency
import com.example.data.model.FinancialReport
import com.example.data.model.FormatUtils
import com.example.data.model.ReportPeriod
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
import com.example.ui.theme.SalimMidGreen
import com.example.ui.theme.SalimSoftMintBg
import com.example.ui.theme.SalimSurface
import com.example.ui.theme.SalimTextMuted
import com.example.ui.theme.SalimTextPrimary
import com.example.ui.theme.SalimTextSecondary

private val DonutColors = listOf(
    Color(0xFF2E7D52), // Mid Green
    Color(0xFFE53935), // Red
    Color(0xFF1E88E5), // Blue
    Color(0xFFF59E0B), // Amber
    Color(0xFF8E24AA), // Purple
    Color(0xFF00ACC1), // Cyan
    Color(0xFFD81B60), // Pink
    Color(0xFF43A047), // Green
    Color(0xFF78909C)  // Blue Grey
)

@Composable
fun ReportsScreen(
    report: FinancialReport?,
    selectedCurrency: Currency,
    selectedPeriod: ReportPeriod,
    onSelectCurrency: (Currency) -> Unit,
    onSelectPeriod: (ReportPeriod) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SalimBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SalimHeader(
                title = "التقارير والإحصائيات",
                subtitle = "نظرة واضحة على حركة أموالك ونشاطك المالي"
            )
        }

        // Period Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportPeriod.entries.forEach { period ->
                    val isSelected = selectedPeriod == period
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectPeriod(period) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) SalimDarkEmerald else SalimSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) SalimDarkEmerald else SalimBorder)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = period.titleAr,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) Color.White else SalimTextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Currency Filter Selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Currency.entries.forEach { currency ->
                    val isSelected = selectedCurrency == currency
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectCurrency(currency) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) SalimLightMint else SalimSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) SalimDarkEmerald else SalimBorder
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
                                color = SalimDarkEmerald,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        if (report == null || (report.totalIncomeCents == 0L && report.totalExpenseCents == 0L && report.debtsRepaidDuringPeriod == 0L)) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SalimSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SalimBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد حركات مالية مسجلة لهذه الفترة (${selectedPeriod.titleAr}) بـ ${selectedCurrency.displayNameAr}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SalimTextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // 4 Summary Metrics Cards (2x2 grid)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SummaryMetricCard(
                            title = "إجمالي المدخول",
                            valueFormatted = FormatUtils.formatMoney(report.totalIncomeCents, selectedCurrency),
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            accentColor = SalimIncomeGreen,
                            bgColor = SalimIncomeBg,
                            modifier = Modifier.weight(1f)
                        )

                        SummaryMetricCard(
                            title = "إجمالي المصروف",
                            valueFormatted = FormatUtils.formatMoney(report.totalExpenseCents, selectedCurrency),
                            icon = Icons.AutoMirrored.Filled.TrendingDown,
                            accentColor = SalimExpenseRed,
                            bgColor = SalimExpenseBg,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SummaryMetricCard(
                            title = "صافي الحركة المالية",
                            valueFormatted = FormatUtils.formatMoney(report.netCashFlowCents, selectedCurrency),
                            icon = Icons.Default.AccountBalance,
                            accentColor = if (report.netCashFlowCents >= 0) SalimDarkEmerald else SalimExpenseRed,
                            bgColor = if (report.netCashFlowCents >= 0) SalimSoftMintBg else SalimExpenseBg,
                            modifier = Modifier.weight(1f)
                        )

                        SummaryMetricCard(
                            title = "عدد العمليات",
                            valueFormatted = "${report.transactionCount} عملية",
                            icon = Icons.Default.ListAlt,
                            accentColor = SalimDarkEmerald,
                            bgColor = SalimSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Income vs Expense Bar Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SalimSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SalimBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مقارنة الدخل والمصروف",
                                style = MaterialTheme.typography.titleMedium,
                                color = SalimDarkEmerald,
                                fontWeight = FontWeight.Bold
                            )

                            // Legend
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(SalimIncomeGreen)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("دخل", style = MaterialTheme.typography.labelSmall, color = SalimTextSecondary)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(SalimExpenseRed)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("مصروف", style = MaterialTheme.typography.labelSmall, color = SalimTextSecondary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Render Canvas Bar Chart
                        IncomeExpenseBarChart(
                            items = report.chartData,
                            currency = selectedCurrency,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                        )
                    }
                }
            }

            // Expense Categories Donut Distribution
            if (report.categoryBreakdown.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SalimSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SalimBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "توزيع المصروفات حسب التصنيف",
                                style = MaterialTheme.typography.titleMedium,
                                color = SalimDarkEmerald,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Donut Chart Graphic
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                DonutChart(
                                    items = report.categoryBreakdown,
                                    modifier = Modifier.size(170.dp)
                                )

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "إجمالي المصروف",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SalimTextMuted
                                    )
                                    Text(
                                        text = FormatUtils.formatMoney(report.totalExpenseCents, selectedCurrency),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = SalimDarkEmerald,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Breakdown items
                            report.categoryBreakdown.forEachIndexed { index, share ->
                                val color = DonutColors[index % DonutColors.size]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Text(
                                            text = share.categoryName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = SalimTextPrimary
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = "${String.format("%.1f", share.percentage)}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SalimTextMuted
                                        )
                                        Text(
                                            text = FormatUtils.formatMoney(share.totalCents, selectedCurrency),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = SalimTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Debts Overview for Selected Currency
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SalimSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SalimBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "موقف الديون بالـ ${selectedCurrency.displayNameAr}",
                            style = MaterialTheme.typography.titleMedium,
                            color = SalimDarkEmerald,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        DebtMetricRow(
                            label = "ديون لك مستحقة عند الآخرين:",
                            amountFormatted = FormatUtils.formatMoney(report.debtsOwedToMeTotalRemaining, selectedCurrency),
                            color = SalimIncomeGreen
                        )

                        DebtMetricRow(
                            label = "ديون عليك مطلوبة للآخرين:",
                            amountFormatted = FormatUtils.formatMoney(report.debtsOwedByMeTotalRemaining, selectedCurrency),
                            color = SalimExpenseRed
                        )

                        DebtMetricRow(
                            label = "مبالغ سددت خلال هذه الفترة:",
                            amountFormatted = FormatUtils.formatMoney(report.debtsRepaidDuringPeriod, selectedCurrency),
                            color = SalimMidGreen
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SummaryMetricCard(
    title: String,
    valueFormatted: String,
    icon: ImageVector,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, SalimBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = SalimTextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = valueFormatted,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DebtMetricRow(
    label: String,
    amountFormatted: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = SalimTextSecondary
        )
        Text(
            text = amountFormatted,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun IncomeExpenseBarChart(
    items: List<BarChartItem>,
    currency: Currency,
    modifier: Modifier = Modifier
) {
    val maxVal = items.maxOfOrNull { kotlin.math.max(it.incomeCents, it.expenseCents) } ?: 1L
    val effectiveMax = if (maxVal <= 0L) 1L else maxVal

    Column(modifier = modifier) {
        Box(modifier = Modifier.weight(1f)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val count = items.size
                if (count == 0) return@Canvas

                val slotWidth = canvasWidth / count
                val barWidth = (slotWidth * 0.28f).coerceAtMost(28.dp.toPx())
                val spaceBetweenBars = 4.dp.toPx()

                items.forEachIndexed { i, item ->
                    val centerX = (i * slotWidth) + (slotWidth / 2f)

                    // Income bar (green)
                    val incomeHeight = (item.incomeCents.toFloat() / effectiveMax.toFloat()) * canvasHeight
                    val incomeLeft = centerX - barWidth - (spaceBetweenBars / 2f)
                    val incomeTop = canvasHeight - incomeHeight

                    if (incomeHeight > 0) {
                        drawRoundRect(
                            color = SalimIncomeGreen,
                            topLeft = Offset(incomeLeft, incomeTop),
                            size = Size(barWidth, incomeHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                        )
                    }

                    // Expense bar (red)
                    val expenseHeight = (item.expenseCents.toFloat() / effectiveMax.toFloat()) * canvasHeight
                    val expenseLeft = centerX + (spaceBetweenBars / 2f)
                    val expenseTop = canvasHeight - expenseHeight

                    if (expenseHeight > 0) {
                        drawRoundRect(
                            color = SalimExpenseRed,
                            topLeft = Offset(expenseLeft, expenseTop),
                            size = Size(barWidth, expenseHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                        )
                    }
                }
            }
        }

        // Labels below bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items.forEach { item ->
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = SalimTextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun DonutChart(
    items: List<CategoryShare>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 24.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
        val arcSize = Size(diameter, diameter)

        var startAngle = -90f
        items.forEachIndexed { index, share ->
            val sweepAngle = (share.percentage / 100f) * 360f
            val color = DonutColors[index % DonutColors.size]

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweepAngle
        }
    }
}
