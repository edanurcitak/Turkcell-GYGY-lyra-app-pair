package com.turkcell.lyraapp.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Login ekranı — stateful giriş noktası (MVI).
 *
 * State'i [LoginViewModel]'den toplar ve kullanıcı aksiyonlarını [LoginIntent] olarak
 * geri iletir. Parolasız akış: yalnızca telefon numarası alınır; "Devam et" OTP doğrulama
 * ekranına geçişi tek seferlik [LoginEffect] ile tetikler.
 */
@Composable
fun LoginScreen(
    onNavigateToOtp: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Tek seferlik navigasyon olayını dinle (state'te tutmadan).
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginEffect.NavigateToOtp -> onNavigateToOtp(effect.phone)
            }
        }
    }
    LoginScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

/**
 * Login ekranının stateless gövdesi: yalnızca verilen [uiState]'i çizer ve
 * etkileşimleri [onIntent] üzerinden dışarı bildirir.
 */
@Composable
private fun LoginScreen(
    uiState: LoginUiState,
    onIntent: (LoginIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            // Üstten alta doğru hafif primary parıltısından yüzeye geçen arka plan.
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        MaterialTheme.colorScheme.surface,
                    ),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            // İçerik ekranın alt bölümüne yaslanır (bkz. ekran görüntüleri).
            verticalArrangement = Arrangement.Bottom,
        ) {
            BrandLogo()

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Tekrar hoş geldin",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Numaranı gir, sana bir doğrulama kodu gönderelim.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))

            PhoneNumberField(
                value = uiState.phoneNumber,
                onValueChange = { onIntent(LoginIntent.PhoneNumberChanged(it)) },
            )

            Spacer(Modifier.height(24.dp))

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.height(12.dp))
            }

            Button(
                onClick = { onIntent(LoginIntent.Submit) },
                enabled = uiState.phoneNumber.isNotBlank() && !uiState.isSubmitting,
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
                    Text("Devam et")
                }
            }
        }
    }
}

@Composable
private fun BrandLogo() {
    Surface(
        modifier = Modifier.size(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = LyraIcons.Waveform,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun PhoneNumberField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Telefon numarası") },
        placeholder = { Text("5XX XXX XX XX") },
        leadingIcon = {
            Icon(
                imageVector = LyraIcons.Smartphone,
                contentDescription = null,
            )
        },
        prefix = { Text("+90 ") },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
    )
}

@Preview(name = "Login • Dark", showBackground = true)
@Composable
private fun LoginScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            LoginScreen(uiState = LoginUiState(), onIntent = {})
        }
    }
}

@Preview(name = "Login • Light", showBackground = true)
@Composable
private fun LoginScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            LoginScreen(uiState = LoginUiState(), onIntent = {})
        }
    }
}
