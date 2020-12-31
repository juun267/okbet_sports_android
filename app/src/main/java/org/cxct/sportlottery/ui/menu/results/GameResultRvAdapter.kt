package org.cxct.sportlottery.ui.menu.results

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_game_result_rv.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.matchresult.list.Match
import org.cxct.sportlottery.network.matchresult.playlist.RvPosition
import org.cxct.sportlottery.network.matchresult.playlist.SettlementRvData
import java.text.SimpleDateFormat
import java.util.*

class GameResultRvAdapter() : RecyclerView.Adapter<ResultItemViewHolder>() {
    private var mIsOpenList: MutableList<Boolean> = mutableListOf()
    var mDataList: MutableList<Match> = mutableListOf()
        set(value) {
            field = value
            if (mIsOpenList.size != value.size) {
                mIsOpenList = MutableList(value.size) { false }
                mGameDetailData?.gameResultRvPosition?.let { mIsOpenList[it] = true }
            }
            notifyDataSetChanged()
        }

    var gameType = ""

    var positionKey = -1

    var mGameDetailData: SettlementRvData? = null
        set(value) {
            field = value
            value?.gameResultRvPosition?.let { notifyItemChanged(it) }
        }

    interface GameResultDetailListener {
        fun getGameResultDetail(gameResultRvPosition: Int, matchId: String)
    }

    var mGameResultDetailListener: GameResultDetailListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultItemViewHolder {
        val viewLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.content_game_result_rv, parent, false)
        return ResultItemViewHolder(viewLayout)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: ResultItemViewHolder, position: Int) {
        holder.itemView.apply {
            setupViewType(this, gameType)
            setupResultItem(this, position)
            setupResultItemEvent(this, position)
            setupDetailRecyclerView(this, position)
        }
    }

    private fun setupViewType(itemView: View, gameType: String) {
        itemView.apply {
            when (gameType) {
                GameType.FT.key -> { //上半場, 全場
                    tv_first_half_score.visibility = View.VISIBLE
                    tv_second_half_score.visibility = View.GONE
                    tv_end_game_score.visibility = View.GONE
                    tv_full_game_score.visibility = View.VISIBLE
                }
                GameType.BK.key -> { //上半場, 下半場, 賽果
                    tv_first_half_score.visibility = View.VISIBLE
                    tv_second_half_score.visibility = View.VISIBLE
                    tv_end_game_score.visibility = View.VISIBLE
                    tv_full_game_score.visibility = View.GONE
                }
                GameType.TN.key, GameType.BM.key, GameType.VB.key -> { //賽果
                    tv_first_half_score.visibility = View.GONE
                    tv_second_half_score.visibility = View.GONE
                    tv_end_game_score.visibility = View.VISIBLE
                    tv_full_game_score.visibility = View.GONE
                }
                else -> ""
            }
        }
    }

    private fun setupResultItem(itemView: View, position: Int) {
        itemView.apply {
            val data = mDataList[position]
            tv_home_name.text = data.matchInfo.homeName
            tv_away_name.text = data.matchInfo.awayName

            val firstHalf = data.matchStatusList.find { it.status == StatusType.FIRST_HALF.code }
            val secondHalf = data.matchStatusList.find { it.status == StatusType.SECOND_HALF.code }
            //110: 加時, 有加時先取加時
            val endGame = data.matchStatusList.find { it.status == StatusType.OVER_TIME.code } ?: data.matchStatusList.find { it.status == StatusType.END_GAME.code }
            val fullGame = data.matchStatusList.find { it.status == StatusType.OVER_TIME.code } ?: data.matchStatusList.find { it.status == StatusType.END_GAME.code }

            data.matchStatusList.let {
                tv_first_half_score.text = firstHalf?.let { filteredItem -> "${filteredItem.homeScore} - ${filteredItem.awayScore}" }
                tv_second_half_score.text = secondHalf?.let { filteredItem -> "${filteredItem.homeScore} - ${filteredItem.awayScore}" }
                tv_end_game_score.text = endGame?.let { filteredItem -> "${filteredItem.homeScore} - ${filteredItem.awayScore}" }
                tv_full_game_score.text = fullGame?.let { filteredItem -> "${filteredItem.homeScore} - ${filteredItem.awayScore}" }
            }

            //TODO Dean : 之後可以寫成Util
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            tv_time.text = dateFormat.format(data.matchInfo.startTime.let {
                calendar.timeInMillis = it.toLong()
                calendar.time
            })

            //判斷詳情展開或關閉
            el_game_result_detail.setExpanded(mIsOpenList[position], false)

            when(mIsOpenList[position]){
                true -> {
                    iv_switch.setImageResource(R.drawable.ic_more_on)
                }
                false ->{
                    iv_switch.setImageResource(R.drawable.ic_more)
                }
            }
        }
    }

    private fun setupResultItemEvent(itemView: View, position: Int) {
        itemView.apply {
            val data = mDataList[position]
            ll_game_detail.setOnClickListener {
                mIsOpenList[position] = !mIsOpenList[position] //切換展開或關閉
                when(mIsOpenList[position]){
                    true -> {
                        iv_switch.setImageResource(R.drawable.ic_more_on)
                    }
                    false ->{
                        iv_switch.setImageResource(R.drawable.ic_more)
                    }
                }
                //若沒有取過資料才打api
                if (mGameDetailData?.settlementRvMap?.get(RvPosition(positionKey, position)) == null) {
                    mGameResultDetailListener?.getGameResultDetail(gameResultRvPosition = position, matchId = data.matchInfo.id)
                } else
                    el_game_result_detail.setExpanded(
                        mIsOpenList[position],
                        false
                    ) //若無需獲取資料則直接展開或收闔
            }
        }
    }

    private fun setupDetailRecyclerView(itemVIew: View, position: Int) {
        itemVIew.apply {
            //詳情資料 RecyclerView
            mGameDetailData?.let {
                rv_time_line_detail.adapter = GameResultDetailAdapter()
                (rv_time_line_detail.adapter as GameResultDetailAdapter).setData(
                    gameType,
                    mDataList[position].matchInfo,
                    mDataList[position].matchStatusList,
                    mGameDetailData?.settlementRvMap?.get(RvPosition(positionKey, position))
                )
            }
        }
    }
}

class ResultItemViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {
}