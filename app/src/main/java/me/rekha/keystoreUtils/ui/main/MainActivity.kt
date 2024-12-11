package me.rekha.keystoreUtils.ui.main

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.rekha.keystoreUtils.core.KeyValueStoreManager
import me.rekha.keystoreUtils.data.model.User
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var keyValueStoreManager: KeyValueStoreManager
    private lateinit var encryptionKey: SecretKey

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Initialize the KeyValueStoreManager
        keyValueStoreManager = KeyValueStoreManager.getInstance(this ,"")

        // Store and retrieve primitive data
        CoroutineScope(Dispatchers.Main).launch {
            try {
                keyValueStoreManager.putString("username", "john_doe")
                val username = keyValueStoreManager.getString("username")
                Log.d("MainActivity", "Retrieved Username: $username")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error storing or retrieving username", e)
            }
        }

        // Store and retrieve complex data (User class)
        val user = User(1, "John Doe")
        CoroutineScope(Dispatchers.Main).launch {
            try {
                keyValueStoreManager.putComplexData("user", user, User::class.java)
                val retrievedUser: User? = keyValueStoreManager.getComplexData("user", User::class.java)
                Log.d("MainActivity", "Retrieved User: $retrievedUser")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error storing or retrieving user data", e)
            }
        }
    }


}