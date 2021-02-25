package org.cxct.sportlottery.ui.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.layout_notice_float_button.*
import kotlinx.android.synthetic.main.layout_notice_float_button.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.infoCenter.InfoCenterActivity
import kotlin.reflect.KClass

abstract class BaseNoticeActivity<T : BaseNoticeViewModel>(clazz: KClass<T>) :
    BaseOddButtonActivity<T>(clazz) {

    private var floatButtonView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.infoCenterRepository.unreadNoticeList.observe(this, Observer {
            updateNoticeButton(it.isNotEmpty())
        })

        receiver.userNotice.observe(this, Observer {
            it?.userNoticeList?.let { list ->
                viewModel.setUserNoticeList(list)
            }
        })
    }

    private fun updateNoticeButton(isVisible: Boolean) {
        if (floatButtonView == null) return

        if (isVisible) {
            notice_float_button.visibility = View.VISIBLE
        } else {
            notice_float_button.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()

        setupNoticeButton()
    }

    private fun setupNoticeButton() {
        if (floatButtonView != null) return

        val contentView: ViewGroup = window.decorView.findViewById(android.R.id.content)

        floatButtonView = LayoutInflater.from(this)
            .inflate(R.layout.layout_notice_float_button, contentView, false).apply {
                this.notice_float_button.visibility = View.GONE
                this.notice_float_button.setOnClickListener {
                    startActivity(Intent(this@BaseNoticeActivity, InfoCenterActivity::class.java))
                }
            }

        contentView.addView(floatButtonView)
    }
}