package org.cxct.sportlottery.ui.game.home.gameTable4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.home_game_table_4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.match.MatchPreloadData
import org.cxct.sportlottery.network.service.match_clock.MatchClockCO
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusCO
import java.util.*

class RvGameTable4Adapter : RecyclerView.Adapter<RvGameTable4Adapter.ItemViewHolder>() {

    private var mOnSelectItemListener: OnSelectItemListener<GameBean>? = null
    private var mOnSelectAllListener: OnSelectItemListener<GameEntity4>? = null
    private var mDataList: List<GameEntity4> = mutableListOf()
    private val mTimerMap = mutableMapOf<Int, Timer?>()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemViewHolder {
        val layout = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.home_game_table_4, viewGroup, false)
        return ItemViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(viewHolder: ItemViewHolder, position: Int) {
        try {
            val data = mDataList[position]
            viewHolder.bind(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onViewRecycled(holder: ItemViewHolder) {
        super.onViewRecycled(holder)

        //當 viewHolder 被回收就 stopTimer
        holder.itemView.apply {
            val adapter = view_pager.adapter
            if (adapter is Vp2GameTable4Adapter)
                adapter.stopAllTimer()
        }
    }

    fun setOnSelectItemListener(onSelectItemListener: OnSelectItemListener<GameBean>?) {
        mOnSelectItemListener = onSelectItemListener
    }

    fun setOnSelectAllListener(onSelectAllListener: OnSelectItemListener<GameEntity4>?) {
        mOnSelectAllListener = onSelectAllListener
    }

    fun setRvGameData(matchPreloadData: MatchPreloadData?) {
        mDataList = matchPreloadData?.datas?.map { data ->
            val gameBeanList: List<GameBean> = data.matchs.map { match ->
                GameBean(data.code, match, matchPreloadData.matchType)
            }
            GameEntity4(data.code, data.name, data.num, gameBeanList)
        } ?: listOf()

        notifyDataSetChanged()
    }

    //TODO simon test  review 刷新邏輯有沒有問題
    fun setMatchStatusData(matchStatusCO: MatchStatusCO?) {
        mDataList.forEachIndexed { index, gameEntity ->
            gameEntity.gameBeanList
                .find { it.match?.id == matchStatusCO?.matchId }
                ?.let {
                    it.matchStatusCO = matchStatusCO
                    notifyItemChanged(index)
                    return
                }
        }
    }

    //TODO simon test  review 刷新邏輯有沒有問題
    fun setMatchClockData(matchClockCO: MatchClockCO?) {
        mDataList.forEachIndexed { index, gameEntity ->
            gameEntity.gameBeanList
                .find { it.match?.id == matchClockCO?.matchId }
                ?.let {
                    it.matchClockCO = matchClockCO
                    notifyItemChanged(index)
                    return
                }
        }
    }

    fun stopAllTimer() {
        mTimerMap.forEach {
            val timer = it.value
            timer?.cancel()
        }
        mTimerMap.clear()
    }


    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: GameEntity4) {
            itemView.apply {
                iv_game_icon.setImageResource(getGameIcon(data.code))
                tv_game_name.text = data.name
                tv_game_num.text = data.num.toString()
                titleBar.setBackgroundResource(getTitleBarBackground(data.code))
                titleBar.setOnClickListener {
                    mOnSelectAllListener?.onClick(data)
                }

                val adapter = if (view_pager.adapter is Vp2GameTable4Adapter)
                    view_pager.adapter as Vp2GameTable4Adapter
                else
                    Vp2GameTable4Adapter()

                adapter.setData(data.gameBeanList)
                adapter.setOnSelectItemListener(mOnSelectItemListener)
                view_pager.adapter = adapter

                indicator_view.setupWithViewPager2(view_pager)
            }
        }

        @DrawableRes
        fun getGameIcon(code: String?): Int {
            return when (code) {
                SportType.FOOTBALL.code -> R.drawable.ic_soccer
                SportType.BASKETBALL.code -> R.drawable.ic_basketball_icon
                SportType.TENNIS.code -> R.drawable.ic_tennis_icon
                SportType.VOLLEYBALL.code -> R.drawable.ic_volley_ball
                SportType.BADMINTON.code -> R.drawable.ic_badminton_icon
                else -> -1
            }
        }

        @DrawableRes
        fun getTitleBarBackground(code: String?): Int {
            return when (code) {
                SportType.FOOTBALL.code -> R.drawable.img_home_title_soccer_background
                SportType.BASKETBALL.code -> R.drawable.img_home_title_basketball_background
                SportType.TENNIS.code -> R.drawable.img_home_title_tennis_background
                SportType.VOLLEYBALL.code -> R.drawable.img_home_title_volleyball_background
                SportType.BADMINTON.code -> -1 //20210624 紀錄：說沒有羽球賽事了，所以沒做圖
                else -> -1
            }
        }

    }

}