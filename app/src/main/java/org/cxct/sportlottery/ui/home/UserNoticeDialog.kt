package org.cxct.sportlottery.ui.home

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import kotlinx.android.synthetic.main.dialog_user_notice.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.service.user_notice.UserNotice

class UserNoticeDialog(context: Context) : AlertDialog(context) {

    private var mDataList = listOf<UserNotice>()
    private var mPage: Int = 0

    fun setNoticeList(noticeList: List<UserNotice>): UserNoticeDialog {
        mDataList = noticeList
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_user_notice)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(false) //disable 點擊外部關閉 dialog
        initButton()
        showPage(mPage++)
    }

    private fun initButton() {
        btn_close.setOnClickListener {
            dismiss()
        }

        btn_positive.setOnClickListener {
            //顯示下一則，到底關閉 dialog
            showPage(mPage++)
        }
    }

    private fun showPage(index: Int) {
        try {
            if (mDataList.isEmpty() || index !in 0..mDataList.lastIndex)
                dismiss()
            else {
                tv_title.text = mDataList[index].title
                tv_message.text = mDataList[index].content
            }
        } catch (e: Exception) {
            e.printStackTrace()
            dismiss()
        }
    }

}
