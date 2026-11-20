package com.nimelssa.vault.desktop.data

/**
 * Matric numbers follow the department convention, e.g. MLS/22/1234:
 * a 2-5 letter department code, a 2 digit entry year and a 3-4 digit serial.
 */
object MatricNo {
    private val pattern = Regex("^[A-Z]{2,5}/\\d{2}/\\d{3,4}$")

    fun normalize(raw: String): String = raw.trim().uppercase()

    fun isValid(raw: String): Boolean = pattern.matches(normalize(raw))
}
