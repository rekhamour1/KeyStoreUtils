package me.rekha.keystoreUtils.ui.main

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.rekha.keystoreUtils.core.customePrefrence.customeKeyValueStorageManager.CustomKeyValueStore
import me.rekha.keystoreUtils.core.customePrefrence.encryptionhelpers.EncryptionHelper
import me.rekha.keystoreUtils.data.model.User
import javax.crypto.SecretKey

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var encryptionKey: SecretKey

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Initialize the KeyValueStoreManager

        // Store and retrieve primitive data
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val customStore = CustomKeyValueStore.Builder(this@MainActivity)
                    .setFileName("user_store.json")
                    .setPrivateMode(true)
                    .setEncryptionKey(EncryptionHelper.generateEncryptionKey())
                    .enableEncryption(true)
                    .build()

// Save a complex object
                val user = User(1, "John Doe")
                customStore.put("user", user)
                customStore.put("day","thursday")

// Retrieve a complex object
                val retrievedUser: User? = customStore.get("user", User::class.java)

                println("Retrieved User: $retrievedUser")
                Log.d("MainActivity", "Retrieved Username: ${retrievedUser?.name}")
                val day = customStore.get("day",String::class.java)
                println("Retrieved day: $day")


            } catch (e: Exception) {
                Log.e("MainActivity", "Error storing or retrieving username", e)
            }
        }


    }


}