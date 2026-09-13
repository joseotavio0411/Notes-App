package com.example.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

object SearchHighlighter {

    @Composable
    fun highlight(
        text: String,
        query: String,
        highlightColor: Color = MaterialTheme.colorScheme.primaryContainer,
        textColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
    ): AnnotatedString {
        if (query.isBlank() || text.isBlank()) {
            return AnnotatedString(text)
        }

        val cleanQuery = query.trim()
        val lowerText = text.lowercase()
        val lowerQuery = cleanQuery.lowercase()

        return buildAnnotatedString {
            var startIndex = 0
            while (startIndex < text.length) {
                val matchIndex = lowerText.indexOf(lowerQuery, startIndex)
                if (matchIndex == -1) {
                    append(text.substring(startIndex))
                    break
                }

                // Append text before match
                if (matchIndex > startIndex) {
                    append(text.substring(startIndex, matchIndex))
                }

                // Append highlighted match
                val endIndex = matchIndex + lowerQuery.length
                val matchText = text.substring(matchIndex, endIndex)
                pushStyle(
                    SpanStyle(
                        background = highlightColor,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                )
                append(matchText)
                pop()

                startIndex = endIndex
            }
        }
    }
}
