package org.cxct.sportlottery.ui.maintab.games

import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemGameCategroyBinding
import org.cxct.sportlottery.net.games.data.OKGamesGroup
import org.cxct.sportlottery.ui.common.adapter.BindingAdapter
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.view.layoutmanager.SocketLinearManager

class GameCategroyAdapter(private val clickCollect: (gameGroup: OKGamesGroup) -> Unit) :
    BindingAdapter<MutableList<OKGamesGroup>, ItemGameCategroyBinding>() {

    override fun onBinding(
        position: Int,
        binding: ItemGameCategroyBinding,
        item: MutableList<OKGamesGroup>,
    ) {
        binding.apply {
            ivIcon.setImageResource(R.drawable.ic_game_fav)
            tvName.text = item.first().gameEntryTagName
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

