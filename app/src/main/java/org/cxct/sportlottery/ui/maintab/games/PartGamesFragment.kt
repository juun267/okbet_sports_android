package org.cxct.sportlottery.ui.maintab.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.animDuang
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.databinding.FragmentPartOkgamesBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.games.bean.OKGameLabel
import org.cxct.sportlottery.util.DisplayUtil.dp

// 指定类别的三方游戏
class PartGamesFragment: BaseBottomNavigationFragment<OKGamesViewModel>(OKGamesViewModel::class) {

    companion object {
        val pageSize = 12
    }

    private lateinit var binding: FragmentPartOkgamesBinding

    private inline fun okGamesFragment() = parentFragment as OKGamesFragment
    private val gameChildAdapter by lazy {
        GameChildAdapter(onFavoriate = { view, gameBean ->
            if (okGamesFragment().collectGame(gameBean)) {
                view.animDuang(1.3f)
            }
        }, moreClick = ::onMoreClick)
    }
    private var currentTab: OKGameLabel? = null
    private var pageIndx = 1
    private var labelName: String ?= null

    override fun createRootView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return FragmentPartOkgamesBinding.inflate(layoutInflater).apply { binding = this }.root
    }

    override fun onBindView(view: View) {
        initObserve()
        initGameList()
        bindClick()
        bindLabels()
    }
    private fun bindClick() {
        binding.tvTag.setOnClickListener { okGamesFragment().backGameAll() }
        binding.tvShowMore.setOnClickListener { okGamesFragment().backGameAll() }
    }



    private fun initGameList() = binding.rvGamesSelect.run {

        setRecycledViewPool(okGamesFragment().gameItemViewPool)
        layoutManager = GridLayoutManager(requireContext(), 3)
        addItemDecoration(GridSpacingItemDecoration(3, 10.dp, false))
        adapter = gameChildAdapter
        gameChildAdapter.setEmptyView(LayoutInflater.from(requireContext()).inflate(R.layout.view_no_games, null))
        gameChildAdapter.setOnItemClickListener { _, _, position ->
            okGamesFragment().enterGame(gameChildAdapter.getItem(position))
        }
    }

    private fun initObserve() = okGamesFragment().viewModel.run {
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
        if (okGamesFragment().loadNextPage(pageIndx)) {
            gameChildAdapter.onLoadingMore()
        } else {
            gameChildAdapter.disableMore()
        }
    }

    private fun bindLabels() {
        if (!::binding.isInitialized) {
            return
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

    fun changeLabel(gameLabel: OKGameLabel, labelName: String ?= null) {
        pageIndx = 1
        currentTab = gameLabel
        this.labelName = labelName
        bindLabels()
        gameChildAdapter.setNewInstance(null)
    }

    fun showSearchResault(list: List<OKGameBean>?, total: Int): Int {
        val count = gameChildAdapter.setGameList(list?.toMutableList(), total)
        if (list?.size ?: 0 >= pageSize) {
            pageIndx++
        }
        return count
    }
}