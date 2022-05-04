package org.cxct.sportlottery.ui.game.home.gameTable4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder
import kotlinx.android.synthetic.main.home_game_table_4.view.*
import kotlinx.android.synthetic.main.home_sport_table_4.view.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.common.GameType.Companion.getGameTypeString
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.MenuCode
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.service.match_clock.MatchClockCO
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusCO
import org.cxct.sportlottery.ui.component.overScrollView.OverScrollDecoratorHelper
import org.cxct.sportlottery.ui.game.home.OnClickFavoriteListener
import org.cxct.sportlottery.ui.game.home.OnClickOddListener
import org.cxct.sportlottery.ui.game.home.OnClickStatisticsListener
import org.cxct.sportlottery.ui.game.home.OnSubscribeChannelHallListener
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.GameConfigManager.getGameIcon
import org.cxct.sportlottery.util.GameConfigManager.getTitleBarBackground
import org.cxct.sportlottery.util.RecyclerViewGridDecoration
import timber.log.Timber

class RvGameTable4Adapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mDataList = mutableListOf<GameEntity>()
    private var mMatchType: MatchType = MatchType.IN_PLAY
    private var selectedOdds = mutableListOf<String>()
    private var oddsType: OddsType = OddsType.EU
    var isLogin: Boolean? = false

    enum class ItemType {
        ODD_DATA, SPORT_GRID_REPAY
    }

    fun setData(dataList: MutableList<GameEntity>, matchType: MatchType, selectedOdds: MutableList<String>) {
        this.mMatchType = matchType
        this.selectedOdds = selectedOdds
        this.mDataList = dataList
        notifyDataSetChanged()
    }

    fun notifySelectedOddsChanged(selectedOdds: MutableList<String>) {
        mDataList.forEach {
            it.vpTableAdapter?.notifySelectedOddsChanged(selectedOdds)
        }
    }

    fun getData() = mDataList

    //指定刷新內部 ViewPager 的 subItem
    fun notifySubItemChanged(index: Int, indexMatchOdd: Int) {
        if (index >= 0 && indexMatchOdd >= 0)
            mDataList[index].vpTableAdapter?.notifyItemChanged(indexMatchOdd)
    }

    var onClickOddListener: OnClickOddListener? = null

    var onClickMatchListener: OnSelectItemListener<MatchInfo>? = null

    var onClickTotalMatchListener: OnSelectItemListener<GameEntity>? = null

    var onClickSportListener: OnSelectItemListener<OtherMatch>? = null

    var onClickFavoriteListener: OnClickFavoriteListener? = null

    var onClickStatisticsListener: OnClickStatisticsListener? = null

    var onSubscribeChannelHallListener: OnSubscribeChannelHallListener? = null

    override fun getItemViewType(position: Int): Int {
        return when (mDataList[position].otherMatch.isNullOrEmpty()) {
            true -> ItemType.ODD_DATA.ordinal
            false -> ItemType.SPORT_GRID_REPAY.ordinal
        }
    }

    fun notifyOddsDiscountChanged(discount: Float) {
        mDataList.forEach{
            it.vpTableAdapter?.notifyOddsDiscountChanged(discount)
        }
    }

    fun notifyOddsTypeChanged(oddsType: OddsType) {
        this.oddsType = oddsType
        mDataList.forEach{
            it.vpTableAdapter?.notifyOddsTypeChanged(oddsType)
        }
    }

    fun notifyMatchStatusChanged(matchStatusCO: MatchStatusCO, statusValue: String?) {
        mDataList.forEach {
            if (matchStatusCO.gameType == it.code) {
                it.vpTableAdapter?.notifyMatchStatusChanged(matchStatusCO, statusValue)
            }
        }
    }

    fun notifyTimeChanged(diff: Int) {
        mDataList.forEach{
            it.vpTableAdapter?.notifyTimeChanged(diff)
        }
    }

    fun notifyUpdateTime(matchClockCO: MatchClockCO?) {
        matchClockCO?.let { matchClock ->
            mDataList.forEach{
                if (matchClock.gameType == it.code) {
                    it.vpTableAdapter?.notifyUpdateTime(matchClock)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ItemType.ODD_DATA.ordinal -> {
                val layout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_game_table_4, parent, false)
                return ItemViewHolder(layout)
            }
            ItemType.SPORT_GRID_REPAY.ordinal -> {
                val layout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_sport_table_4, parent, false)
                return SportGridViewHolder(layout)
            }
            else -> {
                val layout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_game_table_4, parent, false)
                return ItemViewHolder(layout)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = mDataList[position]
        when (holder) {
            is ItemViewHolder -> {
                holder.apply {
                    bind(data)
                }
            }
            is SportGridViewHolder ->{
                holder.apply {
                    bind(data.otherMatch)
                }
            }
        }
    }

    override fun getItemCount(): Int = mDataList.size

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.apply {
                view_pager.getChildAt(0)?.overScrollMode = View.OVER_SCROLL_NEVER //移除漣漪效果
            }
        }

        fun bind(data: GameEntity) {
            itemView.apply {
                tv_game_name.text = getGameTypeString(context, data.code)
                tv_game_num.text = data.num.toString()
                val gameCode = data.code
                getGameIcon(gameCode)?.let {
                    iv_game_icon.setImageResource(it)
                }
                getTitleBarBackground(gameCode, MultiLanguagesApplication.isNightMode)?.let {
                    titleBar.setBackgroundResource(it)
                }
                titleBar.setOnClickListener {
                    onClickTotalMatchListener?.onClick(data)
                }

                data.matchOdds?.let { it ->
                    if (data.vpTableAdapter == null) data.vpTableAdapter = Vp2GameTable4Adapter(mMatchType)
                    data.vpTableAdapter?.onClickMatchListener = onClickMatchListener
                    data.vpTableAdapter?.onClickOddListener = onClickOddListener
                    data.vpTableAdapter?.onClickFavoriteListener = onClickFavoriteListener
                    data.vpTableAdapter?.onClickStatisticsListener = onClickStatisticsListener
                    data.vpTableAdapter?.setData(data.code ?: "", it, isLogin ?: false, oddsType, data.playCateNameMap ?: mutableMapOf(), selectedOdds)
                    view_pager.adapter = data.vpTableAdapter

                    indicator_view.setupWithViewPager2(view_pager)
                    indicator_view.apply {
                        visibility = if (it.size <= 1) {
                            View.GONE
                        } else {
                            View.VISIBLE
                        }
                    }

                    view_pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                            if (position < 0 || position >= it.size || it.isNullOrEmpty()) return
                            val data = it[position]
                            onSubscribeChannelHallListener?.subscribeChannel(
                                data.matchInfo?.gameType,
                                if (mMatchType == MatchType.IN_PLAY || mMatchType == MatchType.MAIN) MenuCode.HOME_INPLAY_MOBILE.code else MenuCode.HOME_ATSTART_MOBILE.code,
                                data.matchInfo?.id
                            )
                        }
                    })
                }

                OverScrollDecoratorHelper.setUpOverScroll(
                    view_pager.getChildAt(0) as RecyclerView,
                    OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL
                )
            }
        }
    }

    inner class SportGridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var adapter: CommonAdapter<OtherMatch>
        init {
            itemView.apply {
                rvSport.layoutManager =
                    LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)
                rvSport.apply {
                    layoutManager = GridLayoutManager(context, 2)
                }
                rvSport.addItemDecoration(
                    RecyclerViewGridDecoration(
                        2,
                        resources.getDimension(R.dimen.margin_8).toInt(),
                        resources.getDimension(R.dimen.margin_8).toInt(),
                        resources.getDimension(R.dimen.margin_8).toInt()
                    )
                )
                rvSport.isNestedScrollingEnabled = false
            }
        }

        fun bind(data: List<OtherMatch>?) {
            itemView.apply {
                adapter = object : CommonAdapter<OtherMatch>( context, R.layout.item_home_sport, data ) {
                    override fun convert( holder: ViewHolder, t: OtherMatch, position: Int ) {
                        getGameIcon(t.code)?.let {
                            holder.getView<ImageView>(R.id.ivSportLogo).setImageResource(it)
                        }
                        holder.setText(R.id.tvSport, getGameTypeString(context, t.code))
                        holder.setText(R.id.tvSportCount, t.num.toString())
                        holder.getView<LinearLayout>(R.id.layoutSport).setOnClickListener {
                            onClickSportListener?.onClick(t)
                        }
                    }
                }
                rvSport.adapter = adapter
            }
        }
    }


}