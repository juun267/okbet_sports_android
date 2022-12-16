package org.cxct.sportlottery.util

/**
 *  fragment重新創建若發生直接觸發observe onChange可以使用該類包裝
 *  ex :
 *  result = MutableLiveData<Event<T>>()
 *
 *
 */
open class Event<out T>(private val content: T, val tag: Any? = null) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}
