package com.turkcell.lyraapp.ui.payment

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.membership.MembershipPlan
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

// ── Premium akışıyla tutarlı, temadan bağımsız koyu palet (PremiumPlansScreen deseni) ──
private val PaymentBackground = Brush.verticalGradient(
    0f to Color(0xFF3E1F2A),
    0.45f to Color(0xFF1C1015),
    1f to Color(0xFF120A0E),
)

// Ekran görüntüsündeki pembe→bordo kredi kartı degradesi (sol-üst açık → sağ-alt koyu).
private val CardGradient = Brush.linearGradient(
    listOf(Color(0xFFC9748C), Color(0xFF6E3A4A)),
)
private val ChipGold = Color(0xFFE6BE5C)
private val AccentPink = Color(0xFFFFB1C8)
private val OnAccent = Color(0xFF5E1133)
private val PrimaryText = Color(0xFFFFFFFF)
private val SecondaryText = Color(0xFFC9B6BC)
private val MutedText = Color(0xFF9C8A8F)
private val FieldBackground = Color(0xFF1E1318)
private val FieldBorder = Color(0xFF3A2A30)
private val BadgeGradient = Brush.linearGradient(
    listOf(Color(0xFFF8CDD4), Color(0xFFEFA98C)),
)

/**
 * Ödeme (checkout) ekranı — stateful giriş noktası (AGENTS.MD §4.5).
 *
 * State'i [PaymentViewModel]'den toplar, aksiyonları [PaymentIntent] olarak iletir; tek seferlik
 * onay olayını ([PaymentEffect.PaymentApproved]) [onPaymentSuccess] callback'ine bağlar
 * (NavController ViewModel'e sızmaz). Görsel içerik Hilt'siz önizlenebilmesi için stateless gövdeye devredilir.
 */
@Composable
fun PaymentScreen(
    onNavigateBack: () -> Unit,
    onPaymentSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                PaymentEffect.PaymentApproved -> onPaymentSuccess()
            }
        }
    }
    PaymentScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

/** Stateless gövde: [uiState]'i çizer, etkileşimleri [onIntent]/[onNavigateBack] ile bildirir. */
@Composable
private fun PaymentScreen(
    uiState: PaymentUiState,
    onIntent: (PaymentIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PaymentBackground),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PaymentTopBar(onNavigateBack = onNavigateBack)

            when {
                uiState.isLoading -> LoadingState()
                uiState.plan == null -> ErrorState(
                    message = uiState.errorMessage ?: "Plan yüklenemedi.",
                    onRetry = { onIntent(PaymentIntent.Retry) },
                )
                else -> PaymentContent(plan = uiState.plan, uiState = uiState, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun PaymentTopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, top = 12.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = LyraIcons.ArrowBack,
            contentDescription = "Geri",
            tint = PrimaryText,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onNavigateBack)
                .padding(8.dp),
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = "Ödeme",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryText,
        )
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AccentPink)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = AccentPink, contentColor = OnAccent),
            ) {
                Text(text = "Tekrar dene", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun PaymentContent(
    plan: MembershipPlan,
    uiState: PaymentUiState,
    onIntent: (PaymentIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 28.dp),
    ) {
        CreditCardVisual(
            cardNumber = uiState.cardNumber,
            holderName = uiState.holderName,
            expiry = uiState.expiry,
        )

        Spacer(Modifier.height(24.dp))

        PaymentField(
            label = "Kart numarası",
            value = formatCardNumber(uiState.cardNumber),
            onValueChange = { onIntent(PaymentIntent.CardNumberChanged(it)) },
            placeholder = "0000 0000 0000 0000",
            keyboardType = KeyboardType.Number,
        )

        Spacer(Modifier.height(16.dp))

        PaymentField(
            label = "Kart üzerindeki isim",
            value = uiState.holderName,
            onValueChange = { onIntent(PaymentIntent.HolderNameChanged(it)) },
            placeholder = "Ad Soyad",
            keyboardType = KeyboardType.Text,
        )

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            PaymentField(
                label = "Son kullanma",
                value = formatExpiry(uiState.expiry),
                onValueChange = { onIntent(PaymentIntent.ExpiryChanged(it)) },
                placeholder = "AA/YY",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
            PaymentField(
                label = "CVC",
                value = uiState.cvc,
                onValueChange = { onIntent(PaymentIntent.CvcChanged(it)) },
                placeholder = "123",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(20.dp))

        PlanSummary(plan = plan)

        if (uiState.errorMessage != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = uiState.errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFFF9AA8),
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { onIntent(PaymentIntent.PayClicked) },
            enabled = uiState.canPay,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentPink,
                contentColor = OnAccent,
                disabledContainerColor = AccentPink.copy(alpha = 0.35f),
                disabledContentColor = OnAccent.copy(alpha = 0.55f),
            ),
        ) {
            if (uiState.isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = OnAccent,
                )
            } else {
                Icon(
                    imageVector = LyraIcons.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = payButtonLabel(plan),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = LyraIcons.Lock,
                contentDescription = null,
                tint = MutedText,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.size(6.dp))
            Text(
                text = "Ödemen 256-bit SSL ile güvende",
                style = MaterialTheme.typography.bodySmall,
                color = MutedText,
            )
        }
    }
}

/**
 * Canlı dolan kredi kartı görseli: kullanıcı yazdıkça numara/isim/SKT güncellenir.
 *
 * Boş alanlar tasarımdaki yer tutucularla gösterilir (numara → "•", isim → "AD SOYAD", SKT → "AA/YY").
 */
