package org.cxct.sportlottery.ui.base

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import org.cxct.sportlottery.R
import kotlin.reflect.KClass

abstract class BaseFavoriteActivity<T : BaseFavoriteViewModel>(clazz: KClass<T>) :
    BaseNoticeActivity<T>(clazz) {

    protected val snackBarLoginNotify by lazy {
        Snackbar.make(
            findViewById(android.R.id.content),
            getString(R.string.login_notify),
            Snackbar.LENGTH_LONG
        ).apply {
            val snackView: View = layoutInflater.inflate(
                R.layout.snackbar_login_notify,
                findViewById(android.R.id.content),
                false
            )
            (this.view as Snackbar.SnackbarLayout).apply {
                findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
                    visibility = View.INVISIBLE
                }
                background.alpha = 0
                addView(snackView, 0)
                setPadding(0, 0, 0, 0)
            }
        }
    }

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