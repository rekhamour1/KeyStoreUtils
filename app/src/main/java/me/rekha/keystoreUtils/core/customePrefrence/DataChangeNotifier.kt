package me.rekha.keystoreUtils.core.customePrefrence

class DataChangeNotifier {
    private val listeners = mutableListOf<DataChangeListener>()

    fun addListener(listener: DataChangeListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: DataChangeListener) {
        listeners.remove(listener)
    }

    fun notifyChange(key: String) {
        listeners.forEach { it.onDataChanged(key) }
    }
}
