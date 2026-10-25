package com.example

import com.example.ui.components.ChecklistOps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChecklistOpsTest {

    @Test
    fun `converte texto em lista e de volta`() {
        val text = "Arroz\n- Feijão\n\n• Café"
        val list = ChecklistOps.toChecklist(text)
        assertEquals("[ ] Arroz\n[ ] Feijão\n\n[ ] Café", list)
        assertEquals("Arroz\nFeijão\n\nCafé", ChecklistOps.toPlainText(list))
    }

    @Test
    fun `marca e desmarca todos sem mexer no texto comum`() {
        val content = "Compras\n[ ] Arroz\n- [x] Feijão"
        assertEquals("Compras\n[x] Arroz\n- [x] Feijão", ChecklistOps.setAllChecked(content, true))
        assertEquals("Compras\n[ ] Arroz\n- [ ] Feijão", ChecklistOps.setAllChecked(content, false))
    }

    @Test
    fun `marcados descem para o fim de cada bloco`() {
        val content = "[x] A\n[ ] B\n[x] C\n[ ] D\nTexto\n[x] E\n[ ] F"
        assertEquals(
            "[ ] B\n[ ] D\n[x] A\n[x] C\nTexto\n[ ] F\n[x] E",
            ChecklistOps.moveCheckedToBottom(content)
        )
    }

    @Test
    fun `marcar e desmarcar uma linha preserva o tracinho`() {
        assertEquals("- [ ] Feijão", ChecklistOps.toggleLine("- [x] Feijão"))
        assertEquals("[x] Arroz", ChecklistOps.toggleLine("[ ] Arroz"))
        assertEquals("Texto", ChecklistOps.toggleLine("Texto"))
    }

    @Test
    fun `remove apenas os marcados`() {
        assertEquals("Título\n[ ] B", ChecklistOps.removeChecked("Título\n[x] A\n[ ] B\n[X] C"))
        assertTrue(ChecklistOps.hasCheckedItems("[X] C"))
        assertFalse(ChecklistOps.hasCheckedItems("[ ] C"))
    }
}
