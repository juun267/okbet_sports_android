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
import org.cxct.sportlottery.network.money.TransferType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.MoneyManager
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

    var addSwitch = true
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
        return if (addSwitch) {
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
            position == bankList.size && addSwitch -> {
                CardType.ADD.ordinal
            }
            else -> {
                when (bankList[position].transferType) {
                    TransferType.BANK -> CardType.EDIT.ordinal
                    TransferType.CRYPTO -> CardType.CRYPTO_EDIT.ordinal
                }
            }
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BankItemViewHolder -> {
                holder.bind(bankList[position], fullName, mBankListClickListener)
            }
            is CryptoItemViewHolder -> {
                holder.bind(bankList[position], mBankListClickListener)
            }
            is LastViewHolder -> {
                holder.bind(mBankListClickListener)
            }
        }
    }

    class BankItemViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: BankCardList, fullName: String, mBankListClickListener: BankListClickListener) {
            itemView.apply {
                iv_bank_icon.setImageResource(MoneyManager.getBankIconByBankName(data.bankName))
                tv_bank_name.text = data.bankName
                tv_name.text = fullName
                tv_tail_number.text = data.cardNo.substring(data.cardNo.length - 4) //尾號四碼
                tv_bind_time.text = stampToDateHMS(data.addTime.toLong())
                if (sConfigData?.enableModifyBank == "1") {
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
        fun bind(data: BankCardList, mBankListClickListener: BankListClickListener) {
            itemView.apply {
                iv_bank_icon.setImageResource(MoneyManager.getBankIconByBankName(data.bankName))
                tv_bank_name.text = data.bankName
                tv_tail_number.text = data.cardNo.substring(data.cardNo.length - 4) //尾號四碼
                tv_bind_time.text = stampToDateHMS(data.addTime.toLong())
                if (sConfigData?.enableModifyBank == "1") {
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
        fun bind(mBankListClickListener: BankListClickListener) {
            itemView.cv_add.setOnClickListener {
                mBankListClickListener.onAdd()
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
        fun bind(mBankListClickListener: BankListClickListener) {
            itemView.cv_add_no_card.setOnClickListener {
                mBankListClickListener.onAdd()
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

class BankListClickListener(private val editBankListener: (bankCard: BankCardList) -> Unit, private val editCryptoListener: (cryptoCard: BankCardList) -> Unit, private val addListener: () -> Unit) {
    fun onBankEdit(bankCard: BankCardList) = editBankListener(bankCard)
    fun onCryptoEdit(cryptoCard: BankCardList) = editCryptoListener(cryptoCard)
    fun onAdd() = addListener()
}