package org.cxct.sportlottery.ui.maintab.games

import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemGameCategroyBinding
import org.cxct.sportlottery.net.games.data.OKGamesBean
import org.cxct.sportlottery.ui.common.adapter.BindingAdapter
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.view.layoutmanager.SocketLinearManager

class GameCategroyAdapter(private val clickCollect: (gameGroup: OKGamesBean) -> Unit) :
    BindingAdapter<MutableList<OKGamesBean>, ItemGameCategroyBinding>() {

    override fun onBinding(
        position: Int,
        binding: ItemGameCategroyBinding,
        item: MutableList<OKGamesBean>,
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

