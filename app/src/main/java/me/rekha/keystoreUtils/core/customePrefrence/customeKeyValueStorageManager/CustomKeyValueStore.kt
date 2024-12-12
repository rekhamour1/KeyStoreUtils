package me.rekha.keystoreUtils.core.customePrefrence.customeKeyValueStorageManager

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.rekha.keystoreUtils.core.customePrefrence.encryptionhelpers.EncryptionHandler
import me.rekha.keystoreUtils.core.customePrefrence.storageHandler.FileHandler
import java.io.File
import java.security.Key

class CustomKeyValueStore private constructor(
    private val fileHandler: FileHandler,
    private val encryptionHandler: EncryptionHandler?,
    private val gson: Gson = Gson(),
) {
    private val listeners = mutableSetOf<() -> Unit>()

    // --- Factory Pattern ---
    class Builder(private val context: Context) {
        private var fileName: String = "custom_store.json"
        private var isPrivate: Boolean = true
        private var encryptionKey: Key? = null
        private var enableEncryption: Boolean = false

        fun setFileName(name: String) = apply { this.fileName = name }
        fun setPrivateMode(isPrivate: Boolean) = apply { this.isPrivate = isPrivate }
        fun setEncryptionKey(key: Key) = apply { this.encryptionKey = key }
        fun enableEncryption(enable: Boolean) = apply { this.enableEncryption = enable }

        fun build(): CustomKeyValueStore {
            val file = if (isPrivate) {
                File(context.filesDir, fileName)
            } else {
                File(context.getExternalFilesDir(null), fileName)
            }
            val fileHandler = FileHandler(file)
            val encryptionHandler = if (enableEncryption && encryptionKey != null) {
                EncryptionHandler(encryptionKey!!)
            } else {
                null
            }
            return CustomKeyValueStore(fileHandler, encryptionHandler)
        }
    }

    init {
        fileHandler.initialize()
    }

    // Save a value
    suspend fun <T> put(key: String, value: T) {
        withContext(Dispatchers.IO) {
            val data = fileHandler.readData().toMutableMap()
            data[key] = gson.toJson(value)
            val json = gson.toJson(data)
            fileHandler.writeData(encrypt(json))
            notifyListeners()
        }
    }

    // Retrieve a value
    suspend fun <T> get(key: String, clazz: Class<T>, defaultValue: T? = null): T? {
        return withContext(Dispatchers.IO) {
            val data = fileHandler.readData()
            val jsonValue = data[key]
            if (jsonValue != null) {
                try {
                    gson.fromJson(decrypt(jsonValue), clazz)
                } catch (e: JsonSyntaxException) {
                    defaultValue
                }
            } else {
                defaultValue
            }
        }
    }

    // Remove a key
    suspend fun remove(key: String) {
        withContext(Dispatchers.IO) {
            val data = fileHandler.readData().toMutableMap()
            data.remove(key)
            val json = gson.toJson(data)
            fileHandler.writeData(encrypt(json))
            notifyListeners()
        }
    }

    // Clear all data
    suspend fun clear() {
        withContext(Dispatchers.IO) {
            fileHandler.writeData(encrypt("{}"))
            notifyListeners()
        }
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

    // Encrypt data
    private fun encrypt(data: String): String {
        return encryptionHandler?.encrypt(data) ?: data
    }

    // Decrypt data
    private fun decrypt(data: String): String {
        return encryptionHandler?.decrypt(data) ?: data
    }
}
/*(
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
*/