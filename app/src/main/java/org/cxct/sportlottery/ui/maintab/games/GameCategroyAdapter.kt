package org.cxct.sportlottery.ui.maintab.games

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemGameCategroyBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesCategory
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.adapter.BindingVH
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.view.layoutmanager.SocketLinearManager
import org.cxct.sportlottery.view.onClick

class GameCategroyAdapter(
    private val clickCollect: (View, OKGameBean) -> Unit,
    private val clickGame: (gameGroup: OKGameBean) -> Unit,
    private val gameItemViewPool: RecyclerView.RecycledViewPool
) :
    BindingAdapter<OKGamesCategory, ItemGameCategroyBinding>() {
    init {
        addChildClickViewIds(R.id.lin_categroy_name)
    }

    override fun onCreateDefViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingVH<ItemGameCategroyBinding> {
        val vh = super.onCreateDefViewHolder(parent, viewType)
        vh.vb.rvGameItem.run {
            setRecycledViewPool(gameItemViewPool)
            layoutManager = GridLayoutManager(context, 3)
            addItemDecoration(GridSpacingItemDecoration(3, 10.dp, false))
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
        val moreEnable = item.gameList!!.size > 6
        tvMore.isVisible = moreEnable
        ivBackPage.isVisible = moreEnable
        ivForwardPage.isVisible = moreEnable
//        ivMore.isVisible = moreEnable
        ivIcon.load(item.icon)
        tvName.text = item.categoryName

        val childAdapter = rvGameItem.adapter as GameChildAdapter
        changeData(rvGameItem.adapter as GameChildAdapter, item, binding)
        //获得总页数
        childAdapter.totalPage = item.gameList!!.size / childAdapter.itemSize
        if (item.gameList!!.size % childAdapter.itemSize != 0) {
            childAdapter.totalPage += 1
        }
        childAdapter.totalCount = item.gameList!!.size
        //上一页
        ivBackPage.onClick {
            if (childAdapter.itemIndex == 1) {
                return@onClick
            }
            childAdapter.itemIndex--
            changeData(rvGameItem.adapter as GameChildAdapter, item, binding)
        }
        //下一页
        ivForwardPage.onClick {
            if (childAdapter.itemIndex == childAdapter.totalPage) {
                return@onClick
            }
            childAdapter.itemIndex++
            changeData(rvGameItem.adapter as GameChildAdapter, item, binding)
        }

        childAdapter.setJumpMoreClick {
            binding.linCategroyName.performClick()
        }
    }


    private fun changeData(
        adapter: GameChildAdapter,
        item: OKGamesCategory,
        binding: ItemGameCategroyBinding
    ) {
        val positionStart = (adapter.itemIndex * adapter.itemSize) - adapter.itemSize
        val positionEnd = positionStart + adapter.itemSize
        if (item.gameList == null) {
            return
        }
        //不能前一页
        if (adapter.itemIndex == 1) {
            binding.ivBackPage.alpha = 0.5f
        } else {
            binding.ivBackPage.alpha = 1f
        }
        //不能后一页
        if (adapter.itemIndex == adapter.totalPage) {
            binding.ivForwardPage.alpha = 0.5f
        } else {
            binding.ivForwardPage.alpha = 1f
        }


        if (positionEnd > item.gameList!!.size) {
            adapter.setList(item.gameList!!.toMutableList().subList(positionStart, item.gameList!!.size))
        } else {
            adapter.setList(item.gameList!!.toMutableList().subList(positionStart, positionEnd))
        }
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

