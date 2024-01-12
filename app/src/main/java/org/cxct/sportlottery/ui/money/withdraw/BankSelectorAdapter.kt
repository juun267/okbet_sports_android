package org.cxct.sportlottery.ui.money.withdraw

import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemListviewBankCardBinding
import org.cxct.sportlottery.network.money.config.Bank
import org.cxct.sportlottery.util.MoneyManager

class BankSelectorAdapter(val onSelect: (Bank)->Unit) : BindingAdapter<Bank, ItemListviewBankCardBinding>() {

    private var selectedPosition = 0

    fun initSelectStatus() {
        //選中狀態初始化
        data.forEach { bank ->
            bank.isSelected = false
        }

        selectedPosition = 0

        data.firstOrNull()?.let {
            it.isSelected = true
            onSelect.invoke(it)
        }
        notifyDataSetChanged()
    }
    override fun onBinding(position: Int, binding: ItemListviewBankCardBinding, item: Bank)=binding.run {
        binding.root.setOnClickListener {
            selectBank(position)
            onSelect.invoke(item)
        }
        tvBankCard.text = item.name
        ivBankIcon.setImageResource(MoneyManager.getBankIconByBankName(item.name ?: ""))
        checkBank.isChecked = selectedPosition == position

        checkBank.setOnClickListener {
            selectBank(position)
            onSelect.invoke(item)
            notifyDataSetChanged()
        }

        if (item.isSelected) {
            selectedPosition = position
            checkBank.isChecked = true
        } else {
            checkBank.isChecked = false
        }
    }

    private fun selectBank(bankPosition: Int) {
        data[selectedPosition].isSelected = false
        notifyItemChanged(selectedPosition)
        selectedPosition = bankPosition
        data[bankPosition].isSelected = true
        notifyItemChanged(bankPosition)
    }
    fun getSelectedItem():Bank?{
        return if (selectedPosition>0) data[selectedPosition] else null
    }

}