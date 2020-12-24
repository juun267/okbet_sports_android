package org.cxct.sportlottery.ui.odds

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.util.DisplayUtil.dp


class TypeHDPAdapter(private val oddsList: List<Odd>) :
    RecyclerView.Adapter<TypeHDPAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_hdp_item, parent, false))
    }


    override fun getItemCount(): Int {
        return oddsList.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindModel(oddsList[position], position)
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tv_name = itemView.findViewById<TextView>(R.id.tv_name)
        private val tv_odds = itemView.findViewById<TextView>(R.id.tv_odds)
        private val tv_spread = itemView.findViewById<TextView>(R.id.tv_spread)
        private val rl_content = itemView.findViewById<RelativeLayout>(R.id.rl_content)

        fun bindModel(odd: Odd, position: Int) {

            tv_name.text = odd.name
            tv_odds.text = odd.odds.toString()
            tv_spread.text = odd.spread

            tv_odds.isSelected = odd.isSelect

            tv_odds.setOnClickListener {
                tv_odds.isSelected = !tv_odds.isSelected
                odd.isSelect = tv_odds.isSelected

                //TODO 添加至投注單

            }

            val rl_params: RelativeLayout.LayoutParams = rl_content.layoutParams as RelativeLayout.LayoutParams

            if (position % 2 != 0 && position != oddsList.size - 1) {
                val params: RecyclerView.LayoutParams = itemView.layoutParams as RecyclerView.LayoutParams
                params.setMargins(0, 0, 0, 2)
                itemView.layoutParams = params
                rl_params.setMargins(0, 5.dp, 0, 10.dp)
            } else {
                rl_params.setMargins(0, 10.dp, 0, 5.dp)
            }
            rl_content.layoutParams = rl_params

        }
    }


}