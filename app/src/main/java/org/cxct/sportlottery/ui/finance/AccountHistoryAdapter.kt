package org.cxct.sportlottery.ui.finance

import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ViewItemAccountHistoryBinding
import org.cxct.sportlottery.network.money.list.SportBillResult
import org.cxct.sportlottery.ui.finance.df.AccountHistory
import org.cxct.sportlottery.util.TextUtil

class AccountHistoryAdapter : BindingAdapter<SportBillResult.Row,ViewItemAccountHistoryBinding>() {

    override fun onBinding(
        position: Int,
        binding: ViewItemAccountHistoryBinding,
        item: SportBillResult.Row,
    )=binding.run {
        rechLogDate.text = item.rechDateStr
        rechLogTime.text = item.rechTimeStr
        rechOrderNum.text = item.orderNo
        tvTypeName.text = context.getString(AccountHistory.getShowName(item.tranTypeName))
        rechBalance.text = TextUtil.format(item.balance)
        if(item.money<0){
            rechAmont.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.color_E44438_e44438
                )
            )
            rechAmont.text =TextUtil.format(item.money)
        }else if(item.money>0){
            rechAmont.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.color_08dc6e_08dc6e
                )
            )
            rechAmont.text = "+"+TextUtil.format(item.money)
        }else{
            rechAmont.text = TextUtil.format(item.money)
        }
    }

}