@Composable
private fun CreditCardVisual(
    cardNumber: String,
    holderName: String,
    expiry: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(196.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CardGradient)
            .padding(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Çip
                Box(
                    modifier = Modifier
                        .size(width = 42.dp, height = 30.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(ChipGold),
                )
                // Marka rozeti
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = LyraIcons.Waveform,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Text(
                text = maskedCardNumber(cardNumber),
                style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 2.sp),
                fontWeight = FontWeight.Medium,
                color = Color.White,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                CardCaption(
                    label = "KART SAHİBİ",
                    value = holderName.ifBlank { "AD SOYAD" }.uppercase(),
                )
                CardCaption(
                    label = "SKT",
                    value = if (expiry.isEmpty()) "AA/YY" else formatExpiry(expiry),
                    alignEnd = true,
                )
            }
        }
    }
}

@Composable
private fun CardCaption(label: String, value: String, alignEnd: Boolean = false) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
    }
}

/** Etiket + koyu, kenarlıklı tek satırlık giriş alanı (ekran görüntüsündeki form alanı). */
@Composable
private fun PaymentField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = SecondaryText,
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(FieldBackground)
                .border(1.dp, FieldBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    color = PrimaryText,
                    fontSize = 16.sp,
                ),
                cursorBrush = SolidColor(AccentPink),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = LocalTextStyle.current.copy(fontSize = 16.sp),
                                color = MutedText,
                            )
                        }
                        inner()
                    }
                },
            )
        }
    }
}

/**
 * Plan özet kartı: seçilen planın adı/türü + fiyatı, altında "Bugün ödenecek" satırı.
 *
 * Ad/fiyat GERÇEK seçili plandan gelir (uydurulmaz, §2.2) — ekran görüntüsündeki ₺59,99 mock'unun
 * yerine gerçek plan fiyatı (recurring ₺139,00·/ay, one-time ₺159,00) gösterilir.
 */
@Composable
private fun PlanSummary(plan: MembershipPlan) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FieldBackground)
            .border(1.dp, FieldBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BadgeGradient),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = LyraIcons.WorkspacePremium,
                    contentDescription = null,
                    tint = OnAccent,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "LyraApp Premium",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subscriptionLabel(plan),
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = priceLabel(plan),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText,
                )
                val period = periodLabel(plan)
                if (period.isNotEmpty()) {
                    Spacer(Modifier.size(4.dp))
                    Text(
                        text = period,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText,
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(FieldBorder),
        )
        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Bugün ödenecek",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText,
            )
            Text(
                text = priceLabel(plan),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText,
            )
        }
    }
}

// ── Biçimlendirme yardımcıları ──

/** Rakamları 4'erli gruplar halinde boşlukla ayırır (kart numarası gösterimi). */
private fun formatCardNumber(digits: String): String = digits.chunked(4).joinToString(" ")

/** "MMYY" rakamlarını "MM/YY" biçimine çevirir (kısmi girişte de çalışır). */
private fun formatExpiry(digits: String): String =
    if (digits.length <= 2) digits else "${digits.take(2)}/${digits.drop(2)}"

/** Kart görselindeki numara: girilen haneler + kalan yer tutucu "•" (4'erli gruplu, 16 hane). */
private fun maskedCardNumber(digits: String): String = buildString {
    for (i in 0 until 16) {
        append(if (i < digits.length) digits[i] else '•')
        if (i % 4 == 3 && i != 15) append("  ")
    }
}

/** API kuruş fiyatını gösterim biçimine çevirir (ör. 13900 → "₺139,00"). */
private fun priceLabel(plan: MembershipPlan): String {
    val symbol = if (plan.currency.equals("TRY", ignoreCase = true)) "₺" else "${plan.currency} "
    val lira = plan.priceKurus / 100
    val kurus = plan.priceKurus % 100
    return "%s%d,%02d".format(symbol, lira, kurus)
}

/** Yenilenen (recurring) planlarda "/ ay" eki; tek seferlikte boş. */
private fun periodLabel(plan: MembershipPlan): String =
    if (plan.type == "recurring") "/ ay" else ""

/** Özet kartı alt başlığı: abonelik türü. */
private fun subscriptionLabel(plan: MembershipPlan): String =
    if (plan.type == "recurring") "Aylık abonelik" else "Tek seferlik · 30 gün"

/** Ödeme butonu metni (ör. "₺139,00 / ay öde" veya "₺159,00 öde"). */
private fun payButtonLabel(plan: MembershipPlan): String {
    val period = periodLabel(plan)
    val suffix = if (period.isEmpty()) "" else " $period"
    return "${priceLabel(plan)}$suffix öde"
}

// ── Önizleme ──
private val previewPlan = MembershipPlan(
    id = "recurring-monthly",
    type = "recurring",
    name = "Premium (Aylık Yenilenen)",
    description = "İstediğin zaman iptal et",
    priceKurus = 13900,
    currency = "TRY",
    durationDays = 30,
    autoRenew = true,
)

@Preview(name = "Payment • Empty", showBackground = true)
@Composable
private fun PaymentEmptyPreview() {
    LyraAppTheme(darkTheme = true) {
        PaymentScreen(
            uiState = PaymentUiState(plan = previewPlan, isLoading = false),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(name = "Payment • Filled", showBackground = true)
@Composable
private fun PaymentFilledPreview() {
    LyraAppTheme(darkTheme = true) {
        PaymentScreen(
            uiState = PaymentUiState(
                plan = previewPlan,
                cardNumber = "4242424242424242",
                holderName = "Zeynep Kaya",
                expiry = "1230",
                cvc = "123",
                isLoading = false,
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(name = "Payment • Loading", showBackground = true)
@Composable
private fun PaymentLoadingPreview() {
    LyraAppTheme(darkTheme = true) {
        PaymentScreen(
            uiState = PaymentUiState(isLoading = true),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
