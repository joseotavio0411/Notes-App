package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

enum class NoteFormatType {
    BOLD,
    ITALIC,
    UNDERLINE,
    STRIKETHROUGH,
    HEADING_1,
    HEADING_2,
    BULLET_LIST,
    CHECKBOX,
    QUOTE
}

object MarkdownHelper {
    /**
     * Parses lightweight markdown for inline and block elements:
     * - **bold text**
     * - *italic text*
     * - <u>underlined text</u> or __underlined text__
     * - ~~strikethrough text~~
     * - `code snippet`
     */
    fun parseSimpleMarkdown(text: String, primaryColor: Color): AnnotatedString {
        return buildAnnotatedString {
            val regex = Regex("""(<u>.*?</u>|__.*?__|~~.*?~~|\*\*.*?\*\*|\*.*?\*|`.*?`|(?:(?!<u>|__|\~\~|\*\*|\*|`).)+)""", RegexOption.DOT_MATCHES_ALL)
            regex.findAll(text).forEach { match ->
                val part = match.value
                when {
                    part.startsWith("<u>") && part.endsWith("</u>") && part.length >= 7 -> {
                        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                            append(part.substring(3, part.length - 4))
                        }
                    }
                    part.startsWith("__") && part.endsWith("__") && part.length >= 4 -> {
                        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                            append(part.substring(2, part.length - 2))
                        }
                    }
                    part.startsWith("~~") && part.endsWith("~~") && part.length >= 4 -> {
                        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                            append(part.substring(2, part.length - 2))
                        }
                    }
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

    /**
     * Applies markdown formatting to text, intelligently toggling formatting
     * on selected text or words at cursor, without showing raw symbols to the user.
     */
    fun applyFormatting(
        currentText: String,
        selection: TextRange,
        formatType: NoteFormatType
    ): Pair<String, TextRange> {
        val start = minOf(selection.start, selection.end).coerceIn(0, currentText.length)
        val end = maxOf(selection.start, selection.end).coerceIn(0, currentText.length)
        val hasSelection = start < end

        // If no selection, check if cursor is on/inside a word
        var selStart = start
        var selEnd = end

        if (!hasSelection) {
            when (formatType) {
                NoteFormatType.BOLD, NoteFormatType.ITALIC, NoteFormatType.UNDERLINE, NoteFormatType.STRIKETHROUGH -> {
                    var wStart = start
                    while (wStart > 0 && !currentText[wStart - 1].isWhitespace() && currentText[wStart - 1] !in "*~<_>#") {
                        wStart--
                    }
                    var wEnd = start
                    while (wEnd < currentText.length && !currentText[wEnd].isWhitespace() && currentText[wEnd] !in "*~<_>#") {
                        wEnd++
                    }
                    if (wStart < wEnd) {
                        selStart = wStart
                        selEnd = wEnd
                    }
                }
                else -> { /* line formats handle line positions directly */ }
            }
        }

        val hasEffectiveSelection = selStart < selEnd
        val selectedText = if (hasEffectiveSelection) currentText.substring(selStart, selEnd) else ""
        val before = currentText.substring(0, selStart)
        val after = currentText.substring(selEnd)

        return when (formatType) {
            NoteFormatType.BOLD -> {
                if (hasEffectiveSelection) {
                    // Check if already bold (enclosed in **)
                    if (before.endsWith("**") && after.startsWith("**")) {
                        val newBefore = before.dropLast(2)
                        val newAfter = after.drop(2)
                        Pair("$newBefore$selectedText$newAfter", TextRange(newBefore.length, newBefore.length + selectedText.length))
                    } else if (selectedText.startsWith("**") && selectedText.endsWith("**") && selectedText.length >= 4) {
                        val inner = selectedText.substring(2, selectedText.length - 2)
                        Pair("$before$inner$after", TextRange(selStart, selStart + inner.length))
                    } else {
                        val insert = "**$selectedText**"
                        Pair("$before$insert$after", TextRange(selStart + 2, selStart + 2 + selectedText.length))
                    }
                } else {
                    val insert = "**negrito**"
                    Pair("$before$insert$after", TextRange(selStart + 2, selStart + 2 + 7))
                }
            }
            NoteFormatType.ITALIC -> {
                if (hasEffectiveSelection) {
                    if (before.endsWith("*") && !before.endsWith("**") && after.startsWith("*") && !after.startsWith("**")) {
                        val newBefore = before.dropLast(1)
                        val newAfter = after.drop(1)
                        Pair("$newBefore$selectedText$newAfter", TextRange(newBefore.length, newBefore.length + selectedText.length))
                    } else if (selectedText.startsWith("*") && selectedText.endsWith("*") && selectedText.length >= 2) {
                        val inner = selectedText.substring(1, selectedText.length - 1)
                        Pair("$before$inner$after", TextRange(selStart, selStart + inner.length))
                    } else {
                        val insert = "*$selectedText*"
                        Pair("$before$insert$after", TextRange(selStart + 1, selStart + 1 + selectedText.length))
                    }
                } else {
                    val insert = "*itálico*"
                    Pair("$before$insert$after", TextRange(selStart + 1, selStart + 1 + 7))
                }
            }
            NoteFormatType.UNDERLINE -> {
                if (hasEffectiveSelection) {
                    if (before.endsWith("<u>") && after.startsWith("</u>")) {
                        val newBefore = before.dropLast(3)
                        val newAfter = after.drop(4)
                        Pair("$newBefore$selectedText$newAfter", TextRange(newBefore.length, newBefore.length + selectedText.length))
                    } else if (selectedText.startsWith("<u>") && selectedText.endsWith("</u>") && selectedText.length >= 7) {
                        val inner = selectedText.substring(3, selectedText.length - 4)
                        Pair("$before$inner$after", TextRange(selStart, selStart + inner.length))
                    } else {
                        val insert = "<u>$selectedText</u>"
                        Pair("$before$insert$after", TextRange(selStart + 3, selStart + 3 + selectedText.length))
                    }
                } else {
                    val insert = "<u>sublinhado</u>"
                    Pair("$before$insert$after", TextRange(selStart + 3, selStart + 3 + 10))
                }
            }
            NoteFormatType.STRIKETHROUGH -> {
                if (hasEffectiveSelection) {
                    if (before.endsWith("~~") && after.startsWith("~~")) {
                        val newBefore = before.dropLast(2)
                        val newAfter = after.drop(2)
                        Pair("$newBefore$selectedText$newAfter", TextRange(newBefore.length, newBefore.length + selectedText.length))
                    } else if (selectedText.startsWith("~~") && selectedText.endsWith("~~") && selectedText.length >= 4) {
                        val inner = selectedText.substring(2, selectedText.length - 2)
                        Pair("$before$inner$after", TextRange(selStart, selStart + inner.length))
                    } else {
                        val insert = "~~$selectedText~~"
                        Pair("$before$insert$after", TextRange(selStart + 2, selStart + 2 + selectedText.length))
                    }
                } else {
                    val insert = "~~riscado~~"
                    Pair("$before$insert$after", TextRange(selStart + 2, selStart + 2 + 7))
                }
            }
            NoteFormatType.HEADING_1 -> {
                var lineStart = start
                while (lineStart > 0 && currentText[lineStart - 1] != '\n') lineStart--
                var lineEnd = start
                while (lineEnd < currentText.length && currentText[lineEnd] != '\n') lineEnd++
                val line = currentText.substring(lineStart, lineEnd)

                if (line.startsWith("# ")) {
                    // Toggle off
                    val newLine = line.removePrefix("# ")
                    val newText = currentText.substring(0, lineStart) + newLine + currentText.substring(lineEnd)
                    Pair(newText, TextRange((start - 2).coerceAtLeast(lineStart)))
                } else {
                    // Toggle on
                    val cleanLine = line.removePrefix("## ").removePrefix("- [ ] ").removePrefix("• ")
                    val newLine = "# $cleanLine"
                    val newText = currentText.substring(0, lineStart) + newLine + currentText.substring(lineEnd)
                    Pair(newText, TextRange((start + 2).coerceAtMost(newText.length)))
                }
            }
            NoteFormatType.HEADING_2 -> {
                var lineStart = start
                while (lineStart > 0 && currentText[lineStart - 1] != '\n') lineStart--
                var lineEnd = start
                while (lineEnd < currentText.length && currentText[lineEnd] != '\n') lineEnd++
                val line = currentText.substring(lineStart, lineEnd)

                if (line.startsWith("## ")) {
                    val newLine = line.removePrefix("## ")
                    val newText = currentText.substring(0, lineStart) + newLine + currentText.substring(lineEnd)
                    Pair(newText, TextRange((start - 3).coerceAtLeast(lineStart)))
                } else {
                    val cleanLine = line.removePrefix("# ").removePrefix("- [ ] ").removePrefix("• ")
                    val newLine = "## $cleanLine"
                    val newText = currentText.substring(0, lineStart) + newLine + currentText.substring(lineEnd)
                    Pair(newText, TextRange((start + 3).coerceAtMost(newText.length)))
                }
            }
            NoteFormatType.BULLET_LIST -> {
                var lineStart = start
                while (lineStart > 0 && currentText[lineStart - 1] != '\n') lineStart--
                var lineEnd = start
                while (lineEnd < currentText.length && currentText[lineEnd] != '\n') lineEnd++
                val line = currentText.substring(lineStart, lineEnd)

                if (line.startsWith("• ")) {
                    val newLine = line.removePrefix("• ")
                    val newText = currentText.substring(0, lineStart) + newLine + currentText.substring(lineEnd)
                    Pair(newText, TextRange((start - 2).coerceAtLeast(lineStart)))
                } else {
                    val cleanLine = line.removePrefix("- [ ] ").removePrefix("- [x] ").removePrefix("# ")
                    val newLine = "• $cleanLine"
                    val newText = currentText.substring(0, lineStart) + newLine + currentText.substring(lineEnd)
                    Pair(newText, TextRange((start + 2).coerceAtMost(newText.length)))
                }
            }
            NoteFormatType.CHECKBOX -> {
                var lineStart = start
                while (lineStart > 0 && currentText[lineStart - 1] != '\n') lineStart--
                var lineEnd = start
                while (lineEnd < currentText.length && currentText[lineEnd] != '\n') lineEnd++
                val line = currentText.substring(lineStart, lineEnd)

                if (line.startsWith("- [ ] ")) {
                    val newLine = line.removePrefix("- [ ] ")
                    val newText = currentText.substring(0, lineStart) + newLine + currentText.substring(lineEnd)
                    Pair(newText, TextRange((start - 6).coerceAtLeast(lineStart)))
                } else if (line.startsWith("- [x] ")) {
                    val newLine = line.removePrefix("- [x] ")
                    val newText = currentText.substring(0, lineStart) + newLine + currentText.substring(lineEnd)
                    Pair(newText, TextRange((start - 6).coerceAtLeast(lineStart)))
                } else {
                    val cleanLine = line.removePrefix("• ").removePrefix("# ")
                    val newLine = "- [ ] $cleanLine"
                    val newText = currentText.substring(0, lineStart) + newLine + currentText.substring(lineEnd)
                    Pair(newText, TextRange((start + 6).coerceAtMost(newText.length)))
                }
            }
            NoteFormatType.QUOTE -> {
                var lineStart = start
                while (lineStart > 0 && currentText[lineStart - 1] != '\n') lineStart--
                var lineEnd = start
                while (lineEnd < currentText.length && currentText[lineEnd] != '\n') lineEnd++
                val line = currentText.substring(lineStart, lineEnd)

                if (line.startsWith("> ")) {
                    val newLine = line.removePrefix("> ")
                    val newText = currentText.substring(0, lineStart) + newLine + currentText.substring(lineEnd)
                    Pair(newText, TextRange((start - 2).coerceAtLeast(lineStart)))
                } else {
                    val newLine = "> $line"
                    val newText = currentText.substring(0, lineStart) + newLine + currentText.substring(lineEnd)
                    Pair(newText, TextRange((start + 2).coerceAtMost(newText.length)))
                }
            }
        }
    }

    fun getFontFamily(fontFamilyName: String?): FontFamily {
        return when (fontFamilyName?.uppercase()) {
            "SERIF" -> FontFamily.Serif
            "MONOSPACE" -> FontFamily.Monospace
            "CURSIVE" -> FontFamily.Cursive
            "SANS_SERIF", "DEFAULT" -> FontFamily.Default
            else -> FontFamily.Default
        }
    }
}

/**
 * VisualTransformation that renders rich text styling directly in the editor
 * without exposing the raw markdown delimiters (**bold**, *italic*, <u>underline</u>, etc.).
 *
 * This provides a clean, discrete, WYSIWYG editing experience.
 */
class RichTextVisualTransformation(
    private val primaryColor: Color,
    private val baseFontSize: Int = 16
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }

