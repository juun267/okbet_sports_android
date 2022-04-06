package org.cxct.sportlottery.ui.base

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.snackbar_login_notify.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.infoCenter.InfoCenterActivity
import kotlin.reflect.KClass

abstract class BaseNoticeActivity<T : BaseNoticeViewModel>(clazz: KClass<T>) :
    BaseActivity<T>(clazz) {

    private var mNoticeButton: ImageView? = null
    private var noticeCount: Int? = null
    private var isGuest: Boolean? = null

    open fun onCloseMenu() {}

    protected val snackBarBetUpperLimitNotify by lazy {
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
            snackView.tv_notify.text = getString(R.string.bet_notify_max_limit)
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

        initNoticeObserve()
    }

    //有 child activity 給定 notice button 顯示
    fun setupNoticeButton(noticeButton: ImageView) {
        mNoticeButton = noticeButton
        mNoticeButton?.setOnClickListener {
            onCloseMenu()
            startActivity(
                Intent(this, InfoCenterActivity::class.java)
                    .putExtra(InfoCenterActivity.KEY_READ_PAGE, InfoCenterActivity.YET_READ)
            )
        }
    }

    private fun initNoticeObserve() {
        viewModel.infoCenterRepository.unreadNoticeList.observe(this, Observer {
            updateNoticeCount(it.size)
        })

        viewModel.userInfo.observe(this) {
            //是否测试用户（0-正常用户，1-游客，2-内部测试）
            updateUserIdentity(it?.testFlag)
        }
    }

    private fun updateNoticeCount(noticeCount: Int) {
        this.noticeCount = noticeCount
        updateNoticeButton()
    }

    private fun updateUserIdentity(isGuest: Long?) {
        this.isGuest = when (isGuest) {
            0.toLong() -> false
            1.toLong() -> true
            else -> null
        }
        updateNoticeButton()
    }

    private fun updateNoticeButton() {
        mNoticeButton?.setImageResource(if (noticeCount ?: 0 > 0 && isGuest == false) R.drawable.icon_bell_with_red_dot else R.drawable.icon_bell)
    }

}