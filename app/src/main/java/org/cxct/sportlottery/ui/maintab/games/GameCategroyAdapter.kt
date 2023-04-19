package org.cxct.sportlottery.ui.maintab.games

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemGameCategroyBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesCategory
import org.cxct.sportlottery.ui.common.adapter.BindingAdapter
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.view.layoutmanager.SocketLinearManager

class GameCategroyAdapter(
    private val clickCollect: (gameBean: OKGameBean) -> Unit,
    private val clickGame: (gameGroup: OKGameBean) -> Unit,
) :
    BindingAdapter<OKGamesCategory, ItemGameCategroyBinding>() {

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
                Glide.with(context)
                    .load(item.icon)
                    .apply(RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontTransform())
                    .into(ivIcon)
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
                                setOnItemClickListener(OnItemClickListener { adapter, view, position ->
                                    clickGame.invoke(data[position])
                                })
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

