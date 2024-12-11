package me.rekha.keystoreUtils.core

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.File
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

class CustomKeyValueStore private constructor(
    private val context: Context,
    fileName: String = "custom_store.json",
    private val isPrivate: Boolean = true, // Private or Public mode
    private val encryptionKey: Key? = null // Optional encryption key
) {

    private val file: File = if (isPrivate) {
        File(context.filesDir, fileName) // Private mode
    } else {
        File(context.getExternalFilesDir(null), fileName) // Public mode
    }
    private val lock = Any()
    private val listeners = mutableSetOf<() -> Unit>()
     val gson = Gson()

    init {

        synchronized(lock) {
            if (!file.exists()) {
                file.writeText("{}")
            }
        }
    }

    // Singleton instance
    companion object {
        @Volatile
        private var INSTANCE: CustomKeyValueStore? = null

        fun getInstance(context: Context, fileName: String = "custom_store.json", isPrivate: Boolean = true, encryptionKey: Key? = null): CustomKeyValueStore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CustomKeyValueStore(context, fileName, isPrivate, encryptionKey).also {
                    INSTANCE = it
                }
            }
        }

        // Generate an AES key for encryption if not provided
        fun generateEncryptionKey(): Key {
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(256)
            return keyGenerator.generateKey()
        }
    }

    // Get all data
    public suspend fun getAllData(): MutableMap<String, Any> {
        return withContext(Dispatchers.IO) {
            synchronized(lock) {
                try {
                    val content = decrypt(file.readBytes())
                    Json.decodeFromString(serializer<MutableMap<String, Any>>(), content)
                } catch (e: Exception) {
                    mutableMapOf()
                }
            }
        }
    }

    // Save data
    public suspend fun saveData(data: MutableMap<String, Any>) {
        withContext(Dispatchers.IO) {
            synchronized(lock) {
                val json = Json.encodeToString(serializer(), data)
                file.writeBytes(encrypt(json))
                notifyListeners()
            }
        }
    }

    // Encrypt data
    private fun encrypt(data: String): ByteArray {
        return encryptionKey?.let {
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, it)
            cipher.doFinal(data.toByteArray())
        } ?: data.toByteArray()
    }

    // Decrypt data
    private fun decrypt(data: ByteArray): String {
        return encryptionKey?.let {
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.DECRYPT_MODE, it)
            cipher.doFinal(data).decodeToString()
        } ?: data.decodeToString()
    }

    // Save a primitive or complex value
    suspend  fun < T> put(key: String, value: T, serializerClass: Class<T>?=null ) {
        val data = getAllData()
        data[key] = if (serializerClass != null) {
            val serializedValue = gson.toJson(value)
            serializedValue
        } else {
            value.toString() // Store primitive types as strings
        }
        saveData(data)
    }

    // Retrieve a primitive or complex value
    suspend fun <T> get(key: String, type: Class<T>? = null, defaultValue: T? = null): T? {
        val data = getAllData()
        val jsonValue = data[key]
        return if (type != null && jsonValue is String) {
            try {
                gson.fromJson(jsonValue, type)

            } catch (e: Exception) {
                defaultValue
            }
        } else {
            try {
                @Suppress("UNCHECKED_CAST")
                jsonValue as T?
            } catch (e: Exception) {
                defaultValue
            }
        }
    }

    // Remove a key
    suspend fun remove(key: String) {
        val data = getAllData()
        data.remove(key)
        saveData(data)
    }

    // Clear all data
    suspend fun clear() {
        saveData(mutableMapOf())
    }

    // Add a listener
    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    // Remove a listener
    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    // Notify listeners
    private fun notifyListeners() {
        listeners.forEach { it.invoke() }
    }

    // Check if the key exists in the store
    fun containsKey(key: String): Boolean {
        val dataMap = loadDataMap()
        return dataMap.containsKey(key)
    }

    private fun loadDataMap(): Map<String, String> {
        return try {
            val file = file
            if (file.exists()) {
                val content = file.readText()
                Json.decodeFromString(content)
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
