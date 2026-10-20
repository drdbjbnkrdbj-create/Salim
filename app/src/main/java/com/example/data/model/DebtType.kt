package com.example.data.model

enum class DebtType(
    val titleAr: String,
    val shortLabelAr: String,
    val descriptionAr: String
) {
    OWED_TO_ME(
        titleAr = "أنت دائن للآخرين",
        shortLabelAr = "لك عند الآخرين",
        descriptionAr = "مال لك عند شخص آخر وتريد استرداده"
    ),
    OWED_BY_ME(
        titleAr = "أنت مدين للآخرين",
        shortLabelAr = "عليك للآخرين",
        descriptionAr = "مال اقترضته من شخص آخر ويجب سداده"
    );

    companion object {
        fun fromString(value: String?): DebtType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OWED_TO_ME
        }
    }
}

enum class DebtStatus(val titleAr: String) {
    OPEN("مفتوح"),
    PARTIALLY_PAID("مسدد جزئياً"),
    FULLY_PAID("مسدد بالكامل")
}
