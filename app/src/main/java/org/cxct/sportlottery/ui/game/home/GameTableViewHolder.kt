package org.cxct.sportlottery.ui.game.home

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.*
import kotlinx.android.synthetic.main.home_game_table_4.view.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.MenuCode
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.component.overScrollView.OverScrollDecoratorHelper
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.home.gameTable4.GameEntity
import org.cxct.sportlottery.ui.game.home.gameTable4.Vp2GameTable4Adapter
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.GameConfigManager
import org.cxct.sportlottery.util.HomePageStatusManager.atStartSelectedPage
import org.cxct.sportlottery.util.HomePageStatusManager.inPlaySelectedPage

class GameTableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var mMatchType: MatchType = MatchType.IN_PLAY
    private var selectedOdds = mutableListOf<String>()
    private var oddsType: OddsType = OddsType.EU
    private var isLogin: Boolean? = false
    private var mPagerPosition = 0
    var mMatchOdd: MatchOdd? = null

    private var onPageChangeCallback: OnPageChangeCallback? = null

    private var onClickTotalMatchListener: OnSelectItemListener<GameEntity>? = null
    private var onClickMatchListener: OnSelectItemListener<MatchInfo>? = null
    private var onClickOddListener: OnClickOddListener? = object : OnClickOddListener {
        override fun onClickBet(matchOdd: MatchOdd, odd: Odd, playCateCode: String, playCateName: String?,
                                betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?) {
            addOddsDialog(matchOdd, odd, playCateCode, playCateName, betPlayCateNameMap)
        }
    }
    private var onClickFavoriteListener: OnClickFavoriteListener? = null
    private var onClickStatisticsListener: OnClickStatisticsListener? = null

    init {
        itemView.apply {
            view_pager.getChildAt(0)?.overScrollMode = View.OVER_SCROLL_NEVER //移除漣漪效果
        }
    }

    fun setParams(matchType: MatchType = MatchType.IN_PLAY,
                  selectedOdds: MutableList<String> = mutableListOf(),
                  oddsType: OddsType = OddsType.EU,
                  isLogin: Boolean? = false) {
        this.mMatchType = matchType
        this.selectedOdds = selectedOdds
        this.oddsType = oddsType
        this.isLogin = isLogin
    }

    fun setListeners(onClickTotalMatchListener: OnSelectItemListener<GameEntity>? = null,
                     onClickMatchListener: OnSelectItemListener<MatchInfo>? = null,
                     onClickOddListener: OnClickOddListener? = null,
                     onClickFavoriteListener: OnClickFavoriteListener? = null,
                     onClickStatisticsListener: OnClickStatisticsListener? = null, ) {
        this.onClickTotalMatchListener = onClickTotalMatchListener
        this.onClickMatchListener = onClickMatchListener
        this.onClickOddListener = onClickOddListener
        this.onClickFavoriteListener = onClickFavoriteListener
        this.onClickStatisticsListener = onClickStatisticsListener
    }

    fun bind(data: GameEntity) {
        itemView.apply {
            tv_game_name.text = data.name
            tv_game_num.text = data.num.toString()
            val gameCode = data.code
            GameConfigManager.getGameIcon(gameCode)?.let {
                iv_game_icon.setImageResource(it)
            }
            GameConfigManager.getTitleBarBackground(gameCode, MultiLanguagesApplication.isNightMode)?.let {
                titleBar.setBackgroundResource(it)
            }
            titleBar.setOnClickListener {
                onClickTotalMatchListener?.onClick(data)
            }

            data.matchOdds.let {
                // TODO 這裡存在一個隱性的效能問題
//                if (data.vpTableAdapter == null)
                data.vpTableAdapter = Vp2GameTable4Adapter(mMatchType)
                data.vpTableAdapter?.onClickMatchListener = onClickMatchListener
                data.vpTableAdapter?.onClickOddListener = onClickOddListener
                data.vpTableAdapter?.onClickFavoriteListener = onClickFavoriteListener
                data.vpTableAdapter?.onClickStatisticsListener = onClickStatisticsListener
                data.vpTableAdapter?.setData(data.code ?: "", it, isLogin ?: false, oddsType, data.playCateNameMap ?: mutableMapOf(), selectedOdds)
                view_pager.adapter = data.vpTableAdapter
                onPageChangeCallback?.let { callback ->
                    view_pager.unregisterOnPageChangeCallback(callback)
                }

                when (mMatchType) {
                    MatchType.AT_START -> atStartSelectedPage
                    else -> inPlaySelectedPage
                }[gameCode]?.let { selectedMatchId ->
                    it.indexOfFirst { matchOdd ->
                        matchOdd.matchInfo?.id == selectedMatchId
                    }.let { selectedIndex ->
                        if (selectedIndex >= 0) {
                            view_pager.setCurrentItem(selectedIndex, false)
                            val mo = data.matchOdds[selectedIndex]
                            subscribeChannelHall(mo.matchInfo?.gameType, mo.matchInfo?.id)
                        }
                    }
                }

                indicator_view.setupWithViewPager2(view_pager)
                indicator_view.apply {
                    visibility = if (it.size <= 1) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                }
                onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        if (position < 0 || position >= it.size || it.isNullOrEmpty()) return
                        mMatchOdd = it[position]
                        mPagerPosition = position
                        subscribeChannelHall(mMatchOdd?.matchInfo?.gameType, mMatchOdd?.matchInfo?.id)
                        mMatchOdd?.matchInfo?.gameType?.let { gameType ->
                            mMatchOdd?.matchInfo?.id?.let { matchId ->
                                when (mMatchType) {
                                    MatchType.AT_START -> atStartSelectedPage
                                    else -> inPlaySelectedPage
                                }[gameType] = matchId
                            }
                        }
                    }

                    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                        super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    }

                    override fun onPageScrollStateChanged(state: Int) {
                        super.onPageScrollStateChanged(state)
                        val matchOdd = it[mPagerPosition]
                        when(state) {
                            SCROLL_STATE_SETTLING -> {
                                unsubscribeHallChannel(matchOdd.matchInfo?.gameType, matchOdd.matchInfo?.id)
                            }
                            SCROLL_STATE_IDLE -> { }
                        }
                    }
                }
                onPageChangeCallback?.let { callback ->
                    view_pager.registerOnPageChangeCallback(callback)
                }
            }

            OverScrollDecoratorHelper.setUpOverScroll(
                view_pager.getChildAt(0) as RecyclerView,
                OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL
            )
        }
    }

    fun subscribeChannelHall(gameType: String?, eventId: String?) {
        Log.d("[subscribe]", "訂閱 ${eventId}(${gameType}) => subscribeChannelHall")
        (itemView.context as BaseSocketActivity<*>).subscribeChannelHall(gameType, eventId)
    }

    fun unsubscribeHallChannel(gameType: String?, eventId: String?) {
        Log.d("[subscribe]", "解除訂閱 ${eventId}(${gameType}) => unsubscribeHallChannel")
        (itemView.context as BaseSocketActivity<*>).unSubscribeChannelHall(gameType, eventId)
    }

    private fun addOddsDialog(
        matchOdd: MatchOdd,
        odd: Odd,
        playCateCode: String,
        playCateName: String?,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?
    ) {
        GameType.getGameType(matchOdd.matchInfo?.gameType)?.let { gameType ->
            matchOdd.matchInfo?.let { matchInfo ->
                val fastBetDataBean = FastBetDataBean(
                    matchType = mMatchType,
                    gameType = gameType,
                    playCateCode = playCateCode,
                    playCateName = playCateName ?: "",
                    matchInfo = matchInfo,
                    matchOdd = null,
                    odd = odd,
                    subscribeChannelType = ChannelType.HALL,
                    betPlayCateNameMap = betPlayCateNameMap,
                    playCateMenuCode = if (mMatchType == MatchType.IN_PLAY || mMatchType == MatchType.MAIN) MenuCode.HOME_INPLAY_MOBILE.code else MenuCode.HOME_ATSTART_MOBILE.code
                )
                (itemView.context as GameActivity).showFastBetFragment(fastBetDataBean)
            }
        }
    }
}