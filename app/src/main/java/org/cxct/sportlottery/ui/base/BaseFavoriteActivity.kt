package org.cxct.sportlottery.ui.base

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.snackbar_my_favorite_notify.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MyFavoriteNotifyType
import kotlin.reflect.KClass

abstract class BaseFavoriteActivity<T : BaseFavoriteViewModel>(clazz: KClass<T>) :
    BaseNoticeActivity<T>(clazz) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.isLogin.observe(this) {
            if (it == true) {
                viewModel.getFavorite()
            } else {
                viewModel.clearFavorite()
            }
        }
    }
}