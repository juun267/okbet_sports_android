package org.cxct.sportlottery.view.dialog

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.FragmentManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.loading
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.DialogFirstDepositRewardBinding
import org.cxct.sportlottery.net.money.data.FirstDepositDetail
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.maintab.home.HomeFragment
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.dialog.queue.BasePriorityDialog
import org.cxct.sportlottery.view.dialog.queue.PriorityDialog
import org.cxct.sportlottery.view.isVisible
import splitties.bundle.put
import java.util.*


/**
 * 次日活动弹窗活动弹窗
 */
class FirstDepositRewardDialog private constructor(): BaseDialog<MainHomeViewModel, DialogFirstDepositRewardBinding>() {


    companion object{
        private const val FIRST_DEPOSIT_DETAIL = "firstDepositDetail"
        fun newInstance(firstDepositDetail: FirstDepositDetail) = FirstDepositRewardDialog().apply{
            arguments = Bundle().apply {
                put(FIRST_DEPOSIT_DETAIL,firstDepositDetail)
            }
        }
        fun buildDialog(priority: Int, fm: () -> FragmentManager, firstDepositDetail: FirstDepositDetail): PriorityDialog? {
            return object : BasePriorityDialog<FirstDepositRewardDialog>() {
                override fun getId() = "FirstDepositRewardDialog"
                override fun getFragmentManager() = fm.invoke()
                override fun priority() = priority
                override fun createDialog() = newInstance(firstDepositDetail)
            }
        }
    }
    private val firstDepositDetail by lazy { requireArguments().getParcelable<FirstDepositDetail>(FIRST_DEPOSIT_DETAIL)!! }

    init {
        setStyle(R.style.FullScreen)
    }

    override fun onInitView()=binding.run {
        val firstDepositConfig = firstDepositDetail.getCurrentDepositConfig()!!
        tvRewardAmount.text = "${sConfigData?.systemCurrencySign}${TextUtil.formatMoney2(firstDepositDetail.rewardAmount ?: 0)}"
        //活动要求投注流水
        val requiredBetMoney = firstDepositConfig.validBetMoney
        tvCondition2.text = String.format(getString(R.string.A009),requiredBetMoney)
        tvDesp.text = String.format(getString(R.string.A011,"${sConfigData?.systemCurrencySign}${TextUtil.formatMoney2(requiredBetMoney)}"))
        //用户流水大于活动要求的流水
        val item2Selected = firstDepositDetail.validBetMoney >= requiredBetMoney
        if (item2Selected){
            ivState2.setImageResource(R.drawable.ic_state_completed)
        }else{
            ivState2.setImageResource(R.drawable.ic_state_failed)
        }
        if (requiredBetMoney == 0) {
            layout2.gone()
        }
        //当前是否充值后的第二日
        val item3Selected = isTomorrow(firstDepositDetail.rechTime)
        if (item3Selected){
            ivState3.setImageResource(R.drawable.ic_state_completed)
        }else{
            ivState3.setImageResource(R.drawable.ic_state_countdown)
        }
        tvGet.isEnabled = (layout2.isGone || item2Selected) && item3Selected

        ivClose.setOnClickListener {
            dismiss()
        }
        tvGet.setOnClickListener {
            requireActivity().loading()
            viewModel.getFirstDepositAfterDay()
        }
    }

    override fun onBindViewStatus(view: View) {
        super.onBindViewStatus(view)
        viewModel.getFirstDepositAfterDay.observe(viewLifecycleOwner){
            requireActivity().hideLoading()
            if (it.succeeded()){
                ToastUtil.showToast(requireContext(),getString(R.string.P454))
                dismiss()
                ((requireParentFragment() as HomeFragment).getCurrentFragment() as? HomeHotFragment)?.getFirstDepositDetail()
            }else{
                ToastUtil.showToast(requireContext(),it.msg)
            }
        }
    }
    private fun isTomorrow(serverTime: Long): Boolean{
        val serverCal = Calendar.getInstance()
        serverCal.timeInMillis = serverTime
        serverCal.add(Calendar.DAY_OF_MONTH, 1)
        val serverDate = TimeUtil.timeFormat(serverCal.timeInMillis, TimeUtil.YMD_FORMAT, timeZone = TimeZone.getTimeZone(TimeUtil.TIMEZONE_DEFAULT))
        val currentDate = TimeUtil.timeFormat(System.currentTimeMillis(), TimeUtil.YMD_FORMAT, timeZone = TimeZone.getTimeZone(TimeUtil.TIMEZONE_DEFAULT))
        return serverDate == currentDate
    }
}