package org.cxct.sportlottery.ui.game.home

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_sport_type_list.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeListener

/**
 * 賽事精選的選單(首頁)
 */
class HighlightGameTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var saveInstanceState: Parcelable? = null
    val mGameTypeAdapter = GameTypeAdapter()

    fun bind(gameType: HomeListAdapter.HighlightGameTypeItemData, gameTypeListener: GameTypeListener?) {
        itemView.apply {
            mGameTypeAdapter.dataSport = gameType.dataSport
            rvSportType.adapter = mGameTypeAdapter
            //rvSportType.layoutManager?.onRestoreInstanceState(saveInstanceState)
            mGameTypeAdapter.gameTypeListener = gameTypeListener
            mGameTypeAdapter.isFromHome = true
        }
    }

    companion object {
        fun from(parent: ViewGroup): HighlightGameTypeViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater
                .inflate(R.layout.itemview_sport_type_list, parent, false)

            return HighlightGameTypeViewHolder(view)
        }
    }
}