package com.turkcell.lyraapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Hilt bağımlılık grafiğinin köküdür.
 *
 * [HiltAndroidApp], uygulama düzeyinde bileşeni üretir; bu sayede
 * `@AndroidEntryPoint` ile işaretli Activity'ler ve `@HiltViewModel`'ler
 * bağımlılıklarını enjekte edebilir.
 */
@HiltAndroidApp
class LyraApplication : Application()
