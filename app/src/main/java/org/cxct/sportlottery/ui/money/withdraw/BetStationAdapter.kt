package org.cxct.sportlottery.ui.money.withdraw

import android.view.View
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemBetStationBinding
import org.cxct.sportlottery.network.bettingStation.BettingStation

class BetStationAdapter(val onSelect: (BettingStation)->Unit): BindingAdapter<BettingStation, ItemBetStationBinding>() {
    private var selectedPosition = 0
    override fun onBinding(position: Int, binding: ItemBetStationBinding, item: BettingStation)=binding.run {
        root.setOnClickListener {
            selectBank(position)
            onSelect.invoke(item)
        }
        tvName.isSelected =  selectedPosition == position
        tvNameNum.isSelected =  selectedPosition == position

        tvName.text = item.name
        tvNameNum.text = context.getString(R.string.outlet)+"${position+1}"
        if (item.isSelected) {
            selectedPosition = position
            imgCheck.visibility = View.VISIBLE
            llSelectBankCard.setBackgroundResource(R.drawable.ic_bule_site)
        } else {
            imgCheck.visibility = View.GONE
            llSelectBankCard.setBackgroundResource(R.drawable.ic_white_site)

        }
    }
    fun setData(newData: List<BettingStation>) {
        setList(newData)
        selectedPosition = 0
        if (newData.isNotEmpty()) {
            newData[selectedPosition].isSelected = true
            onSelect.invoke(newData[0])
        }
        notifyDataSetChanged()
    }

    private fun selectBank(bankPosition: Int) {
        data[selectedPosition].isSelected = false
        notifyItemChanged(selectedPosition)
        selectedPosition = bankPosition
        data[bankPosition].isSelected = true
        notifyItemChanged(bankPosition)
    }

}