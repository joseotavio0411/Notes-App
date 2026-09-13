package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

@Composable
fun DailySummaryCard(
    pendingTasksCount: Int,
    dueTodayDeadlinesCount: Int,
    overdueDeadlinesCount: Int,
    onNavigateToDeadlines: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }

    if (!isVisible || (pendingTasksCount == 0 && dueTodayDeadlinesCount == 0 && overdueDeadlinesCount == 0)) {
        return
    }

    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = when (currentHour) {
        in 5..11 -> "Bom dia!"
        in 12..17 -> "Boa tarde!"
        else -> "Boa noite!"
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("daily_summary_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    IconButton(
                        onClick = { isVisible = false },
                        modifier = Modifier.size(24.dp).testTag("dismiss_daily_summary_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Ocultar resumo",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (pendingTasksCount > 0) {
                        SummaryChip(
                            label = "$pendingTasksCount pendentes",
                            icon = Icons.Default.CheckCircle,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (dueTodayDeadlinesCount > 0) {
                        SummaryChip(
                            label = "$dueTodayDeadlinesCount para hoje",
                            icon = Icons.Default.Alarm,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (overdueDeadlinesCount > 0) {
                        SummaryChip(
                            label = "$overdueDeadlinesCount atrasados",
                            icon = Icons.Default.Alarm,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                if (dueTodayDeadlinesCount > 0 || overdueDeadlinesCount > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(
                        onClick = onNavigateToDeadlines,
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("view_due_deadlines_button")
                    ) {
                        Text("Ver prazos do dia →", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    Surface(
        color = tint.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = tint
            )
        }
    }
}
