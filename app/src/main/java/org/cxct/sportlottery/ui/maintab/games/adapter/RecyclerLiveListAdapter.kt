package org.cxct.sportlottery.ui.maintab.games.adapter

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.databinding.ItemLiveViewListBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesCategory
import org.cxct.sportlottery.service.ServiceBroadcastReceiver

class RecyclerLiveListAdapter : BindingAdapter<OKGamesCategory, ItemLiveViewListBinding>()  {
    //点击更多
    private var onMoreClick: (item: OKGamesCategory) -> Unit = {}
    //点击游戏
    private var onGameClick: (data: OKGameBean) -> Unit = {}
    //收藏点击
    private var onFavoriteClick: (item: OKGameBean) -> Unit = {}

    fun setOnMoreClick(block: (item: OKGamesCategory) -> Unit) {
        onMoreClick = block
    }
    //游戏点击
    fun setOnGameClick(block: (data: OKGameBean) -> Unit) {
        onGameClick = block
    }
    //点击收藏
    fun setOnFavoriteClick(block: (item: OKGameBean) -> Unit) {
        onFavoriteClick = block
    }


    fun bindLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        ServiceBroadcastReceiver.thirdGamesMaintain.collectWith(lifecycleOwner.lifecycleScope) { gamesMaintain ->
            data.forEachIndexed { index, okGamesCategory ->

                var changed = false
                okGamesCategory.gameList?.forEachIndexed { position, okGameBean->
                    if (okGameBean.isMaintain() != (gamesMaintain.maintain == 1) && (okGameBean.firmType == gamesMaintain.firmType)) {
                        okGameBean.maintain = gamesMaintain.maintain.toInt()
                        changed = true
                    }
                }

                if (changed) {
                    notifyItemChanged(index, okGamesCategory)
                }
            }

        }
    }

    override fun onBinding(position: Int, binding: ItemLiveViewListBinding, item: OKGamesCategory, payloads: List<Any>) {
        binding.gameView.notifyDataChanged()
    }

    override fun onBinding(position: Int, binding: ItemLiveViewListBinding, item: OKGamesCategory) {
        binding.gameView
            .setIcon(item.icon)
            .setCategoryName(item.categoryName)
            .setListData(item.gameList)
            .setOnFavoriteClick {
                onFavoriteClick(it)
            }
            .setOnGameClick {
                onGameClick(it)
            }
            .setOnMoreClick {
                onMoreClick(item)
            }
    }

}