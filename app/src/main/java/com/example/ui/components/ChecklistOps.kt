package com.example.ui.components

/**
 * Operações sobre as linhas de lista das notas ("[ ] item" / "[x] item", também com "- " na frente).
 * Funções puras, usadas pelo editor e testadas em ChecklistOpsTest.
 */
object ChecklistOps {

    private val itemRegex = Regex("""^(\s*)(- )?\[( |x|X)] (.*)$""")
    private val bulletRegex = Regex("""^(\s*)([-*•]\s+)(.*)$""")

    data class Item(val indent: String, val dash: String, val checked: Boolean, val text: String)

    fun parse(line: String): Item? = itemRegex.matchEntire(line)?.let { m ->
        Item(
            indent = m.groupValues[1],
            dash = m.groupValues[2],
            checked = m.groupValues[3].equals("x", ignoreCase = true),
            text = m.groupValues[4]
        )
    }

    private fun format(item: Item): String =
        "${item.indent}${item.dash}[${if (item.checked) "x" else " "}] ${item.text}"

    /** Marca ou desmarca a linha, se for um item; senão devolve a linha como está. */
    fun toggleLine(line: String): String = parse(line)?.let { format(it.copy(checked = !it.checked)) } ?: line

    fun hasItems(content: String): Boolean = content.lines().any { parse(it) != null }

    fun hasCheckedItems(content: String): Boolean = content.lines().any { parse(it)?.checked == true }

    /** Linhas não vazias viram itens; marcadores "- " e "• " são removidos. */
    fun toChecklist(content: String): String = content.lines().joinToString("\n") { line ->
        when {
            line.isBlank() -> line
            parse(line) != null -> line
            else -> {
                val bullet = bulletRegex.matchEntire(line)
                if (bullet != null) "${bullet.groupValues[1]}[ ] ${bullet.groupValues[3]}" else "[ ] ${line.trimStart()}"
            }
        }
    }

    /** Remove as caixas de seleção e mantém o texto de cada item. */
    fun toPlainText(content: String): String = content.lines().joinToString("\n") { line ->
        parse(line)?.let { "${it.indent}${it.text}" } ?: line
    }

    fun setAllChecked(content: String, checked: Boolean): String = content.lines().joinToString("\n") { line ->
        parse(line)?.let { format(it.copy(checked = checked)) } ?: line
    }

    fun removeChecked(content: String): String =
        content.lines().filterNot { parse(it)?.checked == true }.joinToString("\n")

    /**
     * Em cada bloco contínuo de itens, os marcados descem para o fim do bloco,
     * mantendo a ordem relativa (como no Google Keep).
     */
    fun moveCheckedToBottom(content: String): String {
        val lines = content.lines()
        val result = mutableListOf<String>()
        var i = 0
        while (i < lines.size) {
            if (parse(lines[i]) == null) {
                result += lines[i]
                i++
                continue
            }
            val block = mutableListOf<String>()
            while (i < lines.size && parse(lines[i]) != null) {
                block += lines[i]
                i++
            }
            result += block.filter { parse(it)?.checked == false }
            result += block.filter { parse(it)?.checked == true }
        }
        return result.joinToString("\n")
    }

    /**
     * Continua a lista ao apertar Enter: se a nova quebra de linha veio depois de um item,
     * a linha nova já começa com "[ ] ". Enter num item vazio remove o marcador.
     * Retorna null quando não há nada a fazer.
     */
    fun continueListOnNewline(oldText: String, newText: String, cursor: Int): Pair<String, Int>? {
        if (newText.length != oldText.length + 1) return null
        if (cursor <= 0 || cursor > newText.length || newText[cursor - 1] != '\n') return null
        if (oldText != newText.removeRange(cursor - 1, cursor)) return null

        val lineStart = newText.lastIndexOf('\n', cursor - 2) + 1
        val previousLine = newText.substring(lineStart, cursor - 1)
        val item = parse(previousLine) ?: return null

        return if (item.text.isBlank()) {
            // Item vazio + Enter: sai da lista, apagando o marcador e a quebra
            val replaced = newText.removeRange(lineStart, cursor)
            replaced to lineStart
        } else {
            val prefix = "${item.indent}${item.dash}[ ] "
            val inserted = newText.substring(0, cursor) + prefix + newText.substring(cursor)
            inserted to cursor + prefix.length
        }
    }
}
