package me.rekha.keystoreUtils.core.customePrefrence

import java.security.Key
import javax.crypto.KeyGenerator

object EncryptionHelper {
    fun generateEncryptionKey(): Key {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        return keyGenerator.generateKey()
    }
}