        val origLen = raw.length
        val origToTrans = IntArray(origLen + 1)
        val transToOrigList = ArrayList<Int>(origLen + 1)
        val builder = AnnotatedString.Builder()

        fun appendChar(origIdx: Int, c: Char) {
            val transIdx = builder.length
            origToTrans[origIdx] = transIdx
            transToOrigList.add(origIdx)
            builder.append(c)
        }

        fun skipChar(origIdx: Int) {
            origToTrans[origIdx] = builder.length
        }

        var lineStart = 0
        while (lineStart < origLen) {
            var lineEnd = raw.indexOf('\n', lineStart)
            val hasNewline = lineEnd != -1
            if (!hasNewline) {
                lineEnd = origLen
            }

            val line = raw.substring(lineStart, lineEnd)
            var currentIdxInRaw = lineStart

            var linePrefixStyle: SpanStyle? = null
            var prefixConsumed = 0

            when {
                line.startsWith("# ") -> {
                    prefixConsumed = 2
                    linePrefixStyle = SpanStyle(fontWeight = FontWeight.Bold, fontSize = (baseFontSize + 4).sp)
                }
                line.startsWith("## ") -> {
                    prefixConsumed = 3
                    linePrefixStyle = SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = (baseFontSize + 2).sp)
                }
                line.startsWith("- [ ] ") -> {
                    for (i in 0 until 6) skipChar(currentIdxInRaw + i)
                    val boxIdx = currentIdxInRaw
                    currentIdxInRaw += 6
                    appendChar(boxIdx, '☐')
                    appendChar(boxIdx, ' ')
                    prefixConsumed = -1
                }
                line.startsWith("[ ] ") -> {
                    for (i in 0 until 4) skipChar(currentIdxInRaw + i)
                    val boxIdx = currentIdxInRaw
                    currentIdxInRaw += 4
                    appendChar(boxIdx, '☐')
                    appendChar(boxIdx, ' ')
                    prefixConsumed = -1
                }
                line.startsWith("- [x] ") || line.startsWith("- [X] ") -> {
                    for (i in 0 until 6) skipChar(currentIdxInRaw + i)
                    val boxIdx = currentIdxInRaw
                    currentIdxInRaw += 6
                    appendChar(boxIdx, '☑')
                    appendChar(boxIdx, ' ')
                    linePrefixStyle = SpanStyle(textDecoration = TextDecoration.LineThrough)
                    prefixConsumed = -1
                }
                line.startsWith("[x] ") || line.startsWith("[X] ") -> {
                    for (i in 0 until 4) skipChar(currentIdxInRaw + i)
                    val boxIdx = currentIdxInRaw
                    currentIdxInRaw += 4
                    appendChar(boxIdx, '☑')
                    appendChar(boxIdx, ' ')
                    linePrefixStyle = SpanStyle(textDecoration = TextDecoration.LineThrough)
                    prefixConsumed = -1
                }
                line.startsWith("• ") -> {
                    appendChar(currentIdxInRaw, '•')
                    appendChar(currentIdxInRaw + 1, ' ')
                    currentIdxInRaw += 2
                    prefixConsumed = -1
                }
                line.startsWith("* ") -> {
                    skipChar(currentIdxInRaw)
                    skipChar(currentIdxInRaw + 1)
                    appendChar(currentIdxInRaw, '•')
                    appendChar(currentIdxInRaw, ' ')
                    currentIdxInRaw += 2
                    prefixConsumed = -1
                }
                line.startsWith("> ") -> {
                    prefixConsumed = 2
                    linePrefixStyle = SpanStyle(fontStyle = FontStyle.Italic, color = primaryColor)
                }
            }

