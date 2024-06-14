package org.cxct.sportlottery.ui.finance

import android.widget.TextView
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ViewItemRechargeLogBinding
import org.cxct.sportlottery.network.money.list.Row
import org.cxct.sportlottery.ui.finance.df.RechType
import org.cxct.sportlottery.ui.finance.df.Status
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.setRecordStatus

class RechargeLogAdapter: BindingAdapter<Row, ViewItemRechargeLogBinding>() {

    override fun onBinding(position: Int, binding: ViewItemRechargeLogBinding, item: Row) = binding.run {

        rechLogDate.text = item.rechDateStr
        rechLogTime.text = item.rechTimeStr
        rechLogAmount.text = TextUtil.formatMoney(item.rechMoney,0)
        rechLogState.setRecordStatus(item.status)
        setupStateTextColor(rechLogState, item)
        rechLogType.text = when (item.rechType) {
            RechType.ONLINE_PAYMENT.type -> context.getString(R.string.recharge_channel_online)
            RechType.ADMIN_ADD_MONEY.type -> context.getString(R.string.recharge_channel_admin)
            RechType.CFT.type -> context.getString(R.string.recharge_channel_cft)
            RechType.WEIXIN.type -> context.getString(R.string.recharge_channel_weixin)
            RechType.ALIPAY.type -> context.getString(R.string.recharge_channel_alipay)
            RechType.BANK_TRANSFER.type -> context.getString(R.string.recharge_channel_bank)
            RechType.CRYPTO.type -> context.getString(R.string.recharge_channel_crypto)
            RechType.GCASH.type -> context.getString(R.string.recharge_channel_gcash)
            RechType.GRABPAY.type -> context.getString(R.string.recharge_channel_grabpay)
            RechType.PAYMAYA.type -> context.getString(R.string.recharge_channel_paymaya)
            RechType.BETTING_STATION.type -> context.getString(R.string.betting_station_deposit)
            RechType.BETTING_STATION_AGENT.type -> context.getString(R.string.P183)
            RechType.ACTIVITY.type -> context.getString(R.string.text_account_history_activity)
            RechType.REDEMTIONCODE.type -> context.getString(R.string.P216)
            else -> ""
        }

    }

    private fun setupStateTextColor(rechLogState: TextView, item: Row) {
        when (item.status) {
            Status.SUCCESS.code -> {
                rechLogState.setTextColor(ContextCompat.getColor(context, R.color.color_08dc6e_08dc6e))
            }

            Status.FAILED.code -> {
                rechLogState.setTextColor(ContextCompat.getColor(context, R.color.color_E44438_e44438))
            }

            else -> {
                rechLogState.setTextColor(ContextCompat.getColor(context, R.color.color_909090_666666))
            }
        }

    }

}