package com.turkcell.lyraapp.ui.payment

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

// ── Premium akışıyla tutarlı, temadan bağımsız koyu palet (PaymentScreen deseni) ──
private val SuccessBackground = Brush.verticalGradient(
    0f to Color(0xFF3E1F2A),
    0.5f to Color(0xFF1C1015),
    1f to Color(0xFF120A0E),
)
private val BadgeGradient = Brush.linearGradient(
    listOf(Color(0xFFF8CDD4), Color(0xFFEFA98C)),
)
private val AccentPink = Color(0xFFFFB1C8)
private val OnAccent = Color(0xFF5E1133)
private val PrimaryText = Color(0xFFFFFFFF)
private val SecondaryText = Color(0xFFC9B6BC)
private val ChipBackground = Color(0xFF2A1A20)

/** Premium erişim süresi (gün). İki planın da süresi 30 gün (bkz. `docs/api/openapi.json`). */
private const val PREMIUM_DURATION_DAYS = 30

/**
 * Ödeme başarılı ekranı — saf sunum composable'ı (AGENTS.MD §4.5 stateless gövde mantığı).
 *
 * Checkout onaylandıktan sonra gösterilir; durumu/aksiyonu yoktur (ViewModel gerekmez, §4.6).
 * [onStartListening] "Dinlemeye başla" ile profile dönüşü taşır (NavController ViewModel'e sızmaz).
 * Üyelik tier'ı zaten checkout sırasında ([com.turkcell.lyraapp.data.membership.MembershipStore])
 * premium'a çevrildiğinden bu ekran yalnızca onay/kutlama içindir.
 */
@Composable
fun PaymentSuccessScreen(
    onStartListening: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SuccessBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))

            SuccessBadge()

            Spacer(Modifier.height(28.dp))

            Text(
                text = "Premium aktif! 🎉",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryText,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "$PREMIUM_DURATION_DAYS günlük Premium erişimin başladı. " +
                    "Reklamsız, sınırsız ve çevrimdışı dinlemenin keyfini çıkar.",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))

            PremiumChip()

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onStartListening,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPink, contentColor = OnAccent),
            ) {
                Text(
                    text = "Dinlemeye başla",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/** Yumuşak pembe ışıltı (glow) + degrade daire içinde onay (✓) işareti. */
@Composable
private fun SuccessBadge() {
    Box(contentAlignment = Alignment.Center) {
        // Daireyi saran yumuşak ışıltı.
        Box(
            modifier = Modifier
                .size(196.dp)
                .background(
                    Brush.radialGradient(
                        listOf(AccentPink.copy(alpha = 0.40f), Color.Transparent),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .size(116.dp)
                .clip(CircleShape)
                .background(BadgeGradient),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = LyraIcons.Check,
                contentDescription = null,
                tint = OnAccent,
                modifier = Modifier.size(54.dp),
            )
        }
    }
}

/** "Premium · 30 gün" rozet çipi (koyu pill + küçük premium ikonu). */
@Composable
private fun PremiumChip() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ChipBackground)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(BadgeGradient),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = LyraIcons.WorkspacePremium,
                contentDescription = null,
                tint = OnAccent,
                modifier = Modifier.size(14.dp),
            )
        }
        Spacer(Modifier.size(8.dp))
        Text(
            text = "Premium · $PREMIUM_DURATION_DAYS gün",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryText,
        )
    }
}

@Preview(name = "PaymentSuccess", showBackground = true)
@Composable
private fun PaymentSuccessPreview() {
    LyraAppTheme(darkTheme = true) {
        PaymentSuccessScreen(onStartListening = {})
    }
}
