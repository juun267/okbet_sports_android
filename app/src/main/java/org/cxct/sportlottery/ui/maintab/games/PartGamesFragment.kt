package org.cxct.sportlottery.ui.maintab.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentPartOkgamesBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.games.bean.GameTab
import org.cxct.sportlottery.ui.maintab.games.bean.OKGameTab
import org.cxct.sportlottery.util.DisplayUtil.dp

// 指定类别的三方游戏
class PartGamesFragment: BaseBottomNavigationFragment<OKGamesViewModel>(OKGamesViewModel::class) {

    private lateinit var binding: FragmentPartOkgamesBinding
    private inline fun okGamesFragment() = parentFragment as OKGamesFragment
    private val gameChildAdapter by lazy { GameChildAdapter() }
    private var dataList = mutableListOf<OKGameBean>()
    private var currentPage: Int = 1
    private var tagName: String? = null
    private var gameName: String? = null
    private var categoryId: String? = null
    private var firmId: String? = null
    private var currentTab: OKGameTab? = null

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
        binding.apply {
            tvTag.setOnClickListener {
                okGamesFragment().backGameAll()
            }
            rvGamesSelect.apply {
                layoutManager = GridLayoutManager(requireContext(), 3)
                addItemDecoration(GridSpacingItemDecoration(3, 10.dp, false))
                adapter = gameChildAdapter.apply {
                    data = dataList
                    setEmptyView(LayoutInflater.from(requireContext())
                        .inflate(R.layout.view_no_games, null))
                    setOnItemChildClickListener(OnItemChildClickListener { adapter, view, position ->
                        dataList[position]?.let {
                            okGamesFragment().viewModel.collectGame(it)
                        }
                    })
                    setOnItemClickListener(OnItemClickListener { adapter, view, position ->
                        dataList[position]?.let {
                            okGamesFragment().viewModel.requestEnterThirdGame(it,
                                this@PartGamesFragment)
                            viewModel.addRecentPlay(it.id.toString())
                        }
                    })
                }
            }
            tvShowMore.setOnClickListener {
                okGamesFragment().backGameAll()
            }
        }
        updateView()
    }

    private fun updateView() {
//        binding.tvTag.text = tagName
        getGameList()
    }

    private fun initObserve() {
        okGamesFragment().viewModel.collectOkGamesResult.observe(this.viewLifecycleOwner) { result ->
            var needUpdate = false
            gameChildAdapter.data.forEach {
                if (it.id == result.first) {
                    it.markCollect = result.second.markCollect
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

    fun setItemList(list: MutableList<OKGameBean>) {
        dataList.clear()
        dataList.addAll(list)
        if (isAdded) {
            gameChildAdapter.notifyDataSetChanged()
        }
    }

    private fun getGameList() {
        viewModel.getOKGamesList(currentPage, gameName, categoryId, firmId)
    }

    fun setData(
        tagName: String?,
        gameName: String? = null,
        categoryId: String? = null,
        firmId: String? = null,
    ) {
        this.tagName = tagName
        this.gameName = gameName
        this.categoryId = categoryId
        this.firmId = firmId
        currentPage = 1
        if (isAdded) {
            updateView()
        }
    }


    fun changeTab(tab: OKGameTab) {
        if (currentTab?.tabId() != tab.tabId()) {
            currentTab = tab
            bindLabels()
        }
    }

    private fun bindLabels() {
        if (!::binding.isInitialized) {
            return
        }

        currentTab?.let { tab ->
            tab.bindLabelIcon(binding.ivIcon)
            tab.bindNameText(binding.tvName)
            tab.bindNameText(binding.tvTag)
        }
    }

    fun showSearchResault(list: List<OKGameBean>?) {
        gameChildAdapter.setNewInstance(list?.toMutableList())
    }
}