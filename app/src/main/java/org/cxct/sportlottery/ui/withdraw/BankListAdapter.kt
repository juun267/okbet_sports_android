package org.cxct.sportlottery.ui.withdraw

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_rv_bank_list_edit.view.*
import kotlinx.android.synthetic.main.content_rv_bank_list_new.view.*
import kotlinx.android.synthetic.main.content_rv_bank_list_new_no_card.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bank.my.BankCardList
import org.cxct.sportlottery.network.money.config.MoneyRechCfg
import org.cxct.sportlottery.network.money.config.MoneyRechCfgData
import org.cxct.sportlottery.network.money.config.TransferType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.MoneyManager
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil.stampToDateHMS

class BankListAdapter(private val mBankListClickListener: BankListClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class CardType { EDIT, CRYPTO_EDIT, ADD, NO_CARD_ADD }

    var bankList = listOf<BankCardList>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var fullName = ""
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var moneyConfig: MoneyRechCfgData? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var transferAddSwitch = TransferTypeAddSwitch(bankTransfer = false, cryptoTransfer = false, walletTransfer = false,)//payMaya = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            CardType.NO_CARD_ADD.ordinal -> {
                NoCardAddViewHolder.from(parent)
            }
            CardType.ADD.ordinal -> {
                LastViewHolder.from(parent)
            }
            CardType.CRYPTO_EDIT.ordinal -> {
                CryptoItemViewHolder.from(parent)
            }
            else -> BankItemViewHolder.from(parent)
        }
    }

    override fun getItemCount(): Int {
        return if (transferAddSwitch.bankTransfer || transferAddSwitch.cryptoTransfer || transferAddSwitch.walletTransfer) {
            bankList.size + 1
        } else {
            bankList.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            bankList.isEmpty() -> {
                CardType.NO_CARD_ADD.ordinal
            }
            position == bankList.size && (transferAddSwitch.bankTransfer || transferAddSwitch.cryptoTransfer || transferAddSwitch.walletTransfer) -> {
                CardType.ADD.ordinal
            }
            else -> {
                when (bankList[position].transferType) {
                    TransferType.BANK -> CardType.EDIT.ordinal
                    TransferType.CRYPTO -> CardType.CRYPTO_EDIT.ordinal
                    TransferType.E_WALLET -> CardType.EDIT.ordinal
                    TransferType.PAYMAYA -> CardType.EDIT.ordinal
                    TransferType.STATION -> CardType.EDIT.ordinal
                }
            }
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BankItemViewHolder -> {
                holder.bind(bankList[position], fullName, moneyConfig, mBankListClickListener)
            }
            is CryptoItemViewHolder -> {
                holder.bind(bankList[position], moneyConfig, mBankListClickListener)
            }
            is LastViewHolder -> {
                holder.bind(transferAddSwitch, mBankListClickListener)
            }
            is NoCardAddViewHolder -> {
                holder.bind(transferAddSwitch, mBankListClickListener)
            }
        }
    }

    class BankItemViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: BankCardList, fullName: String, moneyConfig: MoneyRechCfgData?, mBankListClickListener: BankListClickListener) {
            val bankOpen = moneyConfig?.uwTypes?.find { it.type == TransferType.BANK.type }?.open == MoneyRechCfg.Switch.ON.code

            itemView.apply {
                iv_bank_icon.setImageResource(MoneyManager.getBankIconByBankName(data.bankName))
                tv_bank_name.text = data.bankName
                tv_name.text = TextUtil.maskFullName(fullName)
                tv_tail_number.text =
                    if (data.cardNo.length > 4) data.cardNo.substring(data.cardNo.length - 4) else data.cardNo //尾號四碼
                tv_bind_time.text = stampToDateHMS(data.updateTime.toLong())
                if (sConfigData?.enableModifyBank == "1" && bankOpen) {
                    img_edit_bank.apply {
                        visibility = View.VISIBLE
                        setOnClickListener {
                            mBankListClickListener.onBankEdit(data)
                        }
                    }
                } else {
                    img_edit_bank.visibility = View.GONE
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.content_rv_bank_list_edit, parent, false)
                return BankItemViewHolder(view)
            }
        }
    }

    class CryptoItemViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: BankCardList, moneyConfig: MoneyRechCfgData?, mBankListClickListener: BankListClickListener) {
            val cryptoOpen = moneyConfig?.uwTypes?.find { it.type == TransferType.CRYPTO.type }?.open == MoneyRechCfg.Switch.ON.code

            itemView.apply {
                iv_bank_icon.setImageResource(MoneyManager.getCryptoIconByCryptoName(data.bankName))
                tv_bank_name.text = data.bankName
                tv_tail_number.text = if (data.cardNo.length > 4) data.cardNo.substring(data.cardNo.length - 4) else data.cardNo //尾號四碼
                tv_bind_time.text = stampToDateHMS(data.updateTime.toLong())
                if (sConfigData?.enableModifyBank == "1" && cryptoOpen) {
                    img_edit_bank.apply {
                        visibility = View.VISIBLE
                        setOnClickListener {
                            mBankListClickListener.onCryptoEdit(data)
                        }
                    }
                } else {
                    img_edit_bank.visibility = View.GONE
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.content_rv_bank_list_crypto_edit, parent, false)
                return CryptoItemViewHolder(view)
            }
        }
    }

    class LastViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(transferAddSwitch: TransferTypeAddSwitch, mBankListClickListener: BankListClickListener) {
            itemView.apply {

                tv_add_more_card.text = transferAddSwitch.run {
                    val stringList = arrayListOf<String>()
                    if(bankTransfer) stringList.add(context.getString(R.string.bank_list_bank))
                    if(cryptoTransfer) stringList.add(context.getString(R.string.bank_list_crypto))
                    if(walletTransfer) stringList.add(context.getString(R.string.bank_list_e_wallet))
                //    if(payMaya) stringList.add(context.getString(R.string.pay_maya))
                    context.getString(R.string.bank_list_add, stringList.joinToString("/"))
                }
                cv_add.setOnClickListener {
                    mBankListClickListener.onAdd(transferAddSwitch)
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): LastViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.content_rv_bank_list_new, parent, false)

                return LastViewHolder(view)
            }
        }
    }

    class NoCardAddViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(transferAddSwitch: TransferTypeAddSwitch, mBankListClickListener: BankListClickListener) {
            itemView.apply {
                tv_add_card.text = transferAddSwitch.run {
                    val stringList = arrayListOf<String>()
                    if(bankTransfer) stringList.add(context.getString(R.string.bank_list_bank))
                    if(cryptoTransfer) stringList.add(context.getString(R.string.bank_list_crypto))
                    if(walletTransfer) stringList.add(context.getString(R.string.bank_list_e_wallet))
                    context.getString(R.string.bank_list_add, stringList.joinToString("/"))
                }
            }
            itemView.cv_add_no_card.setOnClickListener {
                mBankListClickListener.onAdd(transferAddSwitch)
            }

        }

        companion object {
            fun from(parent: ViewGroup): NoCardAddViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.content_rv_bank_list_new_no_card, parent, false)

                return NoCardAddViewHolder(view)
            }
        }
    }
}

class BankListClickListener(
    private val editBankListener: (bankCard: BankCardList) -> Unit,
    private val editCryptoListener: (cryptoCard: BankCardList) -> Unit,
    private val addListener: (transferAddSwitch: TransferTypeAddSwitch) -> Unit
) {
    fun onBankEdit(bankCard: BankCardList) = editBankListener(bankCard)
    fun onCryptoEdit(cryptoCard: BankCardList) = editCryptoListener(cryptoCard)
    fun onAdd(transferAddSwitch: TransferTypeAddSwitch) = addListener(transferAddSwitch)
}