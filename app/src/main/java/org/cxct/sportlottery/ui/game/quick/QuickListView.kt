package org.cxct.sportlottery.ui.game.quick

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_league_odd_v5.view.*
import kotlinx.android.synthetic.main.view_quick_list.view.*
import kotlinx.android.synthetic.main.view_quick_odd_btn_eps.view.*
import kotlinx.android.synthetic.main.view_quick_odd_btn_pager.view.*
import kotlinx.android.synthetic.main.view_quick_odd_btn_pair.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.game.common.*
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.LanguageManager

class QuickListView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RelativeLayout(context, attrs, defStyleAttr) {

    private val oddButtonPagerAdapter = OddButtonPagerAdapter()

    init {
        addView(LayoutInflater.from(context).inflate(R.layout.view_quick_list, this, false))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initViews()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    private fun initViews() {

    }

    private fun setupQuickOddButtonPager(item: MatchOdd, oddsType: OddsType, leagueOddListener: LeagueOddListener?) {
        league_odd_quick_odd_btn_pager.visibility = View.VISIBLE
        quick_odd_home.text = item.matchInfo?.homeName ?: ""
        quick_odd_away.text = item.matchInfo?.awayName ?: ""
        quick_odd_btn_pager_other.apply {
            val quickOddButtonPagerAdapter = OddButtonPagerAdapter()
            quickOddButtonPagerAdapter.setData(
                item.matchInfo,
                item.oddsSort,
                item.quickPlayCateNameMap,
                item.betPlayCateNameMap
            )
            this.adapter = quickOddButtonPagerAdapter.apply {
                stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT
                val quickOdds = item.quickPlayCateList?.find { it.isSelected }?.quickOdds ?: mutableMapOf()
                this.odds = quickOdds
                Log.d("Hewie", "setupQuickOddButtonPager: quickOdds => ${quickOdds.size}")

                this.oddsType = oddsType

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
        }
    }

    private fun setupQuickOddButtonEps(item: MatchOdd, oddsType: OddsType, leagueOddListener: LeagueOddListener?) {
        val adapter by lazy {
            OddButtonEpsAdapter(item.matchInfo).apply {
                this.oddsType = oddsType
                listener =
                    OddButtonListener { matchInfo, odd, playCateCode, playCateName, betPlayCateName ->
                        leagueOddListener?.onClickBet(
                            matchInfo,
                            odd,
                            playCateCode,
                            item.quickPlayCateList?.find { it.isSelected }?.name ?: playCateName,
                            item.betPlayCateNameMap
                        )
                    }
            }
        }
        val quickOdds = item.quickPlayCateList?.find { it.isSelected }?.quickOdds ?: mapOf()
        league_odd_quick_odd_btn_eps.visibility = View.VISIBLE
        quick_odd_eps_list.apply {
            this.adapter = adapter.apply {
                data = quickOdds[quickOdds.keys.firstOrNull()] ?: listOf()
            }
        }
    }

    private fun invisibleOddButtons() {
        league_odd_quick_odd_btn_pair.visibility = View.GONE
        league_odd_quick_odd_btn_pager.visibility = View.GONE
        league_odd_quick_odd_btn_eps.visibility = View.GONE
    }

    @SuppressLint("InflateParams")
    private fun setupQuickCategory(
        item: MatchOdd,
        oddsType: OddsType,
        leagueOddListener: LeagueOddListener?
    ) {

        //league_odd_quick_cate_border.visibility = if (item.quickPlayCateList.isNullOrEmpty()) { View.GONE } else { View.VISIBLE }

        league_odd_quick_cate_divider.visibility =
            if (item.quickPlayCateList.isNullOrEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }

        league_odd_quick_cate_close.apply {
            visibility = if (item.quickPlayCateList?.find { it.isSelected } == null) {
                View.GONE
            } else {
                View.VISIBLE
            }

            setOnClickListener {
                leagueOddListener?.onClickQuickCateClose()
            }
        }

        league_odd_quick_cate_tabs.apply {
            removeAllViews()
            visibility = if (item.quickPlayCateList.isNullOrEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }

            item.quickPlayCateList?.sortedBy { it.sort }?.forEachIndexed { index, it ->
                val inflater =
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val rb = inflater.inflate(R.layout.custom_radio_button, null) as RadioButton

                addView(rb.apply {
                    text = it.nameMap?.get(LanguageManager.getSelectLanguage(context).key)
                        ?: it.name

                    id = it.hashCode()

                    setTextColor(
                        ContextCompat.getColorStateList(
                            context,
                            R.color.selector_tab_text_color
                        )
                    )

                    setButtonDrawable(R.drawable.selector_null)

                    setBackgroundResource(R.drawable.selector_tab)

                })
            }

            setOnCheckedChangeListener { group, checkedId ->
                Log.d("Hewie9", "setOnCheckedChangeListener => ${item.matchInfo?.homeName}")
                item.quickPlayCateList?.forEach {
                    it.isSelected = (it.hashCode() == checkedId)
                    it.positionButtonPage = 0
                    it.positionButtonPairTab = 0
                    if(it.isSelected) {
                        item.isExpand = true
                        leagueOddListener?.onClickQuickCateTab(item, it)
                    }
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun updateQuickCategory(
        item: MatchOdd,
        oddsType: OddsType,
        leagueOddListener: LeagueOddListener?
    ) {
        //league_odd_quick_cate_border.visibility = if (item.quickPlayCateList.isNullOrEmpty()) { View.GONE } else { View.VISIBLE }
        league_odd_quick_cate_divider.visibility = if (item.quickPlayCateList.isNullOrEmpty()) { View.GONE } else { View.VISIBLE }
        league_odd_quick_cate_close.apply { visibility = if (item.quickPlayCateList?.find { it.isSelected } == null) {
            View.GONE
        } else {
            View.VISIBLE
        } }
        //itemView.scroll_view_rg.visibility = if(item.quickPlayCateList.isNullOrEmpty()) View.GONE else View.VISIBLE

        invisibleOddButtons()
        val quickPlayCate = item.quickPlayCateList?.find { it.isSelected }
        Log.d("Hewie9", "League Name => ${item?.matchInfo?.homeName}")
        Log.d("Hewie9", "Tab Name => ${quickPlayCate?.name}")
        Log.d("Hewie9", "Code => ${quickPlayCate?.code}")
        Log.d("Hewie9", "quickOdds => ${quickPlayCate?.quickOdds}")
        Log.d("Hewie9", "isExpand => ${item?.isExpand}")
        Log.d("Hewie9", "-----------------------------------------")
        if(quickPlayCate?.quickOdds?.size == 0 && !item.isExpand) return
        when (item.quickPlayCateList?.find { it.isSelected }?.code) {
            org.cxct.sportlottery.network.common.QuickPlayCate.QUICK_OU.value, org.cxct.sportlottery.network.common.QuickPlayCate.QUICK_HDP.value, org.cxct.sportlottery.network.common.QuickPlayCate.QUICK_ADVANCE.value -> {
                setupQuickOddButtonPair(item, oddsType, leagueOddListener)
            }

            org.cxct.sportlottery.network.common.QuickPlayCate.QUICK_CORNERS.value, org.cxct.sportlottery.network.common.QuickPlayCate.QUICK_PENALTY.value -> {
                setupQuickOddButtonPager(item, oddsType, leagueOddListener)
            }

            org.cxct.sportlottery.network.common.QuickPlayCate.QUICK_EPS.value -> {
                setupQuickOddButtonEps(item, oddsType, leagueOddListener)
            }
        }
    }

    private fun setupQuickOddButtonPair(item: MatchOdd, oddsType: OddsType, leagueOddListener: LeagueOddListener?) {
        val adapter by lazy {
            OddButtonPairAdapter(item.matchInfo).apply {
                this.oddsType = oddsType
                listener =
                    OddButtonListener { matchInfo, odd, playCateCode, playCateName, betPlayCateName ->
                        leagueOddListener?.onClickBet(
                            matchInfo,
                            odd,
                            playCateCode,
                            item.quickPlayCateList?.find { it.isSelected }?.name
                                ?: playCateName,
                            item.betPlayCateNameMap
                        )
                    }
            }
        }

        //  item.quickPlayCateList?.find { it.isSelected }?.quickOdds ?: mapOf()
        val quickOdds = item.quickPlayCateList?.find { it.isSelected }?.quickOdds ?: mapOf()
        Log.d("Hewie", "setupQuickOddButtonPair: quickOdds => ${quickOdds.size}")

        league_odd_quick_odd_btn_pair.visibility = View.VISIBLE

        quick_odd_pair_tab_1.apply {
            visibility =
                if (quickOdds.keys.any { it == PlayCate.HDP.value || it == PlayCate.OU.value }
                    && !(quickOdds[PlayCate.HDP.value].isNullOrEmpty() && quickOdds[PlayCate.OU.value].isNullOrEmpty())) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        quick_odd_pair_tab_2.apply {
            visibility =
                if (quickOdds.keys.any { it == PlayCate.HDP_1ST.value || it == PlayCate.OU_1ST.value }
                    && !(quickOdds[PlayCate.HDP_1ST.value].isNullOrEmpty() && quickOdds[PlayCate.OU_1ST.value].isNullOrEmpty())) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        quick_odd_pair_list.apply {
            this.adapter = adapter
        }

        quick_odd_pair_tab.apply {
            clearCheck()
            setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.quick_odd_pair_tab_1 -> {
                        item.quickPlayCateList?.find { it.isSelected }?.positionButtonPairTab = 0
                        adapter.odds = quickOdds[PlayCate.HDP.value] ?: quickOdds[PlayCate.OU.value] ?: listOf()
                    }

                    R.id.quick_odd_pair_tab_2 -> {
                        item.quickPlayCateList?.find { it.isSelected }?.positionButtonPairTab = 1
                        adapter.odds = quickOdds[PlayCate.HDP_1ST.value] ?: quickOdds[PlayCate.OU_1ST.value] ?: listOf()
                    }
                }
            }
        }

        if (quickOdds.keys.any { it == PlayCate.ADVANCE.value } && !(quickOdds[PlayCate.ADVANCE.value].isNullOrEmpty())) {
            adapter.odds = quickOdds[PlayCate.ADVANCE.value] ?: listOf()
        } else {
            quick_odd_pair_tab.check(
                when {
                    (quick_odd_pair_tab_2.isVisible && item.quickPlayCateList?.find { it.isSelected }?.positionButtonPairTab == 1) -> R.id.quick_odd_pair_tab_2
                    else -> R.id.quick_odd_pair_tab_1
                }
            )
        }
    }
}