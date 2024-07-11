package org.cxct.sportlottery.ui.maintab.home.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.animDuang
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.databinding.ViewHomeOkgameBinding
import org.cxct.sportlottery.net.games.OKGamesRepository
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.ThirdGameListActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.GameCollectManager
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.view.onClick
import splitties.systemservices.layoutInflater

class HomeOkGamesView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val binding  = ViewHomeOkgameBinding.inflate(layoutInflater,this)
    private lateinit var fragment: BaseSocketFragment<*, *>
    private val gameAdapter = HomeOkGamesAdapter(onFavoriate = { view, gameBean ->
        if ((fragment.activity as MainTabActivity?)?.collectGame(gameBean)==true) {
            view.animDuang(1.3f)
        }
    })

    init {
        gone()
        initView()
    }

    private fun initView() = binding.run {
        orientation = VERTICAL
        recyclerGames.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL,false)
        recyclerGames.adapter = gameAdapter
    }


    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    fun <T : MainHomeViewModel> setUp(fragment: BaseSocketFragment<T, *>?) {
        if (fragment == null) {
            return
        }
        this.fragment = fragment
        gameAdapter.bindLifecycleOwner(fragment)
        //请求games数据
        fragment.viewModel.getHomeOKGamesList()
        fragment.viewModel.homeOKGamesList.observe(fragment.viewLifecycleOwner) {
            fragment.hideLoading()
            //缓存这一页数据到map
            gameAdapter.setList(it)
            this@HomeOkGamesView.isVisible = gameAdapter.dataCount() > 0
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
        GameCollectManager.gameCollectNum.observe(fragment.viewLifecycleOwner) {
            gameAdapter.notifyDataSetChanged()
        }
        binding.tvMore.onClick {
            if(StaticData.okGameOpened()){
                context.startActivity(Intent(context, ThirdGameListActivity::class.java))
//               (fragment.activity as MainTabActivity).jumpToOKGames()
            }else{
                ToastUtil.showToast(context,context.getString(R.string.N700))
            }
        }

        //item点击 进入游戏
        gameAdapter.setOnItemClickListener{ _, _, position ->
            (fragment.activity as MainTabActivity?)?.enterThirdGame(gameAdapter.data[position])
        }
    }

}