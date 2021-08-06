package org.cxct.sportlottery.ui.base

import org.cxct.sportlottery.service.BackService
import kotlin.reflect.KClass

abstract class BaseSocketDialog<T : BaseViewModel>(clazz: KClass<T>) : BaseDialog<T>(clazz) {

    val receiver by lazy {
        (activity as BaseSocketActivity<*>).receiver
    }

    val service: BackService
        get() = (activity as BaseSocketActivity<*>).backService
}