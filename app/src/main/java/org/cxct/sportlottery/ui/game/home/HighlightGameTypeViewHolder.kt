package org.cxct.sportlottery.ui.game.home

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_sport_type_list.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.common.ScrollCenterLayoutManager
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeListener

/**
 * 賽事精選的選單(首頁)
 */
class HighlightGameTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var saveInstanceState: Parcelable? = null
    private var mGameTypeAdapter = GameTypeAdapter()

    fun bind(gameType: HomeListAdapter.HighlightGameTypeItemData, gameTypeListener: GameTypeListener?) {
        itemView.apply {
            mGameTypeAdapter.dataSport = gameType.dataSport
            rvSportType.layoutManager = ScrollCenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            rvSportType.adapter = mGameTypeAdapter
            //rvSportType.layoutManager?.onRestoreInstanceState(saveInstanceState)
            mGameTypeAdapter.gameTypeListener = gameTypeListener
            mGameTypeAdapter.isFromHome = true
        }
    }

    fun update (gameTypeItemData: HomeListAdapter.HighlightGameTypeItemData) {
        mGameTypeAdapter.dataSport = gameTypeItemData.dataSport
        with(itemView) {
            (rvSportType.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(rvSportType, RecyclerView.State(), gameTypeItemData.dataSport.indexOfFirst { it.isSelected })
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