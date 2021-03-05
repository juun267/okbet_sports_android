package org.cxct.sportlottery.ui.results

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_match_result_match.view.*
import kotlinx.android.synthetic.main.item_match_result_title.view.*
import kotlinx.android.synthetic.main.item_match_result_title.view.tv_type
import org.cxct.sportlottery.R
import java.text.SimpleDateFormat
import java.util.*

class MatchResultRvAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var rvDataList: List<MatchResultData> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var gameType: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ListType.TITLE.ordinal -> MatchTitleViewHolder.form(parent)
            ListType.MATCH.ordinal -> MatchViewHolder.form(parent)
            else -> MatchViewHolder.form(parent)
        }
    }

    override fun getItemCount(): Int {
        return rvDataList.size
    }

    override fun getItemViewType(position: Int): Int {
        return rvDataList[position].dataType.ordinal
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MatchTitleViewHolder -> {
                holder.apply {
                    bind(holder, gameType, rvDataList, position)
                }
            }
            is MatchViewHolder -> {
                holder.apply {
                    bind(gameType, rvDataList, position)
                }
            }
        }
    }

    class MatchTitleViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun form(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.item_match_result_title, viewGroup, false)
                return MatchTitleViewHolder(view)
            }
        }

        fun bind(holder: RecyclerView.ViewHolder, gameType: String, dataList: List<MatchResultData>, position: Int) {
            val data = dataList[position]
            setupData(holder.itemView, gameType, dataList, position)
            holder.apply {
                itemView.apply {
                    tv_type.text = data.titleData?.name
                }
            }
        }

        private fun setupData(itemView: View, gameType: String, dataList: List<MatchResultData>, position: Int) {
            itemView.apply {
                if (position == 0)
                    view_margin_bottom.visibility = View.GONE

                val data = dataList[position]
                tv_type.text = data.titleData?.name
                when (gameType) {
                    GameType.FT.key -> { //上半場, 全場
                        tv_first_half.visibility = View.VISIBLE
                        tv_second_half.visibility = View.GONE
                        tv_end_game.visibility = View.GONE
                        tv_full_game.visibility = View.VISIBLE
                    }
                    GameType.BK.key -> { //上半場, 下半場, 賽果
                        tv_first_half.visibility = View.VISIBLE
                        tv_second_half.visibility = View.VISIBLE
                        tv_end_game.visibility = View.VISIBLE
                        tv_full_game.visibility = View.GONE
                    }
                    GameType.TN.key, GameType.BM.key, GameType.VB.key -> {
                        tv_first_half.visibility = View.GONE
                        tv_second_half.visibility = View.GONE
                        tv_end_game.visibility = View.VISIBLE
                        tv_full_game.visibility = View.GONE
                    }
                    else -> ""
                }

                //要不要展開
                /*if (mIsOpenList[position]) {
                    setupDetailRv(this, position)
                }

                block_drawer_result.setExpanded(mIsOpenList[position], false)

                block_type.setOnClickListener {
                    setupDetailRv(this, position)
                    mIsOpenList[position] = !mIsOpenList[position]
                    this.block_drawer_result.let { expandableLayout ->
                        expandableLayout.setExpanded(
                            mIsOpenList[position],
                            true
                        )
                    }
                    rotateTitleBlock(block_type)
                }*/
            }
        }
    }

    class MatchViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun form(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.item_match_result_match, viewGroup, false)
                return MatchViewHolder(view)
            }
        }

        fun bind(gameType: String, dataList: List<MatchResultData>, position: Int) {
            setupViewType(itemView, gameType)
            setupResultItem(itemView, dataList, position)
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

        private fun setupResultItem(itemView: View, dataList: List<MatchResultData>, position: Int) {
            itemView.apply {
                val data = dataList[position]
                val matchStatusList = data.matchData?.matchStatusList
                val matchInfo = data.matchData?.matchInfo

                matchInfo?.let {
                    tv_home_name.text = matchInfo.homeName
                    tv_away_name.text = matchInfo.awayName

                    //TODO Dean : 之後可以寫成Util
                    val calendar = Calendar.getInstance()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
                    tv_time.text = dateFormat.format(matchInfo.startTime.let {
                        calendar.timeInMillis = it.toLong()
                        calendar.time
                    })
                }

                matchStatusList?.let {
                    val firstHalf = matchStatusList.find { it.status == StatusType.FIRST_HALF.code }
                    val secondHalf = matchStatusList.find { it.status == StatusType.SECOND_HALF.code }
                    //110: 加時, 有加時先取加時
                    val endGame = matchStatusList.find { it.status == StatusType.OVER_TIME.code } ?: matchStatusList.find { it.status == StatusType.END_GAME.code }
                    val fullGame = matchStatusList.find { it.status == StatusType.OVER_TIME.code } ?: matchStatusList.find { it.status == StatusType.END_GAME.code }

                    tv_first_half_score.text = firstHalf?.let { filteredItem -> "${filteredItem.homeScore} - ${filteredItem.awayScore}" }
                    tv_second_half_score.text = secondHalf?.let { filteredItem -> "${filteredItem.homeScore} - ${filteredItem.awayScore}" }
                    tv_end_game_score.text = endGame?.let { filteredItem -> "${filteredItem.homeScore} - ${filteredItem.awayScore}" }
                    tv_full_game_score.text = fullGame?.let { filteredItem -> "${filteredItem.homeScore} - ${filteredItem.awayScore}" }
                }


/*                //判斷詳情展開或關閉
                el_game_result_detail.setExpanded(mIsOpenList[position], false)

                when (mIsOpenList[position]) {
                    true -> {
                        iv_switch.setImageResource(R.drawable.ic_more_on)
                    }
                    false -> {
                        iv_switch.setImageResource(R.drawable.ic_more)
                    }
                }*/
            }
        }
    }
}

