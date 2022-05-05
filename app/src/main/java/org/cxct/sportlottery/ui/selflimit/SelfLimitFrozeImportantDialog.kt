package org.cxct.sportlottery.ui.selflimit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_self_limit_froze_important.*
import androidx.fragment.app.DialogFragment
import org.cxct.sportlottery.R

/**
 * 常用提示對話框
 * 預設按鈕: 取消 / 確定
 *
 * memo:
 * this.setCanceledOnTouchOutside(false) //disable 點擊外部關閉 dialog
 * this.setCancelable(false) //disable 按實體鍵 BACK 關閉 dialog
 */
class SelfLimitFrozeImportantDialog(private var isBet: Boolean): DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.dialog_self_limit_froze_important, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        initView()
    }

    private fun initView() {
        ll_froze.visibility = if(isBet) View.GONE else View.VISIBLE
        ll_bet.visibility = if(isBet) View.VISIBLE else View.GONE

        btn_close.setOnClickListener{
            dismiss()
        }
    }

    fun setCanceledOnTouchOutside(boolean: Boolean){
        dialog?.setCanceledOnTouchOutside(boolean)
    }
}