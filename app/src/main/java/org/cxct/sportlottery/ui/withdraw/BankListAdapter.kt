package org.cxct.sportlottery.ui.withdraw

import android.service.autofill.UserData
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_rv_bank_list_edit.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bank.T

class BankListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class CardType{EDIT, ADD}

    var bankList = listOf<T>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            CardType.ADD.ordinal -> {
                LastViewHolder.from(parent)
            }
            else -> {
                ItemViewHolder.from(parent)
            }
        }
    }

    override fun getItemCount(): Int {
        return bankList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == bankList.size){
            CardType.ADD.ordinal
        }else{
            CardType.EDIT.ordinal
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                holder.bind(bankList[position])
            }
        }
    }

    class ItemViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: T) {
            itemView.apply {
                iv_bank_icon.setImageResource(R.drawable.ic_bank_default) //TODO Dean : 待匯入銀行icon後對key做mapping
                tv_bank_name.text = data.bankName
                tv_bank_number.text = "" //TODO Dean : 與用戶真實姓名相等, Api沒有回傳,根據用戶id或直接抓取自身真實姓名做顯示
                tv_tail_number.text = data.cardNo.substring(data.cardNo.length - 4) //尾號四碼
                tv_bind_time.text = data.addTime
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

    class LastViewHolder private constructor(itemView: View): RecyclerView.ViewHolder(itemView){
        companion object{
            fun from(parent: ViewGroup): LastViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.content_rv_bank_list_new, parent, false)
                return LastViewHolder(view)
            }
        }
    }
}