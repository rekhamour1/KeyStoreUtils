package me.rekha.keystoreUtils.core
import android.content.Context
import android.util.Log
import javax.crypto.SecretKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import javax.crypto.KeyGenerator

class KeyValueStoreManager private constructor(
    private val context: Context,
    filename: String,
    isPrivate:Boolean
) {

    private val TAG = "KeyValueStoreManager"

    // Singleton instance of the manager
    companion object {
        @Volatile
        private  var instance: KeyValueStoreManager? = null
        val encryptionKey=generateSecretkey()

        fun getInstance(context: Context,filename:String): KeyValueStoreManager {
            return instance ?: synchronized(this) {
                instance ?: KeyValueStoreManager(context,filename, isPrivate = false).also { instance = it }
            }
        }

        private fun generateSecretkey(): SecretKey? {
            // Generate or retrieve your encryption key
           return   KeyGenerator.getInstance("AES").apply {
                init(256) // 256-bit AES key
            }.generateKey()

        }
    }

    // Instance of the custom key-value store
    private val customKeyValueStore: CustomKeyValueStore by lazy {
        CustomKeyValueStore.getInstance(context,filename,isPrivate, encryptionKey)
    }

    // Put primitive data to the store
    suspend fun putString(key: String, value: String) {
        try {
            withContext(Dispatchers.IO) {
                customKeyValueStore.put(key, value)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error storing String value for key: $key", e)
        }
    }

    suspend fun putInt(key: String, value: Int) {
        try {
            withContext(Dispatchers.IO) {
                customKeyValueStore.put(key, value)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error storing Int value for key: $key", e)
        }
    }

    suspend fun putBoolean(key: String, value: Boolean) {
        try {
            withContext(Dispatchers.IO) {
                customKeyValueStore.put(key, value)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error storing Boolean value for key: $key", e)
        }
    }

    // Put complex object to the store
    suspend fun <T> putComplexData(key: String, value: T, serializer: Class<T>) {
        try {
            withContext(Dispatchers.IO) {
                customKeyValueStore.put(key, value, serializer)
            }
        } catch (e: SerializationException) {
            Log.e(TAG, "Error serializing complex object for key: $key", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error storing complex data for key: $key", e)
        }
    }

    // Get primitive data from the store
    suspend fun getString(key: String, defaultValue: String? = null): String? {
        return try {
            withContext(Dispatchers.IO) {
                customKeyValueStore.get(key, defaultValue = defaultValue)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving String value for key: $key", e)
            null
        }
    }

    suspend fun getInt(key: String, defaultValue: Int = 0): Int? {
        return try {
            withContext(Dispatchers.IO) {
                customKeyValueStore.get(key, defaultValue = defaultValue)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving Int value for key: $key", e)
            null
        }
    }

    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean? {
        return try {
            withContext(Dispatchers.IO) {
                customKeyValueStore.get(key, defaultValue = defaultValue)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving Boolean value for key: $key", e)
            null
        }
    }

    // Get complex object from the store
    suspend fun <T> getComplexData(key: String, value: Class<T>): T? {
        return try {
            withContext(Dispatchers.IO) {
                customKeyValueStore.get(key, type = value )
            }
        } catch (e: SerializationException) {
            Log.e(TAG, "Error deserializing complex object for key: $key", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error retrieving complex data for key: $key", e)
            null
        }
    }

    // Remove a key from the store
    suspend fun removeKey(key: String) {
        try {
            withContext(Dispatchers.IO) {
                customKeyValueStore.remove(key)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing key: $key", e)
        }
    }

    // Clear all stored data
    suspend fun clearAllData() {
        try {
            withContext(Dispatchers.IO) {
                customKeyValueStore.clear()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing all data", e)
        }
    }

    // Check if a key exists in the store
    suspend fun containsKey(key: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                customKeyValueStore.containsKey(key)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking existence of key: $key", e)
            false
        }
    }

    // To manage encryption key updates, you could implement a method to rotate keys, etc.
    fun updateEncryptionKey(newKey: SecretKey) {
        try {
            // Code to securely update the encryption key
            // This could involve re-encrypting existing data with the new key
        } catch (e: Exception) {
            Log.e(TAG, "Error updating encryption key", e)
        }
    }
}
