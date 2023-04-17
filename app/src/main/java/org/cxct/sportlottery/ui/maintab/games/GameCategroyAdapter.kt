package org.cxct.sportlottery.ui.maintab.games

import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemGameCategroyBinding
import org.cxct.sportlottery.network.third_game.third_games.QueryGameEntryData
import org.cxct.sportlottery.ui.common.adapter.BindingAdapter
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.view.layoutmanager.SocketLinearManager

class GameCategroyAdapter(private val clickCollect: (gameEntryData: QueryGameEntryData) -> Unit) :
    BindingAdapter<MutableList<QueryGameEntryData>, ItemGameCategroyBinding>() {

    override fun onBinding(
        position: Int,
        binding: ItemGameCategroyBinding,
        item: MutableList<QueryGameEntryData>,
    ) {
        binding.apply {
            ivIcon.setImageResource(R.drawable.ic_game_fav)
            tvName.text = "favorites"
            rvGameItem.apply {
                if (adapter == null) {
                    layoutManager = SocketLinearManager(context, RecyclerView.HORIZONTAL, false)
                    if (itemDecorationCount == 0)
                        addItemDecoration(SpaceItemDecoration(context, R.dimen.margin_10))
                    adapter = GameChildAdapter().apply {
                        data = item
                        setOnItemChildClickListener { adapter, view, position ->
                            clickCollect.invoke(data[position])
                        }
                    }
                } else {
                    (adapter as GameChildAdapter).setNewInstance(item)
                }
            }
        }
    }
}

