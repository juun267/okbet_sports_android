package org.cxct.sportlottery.ui.login.signUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setOnClickListeners
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.DialogRegisterSuccessBinding
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.setupSummary
import org.cxct.sportlottery.view.isVisible

class RegisterSuccessDialog(val onRecharge: ()->Unit): BaseDialog<BaseViewModel,DialogRegisterSuccessBinding>() {

    companion object{
        var ifNew = false
        var loginFirstPhoneGiveMoney = false
        fun needShow():Boolean{
            return ifNew
        }
    }

    init {
        setStyle(R.style.FullScreen)
    }

    override fun onInitView() {
        initView()
    }
    private fun initView() {
        UserInfoRepository.userInfo.value?.let {
            if (((sConfigData?.firstPhoneGiveMoney?:0)>0) && loginFirstPhoneGiveMoney){
                binding.tvGiveMoney.text = getString(R.string.P237,"${sConfigData?.systemCurrencySign}${sConfigData?.firstPhoneGiveMoney?:0}")
                binding.tvGiveMoney.visible()
            }
        }
        loginFirstPhoneGiveMoney =false
        setupSummary(binding.tvSummary)
        binding.btnRecharge.setOnClickListener {
            dismissAllowingStateLoss()
            onRecharge.invoke()
        }
        setOnClickListeners(binding.ivClose,binding.btnConfirm){
            dismissAllowingStateLoss()
        }
    }

     override fun show(manager: FragmentManager) {
        super.show(manager)
        ifNew =false
    }
}