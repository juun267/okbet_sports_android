package org.cxct.sportlottery.ui.game.home

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.home_highlight_item.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.enum.MatchSource
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.game.common.OddStateViewHolder
import org.cxct.sportlottery.ui.game.interfaces.UpdateHighLightInterface
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.setTextTypeFace

class ViewHolderHdpOu(itemView: View) : OddStateViewHolder(itemView) {
    private var oddList: MutableList<Odd?>? = null

    private var matchType: MatchType = MatchType.TODAY
    var onClickOddListener: OnClickOddListener? = null
    var onClickMatchListener: OnSelectItemListener<MatchOdd>? = null //賽事畫面跳轉
    var onClickFavoriteListener: OnClickFavoriteListener? = null
    var onClickStatisticsListener: OnClickStatisticsListener? = null
    private val mOddStateRefreshListener by lazy {
        object : OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) { }
        }
    }
    var mMatchOdd: MatchOdd? = null

    fun bind(data: MatchOdd, lastData: MatchOdd, oddsType: OddsType) {
        itemView.testId.text = "${data.matchInfo?.leagueId} - ${data.matchInfo?.id}"
        mMatchOdd = data
        setTitle(data,lastData)
        setupOddList(data)
        setupMatchInfo(data)
        setupTime(data)
        setupOddButton(data, oddsType)

        itemView.iv_match_in_play.visibility =
            if (matchType == MatchType.AT_START) View.VISIBLE else View.GONE

        itemView.iv_match_price.visibility =
            if (data.matchInfo?.eps == 1) View.VISIBLE else View.GONE

        itemView.highlight_match_info.setOnClickListener {
            onClickMatchListener?.onClick(data)
        }

        itemView.tv_match_play_type_count.setOnClickListener {
            onClickMatchListener?.onClick(data)
        }

        with(itemView.btn_chart) {
            visibility =
                if (data.matchInfo?.source == MatchSource.HIDE_STATISTICS.code) View.GONE else View.VISIBLE

            setOnClickListener {
                onClickStatisticsListener?.onClickStatistics(data.matchInfo?.id)
            }
        }
//        subscribeChannelHall(mMatchOdd?.matchInfo?.gameType, mMatchOdd?.matchInfo?.id)
    }

    fun getUpdateHighLightInterface(): UpdateHighLightInterface {
        return object: UpdateHighLightInterface {
            override fun doUpdate(data: MatchOdd, lastData: MatchOdd, oddsType: OddsType) {
                itemView.iv_match_in_play.visibility =
                    if (matchType == MatchType.AT_START) View.VISIBLE else View.GONE

                itemView.iv_match_price.visibility =
                    if (data.matchInfo?.eps == 1) View.VISIBLE else View.GONE

                itemView.highlight_match_info.setOnClickListener {
                    onClickMatchListener?.onClick(data)
                }

                itemView.tv_match_play_type_count.setOnClickListener {
                    onClickMatchListener?.onClick(data)
                }

                with(itemView.btn_chart) {
                    visibility =
                        if (data.matchInfo?.source == MatchSource.HIDE_STATISTICS.code) View.GONE else View.VISIBLE

                    setOnClickListener {
                        onClickStatisticsListener?.onClickStatistics(data.matchInfo?.id)
                    }
                }
            }
        }
    }

    private fun setTitle(data: MatchOdd, lastData: MatchOdd) {
        try {
            itemView.apply {
                when {
                    data.matchInfo?.isStartPosition == true -> { // TODO 這裡因為列表合併為一，所以position不再是0，需要判定這個區塊的第一筆
                        ll_highlight_type.visibility = View.VISIBLE
                        tv_game_type.isVisible = true
                        tv_play_type_highlight.isVisible = true

                        val playCate = if(data.oddsSort?.split(",")?.size?:0 > 0) data.oddsSort?.split(",")
                            ?.getOrNull(0) else data.oddsSort

                        tv_play_type_highlight.text =
                            data.playCateNameMap?.get(playCate)
                                ?.get(LanguageManager.getSelectLanguage(context).key) ?: ""
                    }
                    TimeUtil.isTimeToday(data.matchInfo?.startTime) && !TimeUtil.isTimeToday(
                        lastData.matchInfo?.startTime
                    ) -> {
                        ll_highlight_type.visibility = View.VISIBLE

                        tv_game_type.isVisible = true
                        tv_play_type_highlight.visibility = View.INVISIBLE
                    }
                    !TimeUtil.isTimeToday(data.matchInfo?.startTime) && TimeUtil.isTimeToday(
                        lastData.matchInfo?.startTime
                    ) -> {
                        ll_highlight_type.visibility = View.VISIBLE

                        tv_game_type.isVisible = true
                        tv_play_type_highlight.visibility = View.INVISIBLE
                    }
                    else -> {
                        ll_highlight_type.visibility = View.GONE
                    }
                }
                if(data.matchInfo?.isStartPosition == false) ll_highlight_type.visibility = View.GONE
                tv_game_type.text = if (TimeUtil.isTimeToday(data.matchInfo?.startTime)) {
                    resources.getString(R.string.home_tab_today)
                } else {
                    "${resources.getString(TimeUtil.setupDayOfWeekAndToday(data.matchInfo?.startTime))} ${data.matchInfo?.startDateDisplay}"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupOddList(data: MatchOdd) {
        itemView.apply {

            oddList = if(data.oddsMap?.isNotEmpty() == true) {
                data.oddsMap?.iterator()?.next()?.value
            } else {
                mutableListOf()
            }
        }
    }

    private fun setupMatchInfo(data: MatchOdd) {
        itemView.apply {
            tv_game_name_home.text = data.matchInfo?.homeName
            tv_game_name_away.text = data.matchInfo?.awayName
            showStrongTeam()
            tv_match_play_type_count.text = data.matchInfo?.playCateNum?.toString()

            btn_star.apply {
                this.isSelected = data.matchInfo?.isFavorite ?: false

                setOnClickListener {
                    onClickFavoriteListener?.onClickFavorite(data.matchInfo?.id)
                }
            }
        }
    }

    private fun showStrongTeam() {
        itemView.apply {
            tv_game_name_home.apply {
                setTextTypeFace(if (oddList?.getOrNull(0)?.spread?.contains("-") == true) Typeface.BOLD else Typeface.NORMAL)
            }
            tv_game_name_away.apply {
                setTextTypeFace(if (oddList?.getOrNull(1)?.spread?.contains("-") == true) Typeface.BOLD else Typeface.NORMAL)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupTime(data: MatchOdd) {
        itemView.apply {
            if (matchType == MatchType.AT_START) {
                data.matchInfo?.timeDisplay?.let { timeDisplay ->
                    tv_match_time.text = String.format(itemView.context.resources.getString(R.string.at_start_remain_minute), timeDisplay)
                }
            } else {
                tv_match_time.text = data.matchInfo?.startTimeDisplay ?: ""
            }
        }
    }

    private fun setupOddButton(data: MatchOdd, oddsType: OddsType) {
        try {
            itemView.apply {

                val oddsSort = data.oddsSort
                val playCateName =
                    if (oddsSort?.split(",")?.size ?: 0 > 0)
                        oddsSort?.split(",")?.getOrNull(0) else oddsSort

                val playCateStr = data.playCateNameMap?.get(playCateName)
                    ?.get(LanguageManager.getSelectLanguage(context).key)

                btn_match_odd1.apply {
                    val oddFirst = oddList?.getOrNull(0)
                    this.isSelected = oddFirst?.isSelected ?: false

                    betStatus = if (oddList.isNullOrEmpty() || oddList?.size ?: 0 < 2) {
                        BetStatus.DEACTIVATED.code
                    } else {
                        oddFirst?.status ?: BetStatus.LOCKED.code
                    }

                    if (!oddList.isNullOrEmpty() && oddList?.size ?: 0 >= 2) {
                        this@ViewHolderHdpOu.setupOddState(this, oddFirst)

                        setupOdd(oddFirst, oddsType,"disable") //TODO Bill 這裡要看球種顯示 1/2 不能用disable
                        setupOddName4Home("1" , playCateName)
                        setOnClickListener {
                            if (oddList != null && oddList?.size ?: 0 >= 2) {
                                oddFirst?.let { odd ->
                                    onClickOddListener?.onClickBet(
                                        data,
                                        odd,
                                        playCateName.toString(),
                                        playCateStr,
                                        data.betPlayCateNameMap
                                    )
                                }
                            }
                        }
                    }
                }

                btn_match_odd2.apply {
                    val oddSecond = oddList?.getOrNull(1)
                    this.isSelected = oddSecond?.isSelected ?: false

                    betStatus = if (oddList.isNullOrEmpty() || oddList?.size ?: 0 < 2) {
                        BetStatus.DEACTIVATED.code
                    } else {
                        oddSecond?.status ?: BetStatus.LOCKED.code
                    }

                    if (!oddList.isNullOrEmpty() && oddList?.size ?: 0 >= 2) {
                        this@ViewHolderHdpOu.setupOddState(this, oddSecond)

                        setupOdd(oddSecond, oddsType, "disable")
                        //板球才會有三個賠率
                        if(data.matchInfo?.gameType == GameType.CK.key && oddList?.size ?: 0 > 2) setupOddName4Home("X" , playCateName)
                        else setupOddName4Home("2" , playCateName)
                        setOnClickListener {
                            if (oddList != null && oddList?.size ?: 0 >= 2) {
                                oddSecond?.let { odd ->
                                    onClickOddListener?.onClickBet(
                                        data,
                                        odd,
                                        playCateName.toString(),
                                        playCateStr,
                                        data.betPlayCateNameMap
                                    )
                                }
                            }
                        }
                    }
                }

                btn_match_odd3.apply {
                    isVisible =  data.matchInfo?.gameType == GameType.CK.key && oddList?.size ?: 0 > 2
                    val oddThird = oddList?.getOrNull(2)
                    this.isSelected = oddThird?.isSelected ?: false

                    betStatus = if (oddList.isNullOrEmpty() || oddList?.size ?: 0 < 2) {
                        BetStatus.DEACTIVATED.code
                    } else {
                        oddThird?.status ?: BetStatus.LOCKED.code
                    }

                    if (!oddList.isNullOrEmpty() && oddList?.size ?: 0 >= 2) {
                        this@ViewHolderHdpOu.setupOddState(this, oddThird)

                        setupOdd(oddThird, oddsType, "disable")
                        setupOddName4Home("2" , playCateName)
                        setOnClickListener {
                            if (oddList != null && oddList?.size ?: 0 >= 2) {
                                oddThird?.let { odd ->
                                    onClickOddListener?.onClickBet(
                                        data,
                                        odd,
                                        playCateName.toString(),
                                        playCateStr,
                                        data.betPlayCateNameMap
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override val oddStateChangeListener: OddStateChangeListener
        get() = mOddStateRefreshListener
}