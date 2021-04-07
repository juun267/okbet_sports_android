package org.cxct.sportlottery.ui.money.recharge

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.item_listview_bank_card.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.CustomImageAdapter
import org.cxct.sportlottery.ui.withdraw.ListViewHolder
import org.cxct.sportlottery.util.MoneyManager

open class BankBtsAdapter(
    private val context: Context,
    private val dataList: MutableList<CustomImageAdapter.SelectBank>,
    private val listener: BankAdapterListener
) : BaseAdapter() {

    open var selectedPosition = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val data = dataList[position]
        val holder: ListViewHolder
        if (convertView == null) {
            holder = ListViewHolder()
            val layoutInflater = LayoutInflater.from(context)
            val view = layoutInflater.inflate(R.layout.item_listview_bank_card, parent, false)
            view.tag = holder

            view.apply {
                holder.ivBankIcon = iv_bank_icon
                holder.tvBank = tv_bank_card
                holder.llSelectBankCard = ll_select_bank_card
                setView(holder, data, position, listener)
            }
            return view
        } else {
            holder = convertView.tag as ListViewHolder
            setView(holder, data, position, listener)
        }
        return convertView
    }

    open fun setView(
        holder: ListViewHolder,
        data: CustomImageAdapter.SelectBank,
        position: Int,
        listener: BankAdapterListener
    ) {
        holder.apply {
            tvBank?.text = data.bankName

            if(data.bankIcon!=null){
                ivBankIcon?.setImageResource(data.bankIcon ?: 0)
            }else{
                ivBankIcon?.setImageResource(android.R.color.transparent)
            }

            if (position == selectedPosition)
                this.llSelectBankCard?.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.blue2
                    )
                )
            else
                llSelectBankCard?.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            llSelectBankCard?.setOnClickListener {
                if (selectedPosition != position) {
                    //                data.isSelected = !data.isSelected
                    selectedPosition = position
                    notifyDataSetChanged()
                    listener.onClick(data, position)
                }
            }
        }
    }

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    class BankAdapterListener(val listener: (bankCard: CustomImageAdapter.SelectBank, position: Int) -> Unit) {
        fun onClick(bankCard: CustomImageAdapter.SelectBank, position: Int) =
            listener(bankCard, position)
    }
}
