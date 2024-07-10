package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.youth.banner.itemdecoration.MarginDecoration
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.animDuang
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.databinding.ViewHomeOkgameBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.GameListAdapter
import org.cxct.sportlottery.ui.game.ThirdGameListActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.GameCollectManager
import org.cxct.sportlottery.util.LeftLinearSnapHelper
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.view.onClick
import splitties.systemservices.layoutInflater

class HomeNewGamesView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val binding = ViewHomeOkgameBinding.inflate(layoutInflater,this)
    private lateinit var fragment: BaseSocketFragment<*, *>
    private val gameAdapter = GameListAdapter(::onGameClick, ::onFavorite)

    init {
        gone()
        initView()
    }

    private fun initView() = binding.run {
        orientation = VERTICAL
        recyclerGames.setLinearLayoutManager(RecyclerView.HORIZONTAL)
        recyclerGames.addItemDecoration(MarginDecoration(5.dp))
        LeftLinearSnapHelper().attachToRecyclerView(recyclerGames)
        recyclerGames.adapter = gameAdapter
    }

    fun <T : MainHomeViewModel> setUp(fragment: BaseSocketFragment<T, *>) {
        this.fragment = fragment
        gameAdapter.bindLifecycleOwner(fragment)
        //请求games数据
        fragment.viewModel.getHomeOKGamesList()
        fragment.viewModel.homeOKGamesList.observe(fragment.viewLifecycleOwner) {
            fragment.hideLoading()
            //缓存这一页数据到map
            gameAdapter.setList(it)
            this@HomeNewGamesView.isVisible = gameAdapter.dataCount() > 0
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

        binding.tvMore.onClick {
            if(StaticData.okGameOpened()){
                context.startActivity(Intent(context, ThirdGameListActivity::class.java))
//               (fragment.activity as MainTabActivity).jumpToOKGames()
            }else{
                ToastUtil.showToast(context,context.getString(R.string.N700))
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