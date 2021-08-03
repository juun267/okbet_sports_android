package org.cxct.sportlottery.ui.base

import android.os.Bundle
import android.view.View
import org.cxct.sportlottery.service.BackService
import kotlin.reflect.KClass

abstract class BaseSocketFragment<T : BaseSocketViewModel>(clazz: KClass<T>) :
    BaseFragment<T>(clazz) {


    val receiver by lazy {
        (activity as BaseSocketActivity<*>).receiver
    }

    val service: BackService
        get() = (activity as BaseSocketActivity<*>).backService
}