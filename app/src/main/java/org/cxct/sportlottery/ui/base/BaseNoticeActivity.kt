package org.cxct.sportlottery.ui.base

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import org.cxct.sportlottery.ui.infoCenter.InfoCenterActivity
import kotlin.reflect.KClass

abstract class BaseNoticeActivity<T : BaseNoticeViewModel>(clazz: KClass<T>) :
    BaseOddButtonActivity<T>(clazz) {

    private var mNoticeButton: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initNoticeObserve()
    }

    //有 child activity 給定 notice button 顯示
    fun setupNoticeButton(noticeButton: TextView) {
        mNoticeButton = noticeButton
        mNoticeButton?.setOnClickListener {
            startActivity(
                Intent(this, InfoCenterActivity::class.java)
                    .putExtra(InfoCenterActivity.KEY_READ_PAGE, InfoCenterActivity.YET_READ)
            )
        }
    }

    private fun initNoticeObserve() {
        viewModel.infoCenterRepository.unreadNoticeList.observe(this, Observer {
            updateNoticeButton(it.size)
        })

        receiver.userNotice.observe(this, Observer {
            it?.userNoticeList?.let { list ->
                viewModel.setUserNoticeList(list)
            }
        })
    }

    private fun updateNoticeButton(noticeCount: Int) {
        mNoticeButton?.visibility = if (noticeCount > 0) View.VISIBLE else View.GONE
        mNoticeButton?.text = if (noticeCount < 10) noticeCount.toString() else "N"
    }

}