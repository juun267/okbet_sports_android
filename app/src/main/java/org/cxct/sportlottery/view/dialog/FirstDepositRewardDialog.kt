package org.cxct.sportlottery.view.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.loading
import org.cxct.sportlottery.databinding.DialogFirstDepositRewardBinding
import org.cxct.sportlottery.net.money.data.FirstDepositConfig
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.view.dialog.queue.BasePriorityDialog
import org.cxct.sportlottery.view.dialog.queue.PriorityDialog
import splitties.bundle.put

/**
 * 次日活动弹窗活动弹窗
 */
class FirstDepositRewardDialog : BaseDialog<MainHomeViewModel, DialogFirstDepositRewardBinding>() {


    companion object{
        private const val FIRST_DEPOSIT_CONFIG = "firstDepositConfig"
        private const val VAILD_BET_MONEY = "validBetMoney"
        fun newInstance(isFirstDeposit: FirstDepositConfig,validBetMoney: Double) = FirstDepositRewardDialog().apply{
            arguments = Bundle().apply {
                put(FIRST_DEPOSIT_CONFIG,isFirstDeposit)
                put(VAILD_BET_MONEY,validBetMoney)
            }
        }
        fun buildDialog(priority: Int, fm: () -> FragmentManager,isFirstDeposit: FirstDepositConfig,validBetMoney: Double): PriorityDialog? {
            return object : BasePriorityDialog<FirstDepositRewardDialog>() {
                override fun getFragmentManager() = fm.invoke()
                override fun priority() = priority
                override fun createDialog() = newInstance(isFirstDeposit,validBetMoney)
            }
        }
    }
    private val firstDepositConfig by lazy { requireArguments().getParcelable<FirstDepositConfig>(FIRST_DEPOSIT_CONFIG)!! }
    private val validBetMoney by lazy { requireArguments().getDouble(VAILD_BET_MONEY) }

    init {
        setStyle(R.style.FullScreen)
    }

    override fun onInitView()=binding.run {
        tvRewardAmount.text = "${sConfigData?.systemCurrencySign}${firstDepositConfig.rewardAmount}"
        tvCondition2.text = String.format(getString(R.string.A009),validBetMoney.toString())
        tvDesp.text = String.format(getString(R.string.A011,"${sConfigData?.systemCurrencySign}${TextUtil.formatMoney(validBetMoney)}"))
        //用户流水大于活动要求的流水
        if (validBetMoney>firstDepositConfig.validBetMoney*firstDepositConfig.flowRatio){
            ivState2.setImageResource(R.drawable.ic_state_completed)
            tvGet.isEnabled = true
        }else{
            ivState2.setImageResource(R.drawable.ic_state_countdown)
            tvGet.isEnabled = false
        }
        ivClose.setOnClickListener { dismiss() }
        tvGet.setOnClickListener {
            requireActivity().loading()
            viewModel.getFirstDepositAfterDay()
        }
    }

    override fun onBindViewStatus(view: View) {
        super.onBindViewStatus(view)
        viewModel.getFirstDepositAfterDay.observe(viewLifecycleOwner){
            requireActivity().hideLoading()
            if (it.getData()==true){
                ToastUtil.showToast(requireContext(),getString(R.string.P454))
                dismiss()
            }else{
                ToastUtil.showToast(requireContext(),it.msg)
            }
        }
    }

}