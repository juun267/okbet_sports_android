package org.cxct.sportlottery.ui.base


import org.cxct.sportlottery.service.BackService
import kotlin.reflect.KClass


/**
 * @author Kevin
 * @create 2021/7/13
 * @description
 */
abstract class BaseSocketBottomSheetFragment <T : BaseViewModel>(clazz: KClass<T>) : BaseBottomSheetFragment<T>(clazz){


    val receiver by lazy {
        (activity as BaseSocketActivity<*>).receiver
    }


    val service: BackService
        get() = (activity as BaseSocketActivity<*>).backService


}