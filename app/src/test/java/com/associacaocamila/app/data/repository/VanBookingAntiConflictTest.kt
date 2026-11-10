package com.associacaocamila.app.data.repository

import com.associacaocamila.app.data.repository.VanBookingRepositoryImpl.Companion.parseMinutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VanBookingAntiConflictTest {

    @Test
    fun parseMinutes_convertsCorrectly() {
        assertEquals(0, parseMinutes("00:00"))
        assertEquals(480, parseMinutes("08:00"))
        assertEquals(510, parseMinutes("08:30"))
        assertEquals(630, parseMinutes("10:30"))
        assertEquals(1439, parseMinutes("23:59"))
    }

    private fun hasOverlap(start1: String, end1: String, start2: String, end2: String): Boolean {
        val s1 = parseMinutes(start1)
        val e1 = parseMinutes(end1)
        val s2 = parseMinutes(start2)
        val e2 = parseMinutes(end2)
        // Regra anti-conflito: (novoInicio < existenteFim && novoFim > existenteInicio)
        return s1 < e2 && e1 > s2
    }

    @Test
    fun overlapDetected_whenPartiallyOverlapping() {
        // Existente: 08:00 - 10:00 | Novo: 09:00 - 11:00
        assertTrue(hasOverlap("09:00", "11:00", "08:00", "10:00"))
        // Existente: 08:00 - 10:00 | Novo: 07:00 - 09:00
        assertTrue(hasOverlap("07:00", "09:00", "08:00", "10:00"))
    }

    @Test
    fun overlapDetected_whenCompletelyEnvelopingOrInside() {
        // Novo dentro do existente
        assertTrue(hasOverlap("08:30", "09:30", "08:00", "10:00"))
        // Novo envelopando o existente
        assertTrue(hasOverlap("07:00", "11:00", "08:00", "10:00"))
    }

    @Test
    fun noOverlap_whenAdjacentOrCompletelyDisjoint() {
        // Novo termina exatamente quando o existente começa
        assertFalse(hasOverlap("06:00", "08:00", "08:00", "10:00"))
        // Novo começa exatamente quando o existente termina
        assertFalse(hasOverlap("10:00", "12:00", "08:00", "10:00"))
        // Totalmente antes
        assertFalse(hasOverlap("05:00", "07:00", "08:00", "10:00"))
        // Totalmente depois
        assertFalse(hasOverlap("14:00", "16:00", "08:00", "10:00"))
    }
}
