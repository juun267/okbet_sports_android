package org.cxct.sportlottery.ui.maintab.home.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.animDuang
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.databinding.ViewHomeOkliveBinding
import org.cxct.sportlottery.net.games.OKGamesRepository
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.GameCollectManager
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.view.onClick
import splitties.systemservices.layoutInflater

class HomeOkLiveView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val binding  = ViewHomeOkliveBinding.inflate(layoutInflater,this)
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

    private fun initView() = binding.run{
        orientation = VERTICAL
        recyclerGames.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL,false)
        recyclerGames.adapter = gameAdapter
    }


    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    fun <T : MainHomeViewModel> setUp(fragment: BaseSocketFragment<T, *>) {
        if (fragment == null) {
            return
        }
        this.fragment = fragment
        gameAdapter.bindLifecycleOwner(fragment)
        //请求games数据
        fragment.viewModel.getOkLiveOKGamesList()
        fragment.viewModel.homeOKLiveList.observe(fragment.viewLifecycleOwner) {
            fragment.hideLoading()
            gameAdapter.setList(it)
            this@HomeOkLiveView.isVisible = gameAdapter.dataCount() > 0
        }
        (fragment.activity as MainTabActivity).gamesViewModel.collectOkGamesResult.observe(fragment.viewLifecycleOwner) {
            gameAdapter.data.forEachIndexed { index, item ->
                if (item.id == it.first) {
                    item.markCollect = it.second.markCollect
                    gameAdapter.notifyItemChanged(index, it)
                    return@observe
                }
            }
        }
        GameCollectManager.gameCollectNum.observe(fragment.viewLifecycleOwner) {
            gameAdapter.notifyDataSetChanged()
        }
        binding.tvMore.onClick {
            if(StaticData.okLiveOpened()){
                (fragment.activity as MainTabActivity).jumpToOkLive()
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