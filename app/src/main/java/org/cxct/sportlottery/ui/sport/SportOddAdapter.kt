package org.cxct.sportlottery.ui.sport

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.android.synthetic.main.content_baseball_status.view.*
import kotlinx.android.synthetic.main.item_sport_odd.view.*
import kotlinx.android.synthetic.main.itemview_league_quick.view.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.MatchSource
import org.cxct.sportlottery.enum.PayLoadEnum
import org.cxct.sportlottery.network.common.GameStatus
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.TimeCounting
import org.cxct.sportlottery.network.service.match_status_change.MatchStatus
import org.cxct.sportlottery.ui.common.CustomLinearLayoutManager
import org.cxct.sportlottery.ui.component.overScrollView.OverScrollDecoratorHelper
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.ui.game.common.OddButtonListener
import org.cxct.sportlottery.ui.game.common.OddButtonPagerAdapter
import org.cxct.sportlottery.ui.game.common.OddStateViewHolder
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.*
import java.util.*

class SportOddAdapter(private val matchType: MatchType) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var data = listOf<MatchOdd>()
    var oddsType: OddsType = OddsType.EU
    fun setData(data: List<MatchOdd> = listOf(), oddsType: OddsType = OddsType.EU) {
        this.data = data
        this.oddsType = oddsType
        //notifyDataSetChanged()
    }

    var isTimerEnable = false
        set(value) {
            if (value != field) {
                field = value
                //notifyDataSetChanged()
            }
        }

    var leagueOddListener: LeagueOddListener? = null
    var leagueOdd: LeagueOdd? = null
    var playSelectedCodeSelectionType: Int? = null
    var playSelectedCode: String? = null
    var isNeedRecreateViews = true
    private val oddStateRefreshListener by lazy {
        object : OddStateViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
                notifyItemChanged(data.indexOf(data.find { matchOdd ->
                    matchOdd.oddsMap?.toList()
                        ?.find { map -> map.second?.find { it == odd } != null } != null
                }))
            }
        }
    }

    // region Update functions
    fun update() {
        // Update MatchOdd list
        data.forEachIndexed { index, matchOdd -> notifyItemChanged(index, matchOdd) }
    }
    // endregion

    fun updateByBetInfo(clickOdd: Odd?) {
        data.forEachIndexed { index, matchOdd ->
            matchOdd.oddsMap?.values?.forEach { oddList ->
                if (oddList?.any { it?.id == clickOdd?.id } == true) {
                    notifyItemChanged(index, Pair(PayLoadEnum.PAYLOAD_BET_INFO, matchOdd))
                    leagueOddListener?.clickOdd = null
                }
            }
        }
    }

    fun updateByPlayCate() {
        data.forEachIndexed { index, matchOdd ->
            notifyItemChanged(index, Pair(PayLoadEnum.PAYLOAD_PLAYCATE, matchOdd))
        }
    }

    fun updateBySelectCsTab(matchOdd: MatchOdd) {
        val index = data.indexOf(data.find { it == matchOdd })
        notifyItemChanged(index, matchOdd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolderHdpOu.from(parent, oddStateRefreshListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        Log.d("Hewie", "綁定：賽事($position)")
        val matchInfoList = data.mapNotNull {
            it.matchInfo
        }

        when (holder) {
            is ViewHolderHdpOu -> {
                holder.stopTimer()
                holder.bind(
                    matchType,
                    item,
                    leagueOddListener,
                    isTimerEnable,
                    oddsType,
                    matchInfoList,
                    playSelectedCodeSelectionType,
                    playSelectedCode
                )
            }
        }
    }

    // region update by payload functions
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (payloads.isNullOrEmpty()) {
            onBindViewHolder(holder, position)
            //(holder as ViewHolderHdpOu).update(matchType, data[position], leagueOddListener, isTimerEnable, oddsType, playSelectedCodeSelectionType)
        } else {
            Log.d("Hewie", "更新：賽事($position)")

            when (payloads.first()) {
                is MatchOdd -> {
                    val matchOdd = payloads.first() as MatchOdd
                    (holder as ViewHolderHdpOu).update(
                        matchType,
                        matchOdd,
                        leagueOddListener,
                        isTimerEnable,
                        oddsType,
                        playSelectedCodeSelectionType,
                        playSelectedCode
                    )
                }

                is Pair<*, *> -> {
                    (payloads.first() as Pair<*, *>).apply {
                        when (first) {
                            PayLoadEnum.PAYLOAD_BET_INFO -> {
                                (holder as ViewHolderHdpOu).updateByBetInfo(
                                    item = second as MatchOdd,
                                    leagueOddListener = leagueOddListener,
                                    oddsType = oddsType,
                                    playSelectedCodeSelectionType = playSelectedCodeSelectionType,
                                    playSelectedCode = playSelectedCode
                                )
                            }

                            PayLoadEnum.PAYLOAD_PLAYCATE -> {
                                (holder as ViewHolderHdpOu).updateByPlayCate(
                                    item = second as MatchOdd,
                                    oddsType = oddsType,
                                    playSelectedCodeSelectionType = playSelectedCodeSelectionType,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)

        when (holder) {
            is ViewHolderTimer -> holder.stopTimer()
        }
    }

    class ViewHolderHdpOu private constructor(
        itemView: View,
        private val refreshListener: OddStateChangeListener,
    ) : ViewHolderTimer(itemView) {

        fun bind(
            matchType: MatchType,
            item: MatchOdd,
            leagueOddListener: LeagueOddListener?,
            isTimerEnable: Boolean,
            oddsType: OddsType,
            matchInfoList: List<MatchInfo>,
            playSelectedCodeSelectionType: Int?,
            playSelectedCode: String?,
        ) {
            setupMatchInfo(item, matchType, matchInfoList, leagueOddListener)
            val isTimerPause = item.matchInfo?.stopped == TimeCounting.STOP.value
            setupMatchTimeAndStatus(item, matchType, isTimerEnable, isTimerPause, leagueOddListener)
            setupOddsButton(matchType,
                item,
                oddsType,
                leagueOddListener,
                playSelectedCodeSelectionType)

            //setupQuickCategory(item, oddsType, leagueOddListener)
        }

        // region update functions
        fun update(
            matchType: MatchType,
            item: MatchOdd,
            leagueOddListener: LeagueOddListener?,
            isTimerEnable: Boolean,
            oddsType: OddsType,
            playSelectedCodeSelectionType: Int?,
            playSelectedCode: String?,
        ) {
            updateMatchInfo(item, matchType)
            val isTimerPause = item.matchInfo?.stopped == TimeCounting.STOP.value
            setupMatchTimeAndStatus(item, matchType, isTimerEnable, isTimerPause, leagueOddListener)
            updateOddsButton(item, oddsType, playSelectedCodeSelectionType)
            setupCsTextLayout(matchType, item, leagueOddListener)
        }


        private fun setupCsTextLayout(
            matchType: MatchType,
            item: MatchOdd,
            leagueOddListener: LeagueOddListener?,
        ) {
            itemView.apply {
                if (matchType == MatchType.CS) {
                    ll_cs_text_layout.isVisible = true

                    //比照h5，直接使用local波膽翻譯文字
                    tv_correct_1.text = context.getText(R.string.correct)

                    var correct2 = item.playCateNameMap?.get(PlayCate.CS_1ST_SD.value)
                        ?.getPlayCateName(context) ?: ""
                    if (correct2.contains("||")) {
                        val correct2Split = correct2.split("||")
                        //將換行符後的文字移到前面顯示
                        correct2 = correct2Split[1] + correct2Split[0]
                    }
                    tv_correct_2.text = correct2

                    var correct3 = item.playCateNameMap?.get(PlayCate.LCS.value)
                        ?.getPlayCateName(context) ?: ""
                    tv_correct_3.text = correct3
                    when (item.csTabSelected) {
                        PlayCate.CS -> {
                            tv_correct_1.setTextColor(ContextCompat.getColor(context,
                                R.color.color_535D76))
                            tv_correct_2.setTextColor(ContextCompat.getColor(context,
                                R.color.color_6C7BA8))
                            tv_correct_3.setTextColor(ContextCompat.getColor(context,
                                R.color.color_6C7BA8))
                        }
                        PlayCate.CS_1ST_SD -> {
                            tv_correct_1.setTextColor(ContextCompat.getColor(context,
                                R.color.color_6C7BA8))
                            tv_correct_2.setTextColor(ContextCompat.getColor(context,
                                R.color.color_535D76))
                            tv_correct_3.setTextColor(ContextCompat.getColor(context,
                                R.color.color_6C7BA8))
                        }
                        else -> {
                            tv_correct_1.setTextColor(ContextCompat.getColor(context,
                                R.color.color_6C7BA8))
                            tv_correct_2.setTextColor(ContextCompat.getColor(context,
                                R.color.color_6C7BA8))
                            tv_correct_3.setTextColor(ContextCompat.getColor(context,
                                R.color.color_535D76))
                        }
                    }

                    tv_correct_1.setOnClickListener {
                        tv_correct_1.setTextColor(ContextCompat.getColor(context,
                            R.color.color_535D76))
                        tv_correct_2.setTextColor(ContextCompat.getColor(context,
                            R.color.color_6C7BA8))
                        tv_correct_3.setTextColor(ContextCompat.getColor(context,
                            R.color.color_6C7BA8))
                        leagueOddListener?.onClickCsTabListener(PlayCate.CS, item)
                    }

                    tv_correct_2.setOnClickListener {
                        tv_correct_1.setTextColor(ContextCompat.getColor(context,
                            R.color.color_6C7BA8))
                        tv_correct_2.setTextColor(ContextCompat.getColor(context,
                            R.color.color_535D76))
                        tv_correct_3.setTextColor(ContextCompat.getColor(context,
                            R.color.color_6C7BA8))
                        leagueOddListener?.onClickCsTabListener(PlayCate.CS_1ST_SD, item)
                    }
                    tv_correct_3.setOnClickListener {
                        tv_correct_1.setTextColor(ContextCompat.getColor(context,
                            R.color.color_6C7BA8))
                        tv_correct_2.setTextColor(ContextCompat.getColor(context,
                            R.color.color_6C7BA8))
                        tv_correct_3.setTextColor(ContextCompat.getColor(context,
                            R.color.color_535D76))
                        leagueOddListener?.onClickCsTabListener(PlayCate.LCS, item)
                    }
                } else {
                    ll_cs_text_layout.isVisible = false
                }
            }
        }

        private fun <K, V> Map<K, V>?.getPlayCateName(context: Context): String {
            var selectLanguage = LanguageManager.getSelectLanguage(context)
            val playCateName = this?.get<Any?, V>(selectLanguage.key) ?: this?.get<Any?, V>(
                LanguageManager.Language.EN.key)
            return playCateName.toString()
        }

        private fun String.updatePlayCateColor(): Spanned {
            val color = if (MultiLanguagesApplication.isNightMode) "#a3a3a3"
            else "#666666"

            return Html.fromHtml(
                when {
                    (this.contains("\n")) -> {
                        val strSplit = this.split("\n")
                        "<font color=$color>${strSplit.first()}</font><br><font color=#b73a20>${
                            strSplit.getOrNull(
                                1
                            )
                        }</font>"
                    }
                    else -> {
                        "<font color=$color>${this}</font>"
                    }
                }
            )
        }

        fun updateByBetInfo(
            item: MatchOdd,
            oddsType: OddsType,
            leagueOddListener: LeagueOddListener?,
            playSelectedCodeSelectionType: Int?,
            playSelectedCode: String?,
        ) {
            updateOddsButtonByBetInfo(item)
            itemView.quickListView?.setDatas(item,
                oddsType,
                leagueOddListener,
                playSelectedCodeSelectionType,
                playSelectedCode)
            itemView.quickListView?.refreshTab()
            itemView.quickListView?.updateQuickSelected()
        }

        fun updateByPlayCate(
            item: MatchOdd,
            oddsType: OddsType,
            playSelectedCodeSelectionType: Int?,
        ) {
            updateOddsButton(item, oddsType, playSelectedCodeSelectionType)
        }

        private fun updateMatchInfo(item: MatchOdd, matchType: MatchType) {
            itemView.league_odd_match_name_home.text = item.matchInfo?.homeName
            itemView.league_odd_match_name_away.text = item.matchInfo?.awayName
            itemView.iv_home_team_logo.setTeamLogo(item.matchInfo?.homeIcon)
            itemView.iv_away_team_logo.setTeamLogo(item.matchInfo?.awayIcon)
            setupMatchScore(item, matchType)
            setStatusTextColor(item)
            itemView.league_odd_match_play_count.text =
                item.matchInfo?.playCateNum.toString() + "+>"
            itemView.league_odd_match_favorite.isSelected = item.matchInfo?.isFavorite ?: false
//            itemView.league_odd_match_price_boost.isVisible = item.matchInfo?.eps == 1
            itemView.league_neutral.apply {
                isSelected = item.matchInfo?.neutral == 1
                isVisible = item.matchInfo?.neutral == 1
            }
//            滚球动画 直播 显示控制
            //itemView.space2.isVisible = (item.matchInfo?.eps == 1 || item.matchInfo?.liveVideo == 1)
            itemView.iv_play.isVisible =
                item.matchInfo?.liveVideo == 1 && (TimeUtil.isTimeInPlay(item.matchInfo?.startTime))
            itemView.iv_animation.isVisible =
                TimeUtil.isTimeInPlay(item.matchInfo?.startTime) && !(item.matchInfo?.trackerId.isNullOrEmpty()) && MultiLanguagesApplication.getInstance()
                    ?.getGameDetailAnimationNeedShow() == true && item.matchInfo?.liveVideo == 0
        }

        private fun setupMatchInfo(
            item: MatchOdd,
            matchType: MatchType,
            matchInfoList: List<MatchInfo>,
            leagueOddListener: LeagueOddListener?,
        ) {

            itemView.league_odd_match_name_home.text = item.matchInfo?.homeName

            itemView.league_odd_match_name_away.text = item.matchInfo?.awayName
            itemView.iv_home_team_logo.setTeamLogo(item.matchInfo?.homeIcon)
            itemView.iv_away_team_logo.setTeamLogo(item.matchInfo?.awayIcon)

            setupMatchScore(item, matchType)

            setStatusTextColor(item)

            itemView.league_odd_match_play_count.apply {
                text = item.matchInfo?.playCateNum.toString() + "+>"

                setOnClickListener {
                    leagueOddListener?.onClickPlayType(
                        item.matchInfo?.id,
                        matchInfoList,
                        if (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)) MatchType.IN_PLAY else matchType,
                        item.matchInfo?.liveVideo ?: 0
                    )
                }
            }
            itemView.league_odd_match_favorite.isSelected = item.matchInfo?.isFavorite ?: false
            itemView.league_odd_match_favorite.apply {
                isSelected = item.matchInfo?.isFavorite ?: false
                setOnClickListener {
                    leagueOddListener?.onClickFavorite(item.matchInfo?.id)
                }
            }

            itemView.league_neutral.apply {
                isSelected = item.matchInfo?.neutral == 1
                isVisible = item.matchInfo?.neutral == 1
            }

            itemView.league_odd_match_chart.apply {
                visibility =
                    if (item.matchInfo?.source == MatchSource.SHOW_STATISTICS.code) View.VISIBLE else View.GONE

                setOnClickListener {
                    leagueOddListener?.onClickStatistics(item.matchInfo?.id)
                }
            }

            itemView.lin_match.setOnClickListener {
                leagueOddListener?.onClickPlayType(
                    item.matchInfo?.id,
                    matchInfoList,
                    if (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)) MatchType.IN_PLAY else matchType,
                    item.matchInfo?.liveVideo ?: 0
                )
            }

//            itemView.league_odd_match_price_boost.isVisible = item.matchInfo?.eps == 1
            //itemView.space2.isVisible = (item.matchInfo?.eps == 1 || item.matchInfo?.liveVideo == 1)
            with(itemView.iv_play) {
                isVisible =
                    item.matchInfo?.liveVideo == 1 && (TimeUtil.isTimeInPlay(item.matchInfo?.startTime))

                setOnClickListener {
                    leagueOddListener?.onClickLiveIconListener(
                        item.matchInfo?.id,
                        matchInfoList,
                        if (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)) MatchType.IN_PLAY else matchType,
                        item.matchInfo?.liveVideo ?: 0
                    )
                }
            }
            with(itemView.iv_animation) {
                isVisible =
                    TimeUtil.isTimeInPlay(item.matchInfo?.startTime) && !(item.matchInfo?.trackerId.isNullOrEmpty()) && MultiLanguagesApplication.getInstance()
                        ?.getGameDetailAnimationNeedShow() == true && item.matchInfo?.liveVideo == 0
                setOnClickListener {
                    leagueOddListener?.onClickAnimationIconListener(
                        item.matchInfo?.id,
                        matchInfoList,
                        if (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)) MatchType.IN_PLAY else matchType,
                        item.matchInfo?.liveVideo ?: 0
                    )
                }
            }

        }

        private fun setupMatchScore(item: MatchOdd, matchType: MatchType) {
            itemView.apply {
                when {
                    matchType != MatchType.IN_PLAY -> {
                        linear_layout.isVisible = true
                        content_baseball_status.isVisible = false
                    }
                    else -> {
                        when (item.matchInfo?.gameType) {
                            GameType.BB.key -> {
                                linear_layout.isVisible = false
                                content_baseball_status.isVisible = true
                            }
                            else -> {
                                linear_layout.isVisible = true
                                content_baseball_status.isVisible = false
                            }
                        }

                    }
                }
            }

            when (item.matchInfo?.socketMatchStatus) {
                //20220507 status:999 邏輯變更 隱藏分數 -> 賽事狀態變為滾球
                /*GameMatchStatus.HIDE_SCORE.value -> {
                    hideMatchScoreText()
                }*/
                else -> {
                    when (item.matchInfo?.gameType) {
                        GameType.VB.key -> setVbScoreText(matchType, item)
                        GameType.TN.key -> setTnScoreText(matchType, item)
                        GameType.FT.key -> setFtScoreText(matchType, item)
                        GameType.BK.key -> setBkScoreText(matchType, item)
                        GameType.TT.key -> setVbScoreText(matchType, item)
                        GameType.BM.key -> setBmScoreText(matchType, item)
                        GameType.BB.key -> setBbScoreText(matchType, item)
                        else -> setBkScoreText(matchType, item)
                    }
                }
            }
        }

        private fun setFtScoreText(matchType: MatchType, item: MatchOdd) {
            itemView.apply {
                setScoreTextAtFront(item)
                setCardText(matchType, item)
                setFbKicks(matchType, item)
            }
        }

        private fun setBkScoreText(matchType: MatchType, item: MatchOdd) {
            itemView.apply {
                setScoreTextAtFront(item)
            }
        }

        private fun setVbScoreText(matchType: MatchType, item: MatchOdd) {
            itemView.apply {
                setAllScoreTextAtBottom(matchType, item)
                setSptText(item, matchType)
            }
        }

        private fun setTnScoreText(matchType: MatchType, item: MatchOdd) {

            itemView.apply {

                setAllScoreTextAtBottom(matchType, item)
                setSptText(item, matchType)

            }
        }

        private fun setBmScoreText(matchType: MatchType, item: MatchOdd) {
            itemView.apply {
                setAllScoreTextAtBottom(matchType, item)
                setSptText(item, matchType)
            }
        }

        private fun setBbScoreText(matchType: MatchType, item: MatchOdd) {
            if (matchType == MatchType.IN_PLAY) {
                itemView.apply {
                    setScoreTextAtFront(item)
                    if (item.matchInfo?.attack.equals("H")) {
                        ic_attack_h.visibility = View.VISIBLE
                        ic_attack_c.visibility = View.INVISIBLE
                    } else {
                        ic_attack_h.visibility = View.INVISIBLE
                        ic_attack_c.visibility = View.VISIBLE
                    }

                    league_odd_match_bb_status.apply {
                        text = item.matchInfo?.statusName18n
                        isVisible = true
                    }

                    txvOut.apply {
                        text = this.context.getString(R.string.game_out,
                            item.matchInfo?.outNumber ?: "")
                        isVisible = true
                    }

                    league_odd_match_halfStatus.apply {
                        setImageResource(if (item.matchInfo?.halfStatus == 0) R.drawable.ic_bb_first_half else R.drawable.ic_bb_second_half)
                        isVisible = true
                    }

                    league_odd_match_basebag.apply {
                        setImageResource(
                            when {
                                item.matchInfo?.firstBaseBag == 0 && item.matchInfo.secBaseBag == 0 && item.matchInfo.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_0_0_0
                                item.matchInfo?.firstBaseBag == 1 && item.matchInfo.secBaseBag == 0 && item.matchInfo.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_1_0_0
                                item.matchInfo?.firstBaseBag == 0 && item.matchInfo.secBaseBag == 1 && item.matchInfo.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_0_1_0
                                item.matchInfo?.firstBaseBag == 0 && item.matchInfo.secBaseBag == 0 && item.matchInfo.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_0_0_1
                                item.matchInfo?.firstBaseBag == 1 && item.matchInfo.secBaseBag == 1 && item.matchInfo.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_1_1_0
                                item.matchInfo?.firstBaseBag == 1 && item.matchInfo.secBaseBag == 0 && item.matchInfo.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_1_0_1
                                item.matchInfo?.firstBaseBag == 0 && item.matchInfo.secBaseBag == 1 && item.matchInfo.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_0_1_1
                                item.matchInfo?.firstBaseBag == 1 && item.matchInfo.secBaseBag == 1 && item.matchInfo.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_1_1_1
                                else -> R.drawable.ic_bb_base_bag_0_0_0
                            }
                        )
                        isVisible = true
                    }
                }
            } else
                setBkScoreText(matchType, item)
        }

        //時間的色值同步#000000 即將開賽的Icon不改顏色，和Ian確認過
        private fun setStatusTextColor(item: MatchOdd) {
//            val color =
//                if (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)) R.color.color_CCCCCC_000000 else R.color.color_BCBCBC_666666
//            itemView.apply {
//                league_odd_match_status.setTextColor(ContextCompat.getColor(this.context, color))
//                league_odd_spt.setTextColor(ContextCompat.getColor(this.context, color))
//                league_odd_match_time.setTextColor(ContextCompat.getColor(this.context, color))
//            }
        }

        //賽制(5盤3勝 or /int)
        @SuppressLint("SetTextI18n")
        private fun setSptText(item: MatchOdd, matchType: MatchType) {
            item.matchInfo?.spt?.let {
                when {
                    TimeUtil.isTimeInPlay(item.matchInfo?.startTime) -> { //除0以外顯示
                        itemView.league_odd_spt.visibility = if (it > 0) View.VISIBLE else View.GONE
                        itemView.league_odd_spt.text = "/ $it"
                    }
                    else -> {
                        if (it == 3 || it == 5) {//除3、5以外不顯示
                            itemView.league_spt.visibility = View.VISIBLE
                            itemView.league_spt.text = when (it) {
                                3 -> itemView.context.getString(R.string.spt_number_3_2)
                                5 -> itemView.context.getString(R.string.spt_number_5_3)
                                else -> ""
                            }
                        } else {
                            itemView.league_spt.visibility = View.GONE
                        }
                    }
                }
            }
        }

        private fun View.setCardText(matchType: MatchType, item: MatchOdd) {
            league_odd_match_cards_home.apply {
                visibility = when {
                    TimeUtil.isTimeInPlay(item.matchInfo?.startTime)
                            && (item.matchInfo?.homeCards ?: 0 > 0) -> View.VISIBLE
                    else -> View.GONE
                }
                text = (item.matchInfo?.homeCards ?: 0).toString()
            }
            league_odd_match_cards_away.apply {
                visibility = when {
                    TimeUtil.isTimeInPlay(item.matchInfo?.startTime)
                            && (item.matchInfo?.awayCards ?: 0 > 0) -> View.VISIBLE
                    else -> View.GONE
                }
                text = (item.matchInfo?.awayCards ?: 0).toString()
            }
        }

        private fun View.setFbKicks(matchType: MatchType, item: MatchOdd) {
            league_corner_kicks.apply {
                visibility = when {
                    TimeUtil.isTimeInPlay(item.matchInfo?.startTime)
                            && (item.matchInfo?.homeCornerKicks ?: 0 > 0 || item.matchInfo?.awayCornerKicks ?: 0 > 0) -> View.VISIBLE
                    else -> View.GONE
                }
                text = (item.matchInfo?.homeCornerKicks
                    ?: 0).toString() + "-" + (item.matchInfo?.awayCornerKicks ?: 0)
            }
        }

        private fun View.setScoreTextAtFront(item: MatchOdd) {
            league_odd_match_score_home.apply {
                visibility = when (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)) {
                    true -> View.VISIBLE
                    else -> View.GONE
                }
                text = (item.matchInfo?.homeScore ?: 0).toString()
            }

            league_odd_match_score_away.apply {
                visibility = when (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)) {
                    true -> View.VISIBLE
                    else -> View.GONE
                }
                text = (item.matchInfo?.awayScore ?: 0).toString()
            }
        }

        private val isScoreTextVisible = { _: MatchType, item: MatchOdd ->
            when (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)) {
                true -> View.VISIBLE
                else -> View.GONE
            }
        }


        private fun View.setAllScoreTextAtBottom(matchType: MatchType, item: MatchOdd) {
            item.matchInfo?.matchStatusList?.let {
                setupStatusPeriods(it)
            }
        }

        /**
         * 网球和羽毛球  排球，乒乓球 显示局比分
         */
        private fun setupStatusPeriods(matchStatusList: List<MatchStatus>) {
            var spanny = Spanny()
            matchStatusList.forEachIndexed { index, it ->
                val spanScore = "${it.homeScore ?: 0}-${it.awayScore ?: 0}"
                //9表示已结束，其他代表进行中的
                if (index < matchStatusList.lastIndex) {
                    spanny.append(spanScore)
                    spanny.append("  ")
                } else {
                    spanny.append(spanScore,
                        ForegroundColorSpan(itemView.context.getColor(R.color.color_025BE8)))
                }
            }
            itemView.tv_periods_score.isVisible = true
            itemView.tv_periods_score.text = spanny
        }


        private fun setupMatchTimeAndStatus(
            item: MatchOdd,
            matchType: MatchType,
            isTimerEnable: Boolean,
            isTimerPause: Boolean,
            leagueOddListener: LeagueOddListener?,
        ) {

            /* TODO 依目前開發方式優化，將狀態和時間保存回 viewModel 於下次刷新頁面前 api 取得資料時先行代入相關 data 內，
                此處倒數計時前須先設置時間及狀態，可解決控件短暫空白。(賽事狀態已於 BaseFavoriteViewModel #1 處調整過)*/

            when {
                TimeUtil.isTimeInPlay(item.matchInfo?.startTime) -> {
                    val socketValue = item.matchInfo?.socketMatchStatus

                    if (needCountStatus(socketValue)) {
                        itemView.league_odd_match_time.visibility = View.VISIBLE
                        listener = object : TimerListener {
                            override fun onTimerUpdate(timeMillis: Long) {
                                if (timeMillis > 1000) {
                                    itemView.league_odd_match_time.text =
                                        TimeUtil.longToMmSs(timeMillis)
                                } else {
                                    itemView.league_odd_match_time.text =
                                        itemView.context.getString(R.string.time_up)
                                }
                                item.matchInfo?.leagueTime = (timeMillis / 1000).toInt()
                            }
                        }

                        updateTimer(
                            isTimerEnable,
                            isTimerPause,
                            item.matchInfo?.leagueTime ?: 0,
                            (item.matchInfo?.gameType == GameType.BK.key ||
                                    item.matchInfo?.gameType == GameType.RB.key ||
                                    item.matchInfo?.gameType == GameType.AFT.key)
                        )

                    } else {
                        itemView.league_odd_match_time.visibility = View.GONE
                    }
//                    itemView.league_odd_match_remain_time_icon.visibility = View.GONE
                }

                TimeUtil.isTimeAtStart(item.matchInfo?.startTime) -> {
                    listener = object : TimerListener {
                        override fun onTimerUpdate(timeMillis: Long) {
                            if (timeMillis > 1000) {
                                val min = TimeUtil.longToMinute(timeMillis)
                                itemView.league_odd_match_time.text = String.format(
                                    itemView.context.resources.getString(R.string.at_start_remain_minute),
                                    min
                                )
                            } else {
                                //等待Socket更新
                                itemView.league_odd_match_time.text = String.format(
                                    itemView.context.resources.getString(R.string.at_start_remain_minute),
                                    0
                                )
                            }
                            item.matchInfo?.remainTime = timeMillis
//                            itemView.league_odd_match_remain_time_icon.visibility = View.VISIBLE
                        }
                    }

                    item.matchInfo?.remainTime?.let { remainTime ->
                        updateTimer(
                            true,
                            isTimerPause,
                            (remainTime / 1000).toInt(),
                            true
                        )
                    }
                }
                else -> {
                    itemView.league_odd_match_time.text =
                        TimeUtil.timeFormat(item.matchInfo?.startTime, "HH:mm")
//                    itemView.league_odd_match_remain_time_icon.visibility = if (TimeUtil.isTimeToday(item.matchInfo?.startTime)) View.VISIBLE else View.GONE
                }
            }

            setStatusText(item, matchType)
            setTextViewStatus(item, matchType)
        }

        private fun setStatusText(item: MatchOdd, matchType: MatchType) {
            itemView.league_odd_match_status.text = when {
                (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)
                        && item.matchInfo?.status == GameStatus.POSTPONED.code
                        && (item.matchInfo.gameType == GameType.FT.name || item.matchInfo.gameType == GameType.BK.name || item.matchInfo.gameType == GameType.TN.name)) -> {
                    itemView.context.getString(R.string.game_postponed)
                }
                TimeUtil.isTimeInPlay(item.matchInfo?.startTime) -> {
                    if (item.matchInfo?.statusName18n != null) {
                        item.matchInfo.statusName18n
                    } else {
                        ""
                    }
                }
                else -> {
                    if (TimeUtil.isTimeToday(item.matchInfo?.startTime))
                        itemView.context.getString((R.string.home_tab_today))
                    else
                        item.matchInfo?.startDateDisplay
                }
            }
        }

        private fun setTextViewStatus(item: MatchOdd, matchType: MatchType) {
            when {
                (TimeUtil.isTimeInPlay(item.matchInfo?.startTime) && item.matchInfo?.status == GameStatus.POSTPONED.code && (item.matchInfo.gameType == GameType.FT.name || item.matchInfo.gameType == GameType.BK.name || item.matchInfo.gameType == GameType.TN.name)) -> {
                    itemView.league_odd_spt.visibility = View.GONE
                    itemView.league_odd_match_time.visibility = View.GONE
                }

                TimeUtil.isTimeInPlay(item.matchInfo?.startTime) -> {
                    if (item.matchInfo?.statusName18n != null) {
                        itemView.league_odd_match_status.visibility = View.VISIBLE
                        (itemView.league_odd_match_status.layoutParams as LinearLayout.LayoutParams).marginEnd =
                            6
                    } else {
                        (itemView.league_odd_match_status.layoutParams as LinearLayout.LayoutParams).marginEnd =
                            0
                    }
                }
                TimeUtil.isTimeAtStart(item.matchInfo?.startTime) -> {
                    itemView.league_odd_match_status.visibility = View.GONE
                }

            }
        }

        val linearLayoutManager by lazy {
            CustomLinearLayoutManager(
                itemView.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }

        val oddButtonPagerAdapter = OddButtonPagerAdapter()
        private fun setupOddsButton(
            matchType: MatchType,
            item: MatchOdd,
            oddsType: OddsType,
            leagueOddListener: LeagueOddListener?,
            playSelectedCodeSelectionType: Int?,
        ) {
            itemView.rv_league_odd_btn_pager_main.apply {
                linearLayoutManager.isAutoMeasureEnabled = false
                layoutManager = linearLayoutManager
                setHasFixedSize(true)
                (rv_league_odd_btn_pager_main.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
                    false

                this.adapter = oddButtonPagerAdapter.apply {
                    stateRestorationPolicy = StateRestorationPolicy.PREVENT
                    //this.odds = item.oddsMap ?: mutableMapOf()
                    //this.oddsType = oddsType
                    this.matchType = matchType
                    this.listener =
                        OddButtonListener { matchInfo, odd, playCateCode, playCateName, betPlayCateName ->
                            leagueOddListener?.onClickBet(
                                matchInfo,
                                odd,
                                playCateCode,
                                betPlayCateName,
                                item.betPlayCateNameMap
                            )
                        }
                }
                Log.d("Hewie4",
                    "綁定(${item.matchInfo?.homeName})：item.oddsMap.size => ${item.oddsMap?.size}")
                updateOddsButton(item, oddsType, playSelectedCodeSelectionType)

                OverScrollDecoratorHelper.setUpOverScroll(this,
                    OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)
                itemView.hIndicator.bindRecyclerView(this)
            }

        }

        private fun updateOddsButton(
            item: MatchOdd,
            oddsType: OddsType,
            playSelectedCodeSelectionType: Int?,
        ) {
            itemView.rv_league_odd_btn_pager_main.apply {
                oddButtonPagerAdapter.setData(
                    item.matchInfo,
                    item.oddsSort,
                    item.playCateNameMap,
                    item.betPlayCateNameMap,
                    playSelectedCodeSelectionType,
                    item
                )
                oddButtonPagerAdapter.apply {
                    stateRestorationPolicy = StateRestorationPolicy.PREVENT
                    this.oddsType = oddsType
                    this.odds = item.oddsMap ?: mutableMapOf()
                    //update()
                    //notifyDataSetChanged() // TODO
                }
            }
        }

        private fun updateOddsButtonByBetInfo(item: MatchOdd) {
            oddButtonPagerAdapter.odds = item.oddsMap ?: mutableMapOf()
        }

        companion object {
            fun from(parent: ViewGroup, refreshListener: OddStateChangeListener): ViewHolderHdpOu {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view =
                    layoutInflater.inflate(R.layout.item_sport_odd, parent, false)

                return ViewHolderHdpOu(view, refreshListener)
            }
        }

        override val oddStateChangeListener: OddStateChangeListener
            get() = refreshListener
    }

    abstract class ViewHolderTimer(itemView: View) : OddStateViewHolder(itemView) {
        interface TimerListener {
            fun onTimerUpdate(timeMillis: Long)
        }

        protected var listener: TimerListener? = null

        private var timer: Timer? = null

        fun updateTimer(
            isTimerEnable: Boolean,
            isTimerPause: Boolean,
            startTime: Int,
            isDecrease: Boolean,
        ) {
            when (isTimerEnable) {
                false -> {
                    stopTimer()
                }

                true -> {
                    startTimer(isTimerPause, startTime, isDecrease)
                }

            }
        }

        private fun startTimer(isTimerPause: Boolean, startTime: Int, isDecrease: Boolean) {
            var timeMillis = startTime * 1000L
            stopTimer()
            Handler(Looper.getMainLooper()).post {
                listener?.onTimerUpdate(timeMillis)
            }

            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    when (isDecrease) {
                        true -> {
                            if (!isTimerPause) timeMillis -= 1000
                        }
                        false -> {
                            if (!isTimerPause) timeMillis += 1000
                        }
                    }

                    if (timeMillis > 0) {
                        Handler(Looper.getMainLooper()).post {
                            listener?.onTimerUpdate(timeMillis)
                        }
                    }
                }
            }, 1000L, 1000L)
        }

        fun stopTimer() {
            timer?.cancel()
            timer = null
        }
    }

}
