package org.cxct.sportlottery.ui.maintab.games.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.listener.OnItemClickListener
import kotlinx.android.synthetic.main.view_hot_game.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.adapter.RecyclerHotGameAdapter
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.view.DividerItemDecorator
import org.cxct.sportlottery.view.onClick

class HotGameView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val adapter = RecyclerHotGameAdapter()

    init {
        initView()
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.view_hot_game, this, true)
        val manager = LinearLayoutManager(context)
        recycler_hot_game.let {

            manager.orientation = LinearLayoutManager.HORIZONTAL
            it.layoutManager = manager
            it.adapter = adapter
            it.addItemDecoration(
                DividerItemDecorator(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.divider_trans
                    )
                )
            )
        }
        initItemClick()
        iv_right.onClick {
            scrollRecycler(manager, true)
        }
        iv_left.onClick {
            scrollRecycler(manager, false)
        }

    }

    //data:List<Recommend>
    fun setGameData(receiver: ServiceBroadcastReceiver?, data: List<Recommend>) {
        adapter.setList(data)

        receiver?.let {

        }
    }

    private fun initItemClick(){
        adapter.setOnItemChildClickListener { _, view, position ->
            if(position>adapter.data.size-1){
                return@setOnItemChildClickListener
            }
            when(view.id){
                R.id.llayout_win->{
                    sendToDetails(position)
                }
                R.id.llayout_draw->{
                    sendToDetails(position)
                }
                R.id.llayout_lose->{
                    sendToDetails(position)
                }
            }
        }

        adapter.setOnItemClickListener { _, _, position ->
            sendToDetails(position)
        }
    }

    /**
     * 跳转详情
     */
    private fun sendToDetails(position:Int){
        context?.let {
            val recommend= adapter.data[position]
            recommend.matchInfo?.let {
                SportDetailActivity.startActivity(context,matchInfo = recommend.matchInfo!!)
            }
        }
    }

    /**
     * 快速投注bean
     */
    private fun getFastBetBean(matchInfo: MatchInfo, odd: Odd):FastBetDataBean {
        return FastBetDataBean(
            matchType = MatchType.END_SCORE,
            gameType = GameType.BK,
            playCateCode = null,
            playCateName = null,
            matchInfo = matchInfo,
            matchOdd = null,
            odd = odd,
            subscribeChannelType = ChannelType.HALL,
            betPlayCateNameMap = null)
    }

    fun getAdapter(): RecyclerHotGameAdapter {
        return adapter
    }

    fun notifyAdapterData(index: Int) {
        adapter.notifyItemChanged(index)
    }

    /**
     * 前后滚动recycler
     */
    private fun scrollRecycler(manager: LinearLayoutManager, isNext: Boolean) {
        //第一个完全显示的item
        val visiblePosition = manager.findFirstCompletelyVisibleItemPosition()
        //第一个显示的item
        val visiblePosition2 = manager.findFirstVisibleItemPosition()
        var position = if (visiblePosition == -1) {
            if (isNext) {
                visiblePosition2 + 1
            } else {
                visiblePosition2 - 1
            }
        } else {
            if (isNext) {
                visiblePosition + 1
            } else {
                visiblePosition - 1
            }
        }
        if (position > adapter.itemCount - 1) {
            return
        }
        if (position < 0) {
            position = 0
        }
        recycler_hot_game.smoothScrollToPosition(position)
    }


}