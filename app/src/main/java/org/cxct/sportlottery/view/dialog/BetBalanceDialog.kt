package org.cxct.sportlottery.view.dialog

import android.app.Dialog
import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.betList.BetListFragment
import org.cxct.sportlottery.view.onClick

/**
 * 提示余额不足dialog
 */
class BetBalanceDialog(mContext: Context) : Dialog(mContext)  {
    private var ivClose:ImageView?=null
    init {
        initDialog()
    }

    private fun initDialog() {
        setContentView(R.layout.dialog_bet_balance)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(false)

        ivClose=findViewById<ImageView>(R.id.ivBetClose)
        ivClose?.onClick {
            dismiss()
        }

    }


    fun showDialog(block:()->Unit){
        //充值按钮点击
        findViewById<TextView>(R.id.tvDeposit).onClick {
            dismiss()
            block()
        }
        show()

        //5秒后自动关闭
        ivClose?.postDelayed({
             dismiss()
        },5000)
    }
}