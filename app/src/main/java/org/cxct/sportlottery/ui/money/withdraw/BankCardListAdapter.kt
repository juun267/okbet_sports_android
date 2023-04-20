package org.cxct.sportlottery.ui.money.withdraw

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_rv_bank_card_list.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bank.my.BankCardList
import org.cxct.sportlottery.network.money.config.MoneyRechCfg
import org.cxct.sportlottery.network.money.config.MoneyRechCfgData
import org.cxct.sportlottery.network.money.config.TransferType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.MoneyManager

class BankCardListAdapter(private val mBankCardListClickListener: BankCardListClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class CardType { EDIT, CRYPTO_EDIT }

    var bankList = listOf<BankCardList>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }



    var moneyConfig: MoneyRechCfgData? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return BankItemViewHolder.from(parent)

    }

    override fun getItemCount(): Int {
        return bankList.size

    }

    override fun getItemViewType(position: Int): Int {
        return when (bankList[position].transferType) {
                     TransferType.BANK -> CardType.EDIT.ordinal
                    TransferType.CRYPTO -> CardType.CRYPTO_EDIT.ordinal
                    TransferType.E_WALLET -> CardType.EDIT.ordinal
            TransferType.PAYMAYA -> CardType.EDIT.ordinal
            TransferType.STATION -> CardType.EDIT.ordinal
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BankItemViewHolder -> {
                holder.bind(bankList[position], moneyConfig, mBankCardListClickListener)
            }
            else ->{
                //do nothing
            }
        }
    }

    class BankItemViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: BankCardList, moneyConfig: MoneyRechCfgData?, mBankCardListClickListener: BankCardListClickListener) {
            val bankOpen = moneyConfig?.uwTypes?.find { it.type == TransferType.BANK.type }?.open == MoneyRechCfg.Switch.ON.code
            val cryptoOpen = moneyConfig?.uwTypes?.find { it.type == TransferType.CRYPTO.type }?.open == MoneyRechCfg.Switch.ON.code
            itemView.apply {
                if (data.transferType == TransferType.CRYPTO) {
                    iv_bank_icon.setImageResource(MoneyManager.getCryptoIconByCryptoName(data.bankName))//虚拟币图标
                }else{
                    iv_bank_icon.setImageResource(MoneyManager.getBankIconByBankName(data.bankName)) //银行卡图标
                }

                tv_bank_name.text = data.bankName //银行名字
                tv_tail_number.text =
                    if (data.cardNo.length > 4) data.cardNo.substring(data.cardNo.length - 4) else  data.cardNo //尾號四碼
                bank_card_type.text =  when(data.transferType.type){
                    TransferType.BANK.type -> LocalUtils.getString(R.string.bank_card)
                    TransferType.CRYPTO.type -> LocalUtils.getString(R.string.crypto)
                    TransferType.E_WALLET.type -> LocalUtils.getString(R.string.ewallet)
                    TransferType.PAYMAYA.type -> context.getString(R.string.ewallet)
                    else -> LocalUtils.getString(R.string.bank_card)
                }
                when(data.transferType){
                    TransferType.CRYPTO ->

                        if (sConfigData?.enableModifyBank == "1" && cryptoOpen) {
                            rl_item_content.apply {
                                setOnClickListener {
                                    mBankCardListClickListener.onCryptoEdit(data)
                                }
                            }
                        }
                    else -> {
                        if (sConfigData?.enableModifyBank == "1" && bankOpen) {
                            rl_item_content.apply {
                                setOnClickListener {
                                    mBankCardListClickListener.onBankEdit(data)
                                }
                            }
                        }
                    }
                }

            }
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.content_rv_bank_card_list, parent, false)
                return BankItemViewHolder(view)
            }
        }
    }


}

class BankCardListClickListener(
    private val editBankListener: (bankCard: BankCardList) -> Unit,
    private val editCryptoListener: (cryptoCard: BankCardList) -> Unit,

) {
    fun onBankEdit(bankCard: BankCardList) = editBankListener(bankCard)
    fun onCryptoEdit(cryptoCard: BankCardList) = editCryptoListener(cryptoCard)

}