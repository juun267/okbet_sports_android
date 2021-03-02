package org.cxct.sportlottery.ui.money.recharge

import android.content.Context
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.CustomImageAdapter
import org.cxct.sportlottery.ui.withdraw.ListViewHolder

class ThirdTypeAdapter(private val context: Context, dataList: MutableList<CustomImageAdapter.SelectBank>, listener: BankAdapterListener):BankBtsAdapter(context,dataList,listener) {
    override fun setView(
        holder: ListViewHolder,
        data: CustomImageAdapter.SelectBank,
        position: Int,
        listener: BankAdapterListener
    ) {
        holder.apply {
            tvBank?.text = data.bankName
            data.bankIcon?.let { ivBankIcon?.setImageResource(it) }
            if (position == selectedPosition)
                this.llSelectBankCard?.setBackgroundColor(ContextCompat.getColor(context, R.color.blue2))
            else
                llSelectBankCard?.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            llSelectBankCard?.setOnClickListener {
                if (selectedPosition != position) {
                    selectedPosition = position
                    notifyDataSetChanged()
                    listener.onClick(data,position)
                }
            }
        }
    }
}