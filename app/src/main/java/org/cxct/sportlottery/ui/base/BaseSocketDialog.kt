package org.cxct.sportlottery.ui.base

import kotlin.reflect.KClass

abstract class BaseSocketDialog<T : BaseViewModel>(clazz: KClass<T>) : BaseDialog<T>(clazz) {

    val receiver by lazy {
        (activity as BaseSocketActivity<*>).receiver
    }

    val service by lazy {
        (activity as BaseSocketActivity<*>).backService
    }
}