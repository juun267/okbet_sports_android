package org.cxct.sportlottery.ui.money.withdraw

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.VerifiedType
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.databinding.FragmentWithdrawStepBinding
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity

class WithdrawStepFragment: BaseFragment<WithdrawViewModel, FragmentWithdrawStepBinding>() {

    override fun onInitView(view: View) {
        setup()
    }
    fun setup()=binding.run{
        val needPhoneNumner = UserInfoRepository.userInfo.value?.phone.isNullOrBlank()
        val needPassword = UserInfoRepository.userInfo.value?.passwordSet != false
        val needPayPW = UserInfoRepository.userInfo.value?.updatePayPw == 1
        val needVerify = UserInfoRepository.userInfo.value?.verified != VerifiedType.PASSED.value
        setStepItem(needPhoneNumner,ivStep1,line1,tvStepState1,ivStepArrow1,::onItemClick)
        setStepItem(needPassword,ivStep2,line2,tvStepState2,ivStepArrow2,::onItemClick)
        setStepItem(needPayPW,ivStep3,line3,tvStepState3,ivStepArrow3,::onItemClick)
        setStepItem(needVerify,ivStep4,null,tvStepState4,ivStepArrow4,::onItemClick)
    }
    fun setStepItem(enable: Boolean, ivIcon: ImageView, line: View?, tvState: TextView, ivArrow: ImageView,onClick: ()->Unit){
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
                if (tvState==binding.tvStepState4){
                    VerifiedType.getVerifiedType(UserInfoRepository.userInfo.value?.verified).let {
                        text =  getString(it.nameResId)
                        setTextColor(requireContext().getColor(it.colorResId))
                    }
                }else{
                    text =  "Done"
                    setTextColor(requireContext().getColor(R.color.color_1CD219))
                }
            }
        }
        ivArrow.setImageResource(if (enable) R.drawable.ic_arrow_edit else R.drawable.ic_check_green)
    }
    fun onItemClick(){
        requireActivity().startActivity(ProfileActivity::class.java)
        requireActivity().finish()
    }
}