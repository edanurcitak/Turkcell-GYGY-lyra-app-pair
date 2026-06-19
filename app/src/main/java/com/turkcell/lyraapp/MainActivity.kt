package com.turkcell.lyraapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.ui.navigation.LyraNavHost
import com.turkcell.lyraapp.ui.theme.AppThemeController
import com.turkcell.lyraapp.ui.theme.LyraAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Uygulama düzeyindeki tema modunu tutar; Profil ekranının "Görünüm" toggle'ı buraya yazar.
    @Inject
    lateinit var themeController: AppThemeController

    // İzin sonucundan bağımsız devam ederiz: reddedilse bile oynatma sürer, yalnızca bildirim gizlenir.
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        setContent {
            // Kullanıcı bir tema seçene kadar (null) sistem ayarı izlenir.
            val darkThemeOverride by themeController.darkTheme.collectAsStateWithLifecycle()
            LyraAppTheme(darkTheme = darkThemeOverride ?: isSystemInDarkTheme()) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LyraNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    /** Android 13+ (TIRAMISU) medya bildiriminin görünmesi için POST_NOTIFICATIONS izni iste. */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
