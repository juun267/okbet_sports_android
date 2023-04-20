package org.cxct.sportlottery.ui.maintab.games.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.view_hot_game.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.JumpInPlayEvent
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.ui.maintab.games.adapter.HotMatchAdapter
import org.cxct.sportlottery.ui.maintab.home.HomeRecommendListener
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.view.DividerItemDecorator
import org.cxct.sportlottery.view.onClick

class HotGameView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    var adapter: HotMatchAdapter? = null
    init {
        initView()
    }

    private fun initView(){
        LayoutInflater.from(context).inflate(R.layout.view_hot_game, this, true)
        val manager=LinearLayoutManager(context)
        recycler_hot_game.let {
            manager.orientation=LinearLayoutManager.HORIZONTAL
            it.layoutManager = manager
            it.addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(context,
                R.drawable.divider_trans)))
        }
        iv_right.onClick {
            scrollRecycler(manager, true)
        }
        iv_left.onClick {
            scrollRecycler(manager, false)
        }

        tvHotMore.onClick {
            EventBusUtil.post(JumpInPlayEvent())
        }
        ivHotMore.onClick {
            EventBusUtil.post(JumpInPlayEvent())
        }
    }

    fun setUpAdapter(lifecycleOwner: LifecycleOwner, homeRecommendListener: HomeRecommendListener) {
        adapter = HotMatchAdapter(lifecycleOwner, homeRecommendListener)
        recycler_hot_game.adapter = adapter
    }

    //data:List<Recommend>
    fun setGameData(data: List<Recommend>) {
        adapter?.data = data
    }

    fun notifyAdapterData(index: Int) {
        adapter?.notifyItemChanged(index)
    }

    /**
     * 前后滚动recycler
     */
    private fun scrollRecycler(manager: LinearLayoutManager,isNext:Boolean){
        //第一个完全显示的item
        val visiblePosition=manager.findFirstCompletelyVisibleItemPosition()
        //第一个显示的item
        val visiblePosition2=manager.findFirstVisibleItemPosition()
        var position = if(visiblePosition==-1){
            if(isNext){
                visiblePosition2+1
            }else{
                visiblePosition2-1
            }
        }else{
            if(isNext){
                visiblePosition+1
            }else{
                visiblePosition-1
            }
        }
        if (position >= manager.itemCount - 1) {
            return
        }
        if (position < 0) {
            position = 0
        }
        recycler_hot_game.smoothScrollToPosition(position)
    }

}