package org.cxct.sportlottery.ui.results

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_game_detail_result_bb_rv.view.*
import kotlinx.android.synthetic.main.content_game_detail_result_bk_rv.view.*
import kotlinx.android.synthetic.main.content_game_detail_result_bk_rv.view.tv_away_first
import kotlinx.android.synthetic.main.content_game_detail_result_bk_rv.view.tv_away_fourth
import kotlinx.android.synthetic.main.content_game_detail_result_bk_rv.view.tv_away_second
import kotlinx.android.synthetic.main.content_game_detail_result_bk_rv.view.tv_away_third
import kotlinx.android.synthetic.main.content_game_detail_result_bk_rv.view.tv_home_first
import kotlinx.android.synthetic.main.content_game_detail_result_bk_rv.view.tv_home_fourth
import kotlinx.android.synthetic.main.content_game_detail_result_bk_rv.view.tv_home_second
import kotlinx.android.synthetic.main.content_game_detail_result_bk_rv.view.tv_home_third
import kotlinx.android.synthetic.main.content_game_detail_result_ft_rv.view.*
import kotlinx.android.synthetic.main.content_game_detail_result_ft_rv.view.ll_game_detail_first_item
import kotlinx.android.synthetic.main.content_game_detail_result_ih_rv.view.*
import kotlinx.android.synthetic.main.content_game_detail_result_rv.view.*
import kotlinx.android.synthetic.main.content_game_detail_result_tn_rv.view.*
import kotlinx.android.synthetic.main.content_game_detail_result_tt_rv.view.*
import kotlinx.android.synthetic.main.item_match_result_match_new.view.*
import kotlinx.android.synthetic.main.item_match_result_match_new.view.tv_away_name
import kotlinx.android.synthetic.main.item_match_result_match_new.view.tv_home_name
import kotlinx.android.synthetic.main.item_match_result_title.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.matchresult.list.Match
import org.cxct.sportlottery.network.matchresult.playlist.MatchResultPlayList
import org.cxct.sportlottery.ui.results.vh.*
import org.cxct.sportlottery.util.TimeUtil

