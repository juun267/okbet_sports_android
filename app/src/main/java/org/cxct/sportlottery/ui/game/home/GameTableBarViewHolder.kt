package org.cxct.sportlottery.ui.game.home

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.home_game_table_bar.view.*
import org.cxct.sportlottery.network.common.MatchType

/**
 * GameTable Bar
 * 檢查inPlayResult跟atStartResult
 * 其中一個為空則隱藏該選項，假如都為空，則隱藏這個BAR
 */
class GameTableBarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var mListener: Listener? = null

    init {
        itemView.rb_in_play.setOnClickListener {
            mListener?.onGameTableSelect(MatchType.IN_PLAY)
        }

        itemView.rb_as_start.setOnClickListener {
            mListener?.onGameTableSelect(MatchType.AT_START)
        }
    }

    fun bind(homeGameTableBarItemData: HomeListAdapter.HomeGameTableBarItemData) {
        judgeTableBar(homeGameTableBarItemData)
    }

    fun setOnGameTableSelectListener(listener: Listener?) {
        mListener = listener
    }

    //TableBar 判斷是否隱藏
    private fun judgeTableBar(homeGameTableBarItemData: HomeListAdapter.HomeGameTableBarItemData) {
        val inPlayCount = homeGameTableBarItemData.inPlayResult?.matchPreloadData?.num ?: 0
        val atStartCount = homeGameTableBarItemData.atStartResult?.matchPreloadData?.num ?: 0

        itemView.rb_in_play.visibility = if (inPlayCount == 0) View.GONE else View.VISIBLE
        itemView.rb_as_start.visibility = if (atStartCount == 0) View.GONE else View.VISIBLE
    }

    interface Listener {
        fun onGameTableSelect(matchType: MatchType)
    }
}