package me.rekha.keystoreUtils.core.customePrefrence.storageHandler

import com.google.gson.Gson
import java.io.File

class FileHandler(private val file: File)  {
    fun initialize() {
        if (!file.exists()) {
            file.writeText("{}")
        }
    }

    fun readData(): Map<String, String> {
        return try {
            Gson().fromJson(file.readText(), Map::class.java) as Map<String, String>
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun writeData(data: String) {
        file.writeText(data)
    }
}
