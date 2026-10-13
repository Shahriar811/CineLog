package com.example.cinelog.profile

import java.security.SecureRandom

object UniqueIdGenerator {
    private const val UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val LOWER = "abcdefghijklmnopqrstuvwxyz"
    private const val DIGITS = "0123456789"
    private const val ALL_CHARS = UPPER + LOWER + DIGITS

    /**
     * Generates a unique 10-character ID containing English uppercase letters,
     * lowercase letters, and numbers only. No special characters.
     */
    fun generate(): String {
        val random = SecureRandom()
        while (true) {
            val chars = CharArray(10) { ALL_CHARS[random.nextInt(ALL_CHARS.length)] }
            val candidate = String(chars)
            val hasUpper = candidate.any { it in UPPER }
            val hasLower = candidate.any { it in LOWER }
            val hasDigit = candidate.any { it in DIGITS }
            if (hasUpper && hasLower && hasDigit) {
                return candidate
            }
        }
    }

    /**
     * Validates whether a candidate string is a valid 10-character alphanumeric ID.
     */
    fun isValid(id: String): Boolean {
        if (id.length != 10) return false
        return id.all { it in ALL_CHARS }
    }
}
