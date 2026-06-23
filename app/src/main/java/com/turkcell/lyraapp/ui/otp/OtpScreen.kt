package com.turkcell.lyraapp.ui.otp

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * OTP (doğrulama kodu) ekranı — stateful giriş noktası (AGENTS.MD §4.5).
 *
 * State'i [OtpViewModel]'den toplar, aksiyonları [OtpIntent] olarak iletir; tek seferlik
 * navigasyon olaylarını callback'lere bağlar (NavController ViewModel'e sızmaz).
 */
@Composable
fun OtpScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCompleteInfo: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OtpViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                OtpEffect.NavigateToCompleteInfo -> onNavigateToCompleteInfo()
                OtpEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }
    OtpScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

/** OTP ekranının stateless gövdesi: [uiState]'i çizer, etkileşimleri [onIntent] ile bildirir. */
@Composable
private fun OtpScreen(
    uiState: OtpUiState,
    onIntent: (OtpIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 32.dp),
    ) {
        StepHeader(step = "2 / 3", onNavigateBack = onNavigateBack)

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Doğrulama kodu",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "${uiState.phone.toDisplayPhone()} numarasına gönderdiğimiz " +
                "$OTP_CODE_LENGTH haneli kodu gir.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(32.dp))

        OtpCodeInput(
            value = uiState.code,
            onValueChange = { onIntent(OtpIntent.CodeChanged(it)) },
        )

        Spacer(Modifier.height(20.dp))

        ResendRow(onResend = { onIntent(OtpIntent.Resend) })

        // Butonu ekranın en altına yaslar (bkz. ekran görüntüsü 2/3).
        Spacer(Modifier.weight(1f))

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.height(12.dp))
        }

        Button(
            onClick = { onIntent(OtpIntent.Verify) },
            enabled = uiState.code.length == OTP_CODE_LENGTH && !uiState.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text("Doğrula")
                Spacer(Modifier.size(8.dp))
                Icon(
                    imageVector = LyraIcons.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun StepHeader(
    step: String,
    onNavigateBack: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = LyraIcons.ArrowBack,
                contentDescription = "Geri",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = step,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * 6 kutulu kod girişi: tek bir gizli [BasicTextField] girişi yönetir; her hane ayrı bir
 * [OtpCell] olarak çizilir (gerçek metin alanı gösterilmez).
 */
@Composable
private fun OtpCodeInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit).take(OTP_CODE_LENGTH)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(OTP_CODE_LENGTH) { index ->
                    OtpCell(
                        char = value.getOrNull(index)?.toString().orEmpty(),
                        highlighted = index == value.length.coerceAtMost(OTP_CODE_LENGTH - 1),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
    )
}

@Composable
private fun OtpCell(
    char: String,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
) {
    val active = char.isNotEmpty() || highlighted
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (highlighted) 2.dp else 1.5.dp,
                color = if (active) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                shape = RoundedCornerShape(14.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = char,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ResendRow(onResend: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Kodu almadın mı? ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Tekrar gönder",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onResend),
        )
    }
}

/**
 * E.164-ish numarayı görünür biçime çevirir: "+905321456789" → "+90 532 145 67 89".
 * Beklenmeyen uzunlukta numara olduğu gibi döndürülür.
 */
private fun String.toDisplayPhone(): String {
    val national = filter(Char::isDigit).removePrefix("90")
    if (national.length != 10) return this
    return "+90 ${national.substring(0, 3)} ${national.substring(3, 6)} " +
        "${national.substring(6, 8)} ${national.substring(8, 10)}"
}

@Preview(name = "OTP • Dark", showBackground = true)
@Composable
private fun OtpScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            OtpScreen(
                uiState = OtpUiState(phone = "+905321456789", code = "481920"),
                onIntent = {},
                onNavigateBack = {},
            )
        }
    }
}

@Preview(name = "OTP • Light", showBackground = true)
@Composable
private fun OtpScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            OtpScreen(
                uiState = OtpUiState(phone = "+905321456789", code = "4819"),
                onIntent = {},
                onNavigateBack = {},
            )
        }
    }
}
