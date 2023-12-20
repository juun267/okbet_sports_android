package org.cxct.sportlottery.ui.money.recharge

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_money_pay_type_rv.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.MoneyManager

class MoneyBankTypeAdapter(private val clickListener: ItemClickListener) :
    RecyclerView.Adapter<MoneyBankTypeAdapter.ViewHolder>() {

    private var mSelectedPosition = 0

    var data = mutableListOf<MoneyPayWayData>()
        set(value) {
            mSelectedPosition = 0
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
        holder.rootItem.isSelected = mSelectedPosition == position //選中改變背景
        holder.tvType.isSelected = mSelectedPosition == position
        holder.imgTri.visibility = if (mSelectedPosition == position) View.VISIBLE else View.GONE
        holder.rootItem.setOnClickListener {
            mSelectedPosition = position
            notifyDataSetChanged()
            clickListener.onClick(item)
        }
    }

    override fun getItemCount() = data.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icBank: ImageView = itemView.findViewById(R.id.ic_bank)
        val tvType: TextView = itemView.findViewById(R.id.tv_type)
        val rootItem: RelativeLayout = itemView.findViewById(R.id.rootItem)
        val imgTri: ImageView = itemView.findViewById(R.id.img_tri)

        fun bind(item: MoneyPayWayData) {
            icBank.setImageResource(MoneyManager.getBankIcon(item.image))
            tvType.text = item.titleNameMap[LanguageManager.getLanguageString()]
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.content_money_pay_type_rv, parent, false)
                return ViewHolder(view)
            }
        }
    }

    class ItemClickListener(private val clickListener: (moneyPayWayData: MoneyPayWayData) -> Unit) {
        fun onClick(moneyPayWayData: MoneyPayWayData) = clickListener(moneyPayWayData)
    }
}