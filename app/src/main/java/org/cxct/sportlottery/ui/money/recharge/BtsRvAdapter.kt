package org.cxct.sportlottery.ui.money.recharge

import android.view.View
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemListviewBankCardTickBinding

class BtsRvAdapter(private val dataList: MutableList<SelectBank>,private val clickListener: BankAdapterListener)
    : BindingAdapter<BtsRvAdapter.SelectBank,ItemListviewBankCardTickBinding>(){

    init {
        setList(dataList)
    }
    private var selectedPosition = 0

    override fun onBinding(
        position: Int,
        binding: ItemListviewBankCardTickBinding,
        item: SelectBank,
    ) =binding.run{
        if (item.bankIcon != null){
            ivBankIcon.setImageResource(item.bankIcon ?: 0)
            ivBankIcon.visible()
        } else{
            ivBankIcon.setImageResource(android.R.color.transparent)
            ivBankIcon.gone()
        }
        tvBankCard.text = item.bankName ?: ""
        if (position == selectedPosition){
            llSelectBankCard.setBackgroundColor(ContextCompat.getColor(context, R.color.color_E8EFFD))
            imgTick.visibility = View.VISIBLE
        }
        else{
            llSelectBankCard.setBackgroundColor(ContextCompat.getColor(context, R.color.color_bbbbbb_ffffff))
            imgTick.visibility = View.INVISIBLE
        }

        llSelectBankCard.setOnClickListener {
            if (selectedPosition != position) {
                selectedPosition = position
                notifyDataSetChanged()
                clickListener.onClick(item,position)
            }
        }
    }

    data class SelectBank(var bankName: String?, var bankIcon: Int?)

    class BankAdapterListener(val listener: (bankCard: SelectBank, position: Int) -> Unit) {
        fun onClick(bankCard: SelectBank, position: Int) =
            listener(bankCard, position)
    }


}