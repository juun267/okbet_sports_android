package org.cxct.sportlottery.view.boundsEditText

import android.view.View
import java.util.ArrayList

internal class CompositeListener : View.OnFocusChangeListener {
    private val registeredListeners: MutableList<View.OnFocusChangeListener> = ArrayList()
    fun registerListener(listener: View.OnFocusChangeListener) {
        registeredListeners.add(listener)
    }

    fun clearListeners() {
        registeredListeners.clear()
    }

    override fun onFocusChange(view: View, b: Boolean) {
        for (listener in registeredListeners) {
            listener.onFocusChange(view, b)
        }
    }
}