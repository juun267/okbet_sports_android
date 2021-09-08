package org.cxct.sportlottery.ui.game.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_odd_btn_eps.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.Odd

class OddButtonEpsAdapter : RecyclerView.Adapter<OddButtonEpsViewHolder>() {

    var data: List<Odd?> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val oddStateRefreshListener by lazy {
        object : OddStateViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {}
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OddButtonEpsViewHolder {
        return OddButtonEpsViewHolder.from(parent, oddStateRefreshListener)
    }

    override fun onBindViewHolder(holder: OddButtonEpsViewHolder, position: Int) {
        data[position]?.let {
            holder.bind(it)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}

class OddButtonEpsViewHolder private constructor(
    itemView: View,
    override val oddStateChangeListener: OddStateChangeListener
) : OddStateViewHolder(itemView) {

    fun bind(odd: Odd) {
        itemView.quick_odd_eps_text1.apply {
            text = odd.name ?: ""
        }

        itemView.quick_odd_eps_btn1.apply {
            setOddsValue(odd.extInfo ?: "", odd.odds.toString())
            setFlag()
        }
    }

    companion object {
        fun from(
            parent: ViewGroup,
            oddStateRefreshListener: OddStateChangeListener
        ): OddButtonEpsViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.itemview_odd_btn_eps, parent, false)

            return OddButtonEpsViewHolder(view, oddStateRefreshListener)
        }
    }
}