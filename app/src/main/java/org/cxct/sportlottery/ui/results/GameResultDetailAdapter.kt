package org.cxct.sportlottery.ui.results

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_game_detail_result_bk_rv.view.*
import kotlinx.android.synthetic.main.content_game_detail_result_bk_rv.view.tv_away_first
import kotlinx.android.synthetic.main.content_game_detail_result_bk_rv.view.tv_away_fourth
import kotlinx.android.synthetic.main.content_game_detail_result_bk_rv.view.tv_away_name
import kotlinx.android.synthetic.main.content_game_detail_result_bk_rv.view.tv_away_second
import kotlinx.android.synthetic.main.content_game_detail_result_bk_rv.view.tv_away_third
import kotlinx.android.synthetic.main.content_game_detail_result_bk_rv.view.tv_home_first
import kotlinx.android.synthetic.main.content_game_detail_result_bk_rv.view.tv_home_fourth
import kotlinx.android.synthetic.main.content_game_detail_result_bk_rv.view.tv_home_name
import kotlinx.android.synthetic.main.content_game_detail_result_bk_rv.view.tv_home_second
import kotlinx.android.synthetic.main.content_game_detail_result_bk_rv.view.tv_home_third
import kotlinx.android.synthetic.main.content_game_detail_result_ft_rv.view.*
import kotlinx.android.synthetic.main.content_game_detail_result_ft_rv.view.ll_game_detail_first_item
import kotlinx.android.synthetic.main.content_game_detail_result_rv.view.tv_play_cate_name
import kotlinx.android.synthetic.main.content_game_detail_result_rv.view.tv_play_name
import kotlinx.android.synthetic.main.content_game_detail_result_tn_rv.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.matchresult.list.MatchInfo
import org.cxct.sportlottery.network.matchresult.list.MatchStatus
import org.cxct.sportlottery.network.matchresult.playlist.MatchResultPlayListResult

class GameResultDetailAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    enum class ViewHolderType {
        FT, BK, TN, BM, VB, OTHER
    }

    enum class SituationType {
        YELLOW_CARD, CORNER_KICK
    }

    private var mMatchInfo: MatchInfo? = null //籃球用主隊客隊名稱
    private var mDataList: List<MatchStatus>? = null //罰牌, 角球資料於不同api中取得
    private var mDetailData: MatchResultPlayListResult? = null
    private var gameType: String = ""

    fun setData(gameType: String, matchInfo: MatchInfo, dataList: List<MatchStatus>, detailData: MatchResultPlayListResult?) {
        this.gameType = gameType
        this.mMatchInfo = matchInfo
        this.mDataList = dataList
        this.mDetailData = detailData
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
            ViewHolderType.VB.ordinal -> VbDetailFirstItemViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.content_game_detail_result_bm_rv, parent, false)
            )
            ViewHolderType.TN.ordinal -> TnDetailFirstItemViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.content_game_detail_result_tn_rv, parent, false)
            )
            ViewHolderType.BM.ordinal -> BmDetailFirstItemViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.content_game_detail_result_bm_rv, parent, false)
            )
            else -> DetailItemViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.content_game_detail_result_rv, parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return ((mDetailData?.rows?.size ?: 0) + (if (!mDataList.isNullOrEmpty()) 1 else 0))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FtDetailFirstItemViewHolder -> {
                holder.bind(mDataList)
            }
            is BkDetailFirstItemViewHolder -> {
                holder.bind(mDataList, mMatchInfo)
            }
            is VbDetailFirstItemViewHolder -> {
                holder.bind(mDataList, mMatchInfo)
            }
            is TnDetailFirstItemViewHolder -> {
                holder.bind(mDataList, mMatchInfo)
            }
            is BmDetailFirstItemViewHolder -> {
                holder.bind(mDataList, mMatchInfo)
            }
            is DetailItemViewHolder -> {
                holder.bind(mDetailData, position = position - if (!mDataList.isNullOrEmpty()) 1 else 0) //第一筆為另一個List
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> {
                when (gameType) {
                    GameType.FT.key -> ViewHolderType.FT.ordinal
                    GameType.BK.key -> ViewHolderType.BK.ordinal
                    GameType.VB.key -> ViewHolderType.VB.ordinal
                    GameType.TN.key -> ViewHolderType.TN.ordinal
                    GameType.BM.key -> ViewHolderType.BM.ordinal
                    else -> -1
                }
            }
            else -> ViewHolderType.OTHER.ordinal
        }
    }
}

class FtDetailFirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(data: List<MatchStatus>?) {
        setupFtDetailFirstItem(data)
    }

    private fun setupFtDetailFirstItem(data: List<MatchStatus>?) {
        val firstHalf = data?.find { it.status == StatusType.FIRST_HALF.code }
        val fullGame = data?.find { it.status == StatusType.OVER_TIME.code } ?: data?.find { it.status == StatusType.END_GAME.code }
        fun getSituation(matchStatus: MatchStatus?, situationType: GameResultDetailAdapter.SituationType): String {
            when (situationType) {
                GameResultDetailAdapter.SituationType.YELLOW_CARD -> {
                    matchStatus.let {
                        return if (it?.homeYellowCards == null || it.awayYellowCards == null)
                            ""
                        else
                            "${it.homeYellowCards} - ${it.awayYellowCards}"
                    }
                }
                GameResultDetailAdapter.SituationType.CORNER_KICK -> {
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
            tv_first_half_card.text = getSituation(firstHalf, GameResultDetailAdapter.SituationType.YELLOW_CARD)
            tv_full_game_card.text = getSituation(fullGame, GameResultDetailAdapter.SituationType.YELLOW_CARD)
            tv_first_half_corner.text = getSituation(firstHalf, GameResultDetailAdapter.SituationType.CORNER_KICK)
            tv_full_game_corner.text = getSituation(fullGame, GameResultDetailAdapter.SituationType.CORNER_KICK)
        }
    }
}

class BkDetailFirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(dataList: List<MatchStatus>?, mMatchInfo: MatchInfo?) {
        setupBkDetailFirstItem(dataList, mMatchInfo)
    }

    private fun setupBkDetailFirstItem(dataList: List<MatchStatus>?, mMatchInfo: MatchInfo?) {
        val firstSection = dataList?.find { it.status == StatusType.FIRST_SECTION.code }
        val secondSection = dataList?.find { it.status == StatusType.SECOND_SECTION.code }
        val thirdSection = dataList?.find { it.status == StatusType.THIRD_SECTION.code }
        val fourthSection = dataList?.find { it.status == StatusType.FOURTH_SECTION.code }
        val overSection = dataList?.find { it.status == StatusType.OVER_TIME.code } ?: dataList?.find { it.status == StatusType.END_GAME.code }

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
}

class VbDetailFirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(data: List<MatchStatus>?, mMatchInfo: MatchInfo?) {
        setupBmDetailFirstItem(data, mMatchInfo)
    }

    private fun setupBmDetailFirstItem(data: List<MatchStatus>?, mMatchInfo: MatchInfo?) {
        val firstPlat = data?.find { it.status == StatusType.FIRST_PLAT.code }
        val secondPlat = data?.find { it.status == StatusType.SECOND_PLAT.code }
        val thirdPlat = data?.find { it.status == StatusType.THIRD_PLAT.code }
        val fourthPlat = data?.find { it.status == StatusType.FOURTH_PLAT.code }
        val fifthPlat = data?.find { it.status == StatusType.FIFTH_PLAT.code }
        val finalPlat = data?.find { it.status == StatusType.END_GAME.code }
        var homeRound = 0
        var awayRound = 0

        itemView.apply {
            ll_game_detail_first_item.visibility = View.VISIBLE

            mMatchInfo?.let {
                tv_home_name.text = it.homeName
                tv_away_name.text = it.awayName
            }

            //第一
            firstPlat?.let {
                tv_home_first.text = it.homeScore?.apply { homeRound += this }.toString()
                tv_away_first.text = it.awayScore?.apply { awayRound += this }.toString()
            }

            //第二
            secondPlat?.let {
                tv_home_second.text = it.homeScore?.apply { homeRound += this }.toString()
                tv_away_second.text = it.awayScore?.apply { awayRound += this }.toString()
            }

            //第三
            thirdPlat?.let {
                tv_home_third.text = it.homeScore?.apply { homeRound += this }.toString()
                tv_away_third.text = it.awayScore?.apply { awayRound += this }.toString()
            }

            //第四
            fourthPlat?.let {
                tv_home_fourth.text = it.homeScore?.apply { homeRound += this }.toString()
                tv_away_fourth.text = it.awayScore?.apply { awayRound += this }.toString()
            }

            //第五
            fifthPlat?.let {
                tv_home_fifth.text = it.homeScore?.apply { homeRound += this }.toString()
                tv_away_fifth.text = it.awayScore?.apply { awayRound += this }.toString()
            }

            //終局
            tv_home_score.text = homeRound.toString()
            tv_away_score.text = awayRound.toString()

            //終盤 完賽(賽果)
            finalPlat?.let {
                tv_home_round.text = it.homeScore?.toString()
                tv_away_round.text = it.awayScore?.toString()
            }
        }
    }
}

class TnDetailFirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(data: List<MatchStatus>?, mMatchInfo: MatchInfo?) {
        setupTnDetailFirstItem(data, mMatchInfo)
    }

    private fun setupTnDetailFirstItem(data: List<MatchStatus>?, mMatchInfo: MatchInfo?) {
        val firstPlat = data?.find { it.status == StatusType.FIRST_PLAT.code }
        val secondPlat = data?.find { it.status == StatusType.SECOND_PLAT.code }
        val thirdPlat = data?.find { it.status == StatusType.THIRD_PLAT.code }
        val fourthPlat = data?.find { it.status == StatusType.FOURTH_PLAT.code }
        val fifthPlat = data?.find { it.status == StatusType.FIFTH_PLAT.code }
        val finalPlat = data?.find { it.status == StatusType.END_GAME.code }
        var homeRound = 0
        var awayRound = 0

        itemView.apply {
            ll_game_detail_first_item.visibility = View.VISIBLE

            mMatchInfo?.let {
                tv_home_name.text = it.homeName
                tv_away_name.text = it.awayName
            }

            //第一
            firstPlat?.let {
                tv_home_first.text = it.homeScore?.apply { homeRound += this }.toString()
                tv_away_first.text = it.awayScore?.apply { awayRound += this }.toString()
            }

            //第二
            secondPlat?.let {
                tv_home_second.text = it.homeScore?.apply { homeRound += this }.toString()
                tv_away_second.text = it.awayScore?.apply { awayRound += this }.toString()
            }

            //第三
            thirdPlat?.let {
                tv_home_third.text = it.homeScore?.apply { homeRound += this }.toString()
                tv_away_third.text = it.awayScore?.apply { awayRound += this }.toString()
            }

            //第四
            fourthPlat?.let {
                tv_home_fourth.text = it.homeScore?.apply { homeRound += this }.toString()
                tv_away_fourth.text = it.awayScore?.apply { awayRound += this }.toString()
            }

            //第五
            fifthPlat?.let {
                tv_home_fifth.text = it.homeScore?.apply { homeRound += this }.toString()
                tv_away_fifth.text = it.awayScore?.apply { awayRound += this }.toString()
            }

            //終局
            tv_home_score.text = homeRound.toString()
            tv_away_score.text = awayRound.toString()

            //終盤 完賽(賽果)
            finalPlat?.let {
                tv_home_round.text = it.homeScore?.toString()
                tv_away_round.text = it.awayScore?.toString()
            }
        }
    }

}

class BmDetailFirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(data: List<MatchStatus>?, mMatchInfo: MatchInfo?) {
        setupBmDetailFirstItem(data, mMatchInfo)
    }

    private fun setupBmDetailFirstItem(data: List<MatchStatus>?, mMatchInfo: MatchInfo?) {
        val firstPlat = data?.find { it.status == StatusType.FIRST_PLAT.code }
        val secondPlat = data?.find { it.status == StatusType.SECOND_PLAT.code }
        val thirdPlat = data?.find { it.status == StatusType.THIRD_PLAT.code }
        val fourthPlat = data?.find { it.status == StatusType.FOURTH_PLAT.code }
        val fifthPlat = data?.find { it.status == StatusType.FIFTH_PLAT.code }
        val finalPlat = data?.find { it.status == StatusType.END_GAME.code }
        var homeRound = 0
        var awayRound = 0

        itemView.apply {
            ll_game_detail_first_item.visibility = View.VISIBLE

            mMatchInfo?.let {
                tv_home_name.text = it.homeName
                tv_away_name.text = it.awayName
            }

            //第一
            firstPlat?.let {
                tv_home_first.text = it.homeScore?.apply { homeRound += this }.toString()
                tv_away_first.text = it.awayScore?.apply { awayRound += this }.toString()
            }

            //第二
            secondPlat?.let {
                tv_home_second.text = it.homeScore?.apply { homeRound += this }.toString()
                tv_away_second.text = it.awayScore?.apply { awayRound += this }.toString()
            }

            //第三
            thirdPlat?.let {
                tv_home_third.text = it.homeScore?.apply { homeRound += this }.toString()
                tv_away_third.text = it.awayScore?.apply { awayRound += this }.toString()
            }

            //第四
            fourthPlat?.let {
                tv_home_fourth.text = it.homeScore?.apply { homeRound += this }.toString()
                tv_away_fourth.text = it.awayScore?.apply { awayRound += this }.toString()
            }

            //第五
            fifthPlat?.let {
                tv_home_fifth.text = it.homeScore?.apply { homeRound += this }.toString()
                tv_away_fifth.text = it.awayScore?.apply { awayRound += this }.toString()
            }

            //終局
            tv_home_score.text = homeRound.toString()
            tv_away_score.text = awayRound.toString()

            //終盤 完賽(賽果)
            finalPlat?.let {
                tv_home_round.text = it.homeScore?.toString()
                tv_away_round.text = it.awayScore?.toString()
            }
        }
    }

}

class DetailItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(detailData: MatchResultPlayListResult?, position: Int) {
        setupDetailItem(detailData, position)
    }

    private fun setupDetailItem(detailData: MatchResultPlayListResult?, position: Int) {
        itemView.apply {
            val data = detailData?.rows?.get(position)
            tv_play_cate_name.text = "${data?.playCateName} ${data?.spread}"
            tv_play_name.text = data?.playName
        }
    }
}