package org.cxct.sportlottery.ui.menu.results

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_game_detail_result_rv.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.matchresult.list.MatchStatus
import org.cxct.sportlottery.network.matchresult.playlist.MatchResultPlayListResult

class GameResultDetailAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    enum class ViewHolderType {
        FIRST, OTHER
    }

    enum class SituationType {
        YELLOW_CARD, CORNER_KICK
    }

    private var mDataList: List<MatchStatus>? = null //罰牌, 角球資料於不同api中取得
    private var mDetailData: MatchResultPlayListResult? = null
    private var gameType: String = ""

    fun setData(gameType: String, mDataList: List<MatchStatus>, mDetailData: MatchResultPlayListResult?) {
        this.gameType = gameType
        this.mDataList = mDataList
        this.mDetailData = mDetailData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewHolderType.FIRST.ordinal -> DetailFirstItemViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.content_game_detail_result_rv, parent, false)
            )
            else -> DetailItemViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.content_game_detail_result_rv, parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return mDetailData?.rows?.size ?: 0
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DetailFirstItemViewHolder -> {
                setupDetailFirstItem(holder.itemView)
                setupDetailItem(itemView = holder.itemView, position = position)
            }
            else -> {
                holder.itemView.ll_game_detail_first_item.visibility = View.GONE
                setupDetailItem(itemView = holder.itemView, position = position)
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> ViewHolderType.FIRST.ordinal
            else -> ViewHolderType.OTHER.ordinal
        }
    }

    private fun setupDetailFirstItem(itemView: View) {
        val firstHalf = mDataList?.find { it.status == 6 }
        val fullGame = mDataList?.find { it.status == 100 }
        fun getSituation(matchStatus: MatchStatus?, situationType: SituationType): String {
            when (situationType) {
                SituationType.YELLOW_CARD -> {
                    matchStatus.let {
                        return if (it?.homeYellowCards == null || it.awayYellowCards == null)
                            ""
                        else
                            "${it.homeYellowCards} - ${it.awayYellowCards}"
                    }
                }
                SituationType.CORNER_KICK -> {
                    matchStatus.let {
                        return if (it?.homeCornerKicks == null || it.awayCornerKicks == null)
                            ""
                        else
                            "${it.homeCornerKicks} - ${it.awayCornerKicks}"
                    }
                }
            }
        }
        itemView.apply {
            ll_game_detail_first_item.visibility = View.VISIBLE
            tv_first_half_card.text = getSituation(firstHalf, SituationType.YELLOW_CARD)
            tv_full_game_card.text = getSituation(fullGame, SituationType.YELLOW_CARD)
            tv_first_half_corner.text = getSituation(firstHalf, SituationType.CORNER_KICK)
            tv_full_game_corner.text = getSituation(fullGame, SituationType.CORNER_KICK)
        }
    }

    private fun setupDetailItem(itemView: View, position: Int) {
        itemView.apply {
            val data = mDetailData?.rows?.get(position)
            tv_play_cate_name.text = "${data?.playCateName} ${data?.spread}"
            tv_play_name.text = data?.playName
        }
    }
}

class DetailFirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
}

class DetailItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
}