            if (prefixConsumed > 0) {
                for (i in 0 until prefixConsumed) {
                    skipChar(currentIdxInRaw + i)
                }
                currentIdxInRaw += prefixConsumed
            }

            val lineContentStart = currentIdxInRaw
            val lineContent = raw.substring(lineContentStart, lineEnd)
            val lineTransStart = builder.length

            val inlineRegex = Regex("""(<u>(.*?)</u>|__(.*?)__|~~(.*?)~~|\*\*(.*?)\*\*|\*(.*?)\*|`(.*?)`)""")
            var lastIndex = 0

            inlineRegex.findAll(lineContent).forEach { match ->
                val matchRange = match.range
                val matchStartInRaw = lineContentStart + matchRange.first

                for (i in lastIndex until matchRange.first) {
                    appendChar(lineContentStart + i, lineContent[i])
                }

                val fullMatched = match.value
                when {
                    fullMatched.startsWith("<u>") && fullMatched.endsWith("</u>") && fullMatched.length >= 7 -> {
                        val inner = fullMatched.substring(3, fullMatched.length - 4)
                        for (i in 0 until 3) skipChar(matchStartInRaw + i)
                        val innerStart = builder.length
                        for (i in inner.indices) appendChar(matchStartInRaw + 3 + i, inner[i])
                        builder.addStyle(SpanStyle(textDecoration = TextDecoration.Underline), innerStart, builder.length)
                        for (i in 0 until 4) skipChar(matchStartInRaw + 3 + inner.length + i)
                    }
                    fullMatched.startsWith("__") && fullMatched.endsWith("__") && fullMatched.length >= 4 -> {
                        val inner = fullMatched.substring(2, fullMatched.length - 2)
                        for (i in 0 until 2) skipChar(matchStartInRaw + i)
                        val innerStart = builder.length
                        for (i in inner.indices) appendChar(matchStartInRaw + 2 + i, inner[i])
                        builder.addStyle(SpanStyle(textDecoration = TextDecoration.Underline), innerStart, builder.length)
                        for (i in 0 until 2) skipChar(matchStartInRaw + 2 + inner.length + i)
                    }
                    fullMatched.startsWith("~~") && fullMatched.endsWith("~~") && fullMatched.length >= 4 -> {
                        val inner = fullMatched.substring(2, fullMatched.length - 2)
                        for (i in 0 until 2) skipChar(matchStartInRaw + i)
                        val innerStart = builder.length
                        for (i in inner.indices) appendChar(matchStartInRaw + 2 + i, inner[i])
                        builder.addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), innerStart, builder.length)
                        for (i in 0 until 2) skipChar(matchStartInRaw + 2 + inner.length + i)
                    }
                    fullMatched.startsWith("**") && fullMatched.endsWith("**") && fullMatched.length >= 4 -> {
                        val inner = fullMatched.substring(2, fullMatched.length - 2)
                        for (i in 0 until 2) skipChar(matchStartInRaw + i)
                        val innerStart = builder.length
                        for (i in inner.indices) appendChar(matchStartInRaw + 2 + i, inner[i])
                        builder.addStyle(SpanStyle(fontWeight = FontWeight.Bold), innerStart, builder.length)
                        for (i in 0 until 2) skipChar(matchStartInRaw + 2 + inner.length + i)
                    }
                    fullMatched.startsWith("*") && fullMatched.endsWith("*") && fullMatched.length >= 2 -> {
                        val inner = fullMatched.substring(1, fullMatched.length - 1)
                        skipChar(matchStartInRaw)
                        val innerStart = builder.length
                        for (i in inner.indices) appendChar(matchStartInRaw + 1 + i, inner[i])
                        builder.addStyle(SpanStyle(fontStyle = FontStyle.Italic), innerStart, builder.length)
                        skipChar(matchStartInRaw + 1 + inner.length)
                    }
                    fullMatched.startsWith("`") && fullMatched.endsWith("`") && fullMatched.length >= 2 -> {
                        val inner = fullMatched.substring(1, fullMatched.length - 1)
                        skipChar(matchStartInRaw)
                        val innerStart = builder.length
                        for (i in inner.indices) appendChar(matchStartInRaw + 1 + i, inner[i])
                        builder.addStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                color = primaryColor,
                                background = primaryColor.copy(alpha = 0.12f)
                            ),
                            innerStart,
                            builder.length
                        )
                        skipChar(matchStartInRaw + 1 + inner.length)
                    }
                }
                lastIndex = matchRange.last + 1
            }

            for (i in lastIndex until lineContent.length) {
                appendChar(lineContentStart + i, lineContent[i])
            }

            if (linePrefixStyle != null && builder.length > lineTransStart) {
                builder.addStyle(linePrefixStyle, lineTransStart, builder.length)
            }

            if (hasNewline) {
                appendChar(lineEnd, '\n')
                lineStart = lineEnd + 1
            } else {
                lineStart = lineEnd
            }
        }

        origToTrans[origLen] = builder.length
        transToOrigList.add(origLen)

        val transToOrig = transToOrigList.toIntArray()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, origToTrans.size - 1)
                return origToTrans[clamped].coerceIn(0, builder.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, transToOrig.size - 1)
                return transToOrig[clamped].coerceIn(0, origLen)
            }
        }

        return TransformedText(builder.toAnnotatedString(), offsetMapping)
    }
}


