package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

object MarkdownHelper {
    /**
     * Parses lightweight markdown for inline elements:
     * - **bold text**
     * - *italic text*
     * - `code snippet`
     */
    fun parseSimpleMarkdown(text: String, primaryColor: Color): AnnotatedString {
        return buildAnnotatedString {
            val regex = Regex("""(\*\*.*?\*\*|\*.*?\*|`.*?`|[^\*`]+)""")
            regex.findAll(text).forEach { match ->
                val part = match.value
                when {
                    part.startsWith("**") && part.endsWith("**") && part.length >= 4 -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(part.substring(2, part.length - 2))
                        }
                    }
                    part.startsWith("*") && part.endsWith("*") && part.length >= 2 -> {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(part.substring(1, part.length - 1))
                        }
                    }
                    part.startsWith("`") && part.endsWith("`") && part.length >= 2 -> {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                color = primaryColor,
                                background = primaryColor.copy(alpha = 0.12f)
                            )
                        ) {
                            append(" ${part.substring(1, part.length - 1)} ")
                        }
                    }
                    else -> append(part)
                }
            }
        }
    }
}
