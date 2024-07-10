package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.youth.banner.itemdecoration.MarginDecoration
import org.cxct.sportlottery.common.extentions.animDuang
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.databinding.ViewHomeOkgameBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.GameListAdapter
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.GameCollectManager
import org.cxct.sportlottery.util.RCVDecoration
import splitties.systemservices.layoutInflater

class HomeHotGamesView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val binding = ViewHomeOkgameBinding.inflate(layoutInflater,this)
    private lateinit var fragment: BaseSocketFragment<*, *>
    private val gameAdapter = GameListAdapter(::onGameClick, ::onFavorite, true)

    init {
        gone()
        initView()
    }

    private fun initView() = binding.run {
        tvMore.gone()
        orientation = VERTICAL
        recyclerGames.layoutManager = GridLayoutManager(context, 3)
        recyclerGames.addItemDecoration(MarginDecoration(10.dp))
        recyclerGames.adapter = gameAdapter
    }

    fun <T : MainHomeViewModel> setUp(fragment: BaseSocketFragment<T, *>) {
        this.fragment = fragment
        gameAdapter.bindLifecycleOwner(fragment)
        fragment.viewModel.getHotGameList()
        fragment.viewModel.hotGameList.observe(fragment.viewLifecycleOwner) {
            fragment.hideLoading()
            gameAdapter.setList(it)
            this@HomeHotGamesView.isVisible = gameAdapter.dataCount() > 0
        }

        GameCollectManager.collectStatus.observe(fragment.viewLifecycleOwner) {
            gameAdapter.data.forEachIndexed { index, item ->
                if (item.id == it.first) {
                    item.markCollect = it.second
                    gameAdapter.notifyItemChanged(index, it)
                    return@observe
                }
            }
        }

    }

    private fun onGameClick(okGameBean: OKGameBean) {
        (fragment.activity as MainTabActivity?)?.enterThirdGame(okGameBean)
    }

    private fun onFavorite(view: View, okGameBean: OKGameBean) {
        if ((fragment.activity as MainTabActivity?)?.collectGame(okGameBean) == true) {
            view.animDuang(1.3f)
        }
    }

}