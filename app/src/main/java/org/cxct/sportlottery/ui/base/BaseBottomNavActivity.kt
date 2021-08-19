package org.cxct.sportlottery.ui.base

import android.content.Intent
import android.os.Bundle
import org.cxct.sportlottery.ui.menu.OddsType
import kotlin.reflect.KClass


abstract class BaseBottomNavActivity<T : BaseBottomNavViewModel>(clazz: KClass<T>) :
    BaseSocketActivity<T>(clazz) {

    abstract fun initToolBar()

    abstract fun initMenu()

    abstract fun initBottomNavigation()

    abstract fun showBetListPage()

    abstract fun updateUiWithLogin(isLogin: Boolean)

    abstract fun updateOddsType(oddsType: OddsType)

    abstract fun updateBetListCount(num: Int)

    abstract fun showLoginNotify()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.isLogin.observe(this, {
            updateUiWithLogin(it)
        })

        viewModel.oddsType.observe(this, {
            updateOddsType(it)
        })

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

        viewModel.betInfoList.observe(this, {
            updateBetListCount(it.peekContent().size)
        })

        viewModel.notifyLogin.observe(this, {
            showLoginNotify()
        })
    }
}