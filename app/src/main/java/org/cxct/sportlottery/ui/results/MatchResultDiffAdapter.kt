package org.cxct.sportlottery.ui.results

import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RotateDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_match_result_match.view.*
import kotlinx.android.synthetic.main.item_match_result_title.view.*
import kotlinx.android.synthetic.main.item_match_result_title.view.tv_type
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.finance.pageSize
import java.text.SimpleDateFormat
import java.util.*

class MatchResultDiffAdapter(private val matchItemClickListener: MatchItemClickListener) : ListAdapter<MatchResultData, RecyclerView.ViewHolder>(MatchResultDiffCallBack()) {
    var gameType: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ListType.TITLE.ordinal -> MatchTitleViewHolder.form(parent)
            ListType.MATCH.ordinal -> MatchViewHolder.form(parent)
            else -> MatchViewHolder.form(parent)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).dataType.ordinal
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val rvDataList = getItem(holder.adapterPosition)

        when (holder) {
            is MatchTitleViewHolder -> {
                holder.apply {
                    bind(holder, gameType, rvDataList, matchItemClickListener)
                }
            }
            is MatchViewHolder -> {
                holder.apply {
                    bind(gameType, rvDataList)
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

        fun bind(holder: RecyclerView.ViewHolder, gameType: String, item: MatchResultData, matchItemClickListener: MatchItemClickListener) {
            setupData(itemView, gameType, item)
            setupEvent(itemView, matchItemClickListener)
            holder.apply {
                itemView.apply {
                    tv_type.text = item.titleData?.name
                }
            }
        }

        private fun setupData(itemView: View, gameType: String, item: MatchResultData) {
            itemView.apply {
                if (adapterPosition == 0) {
                    view_margin_bottom.visibility = View.GONE
                } else {
                    view_margin_bottom.visibility = View.VISIBLE
                }

                tv_type.text = item.titleData?.name
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

        private fun setupEvent(itemView: View, matchItemClickListener: MatchItemClickListener) {
            itemView.setOnClickListener {
                matchItemClickListener.leagueTitleClick(adapterPosition)
                rotateTitleBlock(itemView.block_type)
            }
        }

        private fun rotateTitleBlock(block: View) {
            val drawable = block.background
            ((drawable as LayerDrawable).getDrawable(1) as RotateDrawable).level += 10000
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

        fun bind(gameType: String, item: MatchResultData) {
            setupViewType(itemView, gameType)
            setupResultItem(itemView, item)
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

        private fun setupResultItem(itemView: View, item: MatchResultData) {
            itemView.apply {

                val matchStatusList = item.matchData?.matchStatusList
                val matchInfo = item.matchData?.matchInfo

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

class MatchResultDiffCallBack : DiffUtil.ItemCallback<MatchResultData>() {
    override fun areItemsTheSame(oldItem: MatchResultData, newItem: MatchResultData): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: MatchResultData, newItem: MatchResultData): Boolean {
        return oldItem == newItem
    }

}

class MatchItemClickListener(private val titleClick: (titlePosition: Int) -> Unit) {
    fun leagueTitleClick(titlePosition: Int) = titleClick.invoke(titlePosition)
}

