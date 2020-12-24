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

    private var mDataList: List<MatchStatus>? = null //罰牌, 角球資料於不同api中取得
    private var mDetailData: MatchResultPlayListResult? = null
    private var gameType: String = ""

    fun setData(
        gameType: String,
        mDataList: List<MatchStatus>,
        mDetailData: MatchResultPlayListResult?
    ) {
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
                holder.itemView.apply {
                    //顯示 足球：罰牌角球，
                    ll_game_detail_first_item.visibility = View.VISIBLE

                    //罰牌、角球，若null則不顯示
                    mDataList?.let { matchStatus ->
                        tv_first_half_card.text =
                            "${matchStatus.find { it.status == 6 }?.homeYellowCards?.toString()} - ${matchStatus.find { it.status == 6 }?.awayYellowCards?.toString()}}".let {
                                if (it.contains(
                                        "null",
                                        false
                                    )
                                ) return@let "" else it
                            }
                        tv_full_game_card.text =
                            "${matchStatus.find { it.status == 100 }?.homeYellowCards?.toString()} - ${matchStatus.find { it.status == 100 }?.awayYellowCards?.toString()}".let {
                                if (it.contains(
                                        "null",
                                        false
                                    )
                                ) return@let "" else it
                            }
                        tv_first_half_corner.text =
                            "${matchStatus.find { it.status == 6 }?.homeCornerKicks?.toString()} - ${matchStatus.find { it.status == 6 }?.awayCornerKicks?.toString()}".let {
                                if (it.contains(
                                        "null",
                                        false
                                    )
                                ) return@let "" else it
                            }
                        tv_full_game_corner.text =
                            "${matchStatus.find { it.status == 100 }?.homeCornerKicks?.toString()} - ${matchStatus.find { it.status == 100 }?.awayCornerKicks?.toString()}".let {
                                if (it.contains(
                                        "null",
                                        false
                                    )
                                ) return@let "" else it
                            }
                    }
                    mDetailData?.let {
                        val data = it.rows?.get(position)
                        tv_play_cate_name.text = data?.playCateName
                        tv_play_name.text = data?.playName
                    }
                }
            }
            else -> {
                holder.itemView.apply {
                    ll_game_detail_first_item.visibility = View.GONE
                    mDetailData?.let {
                        val data = it.rows?.get(position)
                        tv_play_cate_name.text = data?.playCateName
                        tv_play_name.text = data?.playName
                    }
                }
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> ViewHolderType.FIRST.ordinal
            else -> ViewHolderType.OTHER.ordinal
        }
    }


}

class DetailFirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
}

class DetailItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
}