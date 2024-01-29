package org.cxct.sportlottery.view.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.ui.maintab.entity.EnterThirdGameResult
import org.cxct.sportlottery.util.commonCheckDialog
import org.cxct.sportlottery.view.onClick

class TrialGameDialog(private val mContext: Context,
                      private val firmType: String,
                      private val thirdGameResult: EnterThirdGameResult,
                      private val onConfirm:(String, EnterThirdGameResult) -> Unit) : Dialog(mContext)  {

    private val lifeScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_trial_play)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(true)
        setOnDismissListener{ lifeScope.cancel() }

        val tvLogin=findViewById<TextView>(R.id.tvLogin)
        //去登录
        tvLogin!!.onClick {
            dismiss()
            context.startActivity(Intent(context, LoginOKActivity::class.java))
        }
        tvLogin.text=context.resources.getString(R.string.F025)
        findViewById<TextView>(R.id.tvContent).text=context.resources.getString(R.string.N994)
        findViewById<TextView>(R.id.tvForward).text=context.resources.getString(R.string.N993)

        //进入游戏
        findViewById<ImageView>(R.id.tvForward).onClick {
            dismiss()
            onConfirm(firmType, thirdGameResult)
        }

        ServiceBroadcastReceiver.thirdGamesMaintain.collectWith(lifeScope) {
            if (firmType == it.firmType && it.maintain == 1) {

                if (mContext is BaseActivity<*,*>) {
                    commonCheckDialog(
                        context = mContext,
                        fm = mContext.supportFragmentManager,
                        isError = true,
                        isShowDivider = false,
                        title = mContext.getString(R.string.prompt),
                        errorMessage = mContext.getString(R.string.hint_game_maintenance),
                        buttonText = mContext.getString(R.string.btn_confirm),
                        positiveClickListener = { },
                        negativeText = null,
                    )
                }
                dismiss()
            }
        }
    }



}