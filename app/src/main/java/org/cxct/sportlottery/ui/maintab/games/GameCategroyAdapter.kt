package org.cxct.sportlottery.ui.maintab.games

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemGameCategroyBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesCategory
import org.cxct.sportlottery.ui.common.adapter.BindingAdapter
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.view.layoutmanager.SocketLinearManager

class GameCategroyAdapter(private val clickCollect: (gameGroup: OKGameBean) -> Unit) :
    BindingAdapter<OKGamesCategory, ItemGameCategroyBinding>() {

    init {
        addChildClickViewIds(R.id.lin_categroy_name)
    }

    override fun onBinding(
        position: Int,
        binding: ItemGameCategroyBinding,
        item: OKGamesCategory,
    ) {
        binding.apply {
            if (item.gameList.isNullOrEmpty()) {
                root.isVisible = false
            } else {
                root.isVisible = true
                ivIcon.setImageResource(R.drawable.ic_game_fav)
                tvName.text = item.categoryName
                item.gameList.toMutableList()?.let { gameList ->
                    rvGameItem.apply {
                        if (adapter == null) {
                            layoutManager =
                                SocketLinearManager(context, RecyclerView.HORIZONTAL, false)
                            if (itemDecorationCount == 0)
                                addItemDecoration(SpaceItemDecoration(context, R.dimen.margin_10))
                            adapter = GameChildAdapter().apply {
                                setList(gameList)
                                setOnItemChildClickListener { adapter, view, position ->
                                    clickCollect.invoke(data[position])
                                }
                            }
                        } else {
                            (adapter as GameChildAdapter).setList(gameList)
                        }
                    }
                }
            }
        }
    }
}

