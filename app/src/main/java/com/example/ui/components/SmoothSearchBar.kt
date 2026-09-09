package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class SearchAttachmentFilter(val label: String) {
    ALL("Todos"),
    PHOTOS("Fotos"),
    AUDIO("Áudios"),
    PINNED("Fixadas")
}

@Composable
fun SmoothSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    attachmentFilter: SearchAttachmentFilter,
    onAttachmentFilterChange: (SearchAttachmentFilter) -> Unit,
    discoveredTags: List<String>,
    modifier: Modifier = Modifier,
    placeholder: String = "Pesquisar texto, #tags ou anexos..."
) {
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }

    // Smooth focus indicator animation using FastOutSlowInEasing
    val focusAnimatable = remember { Animatable(0f) }
    LaunchedEffect(isFocused) {
        focusAnimatable.animateTo(
            targetValue = if (isFocused) 1f else 0f,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
        )
    }

    // Smooth blinking / pulse cursor indicator animation
    val cursorBlinkAnimatable = remember { Animatable(1f) }
    LaunchedEffect(isFocused) {
        if (isFocused) {
            cursorBlinkAnimatable.animateTo(
                targetValue = 0.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            cursorBlinkAnimatable.snapTo(1f)
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "searchBorderColor"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Search Input Box
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("smooth_search_bar"),
            shape = RoundedCornerShape(16.dp),
            color = surfaceColor,
            tonalElevation = (focusAnimatable.value * 3).dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search icon
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Pesquisar",
                        tint = if (isFocused || query.isNotEmpty()) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // BasicTextField with smooth custom layout
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            )
                        }

                        BasicTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isFocused = it.isFocused }
                                .testTag("search_text_input"),
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(primaryColor.copy(alpha = cursorBlinkAnimatable.value)),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                        )
                    }

                    // Clear button
                    AnimatedVisibility(
                        visible = query.isNotEmpty(),
                        enter = fadeIn(tween(150)),
                        exit = fadeOut(tween(150))
                    ) {
                        IconButton(
                            onClick = {
                                onQueryChange("")
                                focusManager.clearFocus()
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpar busca",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Smooth Focus Indicator Bar at the bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(focusAnimatable.value)
                            .height(2.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        primaryColor.copy(alpha = 0.5f),
                                        primaryColor,
                                        primaryColor.copy(alpha = 0.5f)
                                    )
                                )
                            )
                    )
                }
            }
        }

        // Filter chips row (All, Photos, Audio, Pinned, and discovered hashtags)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Attachment filter chips
            SearchAttachmentFilter.entries.forEach { filter ->
                val isSelected = attachmentFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { onAttachmentFilterChange(filter) },
                    label = {
                        Text(
                            text = filter.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        when (filter) {
                            SearchAttachmentFilter.ALL -> Icon(
                                Icons.Default.ViewList,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            SearchAttachmentFilter.PHOTOS -> Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            SearchAttachmentFilter.AUDIO -> Icon(
                                Icons.Default.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            SearchAttachmentFilter.PINNED -> Icon(
                                Icons.Default.PushPin,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.height(32.dp)
                )
            }

            // Quick Hashtag chips from user notes/tasks
            discoveredTags.take(8).forEach { tag ->
                val tagFormatted = if (tag.startsWith("#")) tag else "#$tag"
                val isTagInQuery = query.contains(tagFormatted, ignoreCase = true)

                FilterChip(
                    selected = isTagInQuery,
                    onClick = {
                        if (isTagInQuery) {
                            val newQ = query.replace(tagFormatted, "").trim()
                            onQueryChange(newQ)
                        } else {
                            val newQ = if (query.isBlank()) tagFormatted else "$query $tagFormatted"
                            onQueryChange(newQ)
                        }
                    },
                    label = {
                        Text(
                            text = tagFormatted,
                            fontSize = 12.sp,
                            fontWeight = if (isTagInQuery) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Tag,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isTagInQuery) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier.height(32.dp)
                )
            }
        }
    }
}