class MatchResultDiffAdapter(private val matchItemClickListener: MatchItemClickListener) :
    ListAdapter<MatchResultData, RecyclerView.ViewHolder>(MatchResultDiffCallBack()) {

    var gameType: String = ""
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            ListType.TITLE.ordinal -> MatchTitleViewHolder(parent)
            ListType.MATCH.ordinal -> MatchViewHolder(parent)
            ListType.MATCH_FT.ordinal -> FtMatchViewHolder(parent)
            ListType.FIRST_ITEM_FT.ordinal -> FtDetailFirstItemViewHolder(parent)

            ListType.FIRST_ITEM_BK.ordinal, ListType.FIRST_ITEM_AFT.ordinal ->
                BkDetailFirstItemViewHolder(parent)

            ListType.FIRST_ITEM_TN.ordinal -> TnDetailFirstItemViewHolder.from(parent)

            ListType.FIRST_ITEM_BM.ordinal -> BmDetailFirstItemViewHolder.from(parent)

            ListType.FIRST_ITEM_VB.ordinal, ListType.FIRST_ITEM_MR.ordinal, ListType.FIRST_ITEM_GF.ordinal ->
                VbDetailFirstItemViewHolder.from(parent)

            ListType.FIRST_ITEM_BB.ordinal -> BbDetailFirstItemViewHolder.from(parent)

            ListType.FIRST_ITEM_TT.ordinal -> TtDetailFirstItemViewHolder.from(parent)

            ListType.FIRST_ITEM_IH.ordinal -> IhDetailFirstItemViewHolder.from(parent)

            ListType.FIRST_ITEM_CK.ordinal, ListType.FIRST_ITEM_BX.ordinal, ListType.FIRST_ITEM_CB.ordinal, ListType.FIRST_ITEM_RB.ordinal ->
                EmptyItemViewHolder.from(parent)

            ListType.DETAIL.ordinal -> DetailItemViewHolder.from(parent)
            else -> NoDataViewHolder.from(parent)
        }
    }

    private fun isFootBall() = GameType.FT.key == gameType

    override fun getItemViewType(position: Int): Int {
        val itemType = getItem(position).dataType.ordinal
        return if (itemType == ListType.MATCH.ordinal && isFootBall()) ListType.MATCH_FT.ordinal else itemType
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val rvDataList = getItem(holder.adapterPosition)

        when (holder) {
            is MatchTitleViewHolder -> {
                holder.apply {
                    bind(gameType, rvDataList, matchItemClickListener)
                }
            }
            is MatchViewHolder -> {
                holder.apply {
                    bind(gameType, rvDataList, matchItemClickListener)
                    setupBottomLine(position, holder.bottomLine)
                }
            }
            is FtMatchViewHolder -> {
                holder.apply {
                    bind(rvDataList, matchItemClickListener)
                    setupBottomLine(position, holder.bottomLine)
                }
            }
            is FtDetailFirstItemViewHolder -> {
                holder.apply {
                    bind(rvDataList.matchData)
                    setupBottomLine(position, holder.bottomLine, holder.llRoot)
                }
            }
            is BkDetailFirstItemViewHolder -> {
                holder.apply {
                    bind(rvDataList)
                    setupBottomLine(position, holder.bottomLine, holder.llRoot)
                }
            }
            is TnDetailFirstItemViewHolder -> {
                holder.apply {
                    bind(rvDataList.matchData)
                    setupBottomLine(position, holder.bottomLine, holder.llRoot)
                }
            }
            is BmDetailFirstItemViewHolder -> {
                holder.apply {
                    bind(rvDataList.matchData)
                    setupBottomLine(position, holder.bottomLine, holder.llRoot)
                }
            }
            is VbDetailFirstItemViewHolder -> {
                holder.apply {
                    bind(rvDataList.matchData)
                    setupBottomLine(position, holder.bottomLine, holder.llRoot)
                }
            }
            is BbDetailFirstItemViewHolder -> {
                holder.apply {
                    bind(rvDataList.matchData)
                    setupBottomLine(position, holder.bottomLine, holder.llRoot)
                }
            }
            is TtDetailFirstItemViewHolder -> {
                holder.apply {
                    bind(rvDataList.matchData)
                    setupBottomLine(position, holder.bottomLine, holder.llRoot)
                }
            }

            is CbDetailFirstItemViewHolder -> {
                holder.apply {
                    bind(rvDataList.matchDetailData)
                    setupBottomLine(position, holder.bottomLine, holder.llRoot)
                }
            }
            is IhDetailFirstItemViewHolder -> {
                holder.apply {
                    bind(rvDataList.matchData)
                    setupBottomLine(position, holder.bottomLine, holder.llRoot)
                }
            }
            is DetailItemViewHolder -> {
                holder.apply {
                    bind(rvDataList.matchDetailData)
                    setupBottomLine(position, holder.bottomLine, holder.llRoot)
                }
            }

            is EmptyItemViewHolder -> {
            }

        }
    }

    private fun setupBottomLine(position: Int, bottomLine: View, llRoot: View? = null) {
        bottomLine.visibility =
            if (position + 1 < itemCount && (getItemViewType(position + 1) != ListType.TITLE.ordinal || getItemViewType(
                    position
                ) == ListType.MATCH.ordinal)
            ) {
                llRoot?.setBackgroundResource(R.drawable.bg_no_top_bottom_stroke_white)
                View.VISIBLE
            } else {
                llRoot?.setBackgroundResource(R.drawable.bg_shape_bottom_8dp_white_stroke_no_top_stroke)
                View.GONE
            }
    }

    //冰球第一筆
    class IhDetailFirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(
                    R.layout.content_game_detail_result_ih_rv,
                    viewGroup,
                    false
                )
                return IhDetailFirstItemViewHolder(view)
            }
        }

        val bottomLine: View = itemView.findViewById(R.id.bottom_line)
        val llRoot: View = itemView.findViewById(R.id.ll_root)

        fun bind(detailData: Match?) {
            setupIhDetailFirstItem(detailData)
        }

        private fun setupIhDetailFirstItem(detailData: Match?) {
            val matchStatus = detailData?.matchStatusList
            val matchInfo = detailData?.matchInfo
            val firstSection = matchStatus?.find { it.status == StatusType.FIRST_SECTION }
            val secondSection = matchStatus?.find { it.status == StatusType.SECOND_SECTION }
            val thirdSection = matchStatus?.find { it.status == StatusType.THIRD_SECTION }
            val overSection = matchStatus?.find { it.status == StatusType.OVER_TIME }
            val regularSection = matchStatus?.find { it.status == StatusType.END_GAME }

            itemView.apply {
                ll_game_detail_first_item.visibility = View.VISIBLE

                matchInfo?.let {
                    tv_home_name.text = it.homeName
                    tv_away_name.text = it.awayName
                }

                //第一節
                tv_home_first.text = firstSection?.homeScore?.toString() ?: "-"
                tv_away_first.text = firstSection?.awayScore?.toString() ?: "-"

                //第二節
                tv_home_second.text = secondSection?.homeScore?.toString() ?: "-"
                tv_away_second.text = secondSection?.awayScore?.toString() ?: "-"

                //第三節
                tv_home_third.text = thirdSection?.homeScore?.toString() ?: "-"
                tv_away_third.text = thirdSection?.awayScore?.toString() ?: "-"

                //常规比分
                tv_home_regular.text = regularSection?.homeScore?.toString() ?: "-"
                tv_away_regular.text = regularSection?.awayScore?.toString() ?: "-"

                //总分含加时
                tv_home_score_with_overtime.text = overSection?.homeScore?.toString() ?: "-"
                tv_away_score_with_overtime.text = overSection?.awayScore?.toString() ?: "-"
            }
        }
    }


    //網球第一筆
    class TnDetailFirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(
                    R.layout.content_game_detail_result_tn_rv,
                    viewGroup,
                    false
                )
                return TnDetailFirstItemViewHolder(view)
            }
        }

        val bottomLine: View = itemView.findViewById(R.id.bottom_line)
        val llRoot: View = itemView.findViewById(R.id.ll_root)

        fun bind(detailData: Match?) {
            setupTnDetailFirstItem(detailData)
        }

        private fun setupTnDetailFirstItem(detailData: Match?) {
            val matchStatus = detailData?.matchStatusList
            val matchInfo = detailData?.matchInfo

            val firstPlat = matchStatus?.find { it.status == StatusType.FIRST_PLAT }
            val secondPlat = matchStatus?.find { it.status == StatusType.SECOND_PLAT }
            val thirdPlat = matchStatus?.find { it.status == StatusType.THIRD_PLAT }
            val fourthPlat = matchStatus?.find { it.status == StatusType.FOURTH_PLAT }
            val fifthPlat = matchStatus?.find { it.status == StatusType.FIFTH_PLAT }
            val finalPlat = matchStatus?.find { it.status == StatusType.END_GAME }
            var homeRound = 0
            var awayRound = 0

            itemView.apply {
                ll_game_detail_first_item.visibility = View.VISIBLE

                matchInfo?.let {
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

    //羽球第一筆
    class BmDetailFirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(
                    R.layout.content_game_detail_result_bm_rv,
                    viewGroup,
                    false
                )
                return BmDetailFirstItemViewHolder(view)
            }
        }

        val bottomLine: View = itemView.findViewById(R.id.bottom_line)
        val llRoot: View = itemView.findViewById(R.id.ll_root)

        fun bind(detailData: Match?) {
            setupBmDetailFirstItem(detailData)
        }

        private fun setupBmDetailFirstItem(detailData: Match?) {
            val matchStatus = detailData?.matchStatusList
            val matchInfo = detailData?.matchInfo

            val firstPlat = matchStatus?.find { it.status == StatusType.FIRST_PLAT }
            val secondPlat = matchStatus?.find { it.status == StatusType.SECOND_PLAT }
            val thirdPlat = matchStatus?.find { it.status == StatusType.THIRD_PLAT }
            val finalPlat = matchStatus?.find { it.status == StatusType.END_GAME }
            var homeRound = 0
            var awayRound = 0

            itemView.apply {
                ll_game_detail_first_item.visibility = View.VISIBLE

                matchInfo?.let {
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

    //排球第一筆
    class VbDetailFirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(
                    R.layout.content_game_detail_result_vb_rv,
                    viewGroup,
                    false
                )
                return VbDetailFirstItemViewHolder(view)
            }
        }

        val bottomLine: View = itemView.findViewById(R.id.bottom_line)
        val llRoot: View = itemView.findViewById(R.id.ll_root)

        fun bind(detailData: Match?) {
            setupBmDetailFirstItem(detailData)
        }

        private fun setupBmDetailFirstItem(detailData: Match?) {
            val matchStatus = detailData?.matchStatusList
            val matchInfo = detailData?.matchInfo

            val firstPlat = matchStatus?.find { it.status == StatusType.FIRST_PLAT }
            val secondPlat = matchStatus?.find { it.status == StatusType.SECOND_PLAT }
            val thirdPlat = matchStatus?.find { it.status == StatusType.THIRD_PLAT }
            val fourthPlat = matchStatus?.find { it.status == StatusType.FOURTH_PLAT }
            val fifthPlat = matchStatus?.find { it.status == StatusType.FIFTH_PLAT }
            val finalPlat = matchStatus?.find { it.status == StatusType.END_GAME }
            var homeRound = 0
            var awayRound = 0

            itemView.apply {
                ll_game_detail_first_item.visibility = View.VISIBLE

                matchInfo?.let {
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

    //棒球第一筆
    class BbDetailFirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(
                    R.layout.content_game_detail_result_bb_rv,
                    viewGroup,
                    false
                )
                return BbDetailFirstItemViewHolder(view)
            }
        }

        val bottomLine: View = itemView.findViewById(R.id.bottom_line)
        val llRoot: View = itemView.findViewById(R.id.ll_root)

        fun bind(detailData: Match?) {
            setupBbDetailFirstItem(detailData)
        }

        private fun setupBbDetailFirstItem(detailData: Match?) {
            val matchList = detailData?.matchStatusList
            val matchInfo = detailData?.matchInfo

            val firstPlat = matchList?.find { it.status == StatusType.FIRST_HALF }
            val secondPlat = matchList?.find { it.status == StatusType.SECOND_HALF }
            val thirdPlat = matchList?.find { it.status == StatusType.END_GAME }

            itemView.apply {
                matchInfo?.apply {
                    bb_home_name.text = homeName
                    bb_away_name.text = awayName
                }

                matchList.apply {
                    //上半場
                    firstPlat?.let {
                        bb_home_first.text = it.homeScore?.toString()
                        bb_away_first.text = it.awayScore?.toString()
                    }

                    //下半場
                    secondPlat?.let {
                        bb_home_second.text = it.homeScore?.toString()
                        bb_away_second.text = it.awayScore?.toString()
                    }

                    //總分
                    thirdPlat?.let {
                        bb_home_third.text = it.homeScore?.toString()
                        bb_away_third.text = it.awayScore?.toString()
                    }

                }
            }
        }

    }

    //桌球第一筆
    class TtDetailFirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(
                    R.layout.content_game_detail_result_tt_rv,
                    viewGroup,
                    false
                )
                return TtDetailFirstItemViewHolder(view)
            }
        }

        val bottomLine: View = itemView.findViewById(R.id.bottom_line)
        val llRoot: View = itemView.findViewById(R.id.ll_root)

        fun bind(detailData: Match?) {
            setupTtDetailFirstItem(detailData)
        }

        private fun setupTtDetailFirstItem(detailData: Match?) {
            val matchInfo = detailData?.matchInfo

            val matchList = detailData?.matchStatusList
            val gameStatus = matchList?.find { it.status == StatusType.END_GAME }

            itemView.apply {
                matchInfo?.apply {
                    tt_home_name.text = homeName
                    tt_away_name.text = awayName
                }

                //完場
                gameStatus?.let {
                    tt_score_first.text = it.homeTotalScore?.toString()
                    tt_plat_first.text = it.homeScore?.toString()
                    tt_score_second.text = it.awayTotalScore?.toString()
                    tt_plat_second.text = it.awayScore?.toString()
                }
            }
        }
    }

    //台球第一筆
    class CbDetailFirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(
                    R.layout.content_game_detail_result_rv,
                    viewGroup,
                    false
                )
                return TtDetailFirstItemViewHolder(view)
            }
        }

        val bottomLine: View = itemView.findViewById(R.id.bottom_line)
        val llRoot: View = itemView.findViewById(R.id.ll_root)

        fun bind(detailData: MatchResultPlayList?) {
            setupDetailItem(detailData)
        }

        @SuppressLint("SetTextI18n")
        private fun setupDetailItem(detailData: MatchResultPlayList?) {
            itemView.apply {
                tv_play_cate_name.text = "${detailData?.playCateName} ${detailData?.spread}"
                tv_play_content.text = detailData?.playName
            }
        }

    }

    //橄欖球第一筆
    class RbDetailFirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(
                    R.layout.content_game_detail_result_rv,
                    viewGroup,
                    false
                )
                return RbDetailFirstItemViewHolder(view)
            }
        }

        val bottomLine: View = itemView.findViewById(R.id.bottom_line)

        fun bind(detailData: Match?) {
            setupTtDetailFirstItem(detailData)
        }

        private fun setupTtDetailFirstItem(detailData: Match?) {
            val matchList = detailData?.matchStatusList
            val matchInfo = detailData?.matchInfo

            val firstPlat = matchList?.find { it.status == StatusType.FIRST_HALF }
            val secondPlat = matchList?.find { it.status == StatusType.SECOND_HALF }
            val thirdPlat = matchList?.find { it.status == StatusType.END_GAME }

            itemView.apply {
                matchInfo?.apply {
                    bb_home_name.text = homeName
                    bb_away_name.text = awayName
                }

                matchList.apply {
                    //上半場
                    firstPlat?.let {
                        bb_home_first.text = it.homeScore?.toString()
                        bb_away_first.text = it.awayScore?.toString()
                    }

                    //下半場
                    secondPlat?.let {
                        bb_home_second.text = it.homeScore?.toString()
                        bb_away_second.text = it.awayScore?.toString()
                    }

                    //總分
                    thirdPlat?.let {
                        bb_home_third.text = it.homeScore?.toString()
                        bb_away_third.text = it.awayScore?.toString()
                    }

                }
            }
        }

    }

}

