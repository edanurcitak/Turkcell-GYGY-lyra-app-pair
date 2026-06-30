package com.turkcell.lyraapp.ui.premium

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.membership.MembershipPlan
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

// ── Ekran görüntüsüne göre tonlanan, temadan bağımsız koyu "premium" paleti ──
// (Tasarım her zaman bu koyu degrade görünümü; app teması açık olsa da sabit kalır.)
private val PremiumBackground = Brush.verticalGradient(
    0f to Color(0xFF3E1F2A),
    0.45f to Color(0xFF1C1015),
    1f to Color(0xFF120A0E),
)
private val PremiumBadgeGradient = Brush.linearGradient(
    listOf(Color(0xFFF8CDD4), Color(0xFFEFA98C)),
)
private val AccentPink = Color(0xFFFFB1C8)
private val OnAccent = Color(0xFF5E1133)
private val PrimaryText = Color(0xFFFFFFFF)
private val SecondaryText = Color(0xFFC9B6BC)
private val MutedText = Color(0xFF9C8A8F)
private val IconContainer = Color(0xFF2A1A20)
private val SelectedCard = Color(0xFF2E1A22)
private val UnselectedCard = Color(0xFF1E1318)

/**
 * Premium plan seçim ekranı — stateful giriş noktası (AGENTS.MD §4.5).
 *
 * Planlar gerçek API'den ([PremiumPlansViewModel]) gelir. [onNavigateBack] geri navigasyonunu
 * taşır (projenin nav kalıbı: callback ile). Görsel içerik Hilt'siz önizlenebilmesi için ayrı
 * stateless gövdeye devredilir.
 */
@Composable
fun PremiumPlansScreen(
    onNavigateBack: () -> Unit,
    onContinue: (planId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PremiumPlansViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PremiumPlansScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        onContinue = onContinue,
        modifier = modifier,
    )
}

/** Stateless gövde: [uiState]'i çizer, etkileşimleri [onIntent]/[onNavigateBack]/[onContinue] ile bildirir. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumPlansScreen(
    uiState: PremiumPlansUiState,
    onIntent: (PremiumPlansIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onContinue: (planId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PremiumBackground),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Geri oku her durumda (yükleniyor/hata/içerik) görünür.
            Box(modifier = Modifier.padding(start = 12.dp, top = 12.dp)) {
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
            }

            // Aşağı çekince ([PremiumPlansIntent.PullRefresh]) içerik görünür kalarak üstte yenileme göstergesi döner.
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { onIntent(PremiumPlansIntent.PullRefresh) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                when {
                    uiState.isLoading -> LoadingState()
                    uiState.errorMessage != null -> ErrorState(
                        message = uiState.errorMessage,
                        onRetry = { onIntent(PremiumPlansIntent.Retry) },
                    )
                    else -> PremiumContent(uiState = uiState, onIntent = onIntent, onContinue = onContinue)
                }
            }
        }
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
private fun PremiumContent(
    uiState: PremiumPlansUiState,
    onIntent: (PremiumPlansIntent) -> Unit,
    onContinue: (planId: String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 28.dp),
    ) {
        PremiumHeader()

        Spacer(Modifier.height(28.dp))

        uiState.features.forEachIndexed { index, feature ->
            if (index > 0) Spacer(Modifier.height(20.dp))
            FeatureRow(feature)
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = "Planını seç",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MutedText,
        )

        Spacer(Modifier.height(12.dp))

        uiState.plans.forEachIndexed { index, plan ->
            if (index > 0) Spacer(Modifier.height(12.dp))
            PlanCard(
                plan = plan,
                selected = plan.id == uiState.selectedPlanId,
                onClick = { onIntent(PremiumPlansIntent.PlanSelected(plan.id)) },
            )
        }

        Spacer(Modifier.height(24.dp))

        // "Devam et": seçili planla ödeme ekranına geçer (planId taşınır; checkout orada alınır).
        Button(
            onClick = { onContinue(uiState.selectedPlanId) },
            enabled = uiState.selectedPlanId.isNotBlank(),
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
            Text(
                text = "Devam et",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun PremiumHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(PremiumBadgeGradient),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = LyraIcons.WorkspacePremium,
                contentDescription = null,
                tint = OnAccent,
                modifier = Modifier.size(38.dp),
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "LyraApp Premium",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryText,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Reklamsız, sınırsız ve çevrimdışı müziğin keyfini çıkar.",
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FeatureRow(feature: PremiumFeature) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(IconContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = featureIcon(feature.id),
                contentDescription = null,
                tint = SecondaryText,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.size(16.dp))
        Column {
            Text(
                text = feature.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = feature.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MutedText,
            )
        }
    }
}

/**
 * Plan kartı: radio + ad/açıklama (+ "Popüler" rozeti) + fiyat. Tıklanınca seçilir.
 *
 * Ad/açıklama/fiyat GERÇEK API verisidir (uydurulmaz, §2.2). "Popüler" rozeti API'de yoktur;
 * onaylanan tasarım öğesi olarak yalnızca recurring (aylık) plana uygulanır.
 */
