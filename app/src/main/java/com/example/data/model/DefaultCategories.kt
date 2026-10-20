package com.example.data.model

data class CategoryItem(
    val nameAr: String,
    val iconName: String
)

object DefaultCategories {
    val incomeCategories = listOf(
        CategoryItem("راتب", "payments"),
        CategoryItem("تجارة", "store"),
        CategoryItem("دخل إضافي", "savings"),
        CategoryItem("بيع", "sell"),
        CategoryItem("هدية", "card_giftcard"),
        CategoryItem("استثمار", "trending_up"),
        CategoryItem("أخرى", "more_horiz")
    )

    val expenseCategories = listOf(
        CategoryItem("طعام ومشروبات", "restaurant"),
        CategoryItem("مواصلات", "directions_car"),
        CategoryItem("فواتير", "receipt_long"),
        CategoryItem("الصحة", "medical_services"),
        CategoryItem("المنزل", "home"),
        CategoryItem("التعليم", "school"),
        CategoryItem("المشتريات", "shopping_bag"),
        CategoryItem("مصروفات شخصية", "person"),
        CategoryItem("أخرى", "category")
    )
}
