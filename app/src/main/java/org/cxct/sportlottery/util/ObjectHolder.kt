package org.cxct.sportlottery.util

import java.lang.ref.WeakReference

class ObjectHolder {

    private val objectMap = mutableMapOf<Class<*>, WeakReference<*>>()

    fun clear(clazz: Class<*>) {
        objectMap.remove(clazz)
    }

    fun <T> take(clazz: Class<T>): T? {
        return objectMap[clazz]?.get() as T?
    }

    fun put(obj: Any) {
        objectMap[obj.javaClass] = WeakReference(obj)
    }
}