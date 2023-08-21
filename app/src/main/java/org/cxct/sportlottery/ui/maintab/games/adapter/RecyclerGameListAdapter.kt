package org.cxct.sportlottery.ui.maintab.games.adapter


import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemGameViewListBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesCategory


class RecyclerGameListAdapter : BindingAdapter<OKGamesCategory, ItemGameViewListBinding>()  {
    //点击更多
    private var onMoreClick: (item:OKGamesCategory) -> Unit = {}
    //点击游戏
    private var onGameClick: (data: OKGameBean) -> Unit = {}
    //收藏点击
    private var onFavoriteClick: (item: OKGameBean) -> Unit = {}

    fun setOnMoreClick(block: (item:OKGamesCategory) -> Unit) {
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

    override fun onBinding(position: Int, binding: ItemGameViewListBinding, item: OKGamesCategory) {
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