@Composable
private fun PlanCard(
    plan: MembershipPlan,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    val border = if (selected) BorderStroke(1.5.dp, AccentPink) else BorderStroke(1.dp, Color(0xFF3A2A30))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) SelectedCard else UnselectedCard)
            .border(border, shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (selected) LyraIcons.RadioButtonChecked else LyraIcons.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (selected) AccentPink else MutedText,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.size(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = plan.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = plan.description,
                style = MaterialTheme.typography.bodySmall,
                color = MutedText,
            )
        }
        if (plan.type == "recurring") {
            PopularBadge()
            Spacer(Modifier.size(10.dp))
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
}

@Composable
private fun PopularBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AccentPink)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = "Popüler",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = OnAccent,
        )
    }
}

/** Özellik [PremiumFeature.id] değerine karşılık gelen leading ikon. */
private fun featureIcon(id: String): ImageVector = when (id) {
    "no_ads" -> LyraIcons.Block
    "unlimited_skip" -> LyraIcons.SkipNext
    "offline" -> LyraIcons.DownloadForOffline
    "high_quality" -> LyraIcons.Waveform
    "all_devices" -> LyraIcons.Devices
    else -> LyraIcons.WorkspacePremium
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

// ── Önizleme ──
// Sadece preview fikstürü (gönderilen veri değil); gerçek ekranda planlar API'den gelir.
private val previewPlans = listOf(
    MembershipPlan(
        id = "recurring-monthly",
        type = "recurring",
        name = "Premium (Aylık Yenilenen)",
        description = "İstediğin zaman iptal et",
        priceKurus = 13900,
        currency = "TRY",
        durationDays = 30,
        autoRenew = true,
    ),
    MembershipPlan(
        id = "one-time",
        type = "one-time",
        name = "Premium (Tek Seferlik)",
        description = "30 gün erişim · otomatik yenilenmez",
        priceKurus = 15900,
        currency = "TRY",
        durationDays = 30,
        autoRenew = false,
    ),
)

@Preview(name = "PremiumPlans • Content", showBackground = true)
@Composable
private fun PremiumPlansContentPreview() {
    LyraAppTheme(darkTheme = true) {
        PremiumPlansScreen(
            uiState = PremiumPlansUiState(
                plans = previewPlans,
                selectedPlanId = "recurring-monthly",
                isLoading = false,
            ),
            onIntent = {},
            onNavigateBack = {},
            onContinue = {},
        )
    }
}

@Preview(name = "PremiumPlans • Loading", showBackground = true)
@Composable
private fun PremiumPlansLoadingPreview() {
    LyraAppTheme(darkTheme = true) {
        PremiumPlansScreen(
            uiState = PremiumPlansUiState(isLoading = true),
            onIntent = {},
            onNavigateBack = {},
            onContinue = {},
        )
    }
}
