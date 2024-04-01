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

    fun <T> make(clazz: Class<T>): T {
        var obj = objectMap[clazz]?.get()
        if (obj == null) {
            obj = clazz.newInstance()
            objectMap[clazz] = WeakReference(obj)
        }
        return obj as T
    }
}