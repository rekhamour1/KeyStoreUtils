package me.rekha.keystoreUtils.core
import android.content.Context
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.serializer
import me.rekha.keystoreUtils.core.customePrefrence.customeKeyValueStorageManager.CustomKeyValueStore
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.security.Key
import javax.crypto.KeyGenerator

@ExperimentalCoroutinesApi
class CustomKeyValueStoreTest {

    @Mock
    lateinit var mockContext: Context

    private lateinit var customKeyValueStore: CustomKeyValueStore
    private lateinit var encryptionKey: Key

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Generate a mock encryption key
        encryptionKey = KeyGenerator.getInstance("AES").apply {
            init(256)
        }.generateKey()

        // Create instance of CustomKeyValueStore using mock context and encryption
        customKeyValueStore = CustomKeyValueStore.getInstance(mockContext, encryptionKey = encryptionKey)
    }

    @Test
    fun `test singleton instance`() {
        // Retrieve the instance twice
        val store1 = CustomKeyValueStore.getInstance(mockContext, encryptionKey = encryptionKey)
        val store2 = CustomKeyValueStore.getInstance(mockContext, encryptionKey = encryptionKey)

        // Assert that both instances are the same (Singleton behavior)
        assertEquals(store1, store2)
    }

    @Test
    fun `test put and get primitive data`() = runTest {
        // Put primitive data
        customKeyValueStore.put("username", "testUser")
        customKeyValueStore.put("isLoggedIn", true)
        customKeyValueStore.put("score", 100)

        // Get and assert primitive data
        val username: String? = customKeyValueStore.get("username", defaultValue = "defaultUser")
        val isLoggedIn: Boolean? = customKeyValueStore.get("isLoggedIn", defaultValue = false)
        val score: Int? = customKeyValueStore.get("score", defaultValue = 0)

        assertEquals("testUser", username)
        assertEquals(true, isLoggedIn)
        assertEquals(100, score)
    }

    @Test
    fun `test put and get complex data`() = runTest {
        // Complex object: User
        data class User(val id: Int, val name: String)

        val user = User(1, "John Doe")

        // Put complex data
        customKeyValueStore.put("user", user, serializer = User.serializer())

        // Get and assert complex data
        val retrievedUser: User? = customKeyValueStore.get("user", serializer = User.serializer())

        assertEquals(user, retrievedUser)
    }

    @Test
    fun `test encryption and decryption`() = runTest {
        // Put encrypted data
        customKeyValueStore.put("secretMessage", "SensitiveData", serializer = String.serializer())

        // Get encrypted data
        val retrievedMessage: String? = customKeyValueStore.get("secretMessage", serializer = String.serializer())

        assertEquals("SensitiveData", retrievedMessage)
    }

    @Test
    fun `test remove key`() = runTest {
        // Put some data
        customKeyValueStore.put("tempKey", "tempValue")

        // Remove the data
        customKeyValueStore.remove("tempKey")

        // Try to get the removed data
        val value: String? = customKeyValueStore.get("tempKey", defaultValue = "default")

        // Assert that the value is removed and defaults to the fallback
        assertEquals("default", value)
    }

    @Test
    fun `test clear all data`() = runTest {
        // Put some data
        customKeyValueStore.put("key1", "value1")
        customKeyValueStore.put("key2", "value2")

        // Clear all data
        customKeyValueStore.clear()

        // Try to get the cleared data
        val value1: String? = customKeyValueStore.get("key1", defaultValue = "default")
        val value2: String? = customKeyValueStore.get("key2", defaultValue = "default")

        // Assert that all data is cleared
        assertEquals("default", value1)
        assertEquals("default", value2)
    }
}
