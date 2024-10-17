package org.cxct.sportlottery.ui.money.withdraw

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.VerifiedType
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.databinding.FragmentWithdrawStepBinding
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityActivity
import org.cxct.sportlottery.ui.profileCenter.modify.ModifyBindInfoActivity
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyType
import org.cxct.sportlottery.util.LogUtil

class WithdrawStepFragment: BaseFragment<ProfileCenterViewModel, FragmentWithdrawStepBinding>() {

    override fun onInitView(view: View) {
        setup()
       viewModel.userInfo.observe(this){
           hideLoading()
           setup()
       }
    }

    override fun onResume() {
        super.onResume()
        loading()
        viewModel.getUserInfo()
    }
    fun setup()=binding.run{
        val userInfo = UserInfoRepository.userInfo.value!!
        val needPhoneNumber = userInfo.phone.isNullOrBlank()
        val needPassword = userInfo.passwordSet
        val needPayPW = userInfo.updatePayPw == 1
        LogUtil.d("fullVerified="+userInfo.fullVerified+",halfVverifiedCharge="+sConfigData?.halfVerifiedCharge)
        val needVerify = userInfo.fullVerified!=1 && sConfigData?.halfVerifiedCharge==0
        setStepItem(needPhoneNumber,ivStep1,line1,tvStepState1,ivStepArrow1){
            ModifyBindInfoActivity.start(requireActivity(), ModifyType.PhoneNumber, 100, null, null, null)
        }
        setStepItem(needPassword,ivStep2,line2,tvStepState2,ivStepArrow2){
            startActivity(SettingPasswordActivity::class.java)
        }
        setStepItem(needPayPW,ivStep3,line3,tvStepState3,ivStepArrow3){
            startActivity(Intent(requireActivity(), SettingPasswordActivity::class.java)
                .apply { putExtra(SettingPasswordActivity.PWD_PAGE, SettingPasswordActivity.PwdPage.BANK_PWD) }
            )
        }
        setStepItem(needVerify,ivStep4,null,tvStepState4,ivStepArrow4){
            startActivity(VerifyIdentityActivity::class.java)
        }
        //若全部完成，就切换页面到提款页面
        if (!needPhoneNumber&&!needPassword&&!needPayPW&&!needVerify){
            (requireActivity() as WithdrawActivity).jumpToFragment()
        }
    }
    private fun setStepItem(enable: Boolean, ivIcon: ImageView, line: View?, tvState: TextView, ivArrow: ImageView, onClick: ()->Unit){
        (tvState.parent as ViewGroup).let{
            it.isEnabled = enable
            it.setOnClickListener{
                onClick.invoke()
            }
        }
        ivIcon.setBackgroundResource(if(enable) R.drawable.bg_circle_d0d4dd else R.drawable.bg_circle_blue)
        line?.setBackgroundResource(if(enable) R.color.color_D0D4DD else R.color.color_025BE8)

        tvState.apply {
            if(enable){
                text = getString(R.string.P448)
                setTextColor(requireContext().getColor(R.color.color_A5A9B3))
            }else{
                text = getString(R.string.D038)
                setTextColor(requireContext().getColor(R.color.color_1CD219))
            }
            if (tvState==binding.tvStepState4){
                VerifiedType.getVerifiedType(UserInfoRepository.userInfo.value).let {
                    if (it == VerifiedType.NOT_YET || it == VerifiedType.VERIFIED_FAILED || it == VerifiedType.PASSED){
                        return@let
                    }
                    text =  getString(it.nameResId)
                    setTextColor(requireContext().getColor(it.colorResId))
                }
            }
        }
        ivArrow.setImageResource(if (enable) R.drawable.ic_arrow_edit else R.drawable.ic_check_green)
    }
}