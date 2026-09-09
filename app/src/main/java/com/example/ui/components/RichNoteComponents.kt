package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.ui.theme.VioletNeon
import com.example.ui.theme.VioletRoyalDark
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

// ==========================================
// FILE STORAGE HELPERS (OFF-LOADED TO IO)
// ==========================================

suspend fun copyUriToAppStorageAsync(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
    try {
        val imagesDir = File(context.filesDir, "images").apply { if (!exists()) mkdirs() }
        val targetFile = File(imagesDir, "img_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        targetFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun copyUriToAppStorage(context: Context, uri: Uri): String? {
    return try {
        runBlocking(Dispatchers.IO) {
            copyUriToAppStorageAsync(context, uri)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// ==========================================
// AUDIO RECORDER HELPER
// ==========================================

class AudioRecorderHelper(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null

    fun startRecording(): File? {
        return try {
            val audioDir = File(context.filesDir, "audios").apply { mkdirs() }
            val file = File(audioDir, "audio_${System.currentTimeMillis()}.m4a")
            currentFile = file

            val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            rec.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            recorder = rec
            file
        } catch (e: Exception) {
            e.printStackTrace()
            cleanup()
            null
        }
    }

    fun stopRecording(): String? {
        return try {
            recorder?.stop()
            cleanup()
            currentFile?.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            cleanup()
            null
        }
    }

    fun cancelRecording() {
        cleanup()
        currentFile?.delete()
        currentFile = null
    }

    private fun cleanup() {
        try {
            recorder?.release()
        } catch (e: Exception) {
            // ignore
        }
        recorder = null
    }
}

// ==========================================
// AUDIO PLAYER COMPONENT WITH WAVEFORM
// ==========================================

@Composable
fun AudioWaveformPlayer(
    isPlaying: Boolean,
    progress: Float, // 0.0f to 1.0f
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    barCount: Int = 28
) {
    val amplitudes = remember {
        List(barCount) { index ->
            // Natural waveform contour
            val factor = Math.sin((index.toDouble() / barCount) * Math.PI).toFloat()
            (0.20f + factor * 0.70f + ((index % 4) * 0.06f)).coerceIn(0.18f, 0.96f)
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(30.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newProgress = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    onSeek(newProgress)
                }
            }
    ) {
        val totalGaps = barCount - 1
        val barWidth = (size.width * 0.65f) / barCount
        val gap = (size.width - (barWidth * barCount)) / totalGaps.coerceAtLeast(1)

        amplitudes.forEachIndexed { index, amp ->
            val x = index * (barWidth + gap)
            val barHeight = size.height * amp
            val isPassed = (index.toFloat() / barCount) <= progress

            val barColor = if (isPassed) {
                VioletNeon
            } else {
                VioletRoyalDark.copy(alpha = 0.28f)
            }

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, (size.height - barHeight) / 2f),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

@Composable
fun AudioPlayerView(
    audioPath: String,
    modifier: Modifier = Modifier,
    onDeleteAudio: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var playbackProgress by remember { mutableFloatStateOf(0f) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    LaunchedEffect(isPlaying, mediaPlayer) {
        if (isPlaying && mediaPlayer != null) {
            while (isPlaying) {
                try {
                    val current = mediaPlayer?.currentPosition ?: 0
                    val total = mediaPlayer?.duration ?: 1
                    if (total > 0) {
                        playbackProgress = (current.toFloat() / total).coerceIn(0f, 1f)
                    }
                } catch (e: Exception) {
                    // ignore
                }
                delay(100L)
            }
        }
    }

    DisposableEffect(audioPath) {
        onDispose {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            } catch (e: Exception) {
                // ignore
            }
            mediaPlayer = null
        }
    }

    fun togglePlayback() {
        if (isPlaying) {
            try {
                mediaPlayer?.pause()
            } catch (e: Exception) {
                // ignore
            }
            isPlaying = false
        } else {
            try {
                val file = File(audioPath)
                if (!file.exists()) {
                    Toast.makeText(context, "Áudio não encontrado", Toast.LENGTH_SHORT).show()
                    return
                }
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(audioPath)
                        prepare()
                        setOnCompletionListener {
                            isPlaying = false
                            playbackProgress = 0f
                        }
                    }
                }
                mediaPlayer?.start()
                isPlaying = true
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Erro ao reproduzir áudio", Toast.LENGTH_SHORT).show()
                isPlaying = false
            }
        }
    }

    fun seekTo(progress: Float) {
        playbackProgress = progress
        try {
            mediaPlayer?.let { player ->
                val targetMs = (player.duration * progress).toInt()
                player.seekTo(targetMs)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.40f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { togglePlayback() },
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pausar áudio" else "Reproduzir áudio",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gravação de Voz",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isPlaying) "Reproduzindo..." else "Onda de áudio",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                AudioWaveformPlayer(
                    isPlaying = isPlaying,
                    progress = playbackProgress,
                    onSeek = { seekTo(it) }
                )
            }

            if (onDeleteAudio != null) {
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = {
                        try {
                            mediaPlayer?.stop()
                            mediaPlayer?.release()
                        } catch (e: Exception) {
                            // ignore
                        }
                        mediaPlayer = null
                        isPlaying = false
                        onDeleteAudio()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remover áudio",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// VOICE RECORDING ACTIVE DIALOG
// ==========================================

@Composable
fun VoiceRecordingDialog(
    onDismiss: () -> Unit,
    onFinishRecording: (String) -> Unit
) {
    val context = LocalContext.current
    val recorderHelper = remember { AudioRecorderHelper(context) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var isRecordingActive by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val started = recorderHelper.startRecording()
        if (started != null) {
            isRecordingActive = true
            while (isActive && isRecordingActive) {
                delay(1000L)
                recordingSeconds++
            }
        } else {
            Toast.makeText(context, "Não foi possível iniciar a gravação", Toast.LENGTH_SHORT).show()
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = {
            recorderHelper.cancelRecording()
            onDismiss()
        },
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = "Gravando Nota de Voz",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val minutes = recordingSeconds / 60
                val secs = recordingSeconds % 60
                Text(
                    text = String.format("%02d:%02d", minutes, secs),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Fale perto do microfone...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isRecordingActive = false
                    val path = recorderHelper.stopRecording()
                    if (!path.isNullOrBlank()) {
                        onFinishRecording(path)
                    } else {
                        Toast.makeText(context, "Áudio não gravado", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Concluir e Salvar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    isRecordingActive = false
                    recorderHelper.cancelRecording()
                    onDismiss()
                }
            ) {
                Text("Cancelar")
            }
        }
    )
}

// ==========================================
// IMAGE ATTACHMENT VIEW
// ==========================================

@Composable
fun AttachedImageView(
    imageUriOrPath: String,
    modifier: Modifier = Modifier,
    onDeleteImage: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        AsyncImage(
            model = imageUriOrPath,
            contentDescription = "Imagem da nota",
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 240.dp),
            contentScale = ContentScale.Crop
        )

        if (onDeleteImage != null) {
            IconButton(
                onClick = onDeleteImage,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remover imagem",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ==========================================
// RICH CONTENT RENDERER (CHECKLISTS & BULLETS)
// ==========================================

@Composable
fun RichContentView(
    content: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    onToggleCheckbox: ((Int) -> Unit)? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val lines = remember(content) { content.lines() }
    val displayLines = if (maxLines != Int.MAX_VALUE) lines.take(maxLines) else lines

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        displayLines.forEachIndexed { index, line ->
            val trimmed = line.trimStart()
            when {
                trimmed.startsWith("[ ] ") || trimmed.startsWith("- [ ] ") -> {
                    val label = trimmed.removePrefix("- ").removePrefix("[ ] ")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .let {
                                if (onToggleCheckbox != null) it.clickable { onToggleCheckbox(index) } else it
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = false,
                            onCheckedChange = { onToggleCheckbox?.invoke(index) },
                            modifier = Modifier.size(20.dp),
                            colors = CheckboxDefaults.colors(
                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                trimmed.startsWith("[x] ") || trimmed.startsWith("- [x] ") -> {
                    val label = trimmed.removePrefix("- ").removePrefix("[x] ")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .let {
                                if (onToggleCheckbox != null) it.clickable { onToggleCheckbox(index) } else it
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = true,
                            onCheckedChange = { onToggleCheckbox?.invoke(index) },
                            modifier = Modifier.size(20.dp),
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = TextDecoration.LineThrough,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
                trimmed.startsWith("• ") || trimmed.startsWith("* ") || trimmed.startsWith("- ") -> {
                    val label = trimmed.removePrefix("• ").removePrefix("* ").removePrefix("- ")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                else -> {
                    if (line.isNotBlank()) {
                        val annotatedText = remember(line, primaryColor) {
                            MarkdownHelper.parseSimpleMarkdown(line, primaryColor)
                        }
                        Text(
                            text = annotatedText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// LOCKED NOTE CARD OVERLAY & BLUR
// ==========================================

@Composable
fun LockedNoteOverlay(
    isLocked: Boolean,
    isUnlockedThisSession: Boolean,
    onUnlockClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (isLocked && !isUnlockedThisSession) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onUnlockClick() }
        ) {
            // Blurred/Obscured background preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .blur(16.dp)
            ) {
                content()
            }

            // Locked overlay banner
            Surface(
                modifier = Modifier.matchParentSize(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nota Bloqueada",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Toque para desbloquear",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    } else {
        content()
    }
}

// ==========================================
// PIN UNLOCK DIALOG
// ==========================================

@Composable
fun PinUnlockDialog(
    noteTitle: String,
    actualPin: String?,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    fun verify() {
        if (CryptoHelper.verifyPin(enteredPin.trim(), actualPin)) {
            onSuccess()
        } else {
            isError = true
            enteredPin = ""
        }
    }

    fun triggerBiometrics() {
        val activity = context.findActivity()
        if (activity != null) {
            BiometricHelper.authenticate(
                activity = activity,
                title = "Desbloquear Nota",
                subtitle = if (noteTitle.isNotBlank()) noteTitle else "Confirme sua identidade",
                onSuccess = onSuccess,
                onError = { errorMessage ->
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            )
        } else {
            Toast.makeText(context, "Dispositivo indisponível para biometria.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (BiometricHelper.canAuthenticate(context)) {
            triggerBiometrics()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Desbloquear Nota",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (noteTitle.isNotBlank()) {
                    Text(
                        text = noteTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Text(
                    text = "Digite seu PIN de segurança ou use a biometria cadastrada.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = {
                        if (it.length <= 8) {
                            enteredPin = it
                            isError = false
                        }
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    keyboardActions = KeyboardActions(onDone = { verify() }),
                    singleLine = true,
                    isError = isError,
                    placeholder = { Text("Senha / PIN") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (isError) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Senha incorreta! Tente novamente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (BiometricHelper.canAuthenticate(context)) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { triggerBiometrics() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Usar Biometria Digital")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { verify() },
                enabled = enteredPin.isNotBlank(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Desbloquear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// ==========================================
// PIN SETUP DIALOG
// ==========================================

@Composable
fun SetPinDialog(
    initialPin: String?,
    onDismiss: () -> Unit,
    onSavePin: (String) -> Unit
) {
    var pinValue by remember { mutableStateOf(initialPin ?: "1234") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Key,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Definir PIN de Segurança",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Defina um PIN de 4 dígitos ou senha para proteger esta nota. Padrão: 1234",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = pinValue,
                    onValueChange = { if (it.length <= 12) pinValue = it },
                    placeholder = { Text("Ex: 1234") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSavePin(if (pinValue.isNotBlank()) pinValue.trim() else "1234")
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Salvar PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// ==========================================
// FORMATTING & RICH ATTACHMENT TOOLBAR
// ==========================================

@Composable
fun NoteCustomizationToolbar(
    isPinned: Boolean,
    onTogglePin: () -> Unit,
    isLocked: Boolean,
    onToggleLock: () -> Unit,
    onInsertText: (String) -> Unit,
    onAddImage: () -> Unit,
    onStartVoiceRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Checklist inserter
            IconButton(
                onClick = { onInsertText("\n[ ] ") },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckBox,
                    contentDescription = "Inserir item de lista",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Bullet list inserter
            IconButton(
                onClick = { onInsertText("\n• ") },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                    contentDescription = "Inserir marcadores",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Image picker button
            IconButton(
                onClick = onAddImage,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Adicionar imagem",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Voice recorder button
            IconButton(
                onClick = onStartVoiceRecording,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Gravar áudio",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Lock toggle
            IconButton(
                onClick = onToggleLock,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = if (isLocked) "Nota bloqueada" else "Bloquear nota",
                    tint = if (isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Pin toggle
            IconButton(
                onClick = onTogglePin,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                    contentDescription = if (isPinned) "Desafixar" else "Fixar nota",
                    tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
