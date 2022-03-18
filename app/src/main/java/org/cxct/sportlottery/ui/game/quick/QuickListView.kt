package org.cxct.sportlottery.ui.game.quick

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.RelativeLayout
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_quick_list.view.*
import kotlinx.android.synthetic.main.view_quick_odd_btn_eps.view.*
import kotlinx.android.synthetic.main.view_quick_odd_btn_pager.view.*
import kotlinx.android.synthetic.main.view_quick_odd_btn_pair.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.network.odds.quick.QuickListData
import org.cxct.sportlottery.ui.game.common.*
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.LanguageManager

class QuickListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), View.OnClickListener {

    private var lifecycleOwner: LifecycleOwner? = null
    private val mViewModel = QuickListViewModel(context)
    private var mMatchId: String = ""
    private var mQuickPlayCateList = mutableListOf<QuickPlayCate>()
    private var mSelectedQuickPlayCate: QuickPlayCate? = null
    private var mMatchOdd: MatchOdd? = null
    private var mOddsType: OddsType = OddsType.HK
    private var mLeagueOddListener: LeagueOddListener? = null
    private var mCloseTag = false
    private var mPlaySelectedCodeSelectionType: Int? = null
    private var mPlaySelectedCode: String? = null
    private var quickOdds1: MutableMap<String, List<Odd?>?> = mutableMapOf()
    private var mOddButtonPairAdapter: OddButtonPairAdapter =
        OddButtonPairAdapter(mMatchOdd?.matchInfo)
    private var mQuickOddButtonPagerAdapter: OddButtonPagerAdapter = OddButtonPagerAdapter()
    private var mOddButtonEpsAdapter: OddButtonEpsAdapter =
        OddButtonEpsAdapter(mMatchOdd?.matchInfo)

