package me.rekha.keystoreUtils.core.customePrefrence.storageHandler

interface KeyValueStorage {
    fun <T> save(key: String, value: T)
    fun <T> get(key: String, clazz: Class<T>): T?
    fun containsKey(key: String): Boolean
    fun delete(key: String)
}