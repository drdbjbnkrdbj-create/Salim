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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Currency
import com.example.data.model.FormatUtils
import com.example.ui.theme.SalimBackground
import com.example.ui.theme.SalimBorder
import com.example.ui.theme.SalimDarkEmerald
import com.example.ui.theme.SalimGold
import com.example.ui.theme.SalimLightMint
import com.example.ui.theme.SalimMidGreen
import com.example.ui.theme.SalimSoftMintBg
import com.example.ui.theme.SalimSurface
import com.example.ui.theme.SalimTextMuted
import com.example.ui.theme.SalimTextPrimary
import com.example.ui.theme.SalimTextSecondary

@Composable
fun InitialSetupScreen(
    onComplete: (yerCents: Long, sarCents: Long, usdCents: Long, defaultCurrency: Currency) -> Unit
) {
    var yerInput by remember { mutableStateOf("") }
    var sarInput by remember { mutableStateOf("") }
    var usdInput by remember { mutableStateOf("") }
    var selectedDefaultCurrency by remember { mutableStateOf(Currency.YER) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SalimBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, SalimGold, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.salim_logo),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "مرحباً بك في سليم",
            style = MaterialTheme.typography.headlineMedium,
            color = SalimDarkEmerald,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "إعداد الأرصدة الافتتاحية لمحفظتك",
            style = MaterialTheme.typography.bodyMedium,
            color = SalimTextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Info card explaining opening balances
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SalimSoftMintBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, SalimLightMint),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = SalimMidGreen,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "الرصيد الافتتاحي هو المبلغ النقدي المتوفر معك حالياً عند بدء التطبيق. لن يُسجل كدخل تشغيلي، ويمكنك إدخال 0 أو ترك العملة فارغة إذا لم تكن تستخدمها حالياً.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SalimDarkEmerald,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // YER field
        BalanceInputField(
            currency = Currency.YER,
            value = yerInput,
            onValueChange = { yerInput = it }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // SAR field
        BalanceInputField(
            currency = Currency.SAR,
            value = sarInput,
            onValueChange = { sarInput = it }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // USD field
        BalanceInputField(
            currency = Currency.USD,
            value = usdInput,
            onValueChange = { usdInput = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Primary Currency selector
        Text(
            text = "اختر العملة الأساسية الافتراضية:",
            style = MaterialTheme.typography.titleSmall,
            color = SalimDarkEmerald,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Currency.entries.forEach { currency ->
                val isSelected = selectedDefaultCurrency == currency
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedDefaultCurrency = currency },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) SalimDarkEmerald else SalimSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
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
                            color = if (isSelected) Color.White else SalimTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val yer = FormatUtils.parseAmountToCents(yerInput) ?: 0L
                val sar = FormatUtils.parseAmountToCents(sarInput) ?: 0L
                val usd = FormatUtils.parseAmountToCents(usdInput) ?: 0L
                onComplete(yer, sar, usd, selectedDefaultCurrency)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("start_using_salim_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SalimDarkEmerald)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SalimGold)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "حفظ والبدء في سليم",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BalanceInputField(
    currency: Currency,
    value: String,
    onValueChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SalimSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, SalimBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.width(130.dp)
            ) {
                Text(currency.flagEmoji, fontSize = 20.sp)
                Column {
                    Text(
                        text = currency.displayNameAr,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SalimTextPrimary
                    )
                    Text(
                        text = currency.symbolAr,
                        style = MaterialTheme.typography.bodySmall,
                        color = SalimTextMuted
                    )
                }
            }

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("0", color = SalimTextMuted) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_opening_${currency.code.lowercase()}")
            )
        }
    }
}
