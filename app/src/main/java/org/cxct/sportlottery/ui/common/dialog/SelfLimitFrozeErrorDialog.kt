package org.cxct.sportlottery.ui.common.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_self_limit_froze.*
import org.cxct.sportlottery.R

/**
 * 常用提示對話框
 * 預設按鈕: 取消 / 確定
 *
 * memo:
 * this.setCanceledOnTouchOutside(false) //disable 點擊外部關閉 dialog
 * this.setCancelable(false) //disable 按實體鍵 BACK 關閉 dialog
 */
class SelfLimitFrozeErrorDialog : DialogFragment() {

    private var mMessage: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.dialog_self_limit_froze, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun initView() {
        when (mMessage) {
            null -> sv_block_content.visibility = View.GONE
            else -> tv_message.text = mMessage
        }

        btn_positive.setOnClickListener{
            dismiss()
        }
    }

    fun setMessage(message: String?) {
        mMessage = message
    }
}