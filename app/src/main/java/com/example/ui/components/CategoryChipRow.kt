package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryHelper

@Composable
fun CategoryChipRow(
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    categories: List<String> = CategoryHelper.defaultCategories,
    onAddCustomCategory: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryText by remember { mutableStateOf("") }

    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Nova Categoria") },
            text = {
                OutlinedTextField(
                    value = newCategoryText,
                    onValueChange = { newCategoryText = it },
                    label = { Text("Nome da categoria") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_category_input")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = newCategoryText.trim()
                        if (trimmed.isNotBlank()) {
                            onAddCustomCategory?.invoke(trimmed)
                            onSelectCategory(trimmed)
                            newCategoryText = ""
                            showAddCategoryDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_new_category_button")
                ) {
                    Text("Adicionar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "Todas" chip
        FilterChip(
            selected = selectedCategory == "Todas",
            onClick = { onSelectCategory("Todas") },
            label = { Text("Todas") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier.testTag("filter_chip_all_categories")
        )

        // Predefined + Custom categories
        categories.forEach { cat ->
            val isSelected = selectedCategory == cat
            val catColor = CategoryHelper.getCategoryColor(cat)
            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(cat) },
                label = { Text(cat) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(catColor)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = catColor.copy(alpha = 0.2f),
                    selectedLabelColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.testTag("filter_chip_category_$cat")
            )
        }

        // Add custom category button
        if (onAddCustomCategory != null) {
            IconButton(
                onClick = { showAddCategoryDialog = true },
                modifier = Modifier
                    .size(32.dp)
                    .testTag("add_custom_category_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Adicionar categoria",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    if (category.isBlank() || category.equals("Geral", ignoreCase = true)) return
    val color = CategoryHelper.getCategoryColor(category)
    val bgColor = color.copy(alpha = 0.15f)

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = category,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
