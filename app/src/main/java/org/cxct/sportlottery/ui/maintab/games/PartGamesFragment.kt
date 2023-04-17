package org.cxct.sportlottery.ui.maintab.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentPartOkgamesBinding
import org.cxct.sportlottery.network.third_game.third_games.QueryGameEntryData
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.util.DisplayUtil.dp

// 指定类别的三方游戏
class PartGamesFragment: BaseBottomNavigationFragment<OKGamesViewModel>(OKGamesViewModel::class) {

    private lateinit var binding: FragmentPartOkgamesBinding
    private inline fun okGamesFragment() = parentFragment as OKGamesFragment
    private val gameChildAdapter by lazy { GameChildAdapter() }
    private var dataList = mutableListOf<QueryGameEntryData>()
    private var currentPage: Int = 0
    private var gameName: String? = null
    private var categoryId: String? = null
    private var firmId: String? = null

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
        binding.apply {
            tvTag.setOnClickListener {
                okGamesFragment().showGameAll()
            }
            rvGamesSelect.apply {
                layoutManager = GridLayoutManager(requireContext(), 3)
                addItemDecoration(GridSpacingItemDecoration(3, 10.dp, false))
                gameChildAdapter.setEmptyView(LayoutInflater.from(requireContext())
                    .inflate(R.layout.view_no_games, null))
                adapter = gameChildAdapter
                gameChildAdapter.setOnItemChildClickListener(OnItemChildClickListener { adapter, view, position ->
                    dataList[position]?.let {
                        it.id?.let { gameId -> viewModel.collectGame(gameId, !it.markCollect) }
                    }
                })
            }
            tvShowMore.setOnClickListener {
                okGamesFragment().showGameAll()
            }
        }
        getGameList()
    }

    private fun initObserve() {
        viewModel.collectOkGamesResult.observe(this.viewLifecycleOwner) { result ->
            var needUpdate = false
            gameChildAdapter.data.forEach {
                if (it.id == result.first) {
                    it.markCollect = result.second
                    needUpdate = true
                }
            }
            if (needUpdate) {
                gameChildAdapter.notifyDataSetChanged()
            }
        }
        viewModel.gamesList.observe(this.viewLifecycleOwner) {
            setItemList(it.toMutableList())
        }
    }

    fun setItemList(list: MutableList<QueryGameEntryData>) {
        dataList.clear()
        dataList.addAll(list)
        if (isAdded) {
            gameChildAdapter.notifyDataSetChanged()
        }
    }

    fun getGameList() {
        viewModel.getOKGamesList(currentPage, gameName, categoryId, firmId)
    }
}