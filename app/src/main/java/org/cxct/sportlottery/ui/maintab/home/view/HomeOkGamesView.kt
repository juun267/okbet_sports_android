package org.cxct.sportlottery.ui.maintab.home.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_home_okgame.view.*
import org.cxct.sportlottery.databinding.ViewHomeOkgameBinding
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.maintab.home.MainHomeFragment
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.enterThirdGame
import org.cxct.sportlottery.util.loginedRun
import org.cxct.sportlottery.view.onClick
import org.cxct.sportlottery.view.transform.TransformInDialog
import splitties.systemservices.layoutInflater

class HomeOkGamesView(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    private val binding  = ViewHomeOkgameBinding.inflate(layoutInflater,this,true)
    private val gameAdapter = HomeOkGamesAdapter()

    init {
        initView()
    }

    private fun initView() = binding.run {
        recyclerGames.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL,false)
        recyclerGames.adapter = gameAdapter
    }


    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    fun <T : MainHomeViewModel> setUp(fragment: BindingSocketFragment<T, *>?) {
        if (fragment == null) {
            return
        }
        //请求games数据
        fragment.viewModel.getHomeOKGamesList()
        fragment.viewModel.homeOKGamesList.observe(fragment.viewLifecycleOwner) {
            fragment.hideLoading()
            //缓存这一页数据到map
            gameAdapter.setList(it)
        }

        //监听进入游戏
        initEnterGame(fragment)

        tvMore.onClick {
            (fragment as MainHomeFragment).jumpToOKGames()
        }

        //item点击 进入游戏
        gameAdapter.setOnItemClickListener{ _, _, position ->
            val okGameBean=gameAdapter.data[position]
            if(LoginRepository.isLogined()){
                loginedRun(fragment.requireContext()) {
                    okGameBean.let {okGameBean->
                        fragment.viewModel.homeOkGamesEnterThirdGame(okGameBean, fragment)
                        fragment.viewModel.homeOkGameAddRecentPlay(okGameBean)
                    }
                }
            }else{
                //请求试玩路线
                fragment.loading()
                fragment.viewModel.requestEnterThirdGameNoLogin(okGameBean)
            }

        }
    }


    private fun <T : MainHomeViewModel> initEnterGame(fragment: BindingSocketFragment<T, *>) {
        fragment.viewModel.enterThirdGameResult.observe(fragment.viewLifecycleOwner) {
            if (fragment.isVisible) fragment.enterThirdGame(it.second, it.first)
        }
        fragment.viewModel.gameBalanceResult.observe(fragment.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { event ->
                TransformInDialog(event.first, event.second, event.third) { enterResult ->
                    fragment.enterThirdGame(enterResult, event.first)
                }.show(fragment.childFragmentManager, null)
            }
        }
    }


}