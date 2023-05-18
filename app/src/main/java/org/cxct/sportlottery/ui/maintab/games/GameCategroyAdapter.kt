package org.cxct.sportlottery.ui.maintab.games

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemGameCategroyBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesCategory
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.adapter.BindingVH
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.view.layoutmanager.SocketLinearManager

class GameCategroyAdapter(
    private val clickCollect: (View, OKGameBean) -> Unit,
    private val clickGame: (gameGroup: OKGameBean) -> Unit,
    private val gameItemViewPool: RecyclerView.RecycledViewPool
) :
    BindingAdapter<OKGamesCategory, ItemGameCategroyBinding>() {

    init {
        addChildClickViewIds(R.id.lin_categroy_name)
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BindingVH<ItemGameCategroyBinding> {
        val vh = super.onCreateDefViewHolder(parent, viewType)
        vh.vb.rvGameItem.run {
            setRecycledViewPool(gameItemViewPool)
            layoutManager = SocketLinearManager(context, RecyclerView.HORIZONTAL, false)
            addItemDecoration(SpaceItemDecoration(context, R.dimen.margin_10))
            adapter = GameChildAdapter(onFavoriate = { view, gameBean ->
                clickCollect.invoke(view, gameBean)
            }).apply {
                setOnItemClickListener { _, _, position ->
                    clickGame.invoke(getItem(position))
                }
            }
        }

        return vh
    }

    override fun onBinding(
        position: Int,
        binding: ItemGameCategroyBinding,
        item: OKGamesCategory,
    ) = binding.run {

        if (item.gameList.isNullOrEmpty()) {
            root.gone()
            return
        }

        root.visible()
        val moreEnable = item.gameList.size > 3
        tvMore.isVisible = moreEnable
        ivMore.isVisible = moreEnable
        ivIcon.load(item.icon)
        tvName.text = item.categoryName
        (rvGameItem.adapter as GameChildAdapter).setList(item.gameList.toMutableList())
    }

    fun updateMarkCollect(bean: OKGameBean) {
        var needUpdate = false
        data.forEach {
            it.gameList?.forEach { gameBean ->
                if (gameBean.id == bean.id) {
                    gameBean.markCollect = bean.markCollect
                    needUpdate = true
                }
            }
        }

        if (needUpdate) {
            notifyDataSetChanged()
        }
    }

}

