package org.cxct.sportlottery.ui.login.selectAccount

import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.ForgetPwdSelectAccountEvent
import org.cxct.sportlottery.common.event.LoginGlifeOrRegistEvent
import org.cxct.sportlottery.common.event.LoginSelectAccountEvent
import org.cxct.sportlottery.common.extentions.bindFinish
import org.cxct.sportlottery.databinding.ActivitySelectAccountBinding
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.login.signIn.LoginViewModel
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.setServiceClick

class SelectAccountActivity : BindingActivity<LoginViewModel, ActivitySelectAccountBinding>() {

    companion object{
        const val TYPE_LOGIN = 1
        const val TYPE_FORGET = 2
        const val TYPE_LOGINGLIFE_OR_REGIST = 3
        const val TYPE_SELECT = "typeSelect"
    }
    val type by lazy { intent.getIntExtra(TYPE_SELECT,TYPE_LOGIN) }

    override fun onInitView() = binding.run {
        setStatusBarDarkFont()
        bindFinish(btnBack)
        clLiveChat.setServiceClick(supportFragmentManager)
        if (type==TYPE_LOGINGLIFE_OR_REGIST){
            btnOkbet.apply {
                setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_glife_round),null,null,null)
                text = getString(R.string.P176)
            }
            btnGlife.apply {
                setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_okbet_round),null,null,null)
                text = getString(R.string.P175)
            }
        }
        btnOkbet.setOnClickListener {
            finish()
            when(type){
                TYPE_LOGIN->EventBusUtil.post(LoginSelectAccountEvent(false))
                TYPE_FORGET->EventBusUtil.post(ForgetPwdSelectAccountEvent(false))
                TYPE_LOGINGLIFE_OR_REGIST->EventBusUtil.post(LoginGlifeOrRegistEvent(true))
            }
        }
        btnGlife.setOnClickListener {
            finish()
            when(type){
                TYPE_LOGIN->EventBusUtil.post(LoginSelectAccountEvent(true))
                TYPE_FORGET->EventBusUtil.post(ForgetPwdSelectAccountEvent(true))
                TYPE_LOGINGLIFE_OR_REGIST->EventBusUtil.post(LoginGlifeOrRegistEvent(false))
            }
        }
    }
}