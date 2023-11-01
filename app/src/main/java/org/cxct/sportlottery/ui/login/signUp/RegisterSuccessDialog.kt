package org.cxct.sportlottery.ui.login.signUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setOnClickListeners
import org.cxct.sportlottery.databinding.DialogRegisterSuccessBinding
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.setupSummary

class RegisterSuccessDialog(val onRecharge: ()->Unit): BaseDialog<BaseViewModel>(BaseViewModel::class) {

    companion object{
        var ifNew = false
    }

    init {
        setStyle(R.style.FullScreen)
    }
    lateinit var binding : DialogRegisterSuccessBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=DialogRegisterSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }
    private fun initView() {
        UserInfoRepository.userInfo.value?.let {
            if (((sConfigData?.firstPhoneGiveMoney?:0)>0) && it.firstPhoneGiveMoney>0 && it.phone.isNullOrEmpty()){
                binding.tvGiveMoney.text = getString(R.string.P237,"${sConfigData?.systemCurrencySign}${sConfigData?.firstPhoneGiveMoney?:0}")
            }
        }
        setupSummary(binding.tvSummary)
        binding.btnRecharge.setOnClickListener {
            dismissAllowingStateLoss()
            onRecharge.invoke()
        }
        setOnClickListeners(binding.ivClose,binding.btnConfirm){
            dismissAllowingStateLoss()
        }
    }
}