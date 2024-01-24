package org.cxct.sportlottery.ui.finance

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogWithdrawLogDetailBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.finance.df.OrderState
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.TextUtil
import kotlin.math.abs

/**
 * @app_destination 提款详情弹窗
 */
class WithdrawLogDetailDialog : BaseDialog<FinanceViewModel>(FinanceViewModel::class) {
    init {
        setStyle(R.style.CustomDialogStyle)
    }

    private val binding by lazy { DialogWithdrawLogDetailBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return binding.root.apply {
            setupConfirmButton()
        }
    }

    private fun setupConfirmButton() {
        binding.logDetailConfirm.setOnClickListener {
            dismiss()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)=binding.run {
        super.onViewCreated(view, savedInstanceState)
        wdLogDetailRemarksLeft.text = getString(R.string.N064) + "："
        viewModel.withdrawLogDetail.observe(this@WithdrawLogDetailDialog.viewLifecycleOwner) { event ->
            event.peekContent().let { it ->
                wdLogDetailRemarksRight.text = it.reason
                wdLogDetailTransNumSubtitle.text = "${getString(R.string.J630)}："
                wdLogDetailAmountSubtitle.text =
                    "${getString(R.string.text_account_history_amount)}："
                wdLogDetailTransNum.text = it.orderNo ?: ""
                wdLogDetailTime.text = it.withdrawDateAndTime ?: ""
                //用于前端显示单单订单状态 1: 處理中 2:提款成功 3:提款失败 4：待投注站出款
                wdLogDetailStatus.apply {
                    when (it.orderState) {
                        OrderState.PROCESSING.code -> {
                            text = LocalUtils.getString(R.string.log_state_processing)
                            setTextColor(ContextCompat.getColor(context, R.color.color_414655))
                        }

                        OrderState.SUCCESS.code -> {
                            text = LocalUtils.getString(R.string.recharge_state_success)
                            setTextColor(ContextCompat.getColor(context, R.color.color_1EB65B))
                        }

                        OrderState.FAILED.code -> {
                            text = LocalUtils.getString(R.string.recharge_state_failed)
                            setTextColor(ContextCompat.getColor(context, R.color.color_E23434))
                        }

                        OrderState.PENGING.code -> {
                            text = LocalUtils.getString(R.string.N653)
                            setTextColor(ContextCompat.getColor(context, R.color.color_414655))
                        }
                    }
                }

//                wd_log_detail_review_time.text = it.operatorDateAndTime ?: ""
//                wd_log_detail_reason.text = it.reason ?: ""
                it.displayMoney?.let { nonNullDisplayMoney ->
                    wdLogDetailAmount.text =
                        "${sConfigData?.systemCurrencySign} $nonNullDisplayMoney"
                }

                it.withdrawDeductMoney?.let { nonNullDeductMoney ->
                    wdLogDetailCommission.text =
                        "${sConfigData?.systemCurrencySign} $nonNullDeductMoney"
                }

                (it.fee ?: 0.0).let { fee ->
                    wdLogDetailHandleFee.text =
                        "${sConfigData?.systemCurrencySign} ${TextUtil.format(abs(fee))}"
                    wdLogDetailHandleFeeSubtitle.text =
                        if ((fee) > 0.0) getString(R.string.log_detail_rebate_money)
                        else getString(R.string.log_detail_handle_fee)
                }
                it.children.let {
                    if (it.isNullOrEmpty()) {
                        nsvContent.layoutParams.apply {
                            height = LinearLayout.LayoutParams.WRAP_CONTENT
                        }
                    } else {
                        nsvContent.layoutParams.apply {
                            height = 310.dp
                            nsvContent.layoutParams = this
                        }
                        rvChild.layoutManager =
                            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                        rvChild.adapter = WithdrawLogDetailAdapter(it)
                    }
                }
            }
        }
    }
}