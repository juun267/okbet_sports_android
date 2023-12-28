package org.cxct.sportlottery.ui.money.withdraw

import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemMoneyGetCommissionBinding
import org.cxct.sportlottery.network.withdraw.uwcheck.CheckList
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.setMoneyFormat

class CommissionDetailAdapter: BindingAdapter<CheckList,ItemMoneyGetCommissionBinding>() {

    override fun onBinding(position: Int, binding: ItemMoneyGetCommissionBinding, item: CheckList)=binding.run {
        val zero = 0.0
        tvId.text = item.orderNo
        tvDeductMoney.setMoneyFormat(zero.minus(item.deductMoney ?: 0))
        tvTime.text = TimeUtil.stampToDateHMS(item.addTime?.toLong() ?: 0)

        tvRequiredValidBetsMoney.apply {
            setMoneyFormat(item.validCheckAmount?.toLong()?.toDouble() ?: 0.0)
            setTextColor(ContextCompat.getColor(context, if(item.validCheckAmount ?: 0.0 < 0.0) R.color.color_E44438_e44438 else R.color.color_BBBBBB_333333))
        }

        tvSuccessedBetsMoney.apply {
            setMoneyFormat(item.finishValidAmount?.toLong()?.toDouble() ?: 0.0)
            setTextColor(ContextCompat.getColor(context, if(item.finishValidAmount ?: 0.0 < 0.0) R.color.color_E44438_e44438 else R.color.color_BBBBBB_333333))
        }

        tvDeductMoney.apply {
            setMoneyFormat(zero.minus(item.deductMoney ?: 0))
            setTextColor(ContextCompat.getColor(context, if(zero.minus(item.deductMoney ?: 0) < 0.0) R.color.color_E44438_e44438 else R.color.color_BBBBBB_333333))
        }

        when(item.isPass){
            1 ->{
                tvCheckStatus.apply {
                    text = this.context.getString(R.string.commissiom_completed)
                    setTextColor(ContextCompat.getColor(context, R.color.color_08dc6e_08dc6e))
                }
            }
            else ->{
                tvCheckStatus.apply {
                    text = this.context.getString(R.string.commission_not_completed)
                    setTextColor(ContextCompat.getColor(context, R.color.color_E44438_e44438))
                }
            }
        }

        sConfigData?.systemCurrencySign.let {
            tvVnd.text = it
            tvRequiredValidBetsVnd.text = it
            tvSuccessedBetsVnd.text = it
        }
    }

}