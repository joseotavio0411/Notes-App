package com.example.ui

/** Tipo de nota escolhido no botão "+" (estilo Keep), nos atalhos e nos widgets. */
enum class NoteKind { TEXT, LIST, IMAGE, AUDIO }

/** Ordem da lista de notas. As fixadas ficam sempre no topo. */
enum class NoteSortOrder(val label: String) {
    UPDATED("Data de edição"),
    CREATED("Data de criação"),
    TITLE("Título (A–Z)"),
    COLOR("Cor")
}

/**
 * O que fazer quando o app é aberto por um atalho, widget, notificação ou
 * compartilhamento de outro app.
 */
sealed interface LaunchAction {
    data class NewNote(
        val kind: NoteKind,
        val title: String = "",
        val content: String = "",
        val imageUri: String? = null
    ) : LaunchAction

    data object NewTask : LaunchAction

    data class OpenNote(val noteId: Long) : LaunchAction
}
