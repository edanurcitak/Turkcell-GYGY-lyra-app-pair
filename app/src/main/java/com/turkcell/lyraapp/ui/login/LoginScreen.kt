package com.turkcell.lyraapp.ui.login

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
 * geri iletir. Görsel içerik, Hilt'siz de önizlenebilmesi için ayrı bir stateless
 * composable'a ([LoginScreen]) devredilir.
 */
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LoginScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onNavigateToRegister = onNavigateToRegister,
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
    onNavigateToRegister: () -> Unit,
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
                text = "Hesabına giriş yap, kaldığın yerden dinlemeye devam et.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))

            PhoneNumberField(
                value = uiState.phoneNumber,
                onValueChange = { onIntent(LoginIntent.PhoneNumberChanged(it)) },
            )

            Spacer(Modifier.height(12.dp))

            PasswordField(
                value = uiState.password,
                isPasswordVisible = uiState.isPasswordVisible,
                onValueChange = { onIntent(LoginIntent.PasswordChanged(it)) },
                onToggleVisibility = { onIntent(LoginIntent.TogglePasswordVisibility) },
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Şifremi unuttum",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.End),
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Hesabın yok mu? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Kayıt ol",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onNavigateToRegister),
                )
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

@Composable
private fun PasswordField(
    value: String,
    isPasswordVisible: Boolean,
    onValueChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Şifre") },
        leadingIcon = {
            Icon(
                imageVector = LyraIcons.Lock,
                contentDescription = null,
            )
        },
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = LyraIcons.Visibility,
                    contentDescription = null,
                )
            }
        },
        visualTransformation = if (isPasswordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
    )
}

@Preview(name = "Login • Dark", showBackground = true)
@Composable
private fun LoginScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            LoginScreen(uiState = LoginUiState(), onIntent = {}, onNavigateToRegister = {})
        }
    }
}

@Preview(name = "Login • Light", showBackground = true)
@Composable
private fun LoginScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            LoginScreen(uiState = LoginUiState(), onIntent = {}, onNavigateToRegister = {})
        }
    }
}
