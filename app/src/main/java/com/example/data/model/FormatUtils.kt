package com.example.data.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {
    private const val FACTOR: Long = 100L

    /**
     * Parses a string entered by user (e.g. "25000", "150.50", "1,200.75") into
     * integer cents (minor units). Returns null if invalid or negative.
     */
    fun parseAmountToCents(rawInput: String): Long? {
        val sanitized = rawInput.trim()
            .replace("،", ".")
            .replace(",", "")
            .replace("٫", ".")
            .replace(" ", "")

        if (sanitized.isEmpty()) return null

        return try {
            val parts = sanitized.split(".")
            if (parts.size > 2) return null
            val wholePart = parts[0].toLongOrNull() ?: return null
            if (wholePart < 0) return null

            val decimalPart = if (parts.size == 2) {
                val decString = parts[1].take(2).padEnd(2, '0')
                decString.toLongOrNull() ?: 0L
            } else {
                0L
            }
            (wholePart * FACTOR) + decimalPart
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Converts minor units (cents) to a formatted string.
     * E.g. 25000000L -> "250,000" for YER, or "1,500.00" for SAR/USD if decimal exists.
     */
    fun formatMoney(amountInCents: Long, currency: Currency, includeSymbol: Boolean = true): String {
        val isNegative = amountInCents < 0
        val absCents = kotlin.math.abs(amountInCents)

        val wholePart = absCents / FACTOR
        val decimalPart = absCents % FACTOR

        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
        }
        val df = DecimalFormat("#,##0", symbols)
        val formattedWhole = df.format(wholePart)

        val formattedNumber = if (currency.defaultDecimals > 0 && decimalPart > 0) {
            val decStr = String.format(Locale.US, "%02d", decimalPart).trimEnd('0')
            "$formattedWhole.$decStr"
        } else {
            formattedWhole
        }

        val prefix = if (isNegative) "-" else ""
        return if (includeSymbol) {
            "$prefix$formattedNumber ${currency.symbolAr}"
        } else {
            "$prefix$formattedNumber"
        }
    }

    fun formatDate(timestamp: Long, pattern: String = "yyyy/MM/dd"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateWithTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
