package com.turkcell.lyraapp.ui.register

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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Register (Hesap oluştur) ekranı — stateful giriş noktası (AGENTS.MD §4.5).
 *
 * State'i [RegisterViewModel]'den toplar, aksiyonları [RegisterIntent] olarak iletir.
 * Navigasyon, ViewModel'e sızmadan callback'ler üzerinden dışarı bırakılır.
 */
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RegisterScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        onNavigateToLogin = onNavigateToLogin,
        modifier = modifier,
    )
}

/** Register ekranının stateless gövdesi: [uiState]'i çizer, etkileşimleri [onIntent] ile bildirir. */
@Composable
private fun RegisterScreen(
    uiState: RegisterUiState,
    onIntent: (RegisterIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 32.dp),
    ) {
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

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Hesap oluştur",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Birkaç adımda Lyra'ya katıl ve çalma listeni oluştur.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))

        NameRow(
            firstName = uiState.firstName,
            lastName = uiState.lastName,
            onFirstNameChange = { onIntent(RegisterIntent.FirstNameChanged(it)) },
            onLastNameChange = { onIntent(RegisterIntent.LastNameChanged(it)) },
        )

        Spacer(Modifier.height(12.dp))

        PhoneNumberField(
            value = uiState.phoneNumber,
            onValueChange = { onIntent(RegisterIntent.PhoneNumberChanged(it)) },
        )

        Spacer(Modifier.height(12.dp))

        PasswordField(
            value = uiState.password,
            isPasswordVisible = uiState.isPasswordVisible,
            onValueChange = { onIntent(RegisterIntent.PasswordChanged(it)) },
            onToggleVisibility = { onIntent(RegisterIntent.TogglePasswordVisibility) },
        )

        Spacer(Modifier.height(12.dp))

        // Şifre gücü göstergesi: yalnızca görsel iskelet. Gerçek skorlama (validation)
        // business katmanına aittir ve §4.6 gereği varsayılan olarak eklenmez.
        PasswordStrengthBar(filledSegments = 0)

        Spacer(Modifier.height(8.dp))

        Text(
            text = "En az 8 karakter, bir rakam içermeli.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(16.dp))

        TermsRow(
            isChecked = uiState.isTermsAccepted,
            onToggle = { onIntent(RegisterIntent.ToggleTermsAccepted) },
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onIntent(RegisterIntent.SubmitClicked) },
            enabled = uiState.isTermsAccepted && !uiState.isSubmitting,
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
                Text("Kayıt ol")
                Spacer(Modifier.size(8.dp))
                Icon(
                    imageVector = LyraIcons.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        SignInRow(onNavigateToLogin = onNavigateToLogin)
    }
}

@Composable
private fun NameRow(
    firstName: String,
    lastName: String,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = firstName,
            onValueChange = onFirstNameChange,
            modifier = Modifier.weight(1f),
            label = { Text("Ad") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
        )
        OutlinedTextField(
            value = lastName,
            onValueChange = onLastNameChange,
            modifier = Modifier.weight(1f),
            label = { Text("Soyad") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
        )
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

@Composable
private fun PasswordStrengthBar(filledSegments: Int) {
    val totalSegments = 4
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(totalSegments) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(
                        if (index < filledSegments) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                    ),
            )
        }
    }
}

@Composable
private fun TermsRow(
    isChecked: Boolean,
    onToggle: () -> Unit,
) {
    Row(verticalAlignment = Alignment.Top) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onToggle() },
        )
        Spacer(Modifier.size(8.dp))
        val linkStyle = SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = buildAnnotatedString {
                withStyle(linkStyle) { append("Kullanım Koşulları") }
                append(" ve ")
                withStyle(linkStyle) { append("Gizlilik Politikası") }
                append("'nı okudum, kabul ediyorum.")
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .weight(1f)
                .padding(top = 12.dp),
        )
    }
}

@Composable
private fun SignInRow(onNavigateToLogin: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Zaten hesabın var mı? ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Giriş yap",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onNavigateToLogin),
        )
    }
}

@Preview(name = "Register • Dark", showBackground = true)
@Composable
private fun RegisterScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            RegisterScreen(
                uiState = RegisterUiState(),
                onIntent = {},
                onNavigateBack = {},
                onNavigateToLogin = {},
            )
        }
    }
}

@Preview(name = "Register • Light", showBackground = true)
@Composable
private fun RegisterScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            RegisterScreen(
                uiState = RegisterUiState(),
                onIntent = {},
                onNavigateBack = {},
                onNavigateToLogin = {},
            )
        }
    }
}
