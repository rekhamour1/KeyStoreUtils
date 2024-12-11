package me.rekha.keystoreUtils.core.customePrefrence

import android.content.Context
import java.io.File

sealed class StorageMode {
    abstract fun getFile(context: Context, fileName: String): File

    object Private : StorageMode() {
        override fun getFile(context: Context, fileName: String): File {
            return File(context.filesDir, fileName)
        }
    }

    object Public : StorageMode() {
        override fun getFile(context: Context, fileName: String): File {
            return File(context.getExternalFilesDir(null), fileName)
        }
    }
}