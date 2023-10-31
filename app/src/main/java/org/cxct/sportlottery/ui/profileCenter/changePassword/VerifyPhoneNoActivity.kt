package org.cxct.sportlottery.ui.profileCenter.changePassword

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.bindFinish
import org.cxct.sportlottery.databinding.ActivityVerifyPhonenoBinding
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.util.setTitleLetterSpacing

class VerifyPhoneNoActivity: BindingActivity<SettingPasswordViewModel, ActivityVerifyPhonenoBinding>() {

    override fun onInitView() {
        initTitlebar()
    }

    private fun initTitlebar() = binding.run {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        bindFinish(binding.toolBar.btnToolbarBack)
        toolBar.tvToolbarTitle.setTitleLetterSpacing()
        toolBar.tvToolbarTitle.text = getString(R.string.withdraw_password)
    }
}