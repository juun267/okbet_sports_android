package org.cxct.sportlottery.ui.game.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.Odd

class OddButtonPairAdapter : RecyclerView.Adapter<OddButtonPairViewHolder>() {

    var odds: List<Odd?> = listOf()
        set(value) {
            field = value

            data = field.withIndex().groupBy {
                it.index / 2
            }
        }

    private var data: Map<Int, List<IndexedValue<Odd?>>> = mapOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val oddStateRefreshListener by lazy {
        object : OddStateViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {}
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OddButtonPairViewHolder {
        return OddButtonPairViewHolder.from(parent, oddStateRefreshListener)
    }

    override fun onBindViewHolder(holder: OddButtonPairViewHolder, position: Int) {
    }

    override fun getItemCount(): Int {
        return data.size
    }
}

class OddButtonPairViewHolder private constructor(
    itemView: View,
    override val oddStateChangeListener: OddStateChangeListener
) : OddStateViewHolder(itemView) {

    companion object {
        fun from(
            parent: ViewGroup,
            oddStateRefreshListener: OddStateChangeListener
        ): OddButtonPairViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.itemview_odd_btn_pair, parent, false)

            return OddButtonPairViewHolder(view, oddStateRefreshListener)
        }
    }
}