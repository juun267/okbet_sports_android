package org.cxct.sportlottery.view.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.widget.ImageView
import android.widget.TextView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.view.onClick

class TrialGameDialog(mContext: Context) : Dialog(mContext)  {

    init {
        initDialog()
    }

    private fun initDialog() {
        setContentView(R.layout.dialog_trial_play)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(true)

        //去登录
        findViewById<TextView>(R.id.tvLogin).onClick {
            dismiss()
            context.startActivity(Intent(context, LoginOKActivity::class.java))
        }
    }


    fun setEnterGameClick(block:()->Unit){
        //进入游戏
        findViewById<ImageView>(R.id.tvForward).onClick {
            dismiss()
            block()
        }
    }
}