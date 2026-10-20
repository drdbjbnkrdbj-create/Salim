package com.example.data.model

enum class Currency(
    val code: String,
    val displayNameAr: String,
    val symbolAr: String,
    val flagEmoji: String,
    val defaultDecimals: Int
) {
    YER(
        code = "YER",
        displayNameAr = "الريال اليمني",
        symbolAr = "ر.ي",
        flagEmoji = "🇾🇪",
        defaultDecimals = 0
    ),
    SAR(
        code = "SAR",
        displayNameAr = "الريال السعودي",
        symbolAr = "ر.س",
        flagEmoji = "🇸🇦",
        defaultDecimals = 2
    ),
    USD(
        code = "USD",
        displayNameAr = "الدولار الأمريكي",
        symbolAr = "$",
        flagEmoji = "🇺🇸",
        defaultDecimals = 2
    );

    companion object {
        fun fromCode(code: String?): Currency {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: YER
        }
    }
}
