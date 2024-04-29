package org.cxct.sportlottery.ui.maintab.games

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.GameEntryType
import org.cxct.sportlottery.common.extentions.animDuang
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.common.loading.LoadingAdapter
import org.cxct.sportlottery.databinding.FragmentPartOkgamesBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.maintab.games.bean.OKGameLabel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.loadMore

// 指定类别的三方游戏
class PartLiveFragment : BaseFragment<OKLiveViewModel,FragmentPartOkgamesBinding>() {

    companion object {
        val pageSize = 12
    }

    private inline fun mOkLiveFragment() = parentFragment as OKLiveFragment
    private val gameChildAdapter by lazy {
        GameChildAdapter(onFavoriate = { view, gameBean ->
            if (mOkLiveFragment().collectGame(gameBean)) {
                view.animDuang(1.3f)
            }
        }, moreClick = ::onMoreClick, gameEntryType = GameEntryType.OKLIVE)
    }

    var loadingMoreFlag = false
    private var gameTotal: Int = 0
    private var currentTab: OKGameLabel? = null
    private var pageIndx = 1
    private var labelName: String? = null
    private var isLoading = true

    private val loadingHolder: Gloading.Holder by lazy {
        Gloading.from(LoadingAdapter(emptyString = R.string.N883, emptyIcon = R.drawable.ic_no_data, bgColor = Color.TRANSPARENT)).wrap(View(context))
    }
    override fun onInitView(view: View) {
        initObserve()
        initGameList()
        bindClick()
        bindLabels()
        gameChildAdapter.bindLifecycleOwner(this)
    }

    private fun bindClick() {
        binding.tvTag.setOnClickListener { mOkLiveFragment().backGameAll() }
        binding.tvShowMore.setOnClickListener { mOkLiveFragment().backGameAll() }
    }


    private fun initGameList() = binding.rvGamesSelect.run {

        if(loadingHolder.wrapper.parent == null) {
            loadingHolder.wrapper.layoutParams = ViewGroup.LayoutParams(-1, 250.dp)
            gameChildAdapter.setEmptyView(loadingHolder.wrapper)
        }
        setRecycledViewPool(mOkLiveFragment().gameItemViewPool)
        layoutManager = GridLayoutManager(requireContext(), 3)
        addItemDecoration(GridSpacingItemDecoration(3, 10.dp, false))
        adapter = gameChildAdapter
        loadMore { onMoreClick() }
        gameChildAdapter.setOnItemClickListener { _, _, position ->
            val okGameBean = gameChildAdapter.getItemOrNull(position)?:return@setOnItemClickListener
            mOkLiveFragment().enterGame(okGameBean)
        }
    }

    private fun initObserve() = mOkLiveFragment().viewModel.run {
        collectOkGamesResult.observe(viewLifecycleOwner) { result ->
            gameChildAdapter.data.forEachIndexed { index, okGameBean ->
                if (okGameBean.id == result.first) {
                    okGameBean.markCollect = result.second.markCollect
                    gameChildAdapter.notifyItemChanged(index, okGameBean)
                    return@observe
                }
            }
        }

    }

    private fun onMoreClick() {
        if (gameTotal > gameChildAdapter.data.size && !loadingMoreFlag) {
            if (mOkLiveFragment().loadNextPage(pageIndx)) {
                loadingMoreFlag = true
                gameChildAdapter.onLoadingMore()
            } else {
                loadingMoreFlag = false
                gameChildAdapter.disableMore()
            }
        }
    }

    private fun bindLabels() {
        if (!isAdded) {
            return
        }

        if (isLoading) {
            loadingHolder.showLoading()
        } else {
            loadingHolder.showEmpty()
        }
        gameChildAdapter.disableMore()
        currentTab?.let {
            it.bindLabelIcon(binding.ivIcon)
            it.bindLabelName(binding.tvName)
            if (labelName.isEmptyStr()) {
                it.bindLabelName(binding.tvTag)
            } else {
                binding.tvTag.text = labelName
            }
        }
    }

    fun changeLabel(gameLabel: OKGameLabel, labelName: String? = null) {
        pageIndx = 1
        currentTab = gameLabel
        isLoading = true
        this.labelName = labelName
        bindLabels()
        gameChildAdapter.setNewInstance(null)
    }

    fun showSearchResault(list: List<OKGameBean>?, total: Int): Int {
        loadingMoreFlag = false
        gameTotal = total
        val count = gameChildAdapter.setGameList(list?.toMutableList(), total)
        if (list?.size ?: 0 >= pageSize) {
            pageIndx++
        }
        if (isAdded) {
            if (gameChildAdapter.dataCount() == 0) {
                loadingHolder.showEmpty()
            }
        }
        isLoading = false
        return count
    }
}