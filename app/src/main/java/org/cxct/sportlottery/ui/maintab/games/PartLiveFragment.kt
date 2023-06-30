package org.cxct.sportlottery.ui.maintab.games

import android.graphics.Color
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
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.games.bean.OKGameLabel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.setTrialPlayGameDataObserve

// 指定类别的三方游戏
class PartLiveFragment : BaseBottomNavigationFragment<OKLiveViewModel>(OKLiveViewModel::class) {

    companion object {
        val pageSize = 12
    }

    private lateinit var binding: FragmentPartOkgamesBinding

    private inline fun mOkLiveFragment() = parentFragment as OKLiveFragment
    private val gameChildAdapter by lazy {
        GameChildAdapter(onFavoriate = { view, gameBean ->
            if (mOkLiveFragment().collectGame(gameBean)) {
                view.animDuang(1.3f)
            }
        }, moreClick = ::onMoreClick)
    }
    private var currentTab: OKGameLabel? = null
    private var pageIndx = 1
    private var labelName: String? = null
    private var isLoading = true
    private val emptyView by lazy {
        LayoutInflater.from(binding.root.context)
            .inflate(R.layout.view_no_games, binding.root, false)
    }
    private val loadingView by lazy {
        val view = LayoutInflater.from(binding.root.context)
            .inflate(R.layout.layout_loading, binding.root, false)
        view.setBackgroundColor(Color.TRANSPARENT)
        view.minimumHeight = 300.dp
        view
    }

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
        binding.tvTag.setOnClickListener { mOkLiveFragment().backGameAll() }
        binding.tvShowMore.setOnClickListener { mOkLiveFragment().backGameAll() }
    }


    private fun initGameList() = binding.rvGamesSelect.run {

        setRecycledViewPool(mOkLiveFragment().gameItemViewPool)
        layoutManager = GridLayoutManager(requireContext(), 3)
        addItemDecoration(GridSpacingItemDecoration(3, 10.dp, false))
        adapter = gameChildAdapter
        gameChildAdapter.setOnItemClickListener { _, _, position ->
            val okGameBean = gameChildAdapter.getItem(position)
            if (LoginRepository.isLogined()) {
                mOkLiveFragment().enterGame(okGameBean)
            } else {
                //请求试玩路线
                loading()
                viewModel.requestEnterThirdGameNoLogin(okGameBean.firmType,okGameBean.gameCode,okGameBean.thirdGameCategory)
            }

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

        setTrialPlayGameDataObserve()
    }

    private fun onMoreClick() {
        if (mOkLiveFragment().loadNextPage(pageIndx)) {
            gameChildAdapter.onLoadingMore()
        } else {
            gameChildAdapter.disableMore()
        }
    }

    private fun bindLabels() {
        if (!::binding.isInitialized) {
            return
        }

        gameChildAdapter.setEmptyView(if (isLoading) loadingView else emptyView)
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

        val count = gameChildAdapter.setGameList(list?.toMutableList(), total)
        if (list?.size ?: 0 >= pageSize) {
            pageIndx++
        }
        if (::binding.isInitialized) {
            if (gameChildAdapter.dataCount() == 0) {
                gameChildAdapter.setEmptyView(emptyView)
            }
        }
        isLoading = false
        return count
    }
}