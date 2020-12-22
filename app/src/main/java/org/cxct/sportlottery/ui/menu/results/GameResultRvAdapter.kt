package org.cxct.sportlottery.ui.menu.results

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_game_result_rv.view.*
import kotlinx.android.synthetic.main.content_settlement_rv.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.matchresult.list.Match
import java.text.SimpleDateFormat
import java.util.*

class GameResultRvAdapter() : RecyclerView.Adapter<ResultItemViewHolder>() {
    var mDataList: MutableList<Match> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var gameType = ""

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

            when (gameType) {
                GameType.FT.key -> { //上半場, 全場
                    tv_first_half_score.visibility = View.VISIBLE
                    tv_second_half_score.visibility = View.GONE
                    tv_end_game_score.visibility = View.GONE
                    tv_full_game_score.visibility = View.VISIBLE
                }
                GameType.BK.key -> { //上半場, 下半場, 賽果
                    tv_first_half.visibility = View.VISIBLE
                    tv_second_half.visibility = View.VISIBLE
                    tv_end_game.visibility = View.VISIBLE
                    tv_full_game.visibility = View.GONE
                }
                //TODO Dean : 待確認
                GameType.TN.key -> ""
                GameType.BM.key -> ""
                GameType.VB.key -> ""
                else -> ""
            }

            val data = mDataList[position]
            tv_home_name.text = data.matchInfo.homeName
            tv_away_name.text = data.matchInfo.awayName

            data.matchStatusList.let {
                tv_first_half_score.text = it.find { matchStatus -> matchStatus.status == 6 }
                    ?.let { filteredItem -> "${filteredItem.homeScore} - ${filteredItem.awayScore}" }
                tv_second_half_score.text = it.find { matchStatus -> matchStatus.status == 7 }
                    ?.let { filteredItem -> "${filteredItem.homeScore} - ${filteredItem.awayScore}" }
                tv_end_game_score.text = it.find { matchStatus -> matchStatus.status == 100 }
                    ?.let { filteredItem -> "${filteredItem.homeScore} - ${filteredItem.awayScore}" }
                tv_full_game_score.text = it.find { matchStatus -> matchStatus.status == 100 }
                    ?.let { filteredItem -> "${filteredItem.homeScore} - ${filteredItem.awayScore}" }
            }

            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat(
                "yyyy${context.getString(R.string.year)}MM${context.getString(R.string.month)}dd${context.getString(
                    R.string.day
                )} HH:mm"
            )
            tv_time.text = dateFormat.format(data.matchInfo.startTime.let {
                calendar.timeInMillis = it.toLong()
                calendar.time
            })
        }
    }
}

class ResultItemViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {
}