package org.cxct.sportlottery.ui.maintab.games

import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.third_game.third_games.QueryGameEntryData
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.view.layoutmanager.SocketLinearManager

class GameCategroyAdapter(data: List<List<QueryGameEntryData>>) :
    BaseQuickAdapter<List<QueryGameEntryData>, BaseViewHolder>(R.layout.item_game_categroy,
        data?.toMutableList()) {
    override fun convert(helper: BaseViewHolder, item: List<QueryGameEntryData>) {
        helper.setImageResource(R.id.iv_icon, R.drawable.ic_game_fav)
        helper.setText(R.id.tv_name, "favorites")
        helper.getView<RecyclerView>(R.id.rv_game_item).apply {
            if (adapter == null) {
                layoutManager = SocketLinearManager(context, RecyclerView.HORIZONTAL, false)
                if (itemDecorationCount == 0)
                    addItemDecoration(SpaceItemDecoration(context, R.dimen.margin_10))
                adapter = GameChildAdapter(listOf())
            }
        }
    }
}

