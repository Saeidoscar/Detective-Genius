package kotlinx.coroutines.flow

interface Flow<out T>

interface StateFlow<out T> : Flow<T> {
    val value: T
}

class MutableStateFlow<T>(initialValue: T) : StateFlow<T> {
    private val lock = Any()
    private var _value: T = initialValue

    override var value: T
        get() = synchronized(lock) { _value }
        set(v) {
            synchronized(lock) {
                _value = v
            }
        }
}

fun <T> MutableStateFlow<T>.asStateFlow(): StateFlow<T> = this
