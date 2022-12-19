package org.cxct.sportlottery.ui.game.home.highlight

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.home_highlight_item.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.matchCategory.result.OddData
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.game.common.OddStateViewHolder
import org.cxct.sportlottery.ui.game.home.OnClickFavoriteListener
import org.cxct.sportlottery.ui.game.home.OnClickOddListener
import org.cxct.sportlottery.ui.game.home.OnClickStatisticsListener
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.MatchOddUtil.updateDiscount
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.setTextTypeFace
import kotlin.collections.forEach as forEach

class RvHighlightAdapter : RecyclerView.Adapter<RvHighlightAdapter.ViewHolderHdpOu>() {

    private var playCateNameMap: Map<String?, Map<String?, String?>?>? = mapOf()
    var dataList = mutableListOf<MatchOdd>()
    private var discount: Float = 1.0F
    private var oddsType: OddsType = OddsType.EU

    fun setData(
        sportCode: String?,
        newList: List<OddData>?,
        selectedOdds: MutableList<String>,
        newPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?
    ) {
        processData(sportCode, newList, selectedOdds, newPlayCateNameMap)
    }

    private fun processData(
        sportCode: String?,
        newList: List<OddData>?,
        selectedOdds: MutableList<String>,
        newPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?
    ) {
        var newDataList = newList?.map { it ->
                val matchInfo = MatchInfo(
                    gameType = sportCode,
                    awayName = it.matchInfo?.awayName ?: "",
                    endTime = it.matchInfo?.endTime,
                    homeName = it.matchInfo?.homeName ?: "",
                    id = it.matchInfo?.id ?: "",
                    playCateNum = it.matchInfo?.playCateNum ?: 0,
                    startTime = it.matchInfo?.startTime,
                    eps = it.matchInfo?.eps,
                    spt = it.matchInfo?.spt,
                    liveVideo = it.matchInfo?.liveVideo,
                    leagueName = it.matchInfo?.leagueName,
                    status = it.matchInfo?.status ?: -1,
                    source = it.matchInfo?.source,
                    parlay = it.matchInfo?.parlay
                ).apply {
                    startDateDisplay = TimeUtil.timeFormat(this.startTime, "MM/dd")
                    startTimeDisplay = TimeUtil.timeFormat(this.startTime, "HH:mm")
                    isAtStart = TimeUtil.isTimeAtStart(this.startTime)
                }


                val odds: MutableMap<String, MutableList<Odd>?> = mutableMapOf()
                it.oddsMap?.forEach { (key, value) ->
                    value?.forEach { odd ->
                        odd?.id?.let {
                            odd?.isSelected = selectedOdds.contains(it)
                        }
                    }
                    odds[key] = value?.toMutableList()
                }

                MatchOdd(
                    it.betPlayCateNameMap,
                    it.playCateNameMap,
                    matchInfo,
                    odds,
                    it.dynamicMarkets,
                    it.quickPlayCateList,
                    it.oddsSort
                )
            } ?: listOf()
        dataList = newDataList.toMutableList()
        playCateNameMap = newPlayCateNameMap
    }

    fun getData() = dataList

    //TODO simon test review 精選賽事是不是一定是 MatchType.TODAY，是的話可以再簡化判斷邏輯
    private var matchType: MatchType = MatchType.TODAY

    var onClickOddListener: OnClickOddListener? = null

    var onClickMatchListener: OnSelectItemListener<MatchOdd>? = null //賽事畫面跳轉

    var onClickFavoriteListener: OnClickFavoriteListener? = null

    var onClickStatisticsListener: OnClickStatisticsListener? = null

