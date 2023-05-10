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

    protected var snackBarMyFavoriteNotify:Snackbar? = null

    protected fun setSnackBarMyFavoriteNotify(myFavoriteNotifyType:Int){
        val title = when(myFavoriteNotifyType){
            MyFavoriteNotifyType.LEAGUE_ADD.code-> getString(R.string.myfavorite_notify_league_add)

            MyFavoriteNotifyType.LEAGUE_REMOVE.code-> getString(R.string.myfavorite_notify_league_remove)

            MyFavoriteNotifyType.MATCH_ADD.code-> getString(R.string.myfavorite_notify_match_add)

            MyFavoriteNotifyType.MATCH_REMOVE.code-> getString(R.string.myfavorite_notify_match_remove)

            MyFavoriteNotifyType.DETAIL_ADD.code -> getString(R.string.Pinned)

            MyFavoriteNotifyType.DETAIL_REMOVE.code -> getString(R.string.Unpin)

            else -> ""
        }

        snackBarMyFavoriteNotify = Snackbar.make(
            this.findViewById(android.R.id.content),
            title,
            Snackbar.LENGTH_LONG
        ).apply {
            val snackView: View = layoutInflater.inflate(
                R.layout.snackbar_my_favorite_notify,
                findViewById(android.R.id.content),
                false
            )
            snackView.txv_title.text = title
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
        viewModel.isLogin.observe(this) {
            if (it == true) {
                viewModel.getFavorite()
            } else {
                viewModel.clearFavorite()
            }
        }
    }
}