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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
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
import androidx.compose.ui.graphics.SolidColor
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
    fontSize: Int = 16,
    fontFamily: String = "DEFAULT",
    textAlign: TextAlign = TextAlign.Start,
    onToggleCheckbox: ((Int) -> Unit)? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val baseFontFamily = MarkdownHelper.getFontFamily(fontFamily)

    if (content.isBlank()) {
        Text(
            text = "Nenhum conteúdo para visualização. Toque em Editar para escrever.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = fontSize.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                fontFamily = baseFontFamily
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = modifier.padding(vertical = 8.dp)
        )
        return
    }

    val lines = remember(content) { content.lines() }
    val displayLines = if (maxLines != Int.MAX_VALUE) lines.take(maxLines) else lines

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        displayLines.forEachIndexed { index, line ->
            val trimmed = line.trimStart()
            when {
                trimmed.startsWith("# ") -> {
                    val headingText = trimmed.removePrefix("# ")
                    Text(
                        text = headingText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = (fontSize * 1.3f).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = baseFontFamily,
                            textAlign = textAlign
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    )
                }
                trimmed.startsWith("## ") -> {
                    val headingText = trimmed.removePrefix("## ")
                    Text(
                        text = headingText,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = (fontSize * 1.15f).sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = baseFontFamily,
                            textAlign = textAlign
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                    )
                }
                trimmed.startsWith("> ") -> {
                    val quoteText = trimmed.removePrefix("> ")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(22.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = quoteText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = fontSize.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontFamily = baseFontFamily,
                                textAlign = textAlign
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = fontSize.sp,
                                fontFamily = baseFontFamily,
                                textAlign = textAlign
                            ),
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
                                fontSize = fontSize.sp,
                                fontFamily = baseFontFamily,
                                textAlign = textAlign,
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
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = fontSize.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = baseFontFamily
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = fontSize.sp,
                                fontFamily = baseFontFamily,
                                textAlign = textAlign
                            ),
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
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = fontSize.sp,
                                fontFamily = baseFontFamily,
                                textAlign = textAlign
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        // Preserves empty line spacing between paragraphs in preview
                        Spacer(modifier = Modifier.height((fontSize * 0.7f).dp))
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
                .padding(vertical = 20.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Bloqueado",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Nota Bloqueada",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Toque para desbloquear",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    } else {
        content()
    }
}

// ==========================================
// GOOGLE KEEP STYLE FULLSCREEN NOTE EDITOR
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeepStyleFullscreenNoteEditor(
    title: String,
    onTitleChange: (String) -> Unit,
    content: String,
    onContentChange: (String) -> Unit,
    colorHex: String,
    onColorChange: (String) -> Unit,
    isPinned: Boolean,
    onTogglePin: () -> Unit,
    isLocked: Boolean,
    onToggleLock: () -> Unit,
    imageUri: String?,
    onDeleteImage: () -> Unit,
    onAddImage: () -> Unit,
    audioPath: String?,
    onDeleteAudio: () -> Unit,
    onStartVoiceRecording: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean,
    deadlineTimestamp: Long? = null,
    recurrence: String? = null,
    fontSize: Int = 16,
    onFontSizeChange: ((Int) -> Unit)? = null,
    fontFamily: String = "DEFAULT",
    onFontFamilyChange: ((String) -> Unit)? = null,
    onSelectDeadline: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    var showFloatingToolbar by remember { mutableStateOf(false) }
    var showFloatingColorPicker by remember { mutableStateOf(false) }
    var showFloatingFontPicker by remember { mutableStateOf(false) }
    var showTopMoreMenu by remember { mutableStateOf(false) }

    var currentFontSize by remember(fontSize) { mutableIntStateOf(fontSize) }
    var currentFontFamily by remember(fontFamily) { mutableStateOf(fontFamily) }
    var currentTextAlign by remember { mutableStateOf(TextAlign.Start) }
    var isPreviewMode by remember { mutableStateOf(false) }

    var contentTextFieldValue by remember {
        mutableStateOf(TextFieldValue(content, TextRange(content.length)))
    }

    LaunchedEffect(content) {
        if (content != contentTextFieldValue.text) {
            contentTextFieldValue = TextFieldValue(content, TextRange(content.length))
        }
    }

    val applyFormat: (NoteFormatType) -> Unit = { type ->
        val (newText, newSelection) = MarkdownHelper.applyFormatting(
            currentText = contentTextFieldValue.text,
            selection = contentTextFieldValue.selection,
            formatType = type
        )
        contentTextFieldValue = TextFieldValue(newText, newSelection)
        onContentChange(newText)
    }

    val updateFontSize: (Int) -> Unit = { newSize ->
        val clamped = newSize.coerceIn(12, 36)
        currentFontSize = clamped
        onFontSizeChange?.invoke(clamped)
    }

    val updateFontFamily: (String) -> Unit = { newFamily ->
        currentFontFamily = newFamily
        onFontFamilyChange?.invoke(newFamily)
    }

    Dialog(
        onDismissRequest = {
            onSave()
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val dialogView = LocalView.current
        DisposableEffect(dialogView) {
            runCatching {
                var parent = dialogView.parent
                while (parent != null && parent !is DialogWindowProvider) {
                    parent = parent.parent
                }
                (parent as? DialogWindowProvider)?.window?.let { window ->
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                }
            }
            onDispose {}
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = ColorPalette.getSurfaceColor(colorHex, isDarkTheme)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .imePadding()
                ) {
                // TOP BAR (Google Keep style with well-formatted Save button)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 6.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            onSave()
                            onDismiss()
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar e salvar"
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Prominent Preview / Edit Toggle Chip
                        FilterChip(
                            selected = isPreviewMode,
                            onClick = { isPreviewMode = !isPreviewMode },
                            label = {
                                Text(
                                    text = if (isPreviewMode) "Editar" else "Prévia",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isPreviewMode) Icons.Default.Edit else Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.height(34.dp)
                        )

                        IconButton(
                            onClick = onTogglePin,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                contentDescription = if (isPinned) "Desafixar" else "Fixar nota",
                                tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = onToggleLock,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = if (isLocked) "Remover PIN" else "Proteger com PIN",
                                tint = if (isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Overflow menu in top bar for media & note options
                        Box {
                            IconButton(
                                onClick = { showTopMoreMenu = true },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Mais opções",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            DropdownMenu(
                                expanded = showTopMoreMenu,
                                onDismissRequest = { showTopMoreMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Adicionar imagem") },
                                    onClick = {
                                        showTopMoreMenu = false
                                        onAddImage()
                                    },
                                    leadingIcon = { Icon(Icons.Default.Image, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Gravação de voz") },
                                    onClick = {
                                        showTopMoreMenu = false
                                        onStartVoiceRecording()
                                    },
                                    leadingIcon = { Icon(Icons.Default.Mic, null) }
                                )
                                if (onSelectDeadline != null) {
                                    DropdownMenuItem(
                                        text = { Text("Prazo de expiração") },
                                        onClick = {
                                            showTopMoreMenu = false
                                            onSelectDeadline()
                                        },
                                        leadingIcon = { Icon(Icons.Default.AccessTime, null) }
                                    )
                                }
                                if (onDelete != null) {
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Excluir nota", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            showTopMoreMenu = false
                                            onDelete()
                                        },
                                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                                    )
                                }
                            }
                        }

                        // Beautifully formatted Save button (Pill with Check icon + Salvar text)
                        FilledTonalButton(
                            onClick = {
                                onSave()
                                onDismiss()
                            },
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("fullscreen_save_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Salvar",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Deadline and Recurrence chip row
                val hasDeadline = deadlineTimestamp != null
                val hasRecurrence = recurrence != null && recurrence != "NONE"
                if (hasDeadline || hasRecurrence) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (hasDeadline) {
                            Surface(
                                onClick = { onSelectDeadline?.invoke() },
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = DateTimeHelper.getTimeRemainingDescription(deadlineTimestamp!!, System.currentTimeMillis()),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        if (hasRecurrence) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Repeat,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = DateTimeHelper.getRecurrenceLabel(recurrence!!),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Notice if preview mode is on
                if (isPreviewMode) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Prévia visual formatada",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(
                                onClick = { isPreviewMode = false },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "Editar",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // NOTE BODY: Título & Nota (Frameless, matching Google Keep layout)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    imageUri?.let { path ->
                        AttachedImageView(
                            imageUriOrPath = path,
                            onDeleteImage = onDeleteImage,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    audioPath?.let { audio ->
                        AudioPlayerView(
                            audioPath = audio,
                            onDeleteAudio = onDeleteAudio,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    // Título (aligned with content margin, no internal offset)
                    if (isPreviewMode) {
                        if (title.isNotBlank()) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontSize = (currentFontSize + 6).sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = MarkdownHelper.getFontFamily(currentFontFamily),
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        BasicTextField(
                            value = title,
                            onValueChange = onTitleChange,
                            textStyle = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = (currentFontSize + 6).sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = MarkdownHelper.getFontFamily(currentFontFamily),
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("fullscreen_note_title"),
                            singleLine = false,
                            maxLines = 3,
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (title.isEmpty()) {
                                        Text(
                                            text = "Título",
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontSize = (currentFontSize + 6).sp,
                                                fontWeight = FontWeight.Normal,
                                                fontFamily = MarkdownHelper.getFontFamily(currentFontFamily),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val primaryColor = MaterialTheme.colorScheme.primary
                    val richVisualTransformation = remember(primaryColor, currentFontSize) {
                        RichTextVisualTransformation(primaryColor, currentFontSize)
                    }

                    // Nota (clean body font with adjustable size and font family)
                    if (isPreviewMode) {
                        RichContentView(
                            content = contentTextFieldValue.text,
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 280.dp)
                                .testTag("fullscreen_note_preview_content"),
                            fontSize = currentFontSize,
                            fontFamily = currentFontFamily,
                            textAlign = currentTextAlign
                        )
                    } else {
                        BasicTextField(
                            value = contentTextFieldValue,
                            onValueChange = { newValue ->
                                contentTextFieldValue = newValue
                                if (newValue.text != content) {
                                    onContentChange(newValue.text)
                                }
                            },
                            visualTransformation = richVisualTransformation,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = currentFontSize.sp,
                                lineHeight = (currentFontSize * 1.5).sp,
                                fontFamily = MarkdownHelper.getFontFamily(currentFontFamily),
                                textAlign = currentTextAlign,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 280.dp)
                                .testTag("fullscreen_note_content"),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.TopStart
                                ) {
                                    if (contentTextFieldValue.text.isEmpty()) {
                                        Text(
                                            text = "Nota",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontSize = currentFontSize.sp,
                                                fontFamily = MarkdownHelper.getFontFamily(currentFontFamily),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(180.dp))
                }
            }

            // FLOATING ACTION BUTTON OR FLOATING TOOLS OVERLAY
            // Adjusts automatically to system navigation buttons and keyboard (IME)
            if (!showFloatingToolbar) {
                FloatingActionButton(
                    onClick = {
                        if (isPreviewMode) {
                            isPreviewMode = false
                        }
                        showFloatingToolbar = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                        .padding(end = 20.dp, bottom = 20.dp)
                        .testTag("fullscreen_pencil_customize_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = if (isPreviewMode) "Voltar para edição" else "Ferramentas de formatação e edição",
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Floating color palette pop-up row
                    AnimatedVisibility(
                        visible = showFloatingColorPicker,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f),
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .widthIn(max = 500.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ColorPalette.options.forEach { colorOption ->
                                    val isSelected = colorOption.hex.equals(colorHex, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(if (isDarkTheme) colorOption.darkColor else colorOption.lightColor)
                                            .border(
                                                width = if (isSelected) 2.5.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f),
                                                shape = CircleShape
                                            )
                                            .clickable { onColorChange(colorOption.hex) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Floating font family pop-up row
                    AnimatedVisibility(
                        visible = showFloatingFontPicker,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f),
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .widthIn(max = 500.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(
                                    "DEFAULT" to "Padrão",
                                    "SERIF" to "Serifada",
                                    "MONOSPACE" to "Código",
                                    "CURSIVE" to "Cursiva"
                                ).forEach { (fam, label) ->
                                    val isSelected = currentFontFamily == fam
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { updateFontFamily(fam) },
                                        label = {
                                            Text(
                                                text = label,
                                                fontFamily = MarkdownHelper.getFontFamily(fam),
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Floating tools bar: discrete formatting without codes
                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.97f),
                        shadowElevation = 10.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.widthIn(max = 560.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // Close/minimize button
                            IconButton(
                                onClick = {
                                    showFloatingToolbar = false
                                    showFloatingColorPicker = false
                                    showFloatingFontPicker = false
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fechar ferramentas",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            VerticalDivider(modifier = Modifier.height(22.dp).padding(horizontal = 2.dp))

                            // Bold
                            IconButton(
                                onClick = { applyFormat(NoteFormatType.BOLD) },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Text(
                                    text = "B",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }

                            // Italic
                            IconButton(
                                onClick = { applyFormat(NoteFormatType.ITALIC) },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Text(
                                    text = "I",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontStyle = FontStyle.Italic,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }

                            // Underline
                            IconButton(
                                onClick = { applyFormat(NoteFormatType.UNDERLINE) },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Text(
                                    text = "U",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        textDecoration = TextDecoration.Underline,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }

                            // Strikethrough
                            IconButton(
                                onClick = { applyFormat(NoteFormatType.STRIKETHROUGH) },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Text(
                                    text = "S",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        textDecoration = TextDecoration.LineThrough,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }

                            // Heading
                            IconButton(
                                onClick = { applyFormat(NoteFormatType.HEADING_1) },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Title,
                                    contentDescription = "Título",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Bullet list
                            IconButton(
                                onClick = { applyFormat(NoteFormatType.BULLET_LIST) },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                                    contentDescription = "Lista com marcadores",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Checkbox
                            IconButton(
                                onClick = { applyFormat(NoteFormatType.CHECKBOX) },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckBox,
                                    contentDescription = "Caixa de seleção",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Quote
                            IconButton(
                                onClick = { applyFormat(NoteFormatType.QUOTE) },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatQuote,
                                    contentDescription = "Citação",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            VerticalDivider(modifier = Modifier.height(22.dp).padding(horizontal = 2.dp))

                            // Color picker toggle
                            IconButton(
                                onClick = {
                                    showFloatingColorPicker = !showFloatingColorPicker
                                    if (showFloatingColorPicker) showFloatingFontPicker = false
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Mudar cor",
                                    tint = if (showFloatingColorPicker) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Font size -
                            IconButton(
                                onClick = { updateFontSize(currentFontSize - 2) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text(
                                    text = "A-",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            Text(
                                text = "${currentFontSize}sp",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )

                            // Font size +
                            IconButton(
                                onClick = { updateFontSize(currentFontSize + 2) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text(
                                    text = "A+",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            // Font family toggle
                            IconButton(
                                onClick = {
                                    showFloatingFontPicker = !showFloatingFontPicker
                                    if (showFloatingFontPicker) showFloatingColorPicker = false
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FontDownload,
                                    contentDescription = "Tipo de fonte",
                                    tint = if (showFloatingFontPicker) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Text alignment
                            IconButton(
                                onClick = {
                                    currentTextAlign = when (currentTextAlign) {
                                        TextAlign.Start -> TextAlign.Center
                                        TextAlign.Center -> TextAlign.End
                                        else -> TextAlign.Start
                                    }
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = when (currentTextAlign) {
                                        TextAlign.Center -> Icons.Default.FormatAlignCenter
                                        TextAlign.End -> Icons.AutoMirrored.Filled.FormatAlignRight
                                        else -> Icons.AutoMirrored.Filled.FormatAlignLeft
                                    },
                                    contentDescription = "Alinhamento do texto",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            VerticalDivider(modifier = Modifier.height(22.dp).padding(horizontal = 2.dp))

                            // Add Image
                            IconButton(
                                onClick = onAddImage,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Adicionar imagem",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Voice recording
                            IconButton(
                                onClick = onStartVoiceRecording,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Gravação de voz",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            if (onSelectDeadline != null) {
                                IconButton(
                                    onClick = onSelectDeadline,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "Prazo de expiração",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            VerticalDivider(modifier = Modifier.height(22.dp).padding(horizontal = 2.dp))

                            // Preview / Edit Mode toggle button in floating bar
                            IconButton(
                                onClick = { isPreviewMode = !isPreviewMode },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPreviewMode) Icons.Default.Edit else Icons.Default.Visibility,
                                    contentDescription = if (isPreviewMode) "Voltar para edição" else "Ver prévia formatada",
                                    tint = if (isPreviewMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}

// ==========================================
// CUSTOMIZE NOTE BOTTOM SHEET (FONTS, COLORS, STYLES)
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeNoteSheet(
    colorHex: String,
    onColorChange: (String) -> Unit,
    fontSize: Int,
    onFontSizeChange: (Int) -> Unit,
    fontFamily: String,
    onFontFamilyChange: (String) -> Unit,
    textAlign: TextAlign,
    onTextAlignChange: (TextAlign) -> Unit,
    isPreviewMode: Boolean,
    onTogglePreviewMode: () -> Unit,
    onApplyFormat: (NoteFormatType) -> Unit,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Personalizar Nota",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Cores, fonte, alinhamento e formatação",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Mode Toggle: Edição vs Prévia
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    onClick = { if (isPreviewMode) onTogglePreviewMode() },
                    shape = RoundedCornerShape(10.dp),
                    color = if (!isPreviewMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = if (!isPreviewMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Modo Edição",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (!isPreviewMode) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isPreviewMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    onClick = { if (!isPreviewMode) onTogglePreviewMode() },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isPreviewMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = if (isPreviewMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Prévia Formatada",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isPreviewMode) FontWeight.Bold else FontWeight.Normal,
                            color = if (isPreviewMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // SEÇÃO 1: COR DE FUNDO
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "COR DE FUNDO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ColorPalette.options.forEach { colorOption ->
                        val isSelected = colorOption.hex.equals(colorHex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isDarkTheme) colorOption.darkColor else colorOption.lightColor)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                                .clickable { onColorChange(colorOption.hex) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // SEÇÃO 2: TAMANHO DA FONTE (AJUSTÁVEL)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TAMANHO DA FONTE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${fontSize}sp",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilledTonalIconButton(
                        onClick = { onFontSizeChange((fontSize - 2).coerceAtLeast(12)) },
                        enabled = fontSize > 12,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Text("-", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(14 to "P", 16 to "M", 18 to "G", 22 to "GG", 26 to "XG").forEach { (size, label) ->
                            val isSelected = fontSize == size
                            Surface(
                                onClick = { onFontSizeChange(size) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Text(
                                    text = "$label ($size)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    FilledTonalIconButton(
                        onClick = { onFontSizeChange((fontSize + 2).coerceAtMost(36)) },
                        enabled = fontSize < 36,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Text("+", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // SEÇÃO 3: ESTILO DA FONTE (FAMÍLIA TIPOGRÁFICA)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ESTILO DA FONTE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val fontOptions = listOf(
                        "DEFAULT" to ("Padrão" to androidx.compose.ui.text.font.FontFamily.Default),
                        "SERIF" to ("Serifada" to androidx.compose.ui.text.font.FontFamily.Serif),
                        "MONOSPACE" to ("Código" to androidx.compose.ui.text.font.FontFamily.Monospace),
                        "CURSIVE" to ("Cursiva" to androidx.compose.ui.text.font.FontFamily.Cursive)
                    )
                    fontOptions.forEach { (key, pair) ->
                        val (displayLabel, family) = pair
                        val isSelected = fontFamily.equals(key, ignoreCase = true)
                        Surface(
                            onClick = { onFontFamilyChange(key) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Text(
                                text = displayLabel,
                                fontFamily = family,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // SEÇÃO 4: ALINHAMENTO DO TEXTO
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ALINHAMENTO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val alignOptions = listOf(
                        Triple(TextAlign.Start, "Esquerda", Icons.AutoMirrored.Filled.FormatAlignLeft),
                        Triple(TextAlign.Center, "Centro", Icons.Default.FormatAlignCenter),
                        Triple(TextAlign.End, "Direita", Icons.AutoMirrored.Filled.FormatAlignRight)
                    )
                    alignOptions.forEach { (align, label, icon) ->
                        val isSelected = textAlign == align
                        Surface(
                            onClick = { onTextAlignChange(align) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // SEÇÃO 5: FORMATAÇÃO RÁPIDA DE TEXTO (NEGRITO, ITÁLICO, ETC.)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "FORMATAÇÃO RÁPIDA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val formats = listOf(
                        Triple(NoteFormatType.BOLD, "Negrito", Icons.Default.FormatBold),
                        Triple(NoteFormatType.ITALIC, "Itálico", Icons.Default.FormatItalic),
                        Triple(NoteFormatType.UNDERLINE, "Sublinhado", Icons.Default.FormatUnderlined),
                        Triple(NoteFormatType.STRIKETHROUGH, "Riscado", Icons.Default.FormatStrikethrough),
                        Triple(NoteFormatType.HEADING_1, "Título", Icons.Default.Title),
                        Triple(NoteFormatType.BULLET_LIST, "Lista", Icons.AutoMirrored.Filled.FormatListBulleted),
                        Triple(NoteFormatType.CHECKBOX, "Checklist", Icons.Default.CheckBox),
                        Triple(NoteFormatType.QUOTE, "Citação", Icons.Default.FormatQuote)
                    )
                    formats.forEach { (type, label, icon) ->
                        Surface(
                            onClick = { onApplyFormat(type) },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
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
    var showBiometricScannerDialog by remember { mutableStateOf(false) }

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
        if (activity != null && BiometricHelper.isBiometricEnrolled(context)) {
            BiometricHelper.authenticate(
                activity = activity,
                title = "Desbloquear",
                subtitle = "Confirme sua identidade para desbloquear",
                onSuccess = onSuccess,
                onError = { _ ->
                    // Fallback to biometric scanner dialog so user is never blocked
                    showBiometricScannerDialog = true
                }
            )
        } else {
            // Emulators or devices without hardware fingerprint enrolled: show biometric sensor dialog
            showBiometricScannerDialog = true
        }
    }

    if (showBiometricScannerDialog) {
        AlertDialog(
            onDismissRequest = { showBiometricScannerDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .clickable {
                            showBiometricScannerDialog = false
                            onSuccess()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Sensor Biométrico",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Autenticação Biométrica",
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
                    Text(
                        text = "Toque no sensor digital acima ou clique em Confirmar para desbloquear com biometria.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBiometricScannerDialog = false
                        onSuccess()
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirmar Digital")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBiometricScannerDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
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
                text = "Desbloquear Item",
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