    private val mOddStateRefreshListener by lazy {
        object : OddStateViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) { }
        }
    }

    fun notifyTimeChanged(diff: Int) {
        var isUpdate = false
        dataList.forEach { odd ->
            odd.matchInfo?.let {
                it.isAtStart = TimeUtil.isTimeAtStart(it.startTime)
                if (it.isAtStart == true) {
                    it.remainTime = it.startTime?.minus(System.currentTimeMillis())
                    it.remainTime?.let { remainTime ->
                        val newTimeDisplay = TimeUtil.longToMinute(remainTime * 1000)
                        if (it.timeDisplay != newTimeDisplay) {
                            it.timeDisplay = newTimeDisplay
                            isUpdate = true
                        }
                    }
                }
            }
        }
        if (isUpdate) {
            Handler(Looper.getMainLooper()).post {
                notifyDataSetChanged()
            }
        }
    }

    fun notifyOddsDiscountChanged(discount: Float) {
        dataList.forEach { matchOdd ->
            matchOdd.oddsMap?.forEach { (key, value) ->
                value?.forEach { odd ->
                    odd?.updateDiscount(this.discount, discount)
                }
            }
        }
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
        this.discount = discount
    }

    fun notifyOddsTypeChanged(oddsType: OddsType) {
        this.oddsType = oddsType
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    fun notifySelectedOddsChanged(selectedOdds: MutableList<String>) {
        dataList.forEach { matchOdd ->
            matchOdd.oddsMap?.forEach { (key, value) ->
                value?.forEach { odd ->
                    odd?.id?.let {
                        odd?.isSelected = selectedOdds.contains(it)
                    }
                }
            }
        }
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderHdpOu {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_highlight_item, parent, false)
        return ViewHolderHdpOu(view)
    }

    override fun onBindViewHolder(holder: ViewHolderHdpOu, position: Int) {
        try {
            val data = dataList[position]
            var lastIndex = if (position > 0) position - 1 else 0
            val lastData = dataList[lastIndex]
            holder.bind(data,lastData,playCateNameMap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int = dataList.size

    inner class ViewHolderHdpOu(itemView: View) : OddStateViewHolder(itemView) {
        private var oddList: MutableList<Odd?>? = null

        fun bind(
            data: MatchOdd,
            lastData: MatchOdd,
            playCateNameMap: Map<String?, Map<String?, String?>?>?
        ) {
            setTitle(data,lastData,playCateNameMap)
            setupOddList(data)
            setupMatchInfo(data)
            setupTime(data)
            setupOddButton(data)

            itemView.iv_match_in_play.visibility =
                if (matchType == MatchType.AT_START) View.VISIBLE else View.GONE

//            itemView.iv_match_price.visibility = if (data.matchInfo?.eps == 1) View.VISIBLE else View.GONE

            itemView.iv_match_price.visibility = View.GONE

            itemView.highlight_match_info.setOnClickListener {
                onClickMatchListener?.onClick(data)
            }

            itemView.tv_match_play_type_count.setOnClickListener {
                onClickMatchListener?.onClick(data)
            }

            itemView.btn_chart.setOnClickListener {
                onClickStatisticsListener?.onClickStatistics(data.matchInfo?.id)
            }
        }

        private fun setTitle(
            data: MatchOdd,
            lastData: MatchOdd,
            playCateNameMap: Map<String?, Map<String?, String?>?>?
        ) {
            try {
                itemView.apply {
                    when {
                        bindingAdapterPosition == 0 -> {
                            ll_highlight_type.visibility = View.VISIBLE
                            tv_game_type.isVisible = true
                            tv_play_type_highlight.isVisible = true

                            val playCate = if(data.oddsSort?.split(",")?.size?:0 > 0) data.oddsSort?.split(",")
                                ?.getOrNull(0) else data.oddsSort

                            tv_play_type_highlight.text = playCateNameMap?.get( playCateNameMap?.iterator()?.next()?.key)?.get(LanguageManager.getSelectLanguage(context).key) ?: ""
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

                    tv_game_type.text = if (TimeUtil.isTimeToday(data.matchInfo?.startTime)) {
                        resources.getString(R.string.home_tab_today)
                    } else {
                       data.matchInfo?.startDateDisplay
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun setupOddList(data: MatchOdd) {
            itemView.apply {val oddsSort = data.oddsSort
                val playCateName =
                    if (oddsSort?.split(",")?.size ?: 0 > 0)
                        oddsSort?.split(",")?.getOrNull(0) else oddsSort
                data.oddsMap?.let {
                    val odds = it[playCateName]
                    oddList = if(odds?.isNotEmpty() == true) {
                        odds.toMutableList()
                    } else {
                        mutableListOf()
                    }
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
                if (TimeUtil.isTimeInPlay(data.matchInfo?.startTime)){
                    tv_match_time.setTextColor(ContextCompat.getColor(context,R.color.color_CCCCCC_000000))
                }else{
                    tv_match_time.setTextColor(ContextCompat.getColor(context,R.color.color_BCBCBC_666666))
                }
                if (matchType == MatchType.AT_START) {
                    data.matchInfo?.timeDisplay?.let { timeDisplay ->
                        tv_match_time.text = String.format(itemView.context.resources.getString(R.string.at_start_remain_minute), timeDisplay)
                    }
                } else {
                    tv_match_time.text = data.matchInfo?.startTimeDisplay ?: ""
                }
            }
        }

        private fun setupOddButton(data: MatchOdd) {
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
                                        it.isSelected = !it.isSelected
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
                                        it.isSelected = !it.isSelected
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
                                        it.isSelected = !it.isSelected
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

}