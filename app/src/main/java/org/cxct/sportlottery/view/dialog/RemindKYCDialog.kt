package org.cxct.sportlottery.view.dialog

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import org.cxct.sportlottery.common.enums.VerifiedType
import org.cxct.sportlottery.databinding.DialogRemindKycBinding
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.KvUtils
import org.cxct.sportlottery.util.KvUtils.REMIND_KYC_DATE
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.jumpToKYC
import org.cxct.sportlottery.view.dialog.queue.BasePriorityDialog
import org.cxct.sportlottery.view.dialog.queue.PriorityDialog
import java.util.Calendar
import java.util.Date

/**
 * 新用户注册后3天，每天提示一次KYC认证
 */
class RemindKYCDialog : BaseDialog<BaseViewModel,DialogRemindKycBinding>() {

    init {
        marginHorizontal=30.dp
    }
    companion object{
        fun newInstance() = RemindKYCDialog()
        /**
         * 根据条件判断是否需要显示
         */
        fun needShow():Boolean{
            //用户是否已登录/是否未认证/是否注册后3天内/当天是否已经提示过/config开关是否启用
            val registCal = Calendar.getInstance()
            val userInfo = UserInfoRepository.userInfo.value
            registCal.timeInMillis = userInfo?.addTime?:0
            TimeUtil.getEndTimeCalendar(registCal)
            registCal.add(Calendar.DATE,2)
            val isRegist3Days=Calendar.getInstance().before(registCal)
            val todayDateValue = TimeUtil.dateToFormat(Date(),TimeUtil.YMD_FORMAT)
            val isTodayRemind = KvUtils.decodeString(REMIND_KYC_DATE) == todayDateValue
            val isVerified = UserInfoRepository.userInfo.value?.fullVerified==1 || UserInfoRepository.userInfo.value?.verified==VerifiedType.PASSED.value
            return LoginRepository.isLogined() && !isVerified && isRegist3Days && !isTodayRemind && sConfigData?.kycPrompt==1
        }
        fun buildDialog(priority: Int, fm: () -> FragmentManager): PriorityDialog? {
            if (!needShow()) {
                return null
            }
            return object : BasePriorityDialog<AgeVerifyDialog>() {
                override fun getFragmentManager() = fm.invoke()
                override fun priority() = priority
                override fun createDialog() = RemindKYCDialog.newInstance()
            }
        }
    }

    override fun onInitView() {
        isCancelable = false
        KvUtils.put(REMIND_KYC_DATE, TimeUtil.dateToFormat(Date(),TimeUtil.YMD_FORMAT))
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnVerify.setOnClickListener {
            requireActivity().jumpToKYC()
            dismiss()
        }
        binding.ivClose.setOnClickListener {
            dismiss()
        }
    }

}