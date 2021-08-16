package org.cxct.sportlottery.ui.base

import android.content.Intent
import android.os.Bundle
import kotlin.reflect.KClass


abstract class BaseBottomNavActivity<T : BaseBottomNavViewModel>(clazz: KClass<T>) :
    BaseSocketActivity<T>(clazz) {

    abstract fun initBottomNavigation()

    abstract fun showBetListPage()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.intentClass.observe(this, {
            it.getContentIfNotHandled()?.let { clazz ->
                startActivity(Intent(this, clazz))
            }
        })

        viewModel.showShoppingCart.observe(this, {
            if (it) {
                showBetListPage()
            }
        })
    }
}