package devs.core

open class OneTimeEvent(val what: Int, private val content: Any) {

    var hasBeenHandled = false
        private set

    val data: Any?
        get() {
            if (hasBeenHandled) return null
            return content
        }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): Any = content
}
