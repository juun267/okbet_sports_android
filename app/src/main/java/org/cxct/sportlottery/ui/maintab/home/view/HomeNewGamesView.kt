package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.drake.spannable.addSpan
import com.drake.spannable.span.CenterImageSpan
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
        fragment.viewModel.getNewGameList()
        fragment.viewModel.newGameList.observe(fragment.viewLifecycleOwner) {
            fragment.hideLoading()
            //缓存这一页数据到map
            gameAdapter.setList(it)
            this@HomeNewGamesView.isVisible = gameAdapter.dataCount() > 0
        }

        GameCollectManager.observerGameCollect(fragment.viewLifecycleOwner) {
            gameAdapter.data.forEachIndexed { index, item ->
                if (item.id == it.first) {
                    item.markCollect = it.second
                    gameAdapter.notifyItemChanged(index, it)
                    return@observerGameCollect
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
        (fragment.activity as MainTabActivity?)?.enterThirdGame(okGameBean, "首页-新游戏列表")
    }

    private fun onFavorite(view: View, okGameBean: OKGameBean) {
        if ((fragment.activity as MainTabActivity?)?.collectGame(okGameBean) == true) {
            view.animDuang(1.3f)
        }
    }

    fun applyHalloweenStyle() {
        val imageView = AppCompatImageView(context)
        imageView.setImageResource(R.drawable.ic_halloween_logo_1)
        val dp24 = 24.dp
        val lp = LayoutParams(dp24, dp24)
        lp.gravity = Gravity.CENTER_VERTICAL
        binding.linearTitle.addView(imageView, 0, lp)
        binding.tvMore.setPadding(8.dp, 0, 5.dp, 0)
        binding.tvMore.text = context.getString(R.string.N702)
            .addSpan("AAA", CenterImageSpan(context, R.drawable.ic_to_right_withe).setDrawableSize(13.dp).setMarginHorizontal(2.dp))
        binding.tvMore.setTextColor(Color.WHITE)
        binding.tvMore.setBackgroundResource(R.drawable.ic_more_but_bg)
        binding.tvMore.compoundDrawablePadding = 0
        binding.tvMore.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
    }

}