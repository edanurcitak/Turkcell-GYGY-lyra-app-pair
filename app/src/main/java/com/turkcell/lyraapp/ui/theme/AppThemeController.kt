package com.turkcell.lyraapp.ui.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Uygulama düzeyindeki tema modunu tutan tek kaynak (single source of truth).
 *
 * Tema, tüm uygulamayı saran [LyraAppTheme]'i etkilediğinden ekran düzeyinde değil,
 * app-scoped (`@Singleton`) tutulur: app kabuğu ([com.turkcell.lyraapp.MainActivity]) okur,
 * Profil ekranı yazar. Böylece "Görünüm" toggle'ı uygulama geneline yansır.
 *
 * `@Inject constructor`, Hilt'in bu tutucuyu DI modülü olmadan üretebilmesini sağlar.
 * Durum bellekte tutulur; kalıcı saklama (DataStore vb.) talep edilmediğinden eklenmez.
 */
@Singleton
class AppThemeController @Inject constructor() {

    // null = sistem ayarını takip et, true = koyu, false = açık.
    private val _darkTheme = MutableStateFlow<Boolean?>(null)
    val darkTheme: StateFlow<Boolean?> = _darkTheme.asStateFlow()

    /** Kullanıcının seçtiği temayı uygular (açık/koyu). */
    fun setDarkTheme(dark: Boolean) {
        _darkTheme.value = dark
    }
}
