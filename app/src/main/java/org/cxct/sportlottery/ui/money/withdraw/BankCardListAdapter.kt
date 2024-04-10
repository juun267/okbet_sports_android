package org.cxct.sportlottery.ui.money.withdraw

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.clickDelay
import org.cxct.sportlottery.databinding.ContentRvBankCardListBinding
import org.cxct.sportlottery.network.bank.my.BankCardList
import org.cxct.sportlottery.network.money.config.MoneyRechCfg
import org.cxct.sportlottery.network.money.config.TransferType
import org.cxct.sportlottery.network.money.config.UwType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.MoneyManager

class BankCardListAdapter(private val onCryptoEdit: (BankCardList) -> Unit,
                          private val onBankEdit: (BankCardList) -> Unit,
                          private val onDelete: (BankCardList) -> Unit
): BindingAdapter<BankCardList, ContentRvBankCardListBinding>() {

    var uwTypes: List<UwType>? = null
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun onBinding(position: Int, binding: ContentRvBankCardListBinding, data: BankCardList) = binding.run {
        val bankOpen = uwTypes?.find { it.type == TransferType.BANK.type }?.open == MoneyRechCfg.Switch.OPEN.code
        val cryptoOpen = uwTypes?.find { it.type == TransferType.CRYPTO.type }?.open == MoneyRechCfg.Switch.OPEN.code

        if (data.transferType == TransferType.CRYPTO) {
            ivBankIcon.setImageResource(MoneyManager.getCryptoIconByCryptoName(data.bankName))//虚拟币图标
        } else {
            ivBankIcon.setImageResource(MoneyManager.getBankIconByBankName(data.bankName)) //银行卡图标
        }

        tvBankName.text = data.bankName //银行名字
        tvTailNumber.text = if (data.cardNo.length > 4) data.cardNo.substring(data.cardNo.length - 4) else data.cardNo //尾號四碼
        bankCardType.text = when (data.transferType.type) {
            TransferType.BANK.type -> context.getString(R.string.bank_card)
            TransferType.CRYPTO.type -> context.getString(R.string.crypto)
            TransferType.E_WALLET.type -> context.getString(R.string.ewallet)
            TransferType.PAYMAYA.type -> context.getString(R.string.ewallet)
            else -> context.getString(R.string.bank_card)
        }
        //
        if (data.transferType == TransferType.CRYPTO) {
            if (sConfigData?.enableModifyBank == "1" && cryptoOpen) {
                rlItemContent.clickDelay { onCryptoEdit(data) }
                ivDelete.clickDelay { onDelete(data) }
            } else {
                rlItemContent.setOnClickListener(null)
                ivDelete.setOnClickListener(null)
            }
        } else {
            if (sConfigData?.enableModifyBank == "1" && bankOpen) {
                rlItemContent.clickDelay { onBankEdit(data) }
                ivDelete.clickDelay { onDelete(data) }
            } else {
                rlItemContent.setOnClickListener(null)
                ivDelete.setOnClickListener(null)
            }
        }
    }

    fun removeCard(cardNo: String): BankCardList? {
        data.forEachIndexed { index, bankCardList ->
            if (bankCardList.id.toString() == cardNo) {
                removeAt(index)
                return bankCardList
            }
        }

        return null
    }

}
