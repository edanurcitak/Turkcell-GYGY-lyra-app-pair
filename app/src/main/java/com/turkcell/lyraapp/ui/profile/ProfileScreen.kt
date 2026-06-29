package com.turkcell.lyraapp.ui.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Profil ekranı — stateful giriş noktası (AGENTS.MD §4.5).
 *
 * State'i [ProfileViewModel]'den toplar ve kullanıcı aksiyonlarını [ProfileIntent] olarak
 * geri iletir. Görsel içerik, Hilt'siz de önizlenebilmesi için ayrı bir stateless
 * composable'a ([ProfileScreen]) devredilir.
 *
 * [onNavigateToPremiumPlans]: free banner tıklanınca premium plan seçim ekranına yönlendirir
 * (projenin nav kalıbı: navigasyon ViewModel/Intent değil, callback ile taşınır). Varsayılan
 * `{}` olduğundan ekran/preview'lar nav bağlanmadan da derlenir.
 */
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onNavigateToPremiumPlans: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onUpgradeClick = onNavigateToPremiumPlans,
        modifier = modifier,
    )
}

/** Profil ekranının stateless gövdesi: yalnızca [uiState]'i çizer, etkileşimleri [onIntent]/[onUpgradeClick] ile bildirir. */
@Composable
private fun ProfileScreen(
    uiState: ProfileUiState,
    onIntent: (ProfileIntent) -> Unit,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 24.dp),
    ) {
        ProfileTopBar()

        Spacer(Modifier.height(16.dp))

        ProfileHeader(uiState)

        Spacer(Modifier.height(24.dp))

        ProfileStats(uiState)

        Spacer(Modifier.height(24.dp))

        // Plan banner'ı tier'a göre değişir (kaynak: MembershipStore → uiState.isPremium).
        // Her iki banner da tıklanınca premium plan seçim ekranına yönlendirir.
        if (uiState.isPremium) {
            PlanBanner(
                title = "Premium · 3 gün kaldı",
                subtitle = "Yenile ya da aboneliğe geç",
                onClick = onUpgradeClick,
            )
        } else {
            PlanBanner(
                title = "Premium'a yükselt",
                subtitle = "Tüm özelliklerin kilidini aç",
                onClick = onUpgradeClick,
            )
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = "Görünüm",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(12.dp))

        AppearanceToggle(
            isDarkTheme = uiState.isDarkTheme,
            onSelect = { dark -> onIntent(ProfileIntent.ThemeChanged(dark)) },
        )

        Spacer(Modifier.height(20.dp))

        uiState.settings.forEachIndexed { index, setting ->
            if (index > 0) Spacer(Modifier.height(12.dp))
            SettingRow(setting)
        }
    }
}

@Composable
private fun ProfileTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Profil",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        // Ayarlar dişlisi: ekran görüntüsündeki dekoratif üst bar aksiyonu (aksiyon talep edilmedi, §4.6).
        Icon(
            imageVector = LyraIcons.Settings,
            contentDescription = "Ayarlar",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(26.dp),
        )
    }
}

@Composable
private fun ProfileHeader(uiState: ProfileUiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier.size(96.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = uiState.initials,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = uiState.displayName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "${uiState.handle} · ${uiState.membership}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProfileStats(uiState: ProfileUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        ProfileStat(value = uiState.playlistCount, label = "Çalma listesi")
        ProfileStat(value = uiState.followerCount, label = "Takipçi")
        ProfileStat(value = uiState.followingCount, label = "Takip")
    }
}

@Composable
private fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Plan banner'ı: kahverengi degrade kart (ekran görüntüsü referansı).
 *
 * Premium ve free aynı görsel kalıbı paylaşır; yalnızca metin değişir. [onClick] verilirse kart
 * tıklanabilir olur ve premium plan seçim ekranına yönlendirir (her iki banner da bunu kullanır);
 * `null` ise yalnızca dekoratiftir (chevron tasarım gereği durur, aksiyon tetiklemez).
 */
@Composable
private fun PlanBanner(
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(20.dp)
    // Soldan sağa açık → koyu kahve degrade (ekran görüntüsündeki banner).
    val gradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF8A6A52), Color(0xFF5C4031)),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = shape)
            .clip(shape)
            .background(gradient)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = LyraIcons.Diamond,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.size(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.75f),
            )
        }
        Spacer(Modifier.size(12.dp))
        Icon(
            imageVector = LyraIcons.ChevronRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(20.dp),
        )
    }
}

/**
 * "Görünüm" tema seçici: iki segmentli (Açık/Koyu) pill.
 *
 * Seçili segment markanın `primary` rengiyle vurgulanır; tıklanınca [onSelect] ile tema
 * değişimi bildirilir (uygulama geneline uygulanır).
 */
@Composable
private fun AppearanceToggle(
    isDarkTheme: Boolean,
    onSelect: (Boolean) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ThemeSegment(
                modifier = Modifier.weight(1f),
                icon = LyraIcons.LightMode,
                label = "Açık",
                selected = !isDarkTheme,
                onClick = { onSelect(false) },
            )
            ThemeSegment(
                modifier = Modifier.weight(1f),
                icon = LyraIcons.DarkMode,
                label = "Koyu",
                selected = isDarkTheme,
                onClick = { onSelect(true) },
            )
        }
    }
}

@Composable
private fun ThemeSegment(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val content =
        if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(background)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = content,
        )
    }
}

@Composable
private fun SettingRow(setting: ProfileSetting) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = settingIcon(setting.id),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.size(16.dp))
            Text(
                text = setting.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (setting.value != null) {
                Text(
                    text = setting.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.size(8.dp))
            }
            Icon(
                imageVector = LyraIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** Ayar satırının [ProfileSetting.id] değerine karşılık gelen leading ikonu. */
private fun settingIcon(id: String): ImageVector = when (id) {
    "audio_quality" -> LyraIcons.Waveform
    "offline_download" -> LyraIcons.Download
    "notifications" -> LyraIcons.Notifications
    "privacy" -> LyraIcons.Lock
    "help" -> LyraIcons.Help
    else -> LyraIcons.Settings
}

@Preview(name = "Profile • Premium • Light", showBackground = true)
@Composable
private fun ProfileScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            ProfileScreen(
                uiState = ProfileUiState(isDarkTheme = false, isPremium = true),
                onIntent = {},
                onUpgradeClick = {},
            )
        }
    }
}

@Preview(name = "Profile • Premium • Dark", showBackground = true)
@Composable
private fun ProfileScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            ProfileScreen(
                uiState = ProfileUiState(isDarkTheme = true, isPremium = true),
                onIntent = {},
                onUpgradeClick = {},
            )
        }
    }
}

@Preview(name = "Profile • Free • Light", showBackground = true)
@Composable
private fun ProfileScreenFreePreview() {
    LyraAppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            ProfileScreen(
                uiState = ProfileUiState(isDarkTheme = false, isPremium = false, membership = "Ücretsiz"),
                onIntent = {},
                onUpgradeClick = {},
            )
        }
    }
}
