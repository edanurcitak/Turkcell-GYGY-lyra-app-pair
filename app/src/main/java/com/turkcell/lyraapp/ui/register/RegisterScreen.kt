package com.turkcell.lyraapp.ui.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * Register ("Bilgilerini tamamla") ekranı — stateful giriş noktası (AGENTS.MD §4.5).
 *
 * OTP doğrulaması sonrası `firstTime` kullanıcı buraya gelir. State'i [RegisterViewModel]'den
 * toplar, aksiyonları [RegisterIntent] olarak iletir; navigasyon callback'ler üzerinden dışarı
 * bırakılır (NavController ViewModel'e sızmaz).
 */
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                RegisterEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }
    RegisterScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

/** Register ekranının stateless gövdesi: [uiState]'i çizer, etkileşimleri [onIntent] ile bildirir. */
@Composable
private fun RegisterScreen(
    uiState: RegisterUiState,
    onIntent: (RegisterIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 32.dp),
    ) {
        StepHeader(step = "3 / 3", onNavigateBack = onNavigateBack)

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Bilgilerini tamamla",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Hoş geldin! Profilini oluşturmak için birkaç bilgiye ihtiyacımız var.",
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

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Doğum tarihi",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(8.dp))

        BirthDateRow(
            day = uiState.birthDay,
            month = uiState.birthMonth,
            year = uiState.birthYear,
            onDayChange = { onIntent(RegisterIntent.BirthDayChanged(it)) },
            onMonthChange = { onIntent(RegisterIntent.BirthMonthChanged(it)) },
            onYearChange = { onIntent(RegisterIntent.BirthYearChanged(it)) },
        )

        // Butonu ekranın en altına yaslar (bkz. ekran görüntüsü 3/3).
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
            onClick = { onIntent(RegisterIntent.SubmitClicked) },
            enabled = uiState.firstName.isNotBlank() &&
                uiState.lastName.isNotBlank() &&
                uiState.birthDay.isNotBlank() &&
                uiState.birthMonth.isNotBlank() &&
                uiState.birthYear.isNotBlank() &&
                !uiState.isSubmitting,
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
                Text("Tamamla")
                Spacer(Modifier.size(8.dp))
                Icon(
                    imageVector = LyraIcons.Check,
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
private fun BirthDateRow(
    day: String,
    month: String,
    year: String,
    onDayChange: (String) -> Unit,
    onMonthChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        BirthDateField(value = day, onValueChange = onDayChange, placeholder = "GG", weight = 1f)
        BirthDateField(value = month, onValueChange = onMonthChange, placeholder = "AA", weight = 1f)
        BirthDateField(value = year, onValueChange = onYearChange, placeholder = "YYYY", weight = 1.6f)
    }
}

/** Doğum tarihi kutusu: ortalanmış, sayısal tek satırlık alan (gün/ay/yıl için ortak). */
@Composable
private fun RowScope.BirthDateField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    weight: Float,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.weight(weight),
        placeholder = {
            Text(
                text = placeholder,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        },
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
    )
}

@Preview(name = "Register • Dark", showBackground = true)
@Composable
private fun RegisterScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            RegisterScreen(
                uiState = RegisterUiState(
                    firstName = "Zeynep",
                    lastName = "Kaya",
                    birthDay = "14",
                    birthMonth = "06",
                    birthYear = "1998",
                ),
                onIntent = {},
                onNavigateBack = {},
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
            )
        }
    }
}