//詳情
class DetailItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
            val layoutInflater = LayoutInflater.from(viewGroup.context)
            val view =
                layoutInflater.inflate(R.layout.content_game_detail_result_rv, viewGroup, false)
            return DetailItemViewHolder(view)
        }
    }

    val bottomLine: View = itemView.findViewById(R.id.bottom_line)
    val llRoot: View = itemView.findViewById(R.id.ll_root)

    fun bind(detailData: MatchResultPlayList?) {
        setupDetailItem(detailData)
    }

    @SuppressLint("SetTextI18n")
    private fun setupDetailItem(detailData: MatchResultPlayList?) {
        itemView.apply {
            tv_play_cate_name.text = "${detailData?.playCateName} ${detailData?.spread}"
            tv_play_content.text = detailData?.playName
        }
    }
}

//空資料
class EmptyItemViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun from(parent: ViewGroup): EmptyItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater
                .inflate(R.layout.itemview_empty, parent, false)
            return EmptyItemViewHolder(view)
        }
    }
}

//無資料
class NoDataViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun from(parent: ViewGroup): NoDataViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater
                .inflate(R.layout.itemview_game_no_record, parent, false)

            return NoDataViewHolder(view)
        }
    }
}


class MatchResultDiffCallBack : DiffUtil.ItemCallback<MatchResultData>() {
    override fun areItemsTheSame(oldItem: MatchResultData, newItem: MatchResultData): Boolean {
        return oldItem == newItem && oldItem.matchData == newItem.matchData
    }

    override fun areContentsTheSame(oldItem: MatchResultData, newItem: MatchResultData): Boolean {
        return oldItem == newItem
    }

}

class MatchItemClickListener(
    private val titleClick: (titlePosition: Int) -> Unit,
    private val matchClick: (matchClick: Int) -> Unit
) {
    fun leagueTitleClick(titlePosition: Int) = titleClick.invoke(titlePosition)
    fun matchClick(matchPosition: Int) = matchClick.invoke(matchPosition)
}