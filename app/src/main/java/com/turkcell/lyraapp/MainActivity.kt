package com.turkcell.lyraapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.turkcell.lyraapp.ui.navigation.LyraNavHost
import com.turkcell.lyraapp.ui.theme.LyraAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LyraAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LyraNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}