package org.cxct.sportlottery.ui.odds

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.detail.Odd

class TypeFGLGAdapter(private val oddsList: List<Odd>) : RecyclerView.Adapter<TypeFGLGAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_fg_lg_item, parent, false))
    }


    override fun getItemCount(): Int {
        return oddsList.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindModel(oddsList[position])
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvName = itemView.findViewById<TextView>(R.id.tv_name)
        private val tvOdds = itemView.findViewById<TextView>(R.id.tv_odds)

        fun bindModel(odd: Odd) {

            tvName.text = odd.name
            tvOdds.text = odd.odds.toString()

            tvOdds.isSelected = odd.isSelect

            tvOdds.setOnClickListener {
                tvOdds.isSelected = !tvOdds.isSelected
                odd.isSelect = tvOdds.isSelected

                //TODO 添加至投注單

            }

        }
    }


}