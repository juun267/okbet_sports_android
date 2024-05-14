package org.cxct.sportlottery.ui.maintab.games.view

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ItemLivePageBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.util.GameCollectManager.showCollectAmount
import org.cxct.sportlottery.view.onClick

class RecyclerLivePageAdapter:
    BindingAdapter<List<OKGameBean>, ItemLivePageBinding>() {
    private var onFavoriteClick: (item: OKGameBean) -> Unit = {}
    private var onGameClick: (item: OKGameBean) -> Unit = {}
    private var onJumpToMore: () -> Unit = {}
    var isSinglePage = false
    private var isMoreThan18: Boolean = false

    //是否显示收藏
    private var isShowCollect: Boolean = true

    //是否显示收藏按钮
    fun setIsShoeCollect(flag: Boolean) {
        isShowCollect = flag
    }

    //点击收藏
    fun setOnFavoriteClick(block: (item: OKGameBean) -> Unit) {
        onFavoriteClick = block
    }

    //点击更多
    fun setOnJumpToMore(block: () -> Unit) {
        onJumpToMore = block
    }

    //点击游戏
    fun setOnGameClick(block: (item: OKGameBean) -> Unit) {
        onGameClick = block
    }

    fun setIsMoreThan(flag: Boolean) {
        isMoreThan18 = flag
    }

    fun bindLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        ServiceBroadcastReceiver.thirdGamesMaintain.collectWith(lifecycleOwner.lifecycleScope) { gamesMaintain ->
            data.forEachIndexed { index, okGameBeans ->
                val changedPosition = mutableListOf<Pair<Int, OKGameBean>>()

                okGameBeans.forEachIndexed { position, gameBean->
                    if (gameBean.maintain != gamesMaintain.maintain && (gameBean.firmType == gamesMaintain.firmType)) {
                        gameBean.maintain = gamesMaintain.maintain
                        changedPosition.add(Pair(position, gameBean))
                    }
                }
                if (changedPosition.isNotEmpty()) {
                    notifyItemChanged(index, changedPosition)
                }
            }
        }
    }

    override fun onBinding(
        position: Int,
        binding: ItemLivePageBinding,
        item: List<OKGameBean>,
        payloads: List<Any>
    ) {
        payloads.forEach {
            if (it is MutableList<*>) {
                it.forEach { pair->
                    val index = (pair as Pair<Int, OKGameBean>).first
                    val okGameBean = pair.second
                    when(index) {
                        0 -> {
                            binding.tvCover1.isVisible = okGameBean.isMaintain()
                        }

                        1 -> {
                            binding.tvCover3.isVisible = okGameBean.isMaintain()
                        }

                        2 -> {
                            binding.tvCover5.isVisible = okGameBean.isMaintain()
                        }

                        3 -> {
                            binding.tvCover2.isVisible = okGameBean.isMaintain()
                        }

                        4 -> {
                            binding.tvCover4.isVisible = okGameBean.isMaintain()
                        }

                        5 -> {
                            val isMoreItem = position == dataCount() - 1 && isMoreThan18
                            binding.tvCover6.isVisible = !isMoreItem && okGameBean.isMaintain()
                        }
                    }
                }
            }
        }
    }

    override fun onBinding(position: Int, binding: ItemLivePageBinding, item: List<OKGameBean>) = binding.run {
        if (isShowCollect) {
            ivFav1.visible()
            ivFav2.visible()
            ivFav3.visible()
            ivFav4.visible()
            ivFav5.visible()
            ivFav6.visible()
        } else {
            ivFav1.gone()
            ivFav2.gone()
            ivFav3.gone()
            ivFav4.gone()
            ivFav5.gone()
            ivFav6.gone()
        }

        when (item.size) {
            1-> {
                bindItem(item[0], cardGame1, ivCover1, tvName1, tvFirmName1, ivFav1, tvCover1, tvCollect1)
                cardGame2.gone()
                cardGame3.gone()
                cardGame4.gone()
                cardGame5.gone()
                cardGame6.gone()
            }

            2-> {
                bindItem(item[0], cardGame1, ivCover1, tvName1, tvFirmName1, ivFav1, tvCover1, tvCollect1)
                bindItem(item[1], cardGame3, ivCover3, tvName3, tvFirmName3, ivFav3, tvCover3, tvCollect3)
                cardGame2.gone()
                cardGame4.gone()
                cardGame5.gone()
                cardGame6.gone()
            }

            3-> {
                bindItem(item[0], cardGame1, ivCover1, tvName1, tvFirmName1, ivFav1, tvCover1, tvCollect1)
                bindItem(item[1], cardGame3, ivCover3, tvName3, tvFirmName3, ivFav3, tvCover3, tvCollect3)
                bindItem(item[2], cardGame5, ivCover5, tvName5, tvFirmName5, ivFav5, tvCover5, tvCollect5)
                cardGame2.gone()
                cardGame4.gone()
                cardGame6.gone()
            }

            4-> {
                bindItem(item[0], cardGame1, ivCover1, tvName1, tvFirmName1, ivFav1, tvCover1, tvCollect1)
                bindItem(item[1], cardGame3, ivCover3, tvName3, tvFirmName3, ivFav3, tvCover3, tvCollect3)
                bindItem(item[2], cardGame5, ivCover5, tvName5, tvFirmName5, ivFav5, tvCover5, tvCollect5)
                bindItem(item[3], cardGame2, ivCover2, tvName2, tvFirmName2, ivFav2, tvCover2, tvCollect2)
                cardGame4.gone()
                cardGame6.gone()
            }

            5-> {
                bindItem(item[0], cardGame1, ivCover1, tvName1, tvFirmName1, ivFav1, tvCover1, tvCollect1)
                bindItem(item[1], cardGame3, ivCover3, tvName3, tvFirmName3, ivFav3, tvCover3, tvCollect3)
                bindItem(item[2], cardGame5, ivCover5, tvName5, tvFirmName5, ivFav5, tvCover5, tvCollect5)
                bindItem(item[3], cardGame2, ivCover2, tvName2, tvFirmName2, ivFav2, tvCover2, tvCollect2)
                bindItem(item[4], cardGame4, ivCover4, tvName4, tvFirmName4, ivFav4, tvCover4, tvCollect4)
                cardGame6.gone()
            }

            6-> {
                val isMoreItem = position == dataCount() - 1 && isMoreThan18
                bindItem(item[0], cardGame1, ivCover1, tvName1, tvFirmName1, ivFav1, tvCover1, tvCollect1)
                bindItem(item[1], cardGame3, ivCover3, tvName3, tvFirmName3, ivFav3, tvCover3, tvCollect3)
                bindItem(item[2], cardGame5, ivCover5, tvName5, tvFirmName5, ivFav5, tvCover5, tvCollect5)
                bindItem(item[3], cardGame2, ivCover2, tvName2, tvFirmName2, ivFav2, tvCover2, tvCollect2)
                bindItem(item[4], cardGame4, ivCover4, tvName4, tvFirmName4, ivFav4, tvCover4, tvCollect4)
                bindItem(item[5], cardGame6, ivCover6, tvName6, tvFirmName6, ivFav6, tvCover6, tvCollect6, isMoreItem)
                if (isMoreItem) {
                    blurCard6.visible()
                    blurCard6.setupWith(binding.root)
                        .setFrameClearDrawable(binding.root.background)
                        .setBlurRadius(1.3f)
                } else {
                    blurCard6.gone()
                }
                blurCard6.onClick { onJumpToMore() }
            }
        }

    }


    private fun bindItem(
        item: OKGameBean,
        cardGame: View,
        ivCover: ImageView,
        tvName: TextView,
        tvFirmName: TextView,
        ivFav: ImageView,
        tvCover: View,
        tvCollect: TextView,
        moreItem: Boolean = false
    ) {

        cardGame.visible()
        cardGame.onClick {
            if (!tvCover.isVisible) {
                onGameClick(item)
            }
        }
        ivCover.load(item.imgGame, R.drawable.ic_okgames_nodata)
        tvName.text = item.gameName
        tvFirmName.text = item.firmName
        ivFav.isSelected = item.markCollect
        ivFav.isEnabled = !item.isMaintain()
        //收藏点击
        ivFav.onClick {
            ivFav.animDuang(1.3f)
            onFavoriteClick(item)
        }
        tvCover.isVisible = !moreItem && item.isMaintain()
        tvCollect.showCollectAmount(item.id)
    }
}