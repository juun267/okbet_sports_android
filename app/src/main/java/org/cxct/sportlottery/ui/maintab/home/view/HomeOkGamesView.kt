package org.cxct.sportlottery.ui.maintab.home.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.view_home_okgame.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.ScreenUtil
import org.cxct.sportlottery.view.onClick

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
            totalGameMap[fragment.viewModel.pageIndex] = it
            gameAdapter.setList(it)
            //设置当前条目数量
            setIndexCount(fragment.viewModel.pageIndex)
            //总条目数量
            tvPageSize.text = "/${fragment.viewModel.totalCount}"
        }

        //上一页
        ivBackPage.onClick {
            if(fragment.viewModel.pageIndex==1){
                return@onClick
            }

            changePageData(true,fragment)
        }

        //下一页
        ivForwardPage.onClick {
            if(fragment.viewModel.totalPage==fragment.viewModel.pageIndex){
                return@onClick
            }
            changePageData(false,fragment)
        }
    }


    /**
     * 更换页码
     */
    private fun<T : MainHomeViewModel> changePageData(isBackPage:Boolean,fragment: BindingSocketFragment<T, *>){
        if(isBackPage){
            fragment.viewModel.pageIndex--
        }else{
            fragment.viewModel.pageIndex++
        }
        //如果缓存在map
        if(totalGameMap.containsKey(fragment.viewModel.pageIndex)){
            //设置当前条目数量
            setIndexCount(fragment.viewModel.pageIndex)
            gameAdapter.setList(totalGameMap[fragment.viewModel.pageIndex])
        }else{
            //请求该页数据
            fragment.loading()
            fragment.viewModel.getHomeOKGamesList()
        }
    }



    //设置当前页条目数量
    @SuppressLint("SetTextI18n")
    private fun setIndexCount(currentPage:Int){
        if(totalGameMap.size-currentPage<-1){
            return
        }
        val currentCount=totalGameMap[currentPage]?.size
        currentCount?.let {
            tvPageIndex.text="${(currentPage*6)-6+currentCount}"
        }
    }

}