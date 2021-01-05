package org.cxct.sportlottery.ui.odds

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.detail.Odd
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

        private val tvName = itemView.findViewById<TextView>(R.id.tv_name)
        private val tvOdds = itemView.findViewById<TextView>(R.id.tv_odds)
        private val tvSpread = itemView.findViewById<TextView>(R.id.tv_spread)
        private val rlContent = itemView.findViewById<RelativeLayout>(R.id.rl_content)

        fun bindModel(odd: Odd, position: Int) {

            tvName.text = odd.name
            tvOdds.text = odd.odds.toString()
            tvSpread.text = odd.spread

            tvOdds.isSelected = odd.isSelect

            tvOdds.setOnClickListener {
                tvOdds.isSelected = !tvOdds.isSelected
                odd.isSelect = tvOdds.isSelected

                //TODO 添加至投注單

            }

            val rlParams: RelativeLayout.LayoutParams = rlContent.layoutParams as RelativeLayout.LayoutParams

            if (position % 2 != 0) {

                if(position != oddsList.size - 1){
                    val params: RecyclerView.LayoutParams = itemView.layoutParams as RecyclerView.LayoutParams
                    params.setMargins(0, 0, 0, 2)
                    itemView.layoutParams = params
                }

                rlParams.setMargins(0, 5.dp, 0, 10.dp)
            } else {
                rlParams.setMargins(0, 10.dp, 0, 5.dp)
            }
            rlContent.layoutParams = rlParams

        }
    }


}