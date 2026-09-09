package com.example.ui.components

import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64

object CryptoHelper {
    private const val SALT_BYTES = 16

    /**
     * Generates a random cryptographic salt.
     */
    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_BYTES)
        random.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    /**
     * Hashes a PIN with a salt using SHA-256.
     * Format returned: "$salt:$hash"
     */
    fun hashPin(pin: String, salt: String = generateSalt()): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt.toByteArray(Charsets.UTF_8))
        val hashBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        val hash = Base64.encodeToString(hashBytes, Base64.NO_WRAP)
        return "$salt:$hash"
    }

    /**
     * Verifies if entered PIN matches the stored hash (or legacy plain pin for migration).
     */
    fun verifyPin(enteredPin: String, storedValue: String?): Boolean {
        if (storedValue.isNullOrBlank()) return false

        // Check if stored value is in "$salt:$hash" format
        if (storedValue.contains(":")) {
            val parts = storedValue.split(":")
            if (parts.size == 2) {
                val salt = parts[0]
                val expectedHash = parts[1]
                val digest = MessageDigest.getInstance("SHA-256")
                digest.update(salt.toByteArray(Charsets.UTF_8))
                val hashBytes = digest.digest(enteredPin.toByteArray(Charsets.UTF_8))
                val actualHash = Base64.encodeToString(hashBytes, Base64.NO_WRAP)
                return actualHash == expectedHash
            }
        }

        // Backward compatibility: if it was stored as plain text, match directly
        return enteredPin == storedValue
    }
}
