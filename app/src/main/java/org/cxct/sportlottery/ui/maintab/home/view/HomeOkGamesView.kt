package org.cxct.sportlottery.ui.maintab.home.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_home_okgame.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.databinding.ViewHomeOkgameBinding
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.view.onClick
import splitties.systemservices.layoutInflater

class HomeOkGamesView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val binding  = ViewHomeOkgameBinding.inflate(layoutInflater,this)
    private val gameAdapter = HomeOkGamesAdapter()

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
        gameAdapter.bindLifecycleOwner(fragment)
        //请求games数据
        fragment.viewModel.getHomeOKGamesList()
        fragment.viewModel.homeOKGamesList.observe(fragment.viewLifecycleOwner) {
            fragment.hideLoading()
            //缓存这一页数据到map
            gameAdapter.setList(it)
            this@HomeOkGamesView.isVisible = gameAdapter.dataCount() > 0
        }

        tvMore.onClick {
            if(StaticData.okGameOpened()){
               (fragment.activity as MainTabActivity).jumpToOKGames()
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