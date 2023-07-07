package org.cxct.sportlottery.ui.maintab.home.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.drake.spannable.addSpan
import com.drake.spannable.setSpan
import com.drake.spannable.span.ColorSpan
import kotlinx.android.synthetic.main.activity_register.view.view
import kotlinx.android.synthetic.main.view_home_okgame.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeFragment
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.ScreenUtil
import org.cxct.sportlottery.util.enterThirdGame
import org.cxct.sportlottery.util.loginedRun
import org.cxct.sportlottery.view.onClick
import org.cxct.sportlottery.view.transform.TransformInDialog

class HomeOkGamesView(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {
    private val gameAdapter = RecyclerHomeOkGamesAdapter()
    private val totalGameMap = hashMapOf<Int, List<OKGameBean>>()

    init {
        initView()
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.view_home_okgame, this, true)
        gameAdapter.setScreenWidth(ScreenUtil.getScreenWidth(context) - 44.dp)
        recyclerGames.layoutManager = GridLayoutManager(context, 3)
        recyclerGames.adapter = gameAdapter
    }


    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    fun <T : MainHomeViewModel> setOkGamesData(fragment: BindingSocketFragment<T, *>?) {
        if (fragment == null) {
            return
        }
        //请求games数据
        fragment.viewModel.getHomeOKGamesList()
        fragment.viewModel.homeGamesList.observe(fragment.viewLifecycleOwner) {
            fragment.hideLoading()
            //缓存这一页数据到map
            totalGameMap[fragment.viewModel.pageIndexLiveData.value ?: 1] = it
            gameAdapter.setList(it)
            //设置当前条目数量 / 总条目数量
            setIndexCount(
                fragment.viewModel.pageIndexLiveData.value ?: 1,
                fragment.viewModel.totalCountLiveData.value ?: 1
            )
        }

        //监听进入游戏
        initEnterGame(fragment)

        //上一页
        ivBackPage.onClick {
            if (fragment.viewModel.pageIndexLiveData.value == 1) {
                return@onClick
            }

            changePageData(true, fragment)
        }

        //下一页
        ivForwardPage.onClick {
            if (fragment.viewModel.totalPageLiveData.value == fragment.viewModel.pageIndexLiveData.value) {
                return@onClick
            }
            changePageData(false, fragment)
        }

        tvMore.onClick {
            (fragment.activity as MainTabActivity).jumpToOKGames()
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
                fragment.viewModel.requestEnterThirdGameNoLogin(okGameBean.firmType,okGameBean.gameCode,okGameBean.thirdGameCategory)
            }

        }
    }


    /**
     * 更换页码
     */
    private fun <T : MainHomeViewModel> changePageData(
        isBackPage: Boolean, fragment: BindingSocketFragment<T, *>
    ) {

        val viewModel = fragment.viewModel
        viewModel.pageIndexLiveData.observe(fragment.viewLifecycleOwner) {
            when (it) {
                1 -> {
                    ivBackPage.alpha = 0.5f
                }

                viewModel.totalPageLiveData.value -> {
                    ivBackPage.alpha = 1f
                    ivForwardPage.alpha = 0.5f
                }

                else -> {
                    ivBackPage.alpha = 1f
                    ivForwardPage.alpha = 1f
                }
            }
        }

        if (isBackPage) {
            viewModel.pageIndexLiveData.value = (viewModel.pageIndexLiveData.value ?: 1) - 1
        } else {
            viewModel.pageIndexLiveData.value = (viewModel.pageIndexLiveData.value ?: 1) + 1
        }
        //如果缓存在map
        if (totalGameMap.containsKey(viewModel.pageIndexLiveData.value)) {
            //设置当前条目数量
            setIndexCount(
                viewModel.pageIndexLiveData.value ?: 1, viewModel.totalCountLiveData.value ?: 0
            )
            gameAdapter.setList(totalGameMap[viewModel.pageIndexLiveData.value ?: 1])
        } else {
            //请求该页数据
            fragment.loading()
            viewModel.getHomeOKGamesList()
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


    //设置当前页条目数量
    @SuppressLint("SetTextI18n")
    private fun setIndexCount(currentPage: Int, total: Int) {
        if (totalGameMap.size - currentPage < -1) {
            return
        }

        val currentCount = totalGameMap[currentPage]?.size ?: return
        val pageIndex = (currentPage * 6) - 6 + currentCount
        tvPageIndex.text = "$pageIndex".setSpan(ColorSpan(context.getColor(R.color.color_025BE8)))
            .addSpan("/$total", ColorSpan(context.getColor(R.color.color_6D7693)))

    }

}