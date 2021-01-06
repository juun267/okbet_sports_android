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

class TypeTwoSidesAdapter(private val oddsList: List<Odd>) : RecyclerView.Adapter<TypeTwoSidesAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_ou_item, parent, false))
    }


    override fun getItemCount(): Int {
        return oddsList.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindModel(oddsList[position], position)
    }


    inner class ViewHolder(view: View) : OddViewHolder(view) {

        private val tvSpread = itemView.findViewById<TextView>(R.id.tv_spread)
        private val rlContent = itemView.findViewById<RelativeLayout>(R.id.rl_content)

        fun bindModel(odd: Odd, position: Int) {

            setData(odd)

            tvSpread.text = odd.spread

            val rlParams: RelativeLayout.LayoutParams = rlContent.layoutParams as RelativeLayout.LayoutParams

            if (position % 2 != 0) {
                val params: RecyclerView.LayoutParams = itemView.layoutParams as RecyclerView.LayoutParams
                params.setMargins(2, 0, 0, 0)
                itemView.layoutParams = params
            }

            rlParams.setMargins(0, 10.dp, 0, if (position == oddsList.size - 1 || position == oddsList.size - 2) 10.dp else 0)

            rlContent.layoutParams = rlParams

        }
    }


}