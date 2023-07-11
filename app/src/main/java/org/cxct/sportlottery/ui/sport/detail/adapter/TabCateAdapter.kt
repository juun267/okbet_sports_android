package org.cxct.sportlottery.ui.sport.detail.adapter


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.tab_odds_detail.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.detail.PlayCateType
import org.cxct.sportlottery.util.*

@SuppressLint("NotifyDataSetChanged")
class TabCateAdapter(private val onItemSelectedListener: OnItemSelectedListener) :
    RecyclerView.Adapter<TabCateAdapter.TabCateViewHolder>() {

    var dataList: List<PlayCateType> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var selectedPosition: Int = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabCateViewHolder {
        return TabCateViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.tab_odds_detail, parent, false)
        )
    }

    override fun onBindViewHolder(holder: TabCateViewHolder, position: Int) {
        holder.bind(position, selectedPosition, onItemSelectedListener, dataList.getOrNull(position))
    }

    override fun getItemCount() = dataList.size

    class TabCateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(position: Int, selectedPosition: Int, listener: OnItemSelectedListener, data: PlayCateType?) {

//            itemView.img_icon.isVisible = data?.code == PlayCate.LCS.value
//            if(data?.code == PlayCate.LCS.value)
//                itemView.img_icon.setImageResource(R.drawable.selector_lcs_icon)
            val isSelected = position == selectedPosition
            itemView.tv_tab.isSelected = isSelected
            itemView.tv_tab.paint.isFakeBoldText = isSelected
            itemView.img_icon.isVisible = isSelected
            itemView.tv_tab.text = data?.name
            itemView.setOnClickListener {
                listener.onSelectedItem(position)
            }
        }
    }
}

class OnItemSelectedListener(
    val selectedItemListener: (position: Int) -> Unit
) {
    fun onSelectedItem(position: Int) = selectedItemListener(position)
}