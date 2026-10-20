package com.example.data.model

enum class TransactionType(val titleAr: String) {
    INCOME("دخل"),
    EXPENSE("مصروف"),
    DEBT_PAYMENT_IN("تحصيل دين"),
    DEBT_PAYMENT_OUT("دفع دين"),
    DEBT_INIT_LENT("إقراض مال"),
    DEBT_INIT_BORROWED("استدانة مال");

    val isPositiveCash: Boolean
        get() = this == INCOME || this == DEBT_PAYMENT_IN || this == DEBT_INIT_BORROWED

    val isNegativeCash: Boolean
        get() = this == EXPENSE || this == DEBT_PAYMENT_OUT || this == DEBT_INIT_LENT
}
