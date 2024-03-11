package org.cxct.sportlottery.ui.selflimit

import android.view.View
import org.cxct.sportlottery.databinding.DialogSelfLimitFrozeImportantBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel

/**
 * 常用提示對話框
 * 預設按鈕: 取消 / 確定
 *
 * memo:
 * this.setCanceledOnTouchOutside(false) //disable 點擊外部關閉 dialog
 * this.setCancelable(false) //disable 按實體鍵 BACK 關閉 dialog
 */
class SelfLimitFrozeImportantDialog: BaseDialog<BaseViewModel,DialogSelfLimitFrozeImportantBinding>() {

    private val isBet by lazy { arguments?.getBoolean("isBet") ?: true }

    override fun onInitView() {
        initView()
    }

    private fun initView()=binding.run {
        llFroze.visibility = if(isBet) View.GONE else View.VISIBLE
        llBet.visibility = if(isBet) View.VISIBLE else View.GONE

        btnClose.setOnClickListener{
            dismiss()
        }
    }


}