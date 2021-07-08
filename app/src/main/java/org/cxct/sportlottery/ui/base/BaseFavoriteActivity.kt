package org.cxct.sportlottery.ui.base

import android.os.Bundle
import kotlin.reflect.KClass

abstract class BaseFavoriteActivity<T : BaseFavoriteViewModel>(clazz: KClass<T>) :
    BaseNoticeActivity<T>(clazz) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.isLogin.observe(this, {
            when (it) {
                true -> {
                    viewModel.getFavorite()
                }
                false -> {
                    viewModel.clearFavorite()
                }
            }
        })
    }
}