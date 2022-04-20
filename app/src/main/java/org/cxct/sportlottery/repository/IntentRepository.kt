package org.cxct.sportlottery.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.util.Event

class IntentRepository {
    val intentClass: LiveData<Event<Class<*>>>
        get() = _intentClass
    private val _intentClass = MutableLiveData<Event<Class<*>>>()

    fun setIntentClassLiveData(clazz: Class<*>) {
        _intentClass.postValue(Event(clazz))
    }
}