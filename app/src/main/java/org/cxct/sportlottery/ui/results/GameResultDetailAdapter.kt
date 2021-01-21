package org.cxct.sportlottery.ui.results

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_game_detail_result_bk_rv.view.*
import kotlinx.android.synthetic.main.content_game_detail_result_ft_rv.view.*
import kotlinx.android.synthetic.main.content_game_detail_result_ft_rv.view.ll_game_detail_first_item
import kotlinx.android.synthetic.main.content_game_detail_result_ft_rv.view.tv_play_cate_name
import kotlinx.android.synthetic.main.content_game_detail_result_ft_rv.view.tv_play_name
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.matchresult.list.MatchInfo
import org.cxct.sportlottery.network.matchresult.list.MatchStatus
import org.cxct.sportlottery.network.matchresult.playlist.MatchResultPlayListResult

class GameResultDetailAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    enum class ViewHolderType {
        FT, BK, BVT, OTHER
    }

    enum class SituationType {
        YELLOW_CARD, CORNER_KICK
    }

    private var mMatchInfo: MatchInfo? = null //籃球用主隊客隊名稱
    private var mDataList: List<MatchStatus>? = null //罰牌, 角球資料於不同api中取得
    private var mDetailData: MatchResultPlayListResult? = null
    private var gameType: String = ""

    fun setData(gameType: String, mMatchInfo: MatchInfo, mDataList: List<MatchStatus>, mDetailData: MatchResultPlayListResult?) {
        this.gameType = gameType
        this.mMatchInfo = mMatchInfo
        this.mDataList = mDataList
        this.mDetailData = mDetailData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewHolderType.FT.ordinal -> FtDetailFirstItemViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.content_game_detail_result_ft_rv, parent, false)
            )
            ViewHolderType.BK.ordinal -> BkDetailFirstItemViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.content_game_detail_result_bk_rv, parent, false)
            )
            ViewHolderType.BVT.ordinal -> BVTDetailFirstItemViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.content_game_detail_result_bvt_rv, parent, false)
            )
            else -> DetailItemViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.content_game_detail_result_ft_rv, parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return mDetailData?.rows?.size ?: 0
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FtDetailFirstItemViewHolder -> {
                setupFtDetailFirstItem(holder.itemView)
                setupDetailItem(itemView = holder.itemView, position = position)
            }
            is BkDetailFirstItemViewHolder -> {
                setupBkDetailFirstItem(holder.itemView)
                setupDetailItem(itemView = holder.itemView, position = position)
            }
            is BVTDetailFirstItemViewHolder -> {
                setupBVTDetailFirstItem(holder.itemView)
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
            0 -> {
                when (gameType) {
                    GameType.FT.key -> ViewHolderType.FT.ordinal
                    GameType.BK.key -> ViewHolderType.BK.ordinal
                    GameType.BM.key, GameType.VB.key, GameType.TN.key -> ViewHolderType.BVT.ordinal
                    else -> -1
                }
            }
            else -> ViewHolderType.OTHER.ordinal
        }
    }

    private fun setupFtDetailFirstItem(itemView: View) {
        val firstHalf = mDataList?.find { it.status == StatusType.FIRST_HALF.code }
        val fullGame = mDataList?.find { it.status == StatusType.OVER_TIME.code } ?: mDataList?.find { it.status == StatusType.END_GAME.code }
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

    private fun setupBkDetailFirstItem(itemView: View) {
        val firstSection = mDataList?.find { it.status == StatusType.FIRST_SECTION.code }
        val secondSection = mDataList?.find { it.status == StatusType.SECOND_SECTION.code }
        val thirdSection = mDataList?.find { it.status == StatusType.THIRD_SECTION.code }
        val fourthSection = mDataList?.find { it.status == StatusType.FOURTH_SECTION.code }
        val overSection = mDataList?.find { it.status == StatusType.OVER_TIME.code }

        itemView.apply {
            ll_game_detail_first_item.visibility = View.VISIBLE

            mMatchInfo?.let {
                tv_home_name.text = it.homeName
                tv_away_name.text = it.awayName
            }

            //第一節
            firstSection?.let {
                tv_home_first.text = it.homeScore?.toString() ?: ""
                tv_away_first.text = it.awayScore?.toString() ?: ""
            }

            //第二節
            secondSection?.let {
                tv_home_second.text = it.homeScore?.toString() ?: ""
                tv_away_second.text = it.awayScore?.toString() ?: ""
            }

            //第三節
            thirdSection?.let {
                tv_home_third.text = it.homeScore?.toString() ?: ""
                tv_away_third.text = it.awayScore?.toString() ?: ""
            }

            //第四節
            fourthSection?.let {
                tv_home_fourth.text = it.homeScore?.toString() ?: ""
                tv_away_fourth.text = it.awayScore?.toString() ?: ""
            }

            //完場
            overSection?.let {
                tv_home_over_time.text = it.homeScore?.toString() ?: ""
                tv_away_over_time.text = it.awayScore?.toString() ?: ""
            }
        }
    }

    private fun setupBVTDetailFirstItem(itemView: View) {
        val firstPlat = mDataList?.find { it.status == StatusType.FIRST_PLAT.code }
        val secondPlat = mDataList?.find { it.status == StatusType.SECOND_PLAT.code }
        val thirdPlat = mDataList?.find { it.status == StatusType.THIRD_PLAT.code }
        val fourthPlat = mDataList?.find { it.status == StatusType.FOURTH_PLAT.code }
        val fifthPlat = mDataList?.find { it.status == StatusType.FIFTH_PLAT.code }

        itemView.apply {
            ll_game_detail_first_item.visibility = View.VISIBLE

            mMatchInfo?.let {
                tv_home_name.text = it.homeName
                tv_away_name.text = it.awayName
            }

            //第一盤
            firstPlat?.let {
                tv_home_first.text = it.homeScore?.toString() ?: ""
                tv_away_first.text = it.awayScore?.toString() ?: ""
            }

            //第二盤
            secondPlat?.let {
                tv_home_second.text = it.homeScore?.toString() ?: ""
                tv_away_second.text = it.awayScore?.toString() ?: ""
            }

            //第三盤
            thirdPlat?.let {
                tv_home_third.text = it.homeScore?.toString() ?: ""
                tv_away_third.text = it.awayScore?.toString() ?: ""
            }

            //第四盤
            fourthPlat?.let {
                tv_home_fourth.text = it.homeScore?.toString() ?: ""
                tv_away_fourth.text = it.awayScore?.toString() ?: ""
            }

            //第五盤
            fifthPlat?.let {
                tv_home_over_time.text = it.homeScore?.toString() ?: ""
                tv_away_over_time.text = it.awayScore?.toString() ?: ""
            }
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

class FtDetailFirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
}

class BkDetailFirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
}

class BVTDetailFirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
}

class DetailItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
}