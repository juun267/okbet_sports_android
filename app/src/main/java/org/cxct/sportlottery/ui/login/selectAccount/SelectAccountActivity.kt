package org.cxct.sportlottery.ui.login.selectAccount

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.ForgetPwdSelectAccountEvent
import org.cxct.sportlottery.common.event.LoginGlifeOrRegistEvent
import org.cxct.sportlottery.common.event.LoginSelectAccountEvent
import org.cxct.sportlottery.common.extentions.bindFinish
import org.cxct.sportlottery.databinding.ActivitySelectAccountBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.login.signIn.LoginViewModel
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.setServiceClick

class SelectAccountActivity : BaseActivity<LoginViewModel, ActivitySelectAccountBinding>() {

    override fun pageName() = "账号选择页面"

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
        bottomLiences.tvLicense.text = Constants.copyRightString
        if (type==TYPE_LOGINGLIFE_OR_REGIST){
            ivOkbet.setImageResource(R.drawable.ic_glife_round)
            tvOkbet.text = getString(R.string.P176)

            ivGlife.setImageResource(R.drawable.ic_okbet_round)
            tvGlife.text = getString(R.string.P175)
        }
        linOkbet.setOnClickListener {
            finish()
            when(type){
                TYPE_LOGIN->EventBusUtil.post(LoginSelectAccountEvent(false, true))
                TYPE_FORGET->EventBusUtil.post(ForgetPwdSelectAccountEvent(false))
                TYPE_LOGINGLIFE_OR_REGIST->EventBusUtil.post(LoginGlifeOrRegistEvent(true))
            }
        }
        linGlife.setOnClickListener {
            finish()
            when(type){
                TYPE_LOGIN->EventBusUtil.post(LoginSelectAccountEvent(true, false))
                TYPE_FORGET->EventBusUtil.post(ForgetPwdSelectAccountEvent(true))
                TYPE_LOGINGLIFE_OR_REGIST->EventBusUtil.post(LoginGlifeOrRegistEvent(false))
            }
        }
    }
}