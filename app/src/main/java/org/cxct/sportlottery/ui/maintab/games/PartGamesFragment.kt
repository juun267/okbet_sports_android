package org.cxct.sportlottery.ui.maintab.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentPartOkgamesBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.games.bean.GameTab
import org.cxct.sportlottery.ui.maintab.games.bean.OKGameLabel
import org.cxct.sportlottery.ui.maintab.games.bean.OKGameTab
import org.cxct.sportlottery.util.DisplayUtil.dp

// 指定类别的三方游戏
class PartGamesFragment: BaseBottomNavigationFragment<OKGamesViewModel>(OKGamesViewModel::class) {

    private lateinit var binding: FragmentPartOkgamesBinding
    private inline fun okGamesFragment() = parentFragment as OKGamesFragment
    private val gameChildAdapter by lazy { GameChildAdapter() }
    private var currentTab: OKGameLabel? = null

    override fun createRootView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPartOkgamesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onBindView(view: View) {
        initObserve()
        bindLabels()
        initGameList()
        bindClick()
    }
    private fun bindClick() {
        binding.tvTag.setOnClickListener {okGamesFragment().backGameAll() }
        binding.tvShowMore.setOnClickListener {okGamesFragment().backGameAll() }
    }

    private fun initGameList() = binding.rvGamesSelect.run {


        layoutManager = GridLayoutManager(requireContext(), 3)
        addItemDecoration(GridSpacingItemDecoration(3, 10.dp, false))
        adapter = gameChildAdapter
        gameChildAdapter.setEmptyView(LayoutInflater.from(requireContext()).inflate(R.layout.view_no_games, null))
        gameChildAdapter.setOnItemChildClickListener { _, _, position ->
            gameChildAdapter.getItem(position).let {
                okGamesFragment().viewModel.collectGame(it)
            }
        }
        gameChildAdapter.setOnItemClickListener { _, _, position ->
            val item = gameChildAdapter.getItem(position)
            okGamesFragment().viewModel.requestEnterThirdGame(item, this@PartGamesFragment)
            viewModel.addRecentPlay(item.id.toString())
        }
    }

    private fun initObserve() {
        okGamesFragment().viewModel.collectOkGamesResult.observe(this.viewLifecycleOwner) { result ->
            gameChildAdapter.data.forEachIndexed { index, okGameBean ->
                if (okGameBean.id == result.first) {
                    okGameBean.markCollect = result.second.markCollect
                    gameChildAdapter.notifyItemChanged(index)
                    return@observe
                }
            }
        }

    }

    fun isShowSearch(): Boolean {
        return currentTab?.getKey() == GameTab.TAB_SEARCH.getKey()
    }

    fun crrentTabId(): String? {
        return currentTab?.getKey().toString()
    }

    fun changeLabel(gameLabel: OKGameLabel) {
        if (currentTab != gameLabel && currentTab?.getKey() != gameLabel.getKey()) {
            currentTab = gameLabel
            bindLabels()
            gameChildAdapter.setNewInstance(null)
        }
    }

    private fun bindLabels() {
        if (!::binding.isInitialized) {
            return
        }

        currentTab?.let {
            it.bindLabelIcon(binding.ivIcon)
            it.bindLabelName(binding.tvName)
            it.bindLabelName(binding.tvTag)
        }
    }

    fun showSearchResault(list: List<OKGameBean>?): Int {
        gameChildAdapter.setNewInstance(list?.toMutableList())
        return gameChildAdapter.dataCount()
    }
}