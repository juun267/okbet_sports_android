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
    private var noticeCount: Int? = null
    private var isGuest: Boolean? = null

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
            updateNoticeCount(it.size)
        })

        viewModel.userInfo.observe(this, {
            //是否测试用户（0-正常用户，1-游客，2-内部测试）
            updateUserIdentity(it?.testFlag)
        })

        receiver.userNotice.observe(this, Observer {
            it?.userNoticeList?.let { list ->
                viewModel.setUserNoticeList(list)
            }
        })
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

        mNoticeButton?.visibility = if (noticeCount ?: 0 > 0 && isGuest == false) View.VISIBLE else View.GONE
        mNoticeButton?.text = if (noticeCount ?: 0 < 10) noticeCount.toString() else "N"
    }

}