package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.TransactionEntity
import com.example.data.model.Currency
import com.example.data.model.TransactionType
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.SalimHeader
import com.example.ui.components.TransactionRowItem
import com.example.ui.theme.SalimBackground
import com.example.ui.theme.SalimBorder
import com.example.ui.theme.SalimDarkEmerald
import com.example.ui.theme.SalimLightMint
import com.example.ui.theme.SalimSurface
import com.example.ui.theme.SalimTextMuted
import com.example.ui.theme.SalimTextPrimary
import java.util.Calendar

enum class TxFilterType(val titleAr: String) {
    ALL("الكل"),
    INCOME("دخل"),
    EXPENSE("مصروف"),
    DEBT("ديون وسداد")
}

@Composable
fun TransactionsListScreen(
    transactions: List<TransactionEntity>,
    onDeleteTransaction: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf(TxFilterType.ALL) }
    var selectedCurrencyFilter by remember { mutableStateOf<Currency?>(null) }
    var txToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    if (txToDelete != null) {
        ConfirmDeleteDialog(
            title = "حذف العملية",
            message = "هل أنت متأكد من رغبتك في حذف هذه العملية نهائياً؟ سيتم إعادة احتساب الرصيد تلقائياً.",
            onConfirm = {
                onDeleteTransaction(txToDelete!!.id)
                txToDelete = null
            },
            onDismiss = { txToDelete = null }
        )
    }

    val filteredTransactions = transactions.filter { tx ->
        // Currency filter
        val matchesCurrency = selectedCurrencyFilter == null || tx.currency == selectedCurrencyFilter?.code

        // Type filter
        val matchesType = when (selectedTypeFilter) {
            TxFilterType.ALL -> true
            TxFilterType.INCOME -> tx.type == TransactionType.INCOME.name
            TxFilterType.EXPENSE -> tx.type == TransactionType.EXPENSE.name
            TxFilterType.DEBT -> tx.type != TransactionType.INCOME.name && tx.type != TransactionType.EXPENSE.name
        }

        // Search text
        val query = searchQuery.trim().lowercase()
        val matchesSearch = query.isEmpty() ||
                tx.category.lowercase().contains(query) ||
                (tx.notes?.lowercase()?.contains(query) == true)

        matchesCurrency && matchesType && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SalimBackground)
            .padding(horizontal = 16.dp)
    ) {
        SalimHeader(
            title = "سجل العمليات",
            subtitle = "كشف حساب مفصل لجميع الحركات المالية"
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث في التصنيفات والملاحظات...", color = SalimTextMuted) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = SalimDarkEmerald)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "مسح", tint = SalimTextMuted)
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("tx_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Type filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TxFilterType.entries.forEach { filter ->
                val isSelected = selectedTypeFilter == filter
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectedTypeFilter = filter },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) SalimDarkEmerald else SalimSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) SalimDarkEmerald else SalimBorder)
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter.titleAr,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) Color.White else SalimTextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Currency filter row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val allSelected = selectedCurrencyFilter == null
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { selectedCurrencyFilter = null },
                shape = RoundedCornerShape(8.dp),
                color = if (allSelected) SalimLightMint else SalimSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (allSelected) SalimDarkEmerald else SalimBorder)
            ) {
                Text(
                    text = "جميع العملات",
                    style = MaterialTheme.typography.labelSmall,
                    color = SalimDarkEmerald,
                    fontWeight = if (allSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Currency.entries.forEach { curr ->
                val isSelected = selectedCurrencyFilter == curr
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedCurrencyFilter = curr },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) SalimLightMint else SalimSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) SalimDarkEmerald else SalimBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(curr.flagEmoji)
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = curr.symbolAr,
                            style = MaterialTheme.typography.labelSmall,
                            color = SalimDarkEmerald,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Transactions list
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (transactions.isEmpty()) "لا توجد أي عمليات مسجلة بعد." else "لا توجد نتائج مطابقة لخيارات التصفية.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SalimTextMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTransactions, key = { it.id }) { tx ->
                    TransactionRowItem(
                        transaction = tx,
                        onDelete = { txToDelete = tx }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
