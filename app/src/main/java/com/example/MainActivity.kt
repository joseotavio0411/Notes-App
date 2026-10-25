package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notification.NotificationHelper
import com.example.ui.LaunchAction
import com.example.ui.NoteKind
import com.example.ui.ProductivityViewModel
import com.example.ui.components.copyUriToAppStorage
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ProductivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Ao girar a tela a Activity é recriada com o mesmo Intent: só trata na primeira vez
        if (savedInstanceState == null) handleLaunchIntent(intent)
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleLaunchIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        // A nota pode ter sido tirada das notificações pelo botão "Desafixar"
        viewModel.refreshNotificationPinnedIds()
    }

    /** Atalhos, widgets, notificações e compartilhamentos de outros apps chegam aqui. */
    private fun handleLaunchIntent(intent: Intent?) {
        intent ?: return

        val targetTab = intent.getIntExtra(NotificationHelper.EXTRA_TARGET_TAB, -1)
        if (targetTab in 0..3) viewModel.setSelectedTab(targetTab)

        val noteId = intent.getLongExtra(NotificationHelper.EXTRA_OPEN_NOTE_ID, -1L)
        val action: LaunchAction? = when {
            noteId > 0 -> LaunchAction.OpenNote(noteId)
            intent.action == ACTION_NEW_TASK -> LaunchAction.NewTask
            intent.action == ACTION_NEW_NOTE -> LaunchAction.NewNote(
                runCatching { NoteKind.valueOf(intent.getStringExtra(EXTRA_NOTE_KIND) ?: "") }
                    .getOrDefault(NoteKind.TEXT)
            )
            intent.action == Intent.ACTION_SEND -> sharedContentToNote(intent)
            else -> null
        }
        action?.let { viewModel.submitLaunchAction(it) }
    }

    private fun sharedContentToNote(intent: Intent): LaunchAction? {
        val text = intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT).orEmpty()
        val imageUri: Uri? = if (intent.type?.startsWith("image/") == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
        } else {
            null
        }
        val localImage = imageUri?.let { copyUriToAppStorage(this, it) }
        if (text.isBlank() && subject.isBlank() && localImage == null) return null
        return LaunchAction.NewNote(
            kind = if (localImage != null) NoteKind.IMAGE else NoteKind.TEXT,
            title = subject,
            content = text,
            imageUri = localImage
        )
    }

    companion object {
        const val ACTION_NEW_NOTE = "com.example.action.NEW_NOTE"
        const val ACTION_NEW_TASK = "com.example.action.NEW_TASK"
        const val EXTRA_NOTE_KIND = "extra_note_kind"
    }
}
