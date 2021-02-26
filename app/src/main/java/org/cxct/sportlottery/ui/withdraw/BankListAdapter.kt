package org.cxct.sportlottery.ui.withdraw

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_rv_bank_list_edit.view.*
import kotlinx.android.synthetic.main.content_rv_bank_list_new.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bank.my.BankCardList
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.MoneyManager
import org.cxct.sportlottery.util.TimeUtil.stampToDateHMS

class BankListAdapter(private val mBankListClickListener: BankListClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class CardType { EDIT, ADD }

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
            CardType.ADD.ordinal -> {
                LastViewHolder.from(parent)
            }
            else -> {
                ItemViewHolder.from(parent)
            }
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
        return if (position == bankList.size && addSwitch) {
            CardType.ADD.ordinal
        } else {
            CardType.EDIT.ordinal
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                holder.bind(bankList[position], fullName, mBankListClickListener)
            }
            is LastViewHolder -> {
                holder.bind(mBankListClickListener)
            }
        }
    }

    class ItemViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
                            mBankListClickListener.onEdit(data)
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
                return ItemViewHolder(view)
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
}

class BankListClickListener(private val editListener: (item: BankCardList) -> Unit, private val addListener: () -> Unit) {
    fun onEdit(item: BankCardList) = editListener(item)
    fun onAdd() = addListener()
}