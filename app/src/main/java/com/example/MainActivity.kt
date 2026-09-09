package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ProductivityViewModel
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ProductivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkThemeSetting by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val activeDarkTheme = darkThemeSetting ?: systemDark

            MyApplicationTheme(darkTheme = activeDarkTheme) {
                MainScreen(
                    viewModel = viewModel,
                    isDarkTheme = activeDarkTheme,
                    onToggleDarkTheme = { viewModel.toggleDarkTheme(systemDark) }
                )
            }
        }
    }
}

