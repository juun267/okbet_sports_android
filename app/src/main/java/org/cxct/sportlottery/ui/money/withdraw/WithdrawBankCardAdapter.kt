package org.cxct.sportlottery.ui.money.withdraw

import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemListviewBankCardBinding
import org.cxct.sportlottery.network.bank.my.BankCardList
import org.cxct.sportlottery.network.money.config.TransferType
import org.cxct.sportlottery.util.MoneyManager.getBankIconByBankName
import org.cxct.sportlottery.util.MoneyManager.getCryptoIconByCryptoName

class WithdrawBankCardAdapter(
    private val dataList: MutableList<BankCardList>,
    private val listener: BankCardAdapterListener
    ) : BindingAdapter<BankCardList, ItemListviewBankCardBinding>(){

    init {
        setList(dataList)
    }
    private var selectedPosition = 0

    fun initSelectStatus() {
        //初始化選中狀態
        dataList.forEach { bankCard ->
            bankCard.isSelected = false
        }
        dataList.firstOrNull()?.isSelected = true

        selectedPosition = 0
        listener.onClick(dataList[0])
        notifyDataSetChanged()
    }

    override fun onBinding(
        position: Int,
        binding: ItemListviewBankCardBinding,
        item: BankCardList,
    ) = binding.run{
        root.setOnClickListener {
            selectBankCard(position)
            listener.onClick(item)
            notifyDataSetChanged()
        }
        checkBank.setOnClickListener {
            selectBankCard(position)
            listener.onClick(item)
            notifyDataSetChanged()
        }

        tvNumber.text = "(${context.getString(R.string.tail_number)}${item.cardNo})"
        tvBankCard.text = item.bankName
        ivBankIcon.setImageResource(
            when (item.transferType) {
                TransferType.BANK -> getBankIconByBankName(item.bankName)
                TransferType.CRYPTO -> getCryptoIconByCryptoName(item.transferType.type)
                TransferType.E_WALLET -> getBankIconByBankName(item.bankName)
                TransferType.STATION -> getBankIconByBankName(item.bankName)
                TransferType.PAYMAYA -> getBankIconByBankName(item.bankName)
            }
        )
        checkBank.isChecked = selectedPosition == position
        linMaintenance.isVisible  = item.maintainStatus==1
    }



    private fun selectBankCard(bankPosition: Int) {
        dataList[selectedPosition].isSelected = false
        notifyItemChanged(selectedPosition)
        selectedPosition = bankPosition
        dataList[bankPosition].isSelected = true
        notifyItemChanged(bankPosition)
    }


}


class BankCardAdapterListener(val listener: (bankCard: BankCardList) -> Unit) {
    fun onClick(bankCard: BankCardList) = listener(bankCard)
}