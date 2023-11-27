package org.cxct.sportlottery.ui.maintab.home.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_home_okgame.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.databinding.ViewHomeOkgameBinding
import org.cxct.sportlottery.databinding.ViewHomeOkgameChrisBinding
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.enterThirdGame
import org.cxct.sportlottery.util.loginedRun
import org.cxct.sportlottery.view.onClick
import org.cxct.sportlottery.view.transform.TransformInDialog
import splitties.systemservices.layoutInflater

class HomeOkGamesView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val binding  = ViewHomeOkgameChrisBinding.inflate(layoutInflater,this)
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
    fun <T : MainHomeViewModel> setUp(fragment: BindingSocketFragment<T, *>?) {
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

        //监听进入游戏
        initEnterGame(fragment)

        tvMore.onClick {
            if(StaticData.okGameOpened()){
               (fragment.activity as MainTabActivity).jumpToOKGames()
            }else{
                ToastUtil.showToast(context,context.getString(R.string.N700))
            }
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