package org.cxct.sportlottery.ui.login.signUp

import androidx.fragment.app.FragmentManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setOnClickListeners
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.DialogRegisterSuccessBinding
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.setupSummary
import org.cxct.sportlottery.view.dialog.queue.BasePriorityDialog
import org.cxct.sportlottery.view.dialog.queue.PriorityDialog

class RegisterSuccessDialog: BaseDialog<BaseViewModel,DialogRegisterSuccessBinding>() {

    companion object{
        var ifNew = false
        var loginFirstPhoneGiveMoney = false
        fun needShow():Boolean{
            return ifNew
        }

        fun buildRegisterSuccessDialog(priority: Int, fm: () -> FragmentManager, callback: () -> Unit): PriorityDialog? {
            if (!needShow()) {
                return null
            }

            return object : BasePriorityDialog<RegisterSuccessDialog>() {
                override fun getFragmentManager() = fm.invoke()
                override fun priority() = priority
                override fun createDialog() = RegisterSuccessDialog().apply { onRecharge = callback }
            }
        }
    }

    init {
        setStyle(R.style.FullScreen)
    }
    var onRecharge: (()->Unit)?=null

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
        ifNew =false
        loginFirstPhoneGiveMoney =false
        setupSummary(binding.tvSummary)
        binding.btnRecharge.setOnClickListener {
            dismissAllowingStateLoss()
            onRecharge?.invoke()
        }
        setOnClickListeners(binding.ivClose,binding.btnConfirm){
            dismissAllowingStateLoss()
        }
    }
}