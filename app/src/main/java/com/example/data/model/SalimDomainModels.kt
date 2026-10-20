package com.example.data.model

import com.example.data.local.DebtEntity
import com.example.data.local.DebtPaymentEntity

data class DebtWithDetails(
    val debt: DebtEntity,
    val payments: List<DebtPaymentEntity>
) {
    val totalPaidCents: Long = payments.sumOf { it.amountInCents }
    val remainingCents: Long = kotlin.math.max(0L, debt.originalAmountInCents - totalPaidCents)
    val status: DebtStatus = when {
        totalPaidCents <= 0L -> DebtStatus.OPEN
        remainingCents == 0L -> DebtStatus.FULLY_PAID
        else -> DebtStatus.PARTIALLY_PAID
    }
    val currency: Currency = Currency.fromCode(debt.currency)
    val type: DebtType = DebtType.fromString(debt.type)
}

data class CurrencyBalance(
    val currency: Currency,
    val openingBalanceCents: Long,
    val currentBalanceCents: Long
)

enum class ReportPeriod(val titleAr: String) {
    DAY("اليوم"),
    WEEK("الأسبوع"),
    MONTH("الشهر"),
    YEAR("السنة")
}

data class CategoryShare(
    val categoryName: String,
    val totalCents: Long,
    val percentage: Float
)

data class BarChartItem(
    val label: String,
    val incomeCents: Long,
    val expenseCents: Long
)

data class FinancialReport(
    val currency: Currency,
    val period: ReportPeriod,
    val totalIncomeCents: Long,
    val totalExpenseCents: Long,
    val netCashFlowCents: Long,
    val transactionCount: Int,
    val categoryBreakdown: List<CategoryShare>,
    val chartData: List<BarChartItem>,
    val debtsOwedToMeTotalRemaining: Long,
    val debtsOwedByMeTotalRemaining: Long,
    val debtsRepaidDuringPeriod: Long
)
