package org.cxct.sportlottery.ui.results

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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
import kotlinx.android.synthetic.main.content_game_detail_result_rv.view.*
import kotlinx.android.synthetic.main.content_game_detail_result_tn_rv.view.*
import kotlinx.android.synthetic.main.item_match_result_match.view.*
import kotlinx.android.synthetic.main.item_match_result_match.view.tv_away_name
import kotlinx.android.synthetic.main.item_match_result_match.view.tv_home_name
import kotlinx.android.synthetic.main.item_match_result_title.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.matchresult.list.Match
import org.cxct.sportlottery.network.matchresult.list.MatchStatus
import org.cxct.sportlottery.network.matchresult.playlist.MatchResultPlayList
import java.text.SimpleDateFormat
import java.util.*

class MatchResultDiffAdapter(private val matchItemClickListener: MatchItemClickListener) : ListAdapter<MatchResultData, RecyclerView.ViewHolder>(MatchResultDiffCallBack()) {
    var gameType: String = ""
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            ListType.TITLE.ordinal -> MatchTitleViewHolder.from(parent)
            ListType.MATCH.ordinal -> MatchViewHolder.from(parent)
            ListType.FIRST_ITEM_FT.ordinal -> FtDetailFirstItemViewHolder.from(parent)
            ListType.FIRST_ITEM_BK.ordinal -> BkDetailFirstItemViewHolder.from(parent)
            ListType.FIRST_ITEM_TN.ordinal -> TnDetailFirstItemViewHolder.from(parent)
            ListType.FIRST_ITEM_BM.ordinal -> BmDetailFirstItemViewHolder.from(parent)
            ListType.FIRST_ITEM_VB.ordinal -> VbDetailFirstItemViewHolder.from(parent)
            ListType.DETAIL.ordinal -> DetailItemViewHolder.from(parent)
            else -> NoDataViewHolder.from(parent)
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
                    bind(gameType, rvDataList, matchItemClickListener)
                }
            }
            is MatchViewHolder -> {
                holder.apply {
                    bind(gameType, rvDataList, matchItemClickListener)
                    setupBottomLine(position, holder.bottomLine)
                }
            }
            is FtDetailFirstItemViewHolder -> {
                holder.apply {
                    bind(rvDataList.matchData?.matchStatusList)
                    setupBottomLine(position, holder.bottomLine)
                }
            }
            is BkDetailFirstItemViewHolder -> {
                holder.apply {
                    bind(rvDataList.matchData)
                    setupBottomLine(position, holder.bottomLine)
                }
            }
            is TnDetailFirstItemViewHolder -> {
                holder.apply {
                    bind(rvDataList.matchData)
                    setupBottomLine(position, holder.bottomLine)
                }
            }
            is BmDetailFirstItemViewHolder -> {
                holder.apply {
                    bind(rvDataList.matchData)
                    setupBottomLine(position, holder.bottomLine)
                }
            }
            is VbDetailFirstItemViewHolder -> {
                holder.apply {
                    bind(rvDataList.matchData)
                    setupBottomLine(position, holder.bottomLine)
                }
            }
            is DetailItemViewHolder -> {
                holder.apply {
                    bind(rvDataList.matchDetailData)
                    setupBottomLine(position, holder.bottomLine)
                }
            }
        }
    }

    private fun setupBottomLine(position: Int, bottomLine: View) {
        bottomLine.visibility = if (position + 1 < itemCount && (getItemViewType(position + 1) != ListType.TITLE.ordinal || getItemViewType(position) == ListType.MATCH.ordinal)) View.VISIBLE else View.GONE
    }

    class MatchTitleViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.item_match_result_title, viewGroup, false)
                return MatchTitleViewHolder(view)
            }
        }

        fun bind(gameType: String, item: MatchResultData, matchItemClickListener: MatchItemClickListener) {
            setupData(itemView, gameType, item)
            setupEvent(itemView, item, matchItemClickListener)
        }

        private fun setupData(itemView: View, gameType: String, item: MatchResultData) {
            itemView.apply {

                titleArrowRotate(itemView, item)

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
            }
        }

        private fun setupEvent(itemView: View, item: MatchResultData, matchItemClickListener: MatchItemClickListener) {
            itemView.setOnClickListener {
                matchItemClickListener.leagueTitleClick(adapterPosition)
                titleArrowRotate(itemView, item)
            }
        }

        private fun titleArrowRotate(itemView: View, item: MatchResultData) {
            itemView.apply {
                if (item.titleExpanded) {
                    iv_arrow.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_arrow_dark))
                } else {
                    iv_arrow.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_arrow_down_dark))
                }
            }
        }
    }

    class MatchViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.item_match_result_match, viewGroup, false)
                return MatchViewHolder(view)
            }
        }

        val bottomLine = itemView.findViewById<View>(R.id.bottom_line)

        fun bind(gameType: String, item: MatchResultData, matchItemClickListener: MatchItemClickListener) {
            setupView(itemView, item)
            setupViewType(itemView, gameType)
            setupResultItem(itemView, item)
            setupEvent(itemView, matchItemClickListener)
        }

        private fun setupView(itemView: View, item: MatchResultData) {
            itemView.apply {
                when (item.matchExpanded) {
                    true -> iv_switch.setImageResource(R.drawable.ic_more_on)
                    false -> iv_switch.setImageResource(R.drawable.ic_more)
                }
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

        private fun setupEvent(itemView: View, matchItemClickListener: MatchItemClickListener) {
            itemView.apply {
                ll_game_detail.setOnClickListener {
                    matchItemClickListener.matchClick(adapterPosition)
                }
            }
        }
    }


    //賽事詳情
    //足球第一筆
    class FtDetailFirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.content_game_detail_result_ft_rv, viewGroup, false)
                return FtDetailFirstItemViewHolder(view)
            }
        }

        val bottomLine = itemView.findViewById<View>(R.id.bottom_line)

        fun bind(data: List<MatchStatus>?) {
            setupFtDetailFirstItem(data)
        }

        private fun setupFtDetailFirstItem(data: List<MatchStatus>?) {
            val firstHalf = data?.find { it.status == StatusType.FIRST_HALF.code }
            val fullGame = data?.find { it.status == StatusType.OVER_TIME.code } ?: data?.find { it.status == StatusType.END_GAME.code }
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
    }

    //籃球第一筆
    class BkDetailFirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.content_game_detail_result_bk_rv, viewGroup, false)
                return BkDetailFirstItemViewHolder(view)
            }
        }

        val bottomLine: View = itemView.findViewById<View>(R.id.bottom_line)

        fun bind(detailData: Match?) {
            setupBkDetailFirstItem(detailData)
        }

        private fun setupBkDetailFirstItem(detailData: Match?) {
            val matchStatus = detailData?.matchStatusList
            val matchInfo = detailData?.matchInfo
            val firstSection = matchStatus?.find { it.status == StatusType.FIRST_SECTION.code }
            val secondSection = matchStatus?.find { it.status == StatusType.SECOND_SECTION.code }
            val thirdSection = matchStatus?.find { it.status == StatusType.THIRD_SECTION.code }
            val fourthSection = matchStatus?.find { it.status == StatusType.FOURTH_SECTION.code }
            val overSection = matchStatus?.find { it.status == StatusType.OVER_TIME.code } ?: matchStatus?.find { it.status == StatusType.END_GAME.code }

            itemView.apply {
                ll_game_detail_first_item.visibility = View.VISIBLE

                matchInfo?.let {
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

    //網球第一筆
    class TnDetailFirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.content_game_detail_result_tn_rv, viewGroup, false)
                return TnDetailFirstItemViewHolder(view)
            }
        }

        val bottomLine: View = itemView.findViewById<View>(R.id.bottom_line)

        fun bind(detailData: Match?) {
            setupTnDetailFirstItem(detailData)
        }

        private fun setupTnDetailFirstItem(detailData: Match?) {
            val matchStatus = detailData?.matchStatusList
            val matchInfo = detailData?.matchInfo

            val firstPlat = matchStatus?.find { it.status == StatusType.FIRST_PLAT.code }
            val secondPlat = matchStatus?.find { it.status == StatusType.SECOND_PLAT.code }
            val thirdPlat = matchStatus?.find { it.status == StatusType.THIRD_PLAT.code }
            val fourthPlat = matchStatus?.find { it.status == StatusType.FOURTH_PLAT.code }
            val fifthPlat = matchStatus?.find { it.status == StatusType.FIFTH_PLAT.code }
            val finalPlat = matchStatus?.find { it.status == StatusType.END_GAME.code }
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
                val view = layoutInflater.inflate(R.layout.content_game_detail_result_bm_rv, viewGroup, false)
                return BmDetailFirstItemViewHolder(view)
            }
        }

        val bottomLine: View = itemView.findViewById<View>(R.id.bottom_line)

        fun bind(detailData: Match?) {
            setupBmDetailFirstItem(detailData)
        }

        private fun setupBmDetailFirstItem(detailData: Match?) {
            val matchStatus = detailData?.matchStatusList
            val matchInfo = detailData?.matchInfo

            val firstPlat = matchStatus?.find { it.status == StatusType.FIRST_PLAT.code }
            val secondPlat = matchStatus?.find { it.status == StatusType.SECOND_PLAT.code }
            val thirdPlat = matchStatus?.find { it.status == StatusType.THIRD_PLAT.code }
            val fourthPlat = matchStatus?.find { it.status == StatusType.FOURTH_PLAT.code }
            val fifthPlat = matchStatus?.find { it.status == StatusType.FIFTH_PLAT.code }
            val finalPlat = matchStatus?.find { it.status == StatusType.END_GAME.code }
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

    //排球第一筆
    class VbDetailFirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.content_game_detail_result_bm_rv, viewGroup, false)
                return VbDetailFirstItemViewHolder(view)
            }
        }

        val bottomLine: View = itemView.findViewById<View>(R.id.bottom_line)

        fun bind(detailData: Match?) {
            setupBmDetailFirstItem(detailData)
        }

        private fun setupBmDetailFirstItem(detailData: Match?) {
            val matchStatus = detailData?.matchStatusList
            val matchInfo = detailData?.matchInfo

            val firstPlat = matchStatus?.find { it.status == StatusType.FIRST_PLAT.code }
            val secondPlat = matchStatus?.find { it.status == StatusType.SECOND_PLAT.code }
            val thirdPlat = matchStatus?.find { it.status == StatusType.THIRD_PLAT.code }
            val fourthPlat = matchStatus?.find { it.status == StatusType.FOURTH_PLAT.code }
            val fifthPlat = matchStatus?.find { it.status == StatusType.FIFTH_PLAT.code }
            val finalPlat = matchStatus?.find { it.status == StatusType.END_GAME.code }
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

    //詳情
    class DetailItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.content_game_detail_result_rv, viewGroup, false)
                return DetailItemViewHolder(view)
            }
        }

        val bottomLine: View = itemView.findViewById<View>(R.id.bottom_line)

        fun bind(detailData: MatchResultPlayList?) {
            setupDetailItem(detailData)
        }

        private fun setupDetailItem(detailData: MatchResultPlayList?) {
            itemView.apply {
                tv_play_cate_name.text = "${detailData?.playCateName} ${detailData?.spread}"
                tv_play_name.text = detailData?.playName
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
}

class MatchResultDiffCallBack : DiffUtil.ItemCallback<MatchResultData>() {
    override fun areItemsTheSame(oldItem: MatchResultData, newItem: MatchResultData): Boolean {
        return oldItem == newItem && oldItem.matchData == newItem.matchData
    }

    override fun areContentsTheSame(oldItem: MatchResultData, newItem: MatchResultData): Boolean {
        return oldItem == newItem
    }

}

class MatchItemClickListener(private val titleClick: (titlePosition: Int) -> Unit, private val matchClick: (matchClick: Int) -> Unit) {
    fun leagueTitleClick(titlePosition: Int) = titleClick.invoke(titlePosition)
    fun matchClick(matchPosition: Int) = matchClick.invoke(matchPosition)
}