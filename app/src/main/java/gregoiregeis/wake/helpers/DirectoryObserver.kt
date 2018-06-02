package gregoiregeis.wake.helpers

import android.os.FileObserver

abstract class DirectoryObserver(directory: String) : FileObserver(directory, MASK) {
    companion object {
        private const val MASK: Int = FileObserver.ALL_EVENTS
    }

    override fun onEvent(event: Int, path: String?) = when(event) {
        FileObserver.CREATE -> onCreated(path!!)
        FileObserver.DELETE -> onDeleted(path!!)
        FileObserver.MODIFY -> onModified(path!!)
        else -> {}
    }

    open fun onCreated(path: String) {}

    open fun onDeleted(path: String) {}

    open fun onModified(path: String) {}

    fun close() {
        finalize()
    }
}
