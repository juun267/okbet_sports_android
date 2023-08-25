package org.cxct.sportlottery.view.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.widget.ImageView
import android.widget.TextView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.ui.maintab.entity.EnterThirdGameResult
import org.cxct.sportlottery.view.onClick

class TrialGameDialog(mContext: Context,
                      private val firmType: String,
                      private val thirdGameResult: EnterThirdGameResult,
                      private val onConfirm:(String, EnterThirdGameResult) -> Unit) : Dialog(mContext)  {

    init {
        initDialog()
    }

    private fun initDialog() {
        setContentView(R.layout.dialog_trial_play)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(true)

        val tvLogin=findViewById<TextView>(R.id.tvLogin)
        //去登录
        tvLogin.onClick {
            dismiss()
            context.startActivity(Intent(context, LoginOKActivity::class.java))
        }
        tvLogin.text=context.resources.getString(R.string.F025)
        findViewById<TextView>(R.id.tvContent).text=context.resources.getString(R.string.N994)
        findViewById<TextView>(R.id.tvForward).text=context.resources.getString(R.string.N993)

        //进入游戏
        findViewById<ImageView>(R.id.tvForward).onClick { onConfirm(firmType, thirdGameResult) }
    }

}