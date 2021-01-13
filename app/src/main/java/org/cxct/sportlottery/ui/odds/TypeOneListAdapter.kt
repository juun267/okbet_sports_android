package org.cxct.sportlottery.ui.odds

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.detail.Odd

class TypeOneListAdapter(private val oddsList: List<Odd>, private val onOddClickListener: OnOddClickListener) : RecyclerView.Adapter<TypeOneListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_fg_lg_item, parent, false))
    }


    override fun getItemCount(): Int {
        return oddsList.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindModel(oddsList[position])
    }


    inner class ViewHolder(view: View) : OddViewHolder(view) {

        fun bindModel(odd: Odd) {
            setData(odd, onOddClickListener)
        }
    }


}