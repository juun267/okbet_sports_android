package org.cxct.sportlottery.ui.money.withdraw

import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.VerifiedType
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.databinding.ActivityWithdrawBinding
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.FragmentHelper
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.Param
import org.cxct.sportlottery.util.setTitleLetterSpacing

/**
 * @app_destination 提款
 */
class WithdrawActivity : BaseActivity<WithdrawViewModel, ActivityWithdrawBinding>() {
    private val fragmentHelper: FragmentHelper by lazy {
        FragmentHelper(
            supportFragmentManager, binding.flContent.id, arrayOf(
                Param(WithdrawStepFragment::class.java, needRemove = true),
                Param(WithdrawFragment::class.java, needRemove = true),
                Param(BetStationFragment::class.java,needRemove = true),
            )
        )
    }
    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF,true)
        initToolbar()
        checkNeedStep().apply {
            if (this){
                fragmentHelper.showFragment(0)
            }else{
                fragmentHelper.showFragment(1)
                setToolbarRightText()
            }
        }
        viewModel.isVisibleView.observe(this) {
            binding.toolBar.tvToolbarTitleRight.isVisible = it
        }
    }

    private fun initToolbar() =binding.toolBar.run{
        tvToolbarTitle.setTitleLetterSpacing()
        tvToolbarTitle.text = getString(R.string.withdraw)
        btnToolbarBack.setOnClickListener {
            finish()
        }
    }
    private fun setToolbarRightText()=binding.toolBar.run{
        tvToolbarTitleRight.setOnClickListener {
            startActivity(BankActivity::class.java)
        }
        tvToolbarTitleRight.isVisible = viewModel.isVisibleView.value ?: true
        tvToolbarTitleRight.text = getString(R.string.withdraw_setting)
    }
    private fun checkNeedStep():Boolean{
        val needPhoneNumner = UserInfoRepository.userInfo.value?.phone.isNullOrBlank()
        val needPassword = UserInfoRepository.userInfo.value?.passwordSet != false
        val needPayPW = UserInfoRepository.userInfo.value?.updatePayPw == 1
        val needVerify = UserInfoRepository.userInfo.value?.verified != VerifiedType.PASSED.value
        return needPhoneNumner||needPassword||needPayPW||needVerify
    }

}