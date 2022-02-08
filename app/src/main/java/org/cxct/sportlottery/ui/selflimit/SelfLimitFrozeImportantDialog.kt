package org.cxct.sportlottery.ui.selflimit

import android.content.Context
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.dialog_self_limit_froze_important.*
import androidx.appcompat.app.AlertDialog
import org.cxct.sportlottery.R

/**
 * 常用提示對話框
 * 預設按鈕: 取消 / 確定
 *
 * memo:
 * this.setCanceledOnTouchOutside(false) //disable 點擊外部關閉 dialog
 * this.setCancelable(false) //disable 按實體鍵 BACK 關閉 dialog
 */
class SelfLimitFrozeImportantDialog(context: Context,private var isBet: Boolean) : AlertDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_self_limit_froze_important)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        initView()
    }

    private fun initView() {
        ll_froze.visibility = if(isBet) View.GONE else View.VISIBLE
        ll_bet.visibility = if(isBet) View.VISIBLE else View.GONE

        btn_close.setOnClickListener{
            dismiss()
        }
    }
}