package me.rekha.keystoreUtils.core.customePrefrence.encryptionhelpers

import java.security.Key
import javax.crypto.Cipher

class EncryptionHandler(private val key: Key) {
    fun encrypt(data: String): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(data.toByteArray()).toString(Charsets.UTF_8)
    }

    fun decrypt(data: String): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        return cipher.doFinal(data.toByteArray(Charsets.UTF_8)).toString(Charsets.UTF_8)
    }
}