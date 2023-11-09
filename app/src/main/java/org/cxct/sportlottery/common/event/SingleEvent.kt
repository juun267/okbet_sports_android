package org.cxct.sportlottery.common.event

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

@Deprecated("请使用SingleLiveEvent")
class SingleEvent<T> ( ){

    private val event: LiveData<T>
        get() = _event
    private val _event = MutableLiveData<T>()


    fun post(t:T){
        _event.postValue(t)
    }

    fun observe( owner: LifecycleOwner,block:(t:T)->Unit){
        event.observe(owner) {
            block(it)
        }
    }
}