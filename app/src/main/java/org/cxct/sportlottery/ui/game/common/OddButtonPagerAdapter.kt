package org.cxct.sportlottery.ui.game.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.button_odd_detail.view.*
import kotlinx.android.synthetic.main.itemview_odd_btn_2x2_v4.view.*
import kotlinx.android.synthetic.main.view_odd_btn_column_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.game.PlayCateUtils
import org.cxct.sportlottery.ui.game.widget.OddsButton
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.LanguageManager


class OddButtonPagerAdapter(private val matchInfo: MatchInfo?) :
    RecyclerView.Adapter<OddButtonPagerViewHolder>() {

    var odds: Map<String, List<Odd?>> = mapOf()
        set(value) {
            field = value

            data = field.keys.withIndex().groupBy {
                it.index / 2
            }.map {
                it.value.map { it.value }
            }.map {
                it.map { playCate ->
                    playCate to field[playCate]
                }
            }
        }

    var oddsType: OddsType = OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    private var data: List<List<Pair<String, List<Odd?>?>>> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var listener: OddButtonListener? = null

    private val oddStateRefreshListener by lazy {
        object : OddStateViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
                notifyItemChanged(data.indexOf(data.find {
                    it.any {
                        it.second?.any {
                            it == odd
                        } ?: false
                    }
                }))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OddButtonPagerViewHolder {
        return OddButtonPagerViewHolder.from(parent, oddStateRefreshListener)
    }

    override fun onBindViewHolder(holder: OddButtonPagerViewHolder, position: Int) {
        holder.bind(
            matchInfo,
            listOf(
                Pair(
                    data[position].getOrNull(0)?.first,
                    data[position].getOrNull(0)?.second?.sortedBy { it?.id?.toDouble() }),
                Pair(
                    data[position].getOrNull(1)?.first,
                    data[position].getOrNull(1)?.second?.sortedBy { it?.id?.toDouble() })
            ),
            oddsType,
            listener
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }
}

class OddButtonPagerViewHolder private constructor(
    itemView: View,
    private val oddStateRefreshListener: OddStateChangeListener
) :
    OddStateViewHolder(itemView) {

    fun bind(
        matchInfo: MatchInfo?,
        odds: List<Pair<String?, List<Odd?>?>>?,
        oddsType: OddsType,
        oddButtonListener: OddButtonListener?,
    ) {
        setupOddsButton(
            itemView.odd_btn_row1_type,
            itemView.odd_btn_row1_home,
            itemView.odd_btn_row1_away,
            itemView.odd_btn_row1_draw,
            matchInfo, odds?.get(0), oddsType, oddButtonListener
        )

        setupOddsButton(
            itemView.odd_btn_row2_type,
            itemView.odd_btn_row2_home,
            itemView.odd_btn_row2_away,
            itemView.odd_btn_row2_draw,
            matchInfo, odds?.get(1), oddsType, oddButtonListener
        )
    }

    private fun setupOddsButton(
        oddBtnType: TextView,
        oddBtnHome: OddsButton,
        oddBtnAway: OddsButton,
        oddBtnDraw: OddsButton,
        matchInfo: MatchInfo?,
        odds: Pair<String?, List<Odd?>?>?,
        oddsType: OddsType,
        oddButtonListener: OddButtonListener?,
    ) {

        val playCateName = PlayCateUtils
            .getPlayCateTitleResId(odds?.first ?: "", matchInfo?.gameType)?.let {
                itemView.context.getString(it)
            } ?: ""

        oddBtnType.text = playCateName

        oddBtnHome.apply homeButtonSettings@{
            when {
                (odds?.second == null) -> {
                    betStatus = BetStatus.DEACTIVATED.code
                    return@homeButtonSettings
                }
                (odds.second?.size ?: 0 < 2 || odds.second?.getOrNull(0)?.odds == null) -> {
                    betStatus = BetStatus.LOCKED.code
                    return@homeButtonSettings
                }
                else -> {
                    betStatus = odds.second?.getOrNull(0)?.status
                }
            }

            tv_name.apply {
                visibility = when {
                    PlayCateUtils.getOUSeries().map { it.value }
                        .contains(odds.first) -> View.VISIBLE
                    else -> {
                        when (!odds.second?.getOrNull(0)?.spread.isNullOrEmpty()) {
                            true -> View.INVISIBLE
                            false -> View.GONE
                        }
                    }
                }

                text = when {
                    PlayCateUtils.getOUSeries().map { it.value }.contains(odds.first) -> {
                        odds.second?.getOrNull(0)?.nameMap?.get(
                            LanguageManager.getSelectLanguage(
                                context
                            ).key
                        ) ?: odds.second?.getOrNull(0)?.name
                    }
                    else -> ""
                }
            }

            tv_spread.apply {
                visibility = when (!odds.second?.getOrNull(0)?.spread.isNullOrEmpty()) {
                    true -> View.VISIBLE
                    false -> {
                        when {
                            PlayCateUtils.getOUSeries().map { it.value }
                                .contains(odds.first) -> View.INVISIBLE
                            else -> View.GONE
                        }
                    }
                }

                text = odds.second?.getOrNull(0)?.spread ?: ""
            }

            tv_odds.text = when (oddsType) {
                OddsType.EU -> odds.second?.getOrNull(0)?.odds.toString()
                OddsType.HK -> odds.second?.getOrNull(0)?.hkOdds.toString()
            }

            this@OddButtonPagerViewHolder.setupOddState(this, odds.second?.getOrNull(0))

            isSelected = odds.second?.getOrNull(0)?.isSelected ?: false

            setOnClickListener { _ ->
                odds.second?.getOrNull(0)?.let { odd ->

                    val playName = when {
                        PlayCateUtils.getOUSeries().map { it.value }
                            .contains(odds.first) -> {
                            odds.second?.getOrNull(0)?.nameMap?.get(
                                LanguageManager.getSelectLanguage(
                                    context
                                ).key
                            ) ?: odds.second?.getOrNull(0)?.name
                        }
                        else -> {
                            matchInfo?.homeName
                        }
                    } ?: ""

                    oddButtonListener?.onClickBet(
                        matchInfo,
                        odd,
                        playCateName,
                        playName
                    )
                }
            }
        }

        oddBtnAway.apply awayButtonSettings@{
            when {
                (odds?.second == null) -> {
                    betStatus = BetStatus.DEACTIVATED.code
                    return@awayButtonSettings
                }
                (odds.second?.size ?: 0 < 2 || odds.second?.getOrNull(1)?.odds == null) -> {
                    betStatus = BetStatus.LOCKED.code
                    return@awayButtonSettings
                }
                else -> {
                    betStatus = odds.second?.getOrNull(1)?.status
                }
            }

            tv_name.apply {
                visibility = when {
                    PlayCateUtils.getOUSeries().map { it.value }
                        .contains(odds.first) -> View.VISIBLE
                    else -> {
                        when (!odds.second?.getOrNull(1)?.spread.isNullOrEmpty()) {
                            true -> View.INVISIBLE
                            false -> View.GONE
                        }
                    }
                }

                text = when {
                    PlayCateUtils.getOUSeries().map { it.value }.contains(odds.first) -> {
                        odds.second?.getOrNull(1)?.nameMap?.get(
                            LanguageManager.getSelectLanguage(
                                context
                            ).key
                        ) ?: odds.second?.getOrNull(1)?.name
                    }
                    else -> ""
                }
            }

            tv_spread.apply {
                visibility = when (!odds.second?.getOrNull(1)?.spread.isNullOrEmpty()) {
                    true -> View.VISIBLE
                    false -> {
                        when {
                            PlayCateUtils.getOUSeries().map { it.value }
                                .contains(odds.first) -> View.INVISIBLE
                            else -> View.GONE
                        }
                    }
                }

                text = odds.second?.getOrNull(1)?.spread ?: ""
            }

            tv_odds.text = when (oddsType) {
                OddsType.EU -> odds.second?.getOrNull(1)?.odds.toString()
                OddsType.HK -> odds.second?.getOrNull(1)?.hkOdds.toString()
            }

            this@OddButtonPagerViewHolder.setupOddState(this, odds.second?.getOrNull(1))

            isSelected = odds.second?.getOrNull(1)?.isSelected ?: false

            setOnClickListener { _ ->
                odds.second?.getOrNull(1)?.let { odd ->

                    val playName = when {
                        PlayCateUtils.getOUSeries().map { it.value }
                            .contains(odds.first) -> {
                            odds.second?.getOrNull(1)?.nameMap?.get(
                                LanguageManager.getSelectLanguage(
                                    context
                                ).key
                            ) ?: odds.second?.getOrNull(1)?.name
                        }
                        else -> {
                            matchInfo?.awayName
                        }
                    } ?: ""

                    oddButtonListener?.onClickBet(
                        matchInfo,
                        odd,
                        playCateName,
                        playName
                    )
                }
            }
        }

        oddBtnDraw.apply drawButtonSettings@{
            when {
                (odds?.second?.size ?: 0 < 3) -> {
                    betStatus = BetStatus.DEACTIVATED.code
                    return@drawButtonSettings
                }
                (odds?.second?.getOrNull(2)?.odds == null) -> {
                    betStatus = BetStatus.LOCKED.code
                    return@drawButtonSettings
                }
                else -> {
                    betStatus = odds.second?.getOrNull(2)?.status
                }
            }

            tv_name.apply {
                text = odds.second?.getOrNull(2)?.nameMap?.get(
                    LanguageManager.getSelectLanguage(context).key
                ) ?: odds.second?.getOrNull(2)?.name
                visibility = View.VISIBLE
            }

            tv_spread.apply {
                visibility = View.INVISIBLE
            }

            tv_odds.text = when (oddsType) {
                OddsType.EU -> odds.second?.getOrNull(2)?.odds.toString()
                OddsType.HK -> odds.second?.getOrNull(2)?.hkOdds.toString()
            }

            this@OddButtonPagerViewHolder.setupOddState(this, odds.second?.getOrNull(2))

            isSelected = odds.second?.getOrNull(2)?.isSelected ?: false

            setOnClickListener { _ ->
                odds.second?.getOrNull(2)?.let { odd ->
                    oddButtonListener?.onClickBet(
                        matchInfo,
                        odd,
                        playCateName,
                        odds.second?.getOrNull(2)?.nameMap?.get(
                            LanguageManager.getSelectLanguage(context).key
                        ) ?: odds.second?.getOrNull(2)?.name ?: ""
                    )
                }
            }
        }
    }

    override val oddStateChangeListener: OddStateChangeListener
        get() = oddStateRefreshListener

    companion object {
        fun from(
            parent: ViewGroup,
            oddStateRefreshListener: OddStateChangeListener
        ): OddButtonPagerViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater
                .inflate(R.layout.itemview_odd_btn_2x2_v4, parent, false)

            return OddButtonPagerViewHolder(view, oddStateRefreshListener)
        }
    }
}

class OddButtonListener(
    val clickListenerBet: (matchInfo: MatchInfo?, odd: Odd, playCateName: String, playName: String) -> Unit
) {

    fun onClickBet(
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateName: String = "",
        playName: String = ""
    ) = clickListenerBet(matchInfo, odd, playCateName, playName)
}