    init {
        addView(LayoutInflater.from(context).inflate(R.layout.view_quick_list, this, false))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        lifecycleOwner = context as? LifecycleOwner
        initViews()
        initObserver()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            league_odd_quick_cate_close.id -> {
                mCloseTag = true
                league_odd_quick_cate_close.visibility = View.GONE
                invisibleOddButtons()
                clearTabState()
                mLeagueOddListener?.onClickQuickCateClose()
            }
        }
    }

    private fun initViews() {
        league_odd_quick_cate_close?.setOnClickListener(this)
        league_odd_quick_cate_tabs?.setOnCheckedChangeListener { group, checkedId ->
            if (mCloseTag) {
                mSelectedQuickPlayCate = null
                mCloseTag = false
            } else {
                mSelectedQuickPlayCate = mQuickPlayCateList.find { it.hashCode() == checkedId }
                mSelectedQuickPlayCate?.let { mViewModel.apiGetQuickList(mMatchId) }
            }
        }
        // expand view by data
        if (mMatchOdd?.isExpand == true) {
            mViewModel.apiGetQuickList(mMatchOdd?.matchInfo?.id ?: "")
            val selectedQuickPlayCateCode =
                mMatchOdd?.quickPlayCateList?.find { it.isSelected }.hashCode()
            mSelectedQuickPlayCate =
                mQuickPlayCateList.find { it.hashCode() == selectedQuickPlayCateCode }
            setTabState(selectedQuickPlayCateCode)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserver() {
        lifecycleOwner?.let { lifecycleOwner ->
            mViewModel._quickOddsListGameHallResult.observe(lifecycleOwner) {
                it.getContentIfNotHandled()?.let { quickListResult ->
                    if (quickListResult.success) {
                        league_odd_quick_cate_close.visibility = View.VISIBLE
                        invisibleOddButtons()
                        val selectedQuickPlayCateCode = mSelectedQuickPlayCate?.code ?: ""
                        quickOdds1 =
                            quickListResult.quickListData?.quickOdds?.get(selectedQuickPlayCateCode)
                                ?: mutableMapOf()
                        when (selectedQuickPlayCateCode) {
                            org.cxct.sportlottery.network.common.QuickPlayCate.QUICK_OU.value, org.cxct.sportlottery.network.common.QuickPlayCate.QUICK_HDP.value, org.cxct.sportlottery.network.common.QuickPlayCate.QUICK_ADVANCE.value -> {
                                setupQuickOddButtonPair(
                                    mSelectedQuickPlayCate!!,
                                    quickOdds1,
                                    mOddsType,
                                    mLeagueOddListener
                                )
                            }
                            org.cxct.sportlottery.network.common.QuickPlayCate.QUICK_CORNERS.value, org.cxct.sportlottery.network.common.QuickPlayCate.QUICK_PENALTY.value -> {
                                setupQuickOddButtonPager(
                                    quickOdds1,
                                    mOddsType,
                                    mLeagueOddListener,
                                    quickListResult.quickListData,
                                )
                            }
                            org.cxct.sportlottery.network.common.QuickPlayCate.QUICK_EPS.value -> {
                                setupQuickOddButtonEps(
                                    mSelectedQuickPlayCate!!,
                                    quickOdds1,
                                    mOddsType,
                                    mLeagueOddListener
                                )
                            }
                        }
                        mLeagueOddListener?.onClickQuickCateTab(
                            mMatchOdd!!,
                            mSelectedQuickPlayCate!!
                        )
                        SpaceItemDecorationView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    fun setDatas(
        matchOdd: MatchOdd,
        oddsType: OddsType,
        leagueOddListener: LeagueOddListener?,
        playSelectedCodeSelectionType: Int?,
        playSelectedCode: String?
    ) {
        mOddsType = oddsType
        mMatchOdd = matchOdd
        mMatchId = matchOdd.matchInfo?.id ?: ""
        mQuickPlayCateList = matchOdd.quickPlayCateList ?: mutableListOf()
        mLeagueOddListener = leagueOddListener
        mPlaySelectedCodeSelectionType = playSelectedCodeSelectionType
        mPlaySelectedCode = playSelectedCode
    }


    fun refreshTab() {
        Log.e("jeff", "refreshTab")
        league_odd_quick_cate_tabs?.removeAllViews()
        mQuickPlayCateList.sortedBy { it.sort }.forEachIndexed { index, it ->
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val rb = inflater.inflate(R.layout.custom_radio_button, null) as RadioButton
            league_odd_quick_cate_tabs?.addView(rb.apply {
                text = it.nameMap?.get(LanguageManager.getSelectLanguage(context).key) ?: it.name
                id = it.hashCode()
            })
        }
    }

    private fun setTabState(id: Int) {
        league_odd_quick_cate_tabs.check(id)
    }

    private fun clearTabState() {
        league_odd_quick_cate_tabs.clearCheck()
    }

    private fun setupQuickOddButtonPair(
        selectedQuickPlayCate: QuickPlayCate,
        quickOdds: MutableMap<String, List<Odd?>?>,
        oddsType: OddsType,
        leagueOddListener: LeagueOddListener?
    ) {
        mOddButtonPairAdapter = OddButtonPairAdapter(mMatchOdd?.matchInfo).apply {
            this.oddsType = oddsType
            listener =
                OddButtonListener { matchInfo, odd, playCateCode, playCateName, betPlayCateName ->
                    leagueOddListener?.onClickBet(
                        matchInfo,
                        odd,
                        playCateCode,
                        selectedQuickPlayCate.name ?: playCateName,
                        mMatchOdd?.betPlayCateNameMap
                    )
                    notifyDataSetChanged()
                }
        }

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
            this.adapter = mOddButtonPairAdapter
        }

        quick_odd_pair_tab.apply {
            clearCheck()
            setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.quick_odd_pair_tab_1 -> {
                        selectedQuickPlayCate.positionButtonPairTab = 0
                        mOddButtonPairAdapter.odds =
                            quickOdds[PlayCate.HDP.value] ?: quickOdds[PlayCate.OU.value]
                                    ?: listOf()
                    }

                    R.id.quick_odd_pair_tab_2 -> {
                        selectedQuickPlayCate.positionButtonPairTab = 1
                        mOddButtonPairAdapter.odds =
                            quickOdds[PlayCate.HDP_1ST.value] ?: quickOdds[PlayCate.OU_1ST.value]
                                    ?: listOf()
                    }
                }
            }
        }

        if (quickOdds.keys.any { it == PlayCate.ADVANCE.value } && !(quickOdds[PlayCate.ADVANCE.value].isNullOrEmpty())) {
            mOddButtonPairAdapter.odds = quickOdds[PlayCate.ADVANCE.value] ?: listOf()
        } else {
            quick_odd_pair_tab.check(
                when {
                    (quick_odd_pair_tab_2.isVisible && selectedQuickPlayCate.positionButtonPairTab == 1) -> R.id.quick_odd_pair_tab_2
                    else -> R.id.quick_odd_pair_tab_1
                }
            )
        }
    }

    private fun setupQuickOddButtonPager(
        quickOdds: MutableMap<String, List<Odd?>?>,
        oddsType: OddsType,
        leagueOddListener: LeagueOddListener?,
        quickListData: QuickListData?,
    ) {
        Log.e("jeff", "setupQuickOddButtonPager")
        league_odd_quick_odd_btn_pager.visibility = View.VISIBLE
        quick_odd_home.text = mMatchOdd?.matchInfo?.homeName ?: ""
        quick_odd_away.text = mMatchOdd?.matchInfo?.awayName ?: ""
        quick_odd_btn_pager_other.apply {
            mQuickOddButtonPagerAdapter = OddButtonPagerAdapter()
            mQuickOddButtonPagerAdapter.setData(
                mMatchOdd?.matchInfo,
                null,
                quickListData?.playCateNameMap,
                quickListData?.betPlayCateNameMap,
                mPlaySelectedCodeSelectionType
            )
            this.adapter = mQuickOddButtonPagerAdapter.apply {
                stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT
                this.odds = quickOdds
                this.oddsType = oddsType
                this.listener =
                    OddButtonListener { matchInfo, odd, playCateCode, playCateName, betPlayCateName ->
                        leagueOddListener?.onClickBet(
                            matchInfo,
                            odd,
                            playCateCode,
                            betPlayCateName,
                            mMatchOdd?.betPlayCateNameMap
                        )
                    }
            }
        }
    }

    private fun setupQuickOddButtonEps(
        selectedQuickPlayCate: QuickPlayCate,
        quickOdds: MutableMap<String, List<Odd?>?>,
        oddsType: OddsType,
        leagueOddListener: LeagueOddListener?
    ) {
        mOddButtonEpsAdapter =
            OddButtonEpsAdapter(mMatchOdd?.matchInfo).apply {
                this.oddsType = oddsType
                listener =
                    OddButtonListener { matchInfo, odd, playCateCode, playCateName, betPlayCateName ->
                        leagueOddListener?.onClickBet(
                            matchInfo,
                            odd,
                            playCateCode,
                            selectedQuickPlayCate.name ?: playCateName,
                            mMatchOdd?.betPlayCateNameMap
                        )
                    }
            }

        league_odd_quick_odd_btn_eps.visibility = View.VISIBLE
        quick_odd_eps_list.apply {
            this.adapter = mOddButtonEpsAdapter.apply {
                data = quickOdds[quickOdds.keys.firstOrNull()] ?: listOf()
            }
        }
    }

    private fun invisibleOddButtons() {
        league_odd_quick_odd_btn_pair.visibility = View.GONE
        league_odd_quick_odd_btn_pager.visibility = View.GONE
        league_odd_quick_odd_btn_eps.visibility = View.GONE
        SpaceItemDecorationView.visibility = View.GONE
    }

    fun updateQuickSelected() {
        Log.e("jeff", "updateQuickSelected")
        mOddButtonPairAdapter.notifyDataSetChanged()
        mQuickOddButtonPagerAdapter.notifyDataSetChanged()
        mOddButtonEpsAdapter.notifyDataSetChanged()
    }
}