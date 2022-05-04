package org.cxct.sportlottery.ui.odds


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.OddSpreadForSCO
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.common.DividerItemDecorator
import org.cxct.sportlottery.ui.common.IndicatorView
import org.cxct.sportlottery.ui.component.overScrollView.OverScrollDecoratorHelper
import org.cxct.sportlottery.ui.feedback.record.FeedbackListAdapter
import org.cxct.sportlottery.ui.game.common.LeagueAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.CountryAdapter
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.MatchOddUtil.updateDiscount
import org.cxct.sportlottery.util.MatchOddUtil.updateEPSDiscount
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 * @author Kevin
 * @create 2020/12/23
 * @description 表格型排版與後端回傳順序有關
 * @edit:
 * 2021/08/17 玩法六個一組和四個一組的排版改為依順序分組
 */
@SuppressLint("NotifyDataSetChanged")
class OddsDetailListAdapter(private val onOddClickListener: OnOddClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var betInfoList: MutableList<BetInfoListData> = mutableListOf()
        set(value) {
            field = value
            oddsDetailDataList.forEachIndexed { index, data ->
                data.oddArrayList.forEach { odd ->
                    odd?.isSelected = betInfoList.any { it.matchOdd.oddsId == odd?.id }
                    if (onOddClickListener.clickOdd == odd) {
                        notifyItemChanged(index)
                    }
                }
            }
        }

    var discount: Float = 1.0F
        set(value) {
            if (field == value) return

            oddsDetailDataList.forEach { oddsDetailListData ->
                if (oddsDetailListData.gameType == PlayCate.EPS.value) {
                    oddsDetailListData.oddArrayList.forEach { odd ->
                        odd?.updateEPSDiscount(field, value)
                    }
                } else {
                    //LCS的玩法不能使用DISCOUNT
                    if (oddsDetailListData.gameType != PlayCate.LCS.value) {
                        oddsDetailListData.oddArrayList.forEach { odd ->
                            odd?.updateDiscount(field, value)
                        }
                    }
                }
            }

            field = value
            notifyDataSetChanged()
        }


    var oddsDetailDataList: ArrayList<OddsDetailListData> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    var homeName: String? = null


    var awayName: String? = null


    var sportCode: GameType? = null


    private lateinit var code: String


    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var oddsDetailListener: OddsDetailListener? = null


    enum class LayoutType(val layout: Int) {
        CS(R.layout.content_odds_detail_list_cs),
        SINGLE_2_CS(R.layout.content_odds_detail_list_single_2_cs_item),
        ONE_LIST(R.layout.content_odds_detail_list_one),
        SINGLE(R.layout.content_odds_detail_list_single),
        SINGLE_2_ITEM(R.layout.content_odds_detail_list_single_2_item),
        FG_LG(R.layout.content_odds_detail_list_fg_lg),
        GROUP_6(R.layout.content_odds_detail_list_group_6_item),
        GROUP_4(R.layout.content_odds_detail_list_group_4_item),
        SCO(R.layout.content_odds_detail_list_sco),
        EPS(R.layout.content_odds_detail_list_eps)
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            oddsDetailDataList.isEmpty() -> PlayCate.getPlayCate("NO_DATA").ordinal
            position == oddsDetailDataList.size  -> PlayCate.getPlayCate("BOTTOM_NAVIGATION").ordinal
            else -> PlayCate.getPlayCate(oddsDetailDataList[position].gameType).ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout: Int = when (sportCode) {
            GameType.FT -> {
                when (viewType) {
                    PlayCate.TG_OU.ordinal, PlayCate.HDP.ordinal, PlayCate.HDP_1ST.ordinal, PlayCate.OU.ordinal, PlayCate.OU_1ST.ordinal, PlayCate.TG.ordinal, PlayCate.TG_1ST.ordinal, PlayCate.BTS.ordinal,
                    PlayCate.BTS_1ST.ordinal, PlayCate.BTS_2ST.ordinal, PlayCate.OE.ordinal, PlayCate.OE_1ST.ordinal, PlayCate.CLSH.ordinal, PlayCate.TWTN.ordinal, PlayCate.RTG2.ordinal, PlayCate.RTG3.ordinal, PlayCate.HWMG.ordinal,
                    PlayCate.SBH.ordinal, PlayCate.OWN_GOAL.ordinal, PlayCate.TWFB.ordinal, PlayCate.WEH.ordinal, PlayCate.WBH.ordinal, PlayCate.CORNER_FIRST.ordinal, PlayCate.CORNER_LAST.ordinal, PlayCate.PENALTY_FIRST.ordinal,
                    PlayCate.PENALTY_LAST.ordinal, PlayCate.KICK_OFF.ordinal, PlayCate.PENALTY_AWARDED.ordinal, PlayCate.FREE_KICK_FIRST.ordinal, PlayCate.FREE_KICK_LAST.ordinal, PlayCate.GOAL_KICK_FIRST.ordinal, PlayCate.GOAL_KICK_LAST.ordinal,
                    PlayCate.FOUL_BALL_FIRST.ordinal, PlayCate.FOUL_BALL_LAST.ordinal, PlayCate.OFFSIDE_FIRST.ordinal, PlayCate.OFFSIDE_LAST.ordinal, PlayCate.SUBSTITUTION_FIRST.ordinal, PlayCate.SUBSTITUTION_LAST.ordinal,
                    PlayCate.EXTRA_TIME.ordinal, PlayCate.RED_CARD_PLAYER.ordinal, PlayCate.CORNER_HDP.ordinal, PlayCate.CORNER_1ST_HDP.ordinal, PlayCate.CORNER_OU.ordinal, PlayCate.CORNER_1ST_OU.ordinal, PlayCate.CORNER_OE.ordinal, PlayCate.CORNER_1ST_OE.ordinal,
                    PlayCate.PENALTY_HDP.ordinal, PlayCate.PENALTY_1ST_HDP.ordinal, PlayCate.PENALTY_OU.ordinal, PlayCate.PENALTY_1ST_OU.ordinal, PlayCate.PENALTY_OE.ordinal, PlayCate.PENALTY_1ST_OE.ordinal,
                    PlayCate.HDP_OT.ordinal, PlayCate.HDP_1ST_OT.ordinal, PlayCate.OU_OT.ordinal, PlayCate.OU_1ST_OT.ordinal, PlayCate.TG_OT.ordinal, PlayCate.OE_OT.ordinal, PlayCate.BTS_OT.ordinal, PlayCate.PK.ordinal, PlayCate.ADVANCE.ordinal,
                    PlayCate.TG_OU_H.ordinal, PlayCate.TG_OU_H_1ST.ordinal, PlayCate.TG_OE_H.ordinal, PlayCate.TG_OE_H_1ST.ordinal, PlayCate.TG_OU_C.ordinal, PlayCate.TG_OU_C_1ST.ordinal, PlayCate.TG_OE_C.ordinal, PlayCate.TG_OE_C_1ST.ordinal,
                    PlayCate.P_HDP.ordinal, PlayCate.P_HDP_1ST.ordinal, PlayCate.P_OU.ordinal, PlayCate.P_OU_1ST.ordinal, PlayCate.P_OE.ordinal, PlayCate.P_OE_1ST.ordinal, PlayCate.HDP_SEG1.ordinal, PlayCate.OU_SEG1.ordinal, PlayCate.HDP_SEG2.ordinal,
                    PlayCate.OU_SEG2.ordinal, PlayCate.HDP_SEG3.ordinal, PlayCate.OU_SEG3.ordinal, PlayCate.HDP_SEG4.ordinal, PlayCate.OU_SEG4.ordinal, PlayCate.HDP_SEG5.ordinal, PlayCate.OU_SEG5.ordinal, PlayCate.HDP_SEG6.ordinal, PlayCate.OU_SEG6.ordinal,
                    PlayCate.CORNER_HDP_SEG1.ordinal, PlayCate.CORNER_OU_SEG1.ordinal, PlayCate.CORNER_HDP_SEG2.ordinal, PlayCate.CORNER_OU_SEG2.ordinal, PlayCate.CORNER_HDP_SEG3.ordinal, PlayCate.CORNER_OU_SEG3.ordinal,
                    PlayCate.CORNER_HDP_SEG4.ordinal, PlayCate.CORNER_OU_SEG4.ordinal, PlayCate.CORNER_HDP_SEG5.ordinal, PlayCate.CORNER_OU_SEG5.ordinal, PlayCate.CORNER_HDP_SEG6.ordinal, PlayCate.CORNER_OU_SEG6.ordinal,
                    PlayCate.PENALTY_HDP_SEG1.ordinal, PlayCate.PENALTY_OU_SEG1.ordinal, PlayCate.PENALTY_HDP_SEG2.ordinal, PlayCate.PENALTY_OU_SEG2.ordinal, PlayCate.PENALTY_HDP_SEG3.ordinal, PlayCate.PENALTY_OU_SEG3.ordinal,
                    PlayCate.PENALTY_HDP_SEG4.ordinal, PlayCate.PENALTY_OU_SEG4.ordinal, PlayCate.PENALTY_HDP_SEG5.ordinal, PlayCate.PENALTY_OU_SEG5.ordinal, PlayCate.PENALTY_HDP_SEG6.ordinal, PlayCate.PENALTY_OU_SEG6.ordinal,
                    PlayCate.TG_OU_OT_H.ordinal, PlayCate.TG_OU_OT_C.ordinal
                    -> LayoutType.SINGLE_2_ITEM.layout

                    PlayCate.SINGLE.ordinal, PlayCate.SINGLE_1ST.ordinal, PlayCate.HWMG_SINGLE.ordinal, PlayCate.CORNER_SINGLE.ordinal, PlayCate.CORNER_1ST_SINGLE.ordinal, PlayCate.PENALTY_SINGLE.ordinal, PlayCate.PENALTY_1ST_SINGLE.ordinal,
                    PlayCate.SINGLE_OT.ordinal, PlayCate.SINGLE_1ST_OT.ordinal, PlayCate.P_SINGLE.ordinal, PlayCate.P_SINGLE_1ST.ordinal, PlayCate.SINGLE_SEG1.ordinal, PlayCate.SINGLE_SEG2.ordinal, PlayCate.SINGLE_SEG3.ordinal,
                    PlayCate.SINGLE_SEG4.ordinal, PlayCate.SINGLE_SEG5.ordinal, PlayCate.SINGLE_SEG6.ordinal, PlayCate.CORNER_SINGLE_SEG1.ordinal, PlayCate.CORNER_SINGLE_SEG2.ordinal, PlayCate.CORNER_SINGLE_SEG3.ordinal,
                    PlayCate.CORNER_SINGLE_SEG4.ordinal, PlayCate.CORNER_SINGLE_SEG5.ordinal, PlayCate.CORNER_SINGLE_SEG6.ordinal, PlayCate.PENALTY_SINGLE_SEG1.ordinal, PlayCate.PENALTY_SINGLE_SEG2.ordinal, PlayCate.PENALTY_SINGLE_SEG3.ordinal,
                    PlayCate.PENALTY_SINGLE_SEG4.ordinal, PlayCate.PENALTY_SINGLE_SEG5.ordinal, PlayCate.PENALTY_SINGLE_SEG6.ordinal
                    -> LayoutType.SINGLE.layout

                    PlayCate.CS.ordinal, PlayCate.CS_OT.ordinal, PlayCate.CS_1ST_SD.ordinal, PlayCate.LCS.ordinal
                    -> LayoutType.CS.layout

                    PlayCate.FGLG.ordinal
                    -> LayoutType.FG_LG.layout

                    PlayCate.SCO.ordinal
                    -> LayoutType.SCO.layout

                    PlayCate.DC_OU.ordinal, PlayCate.SINGLE_OU.ordinal, PlayCate.SINGLE_BTS.ordinal, PlayCate.SINGLE_FLG.ordinal, PlayCate.DC_BTS.ordinal, PlayCate.DC_FLG.ordinal
                    -> LayoutType.GROUP_6.layout

                    PlayCate.OU_BTS.ordinal, PlayCate.OU_OE.ordinal, PlayCate.OU_TTS1ST.ordinal
                    -> LayoutType.GROUP_4.layout

                    PlayCate.EPS.ordinal
                    -> LayoutType.EPS.layout

                    else -> LayoutType.ONE_LIST.layout
                }
            }

            GameType.BK -> {
                when (viewType) {
                    PlayCate.HDP_INCL_OT.ordinal, PlayCate.HDP_1ST.ordinal, PlayCate.HDP_2ST_INCL_OT.ordinal, PlayCate.HDP_SEG1.ordinal, PlayCate.HDP_SEG2.ordinal, PlayCate.HDP_SEG3.ordinal, PlayCate.HDP_SEG4.ordinal,
                    PlayCate.OU_INCL_OT.ordinal, PlayCate.OU_1ST.ordinal, PlayCate.OU_2ST_INCL_OT.ordinal, PlayCate.OU_SEG1.ordinal, PlayCate.OU_SEG2.ordinal, PlayCate.OU_SEG3.ordinal, PlayCate.OU_SEG4.ordinal,
                    PlayCate.SINGLE_INCL_OT.ordinal, PlayCate.SINGLE_1ST_ND.ordinal, PlayCate.SINGLE_2ST_INCL_OT.ordinal, PlayCate.SINGLE_SEG1_ND.ordinal, PlayCate.SINGLE_SEG2_ND.ordinal, PlayCate.SINGLE_SEG3_ND.ordinal, PlayCate.SINGLE_SEG4_ND.ordinal,
                    PlayCate.OE_INCL_OT.ordinal, PlayCate.OE_1ST.ordinal, PlayCate.OE_2ST_INCL_OT.ordinal, PlayCate.OE_SEG1.ordinal, PlayCate.OE_SEG2.ordinal, PlayCate.OE_SEG3.ordinal, PlayCate.OU_SEG4.ordinal,
                    PlayCate.SEG_POINT.ordinal, PlayCate.LS_SEG1.ordinal, PlayCate.LS_SEG2.ordinal, PlayCate.LS_SEG3.ordinal, PlayCate.LS_SEG4.ordinal, PlayCate.RTP10.ordinal, PlayCate.RTP20.ordinal, PlayCate.P_SCO_OU.ordinal,
                    PlayCate.P_REBOUND_OU.ordinal, PlayCate.P_ASSIST_OU.ordinal, PlayCate.P_THREE_OU.ordinal, PlayCate.P_BLOCK_OU.ordinal, PlayCate.P_STEAL_OU.ordinal, PlayCate.TG_OU_H_1ST.ordinal,
                    PlayCate.TG_OU_H_INCL_OT.ordinal, PlayCate.TG_OU_H_2ST_INCL_OT.ordinal, PlayCate.TG_OU_H_SEG1.ordinal, PlayCate.TG_OU_H_SEG2.ordinal, PlayCate.TG_OU_H_SEG3.ordinal, PlayCate.TG_OU_H_SEG4.ordinal,
                    PlayCate.TG_OU_C_INCL_OT.ordinal, PlayCate.TG_OU_C_1ST.ordinal, PlayCate.TG_OU_C_2ST_INCL_OT.ordinal, PlayCate.TG_OU_C_SEG1.ordinal, PlayCate.TG_OU_C_SEG2.ordinal, PlayCate.TG_OU_C_SEG3.ordinal, PlayCate.TG_OU_C_SEG4.ordinal,
                    PlayCate.OE_SEG4.ordinal
                    -> LayoutType.SINGLE_2_ITEM.layout

                    PlayCate.EPS.ordinal
                    -> LayoutType.EPS.layout

                    else -> LayoutType.ONE_LIST.layout
                }
            }

            GameType.TN -> {
                when (viewType) {
                    PlayCate.SINGLE_SEG1_GAMES_1.ordinal, PlayCate.SINGLE_SEG1_GAMES_2.ordinal, PlayCate.SINGLE_SEG1_GAMES_3.ordinal, PlayCate.SINGLE_SEG1_GAMES_4.ordinal, PlayCate.SINGLE_SEG1_GAMES_5.ordinal,
                    PlayCate.SINGLE_SEG2_GAMES_1.ordinal, PlayCate.SINGLE_SEG2_GAMES_2.ordinal, PlayCate.SINGLE_SEG2_GAMES_3.ordinal, PlayCate.SINGLE_SEG2_GAMES_4.ordinal, PlayCate.SINGLE_SEG2_GAMES_5.ordinal,
                    PlayCate.SINGLE_SEG3_GAMES_1.ordinal, PlayCate.SINGLE_SEG3_GAMES_2.ordinal, PlayCate.SINGLE_SEG3_GAMES_3.ordinal, PlayCate.SINGLE_SEG3_GAMES_4.ordinal, PlayCate.SINGLE_SEG3_GAMES_5.ordinal,
                    PlayCate.SINGLE_SEG4_GAMES_1.ordinal, PlayCate.SINGLE_SEG4_GAMES_2.ordinal, PlayCate.SINGLE_SEG4_GAMES_3.ordinal, PlayCate.SINGLE_SEG4_GAMES_4.ordinal, PlayCate.SINGLE_SEG4_GAMES_5.ordinal,
                    PlayCate.SINGLE_SEG5_GAMES_1.ordinal, PlayCate.SINGLE_SEG5_GAMES_2.ordinal, PlayCate.SINGLE_SEG5_GAMES_3.ordinal, PlayCate.SINGLE_SEG5_GAMES_4.ordinal, PlayCate.SINGLE_SEG5_GAMES_5.ordinal,
                    PlayCate.SINGLE.ordinal, PlayCate.SINGLE_SEG1.ordinal, PlayCate.SINGLE_SEG2.ordinal, PlayCate.SINGLE_SEG3.ordinal, PlayCate.SINGLE_SEG4.ordinal, PlayCate.SINGLE_SEG5.ordinal,
                    PlayCate.SET_HDP.ordinal, PlayCate.HDP.ordinal, PlayCate.HDP_SEG1.ordinal, PlayCate.HDP_SEG2.ordinal, PlayCate.HDP_SEG3.ordinal, PlayCate.HDP_SEG4.ordinal, PlayCate.HDP_SEG5.ordinal,
                    PlayCate.OU.ordinal, PlayCate.OU_SEG1.ordinal, PlayCate.OU_SEG2.ordinal, PlayCate.OU_SEG3.ordinal, PlayCate.OU_SEG4.ordinal, PlayCate.OU_SEG5.ordinal,
                    PlayCate.WIN_SEG1_CHAMP.ordinal, PlayCate.LOSE_SEG1_CHAMP.ordinal, PlayCate.TIE_BREAK.ordinal
                    -> LayoutType.SINGLE_2_ITEM.layout

                    PlayCate.CS.ordinal, PlayCate.CS_SEG1.ordinal, PlayCate.LCS.ordinal -> LayoutType.SINGLE_2_CS.layout

                    PlayCate.EPS.ordinal
                    -> LayoutType.EPS.layout

                    else -> LayoutType.ONE_LIST.layout
                }
            }

            GameType.VB -> {
                when (viewType) {
                    PlayCate.SINGLE.ordinal, PlayCate.SET_HDP.ordinal, PlayCate.HDP.ordinal, PlayCate.HDP_SEG1.ordinal, PlayCate.HDP_SEG2.ordinal, PlayCate.HDP_SEG3.ordinal, PlayCate.HDP_SEG4.ordinal, PlayCate.HDP_SEG5.ordinal,
                    PlayCate.OU.ordinal, PlayCate.OU_SEG1.ordinal, PlayCate.OU_SEG2.ordinal, PlayCate.OU_SEG3.ordinal, PlayCate.OU_SEG4.ordinal, PlayCate.OU_SEG5.ordinal,
                    PlayCate.SINGLE_SEG1.ordinal, PlayCate.SINGLE_SEG2.ordinal, PlayCate.SINGLE_SEG3.ordinal, PlayCate.SINGLE_SEG4.ordinal, PlayCate.SINGLE_SEG5.ordinal,
                    PlayCate.OE.ordinal, PlayCate.OE_SEG1.ordinal, PlayCate.OE_SEG2.ordinal, PlayCate.OE_SEG3.ordinal, PlayCate.OE_SEG4.ordinal, PlayCate.OE_SEG5.ordinal
                    -> LayoutType.SINGLE_2_ITEM.layout

                    PlayCate.EPS.ordinal
                    -> LayoutType.EPS.layout

                    else -> LayoutType.ONE_LIST.layout
                }
            }

            GameType.BM -> {
                when (viewType) {
                    PlayCate.SINGLE.ordinal, PlayCate.SINGLE_SEG1.ordinal, PlayCate.SINGLE_SEG2.ordinal, PlayCate.SINGLE_SEG3.ordinal,
                    PlayCate.HDP.ordinal, PlayCate.HDP_SEG1.ordinal, PlayCate.HDP_SEG2.ordinal, PlayCate.HDP_SEG3.ordinal,
                    PlayCate.OU.ordinal, PlayCate.OU_SEG1.ordinal, PlayCate.OU_SEG2.ordinal, PlayCate.OU_SEG3.ordinal, PlayCate.OE_SEG1.ordinal, PlayCate.OE_SEG2.ordinal, PlayCate.OE_SEG3.ordinal, PlayCate.OE.ordinal, PlayCate.SET_HDP.ordinal
                    -> LayoutType.SINGLE_2_ITEM.layout

                    PlayCate.EPS.ordinal
                    -> LayoutType.EPS.layout

                    else -> LayoutType.ONE_LIST.layout
                }
            }

            GameType.AFT -> {
                when (viewType) {
                    PlayCate.SINGLE.ordinal, PlayCate.SINGLE_SEG1.ordinal, PlayCate.SINGLE_SEG2.ordinal, PlayCate.SINGLE_SEG3.ordinal, PlayCate.SINGLE_SEG4.ordinal, PlayCate.SINGLE_1ST.ordinal, PlayCate.SINGLE_2ST.ordinal,
                    PlayCate.HDP.ordinal, PlayCate.HDP_SEG1.ordinal, PlayCate.HDP_SEG2.ordinal, PlayCate.HDP_SEG3.ordinal, PlayCate.HDP_SEG4.ordinal, PlayCate.HDP_1ST.ordinal, PlayCate.HDP_2ST.ordinal,
                    PlayCate.OU.ordinal, PlayCate.OU_SEG1.ordinal, PlayCate.OU_SEG2.ordinal, PlayCate.OU_SEG3.ordinal, PlayCate.OU_SEG4.ordinal, PlayCate.OU_1ST.ordinal, PlayCate.OU_2ST.ordinal
                    -> LayoutType.SINGLE_2_ITEM.layout

                    PlayCate.EPS.ordinal
                    -> LayoutType.EPS.layout

                    else -> LayoutType.ONE_LIST.layout
                }
            }
            GameType.BB -> {
                when (viewType) {
                    PlayCate.SINGLE.ordinal, PlayCate.HDP.ordinal, PlayCate.HDP_SEG1.ordinal, PlayCate.OU.ordinal, PlayCate.OU_1ST.ordinal,
                    PlayCate.EXTRA_TIME.ordinal, PlayCate.SINGLE_1ST.ordinal, PlayCate.TG_OU_H.ordinal, PlayCate.TG_OU_C.ordinal
                    -> LayoutType.SINGLE_2_ITEM.layout

                    PlayCate.SINGLE_SEG1.ordinal, PlayCate.SINGLE_1ST.ordinal
                    -> LayoutType.SINGLE.layout

                    PlayCate.EPS.ordinal
                    -> LayoutType.EPS.layout

                    else -> LayoutType.ONE_LIST.layout
                }
            }

            GameType.CB -> {
                when (viewType) {
                    PlayCate.SINGLE.ordinal, PlayCate.SINGLE_SEG1.ordinal, PlayCate.SINGLE_SEG2.ordinal, PlayCate.SINGLE_SEG3.ordinal, PlayCate.SINGLE_SEG4.ordinal,
                    PlayCate.SINGLE_SEG5.ordinal, PlayCate.SINGLE_SEG6.ordinal, PlayCate.SINGLE_SEG7.ordinal, PlayCate.SINGLE_SEG8.ordinal, PlayCate.SINGLE_1ST.ordinal,
                    PlayCate.HDP.ordinal, PlayCate.OU.ordinal
                    -> LayoutType.SINGLE_2_ITEM.layout

                    PlayCate.EPS.ordinal
                    -> LayoutType.EPS.layout

                    else -> LayoutType.ONE_LIST.layout
                }
            }
            GameType.IH -> {
                when (viewType) {
                    PlayCate.HDP.ordinal, PlayCate.OU.ordinal, PlayCate.OE.ordinal, PlayCate.SINGLE_ND.ordinal
                    -> LayoutType.SINGLE_2_ITEM.layout

                    PlayCate.SINGLE.ordinal
                    -> LayoutType.SINGLE.layout

                    PlayCate.EPS.ordinal
                    -> LayoutType.EPS.layout

                    else -> LayoutType.ONE_LIST.layout
                }
            }
            GameType.RB -> {
                when (viewType) {
                    PlayCate.SINGLE_ND.ordinal,
                    PlayCate.HDP.ordinal, PlayCate.HDP_1ST.ordinal, PlayCate.HDP_2ST.ordinal,
                    PlayCate.OU.ordinal, PlayCate.OU_1ST.ordinal, PlayCate.OU_2ST.ordinal,
                    PlayCate.WM.ordinal
                    -> LayoutType.SINGLE_2_ITEM.layout

                    PlayCate.SINGLE.ordinal, PlayCate.SINGLE_1ST.ordinal, PlayCate.SINGLE_2ST.ordinal
                    -> LayoutType.SINGLE.layout

                    PlayCate.EPS.ordinal
                    -> LayoutType.EPS.layout

                    else -> LayoutType.ONE_LIST.layout
                }
            }

            GameType.TT -> {
                when (viewType) {
                    PlayCate.SINGLE.ordinal, PlayCate.SINGLE_SEG1.ordinal, PlayCate.SINGLE_SEG2.ordinal, PlayCate.SINGLE_SEG3.ordinal, PlayCate.SINGLE_SEG4.ordinal,
                    PlayCate.HDP.ordinal, PlayCate.HDP_SEG1.ordinal, PlayCate.HDP_SEG2.ordinal, PlayCate.HDP_SEG3.ordinal, PlayCate.HDP_SEG4.ordinal,
                    PlayCate.OU.ordinal, PlayCate.OU_SEG1.ordinal, PlayCate.OU_SEG2.ordinal, PlayCate.OU_SEG3.ordinal, PlayCate.OU_SEG4.ordinal,
                    PlayCate.SET_HDP.ordinal
                    -> LayoutType.SINGLE_2_ITEM.layout

                    PlayCate.EPS.ordinal
                    -> LayoutType.EPS.layout

                    else -> LayoutType.ONE_LIST.layout
                }
            }

            GameType.BX -> {
                when (viewType) {
                    PlayCate.SINGLE.ordinal, PlayCate.OU.ordinal, PlayCate.GTD.ordinal, PlayCate.MOV.ordinal, PlayCate.MOV_UFC.ordinal, PlayCate.ROUND.ordinal, PlayCate.ROUND_UFC.ordinal
                    -> LayoutType.SINGLE_2_ITEM.layout

                    PlayCate.EPS.ordinal
                    -> LayoutType.EPS.layout

                    else -> LayoutType.ONE_LIST.layout
                }
            }

            GameType.CK -> {
                when (viewType) {
                    PlayCate.SINGLE.ordinal, PlayCate.TO_WIN_THE_TOSS.ordinal, PlayCate.TOP_TEAM_BATSMAN_H.ordinal, PlayCate.TOP_TEAM_BATSMAN_C.ordinal, PlayCate.TOP_TEAM_BOWLER_H.ordinal, PlayCate.TOP_TEAM_BOWLER_C.ordinal, PlayCate.MOST_MATCH_FOURS.ordinal, PlayCate.MOST_MATCH_SIXES.ordinal,
                    PlayCate.HIGHEST_OPENING_PARTNERSHIP.ordinal, PlayCate.RUN_AT_FALL_OF_1ST_WICKET_H.ordinal, PlayCate.RUN_AT_FALL_OF_1ST_WICKET_C.ordinal, PlayCate.WICKET_METHOD_1ST.ordinal, PlayCate.WICKET_METHOD_H_1ST.ordinal, PlayCate.WICKET_METHOD_C_1ST.ordinal,
                    PlayCate.OVER_RUNS_2_WAY_H_1ST.ordinal, PlayCate.OVER_RUNS_2_WAY_C_1ST.ordinal,
                    PlayCate.SINGLE_ND.ordinal, PlayCate.TWTT.ordinal, PlayCate.T_BATSMAN_H.ordinal, PlayCate.T_BATSMAN_C.ordinal,
                    PlayCate.T_BOWLER_H.ordinal, PlayCate.T_BOWLER_C.ordinal, PlayCate.RAFO_1ST_W_H.ordinal, PlayCate.RAFO_1ST_W_C.ordinal, PlayCate.W_METHOD_1ST.ordinal,
                    PlayCate.W_METHOD_H_1ST.ordinal, PlayCate.W_METHOD_C_1ST.ordinal, PlayCate.O_R_2_WAY_H_1ST.ordinal, PlayCate.O_R_2_WAY_C_1ST.ordinal,
                    PlayCate.NMO_1ST_H.ordinal, PlayCate.NMO_1ST_C.ordinal, PlayCate.NMO_2ND_H.ordinal, PlayCate.NMO_2ND_C.ordinal, PlayCate.MODW_1ST_H.ordinal,
                    PlayCate.MODW_1ST_C.ordinal, PlayCate.MODW_2ND_H.ordinal,
                    PlayCate.MODW_2ND_C.ordinal, PlayCate.S_RAFO_1ST_W_H.ordinal, PlayCate.S_RAFO_1ST_W_C.ordinal,
                    PlayCate.S_RAFO_2ND_W_H.ordinal, PlayCate.S_RAFO_2ND_W_C.ordinal,
                    PlayCate.S_MR_1ST_H.ordinal, PlayCate.S_MR_1ST_C.ordinal, PlayCate.S_MR_2ND_H.ordinal, PlayCate.S_MR_2ND_C.ordinal, PlayCate.NMO_H.ordinal, PlayCate.NMO_C.ordinal,
                    PlayCate.S_MR_H.ordinal, PlayCate.S_MR_C.ordinal, PlayCate.MOD_W_H.ordinal, PlayCate.MOD_W_C.ordinal, PlayCate.OU_2_WAY_1ST_C.ordinal, PlayCate.OU_2_WAY_1ST_H.ordinal,
                    -> LayoutType.SINGLE_2_ITEM.layout

                    PlayCate.MOST_FOUR.ordinal, PlayCate.MOST_SIX.ordinal,
                    PlayCate.HOP.ordinal, PlayCate.FIL.ordinal
                    -> LayoutType.SINGLE.layout

                    PlayCate.EPS.ordinal
                    -> LayoutType.EPS.layout

                    else -> LayoutType.ONE_LIST.layout
                }
            }

            else -> LayoutType.ONE_LIST.layout

        }

        return when (viewType) {
            PlayCate.BOTTOM_NAVIGATION.ordinal -> {
                BottomNavigationViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.home_bottom_navigation, parent, false)
                )
            }
            else ->{
                ViewHolder(
                    LayoutInflater.from(parent.context).inflate(layout, parent, false),
                    viewType
                ).apply {

                    when (layout) {

                        LayoutType.SCO.layout -> {
                            rvBet?.apply {
                                addItemDecoration(
                                    DividerItemDecorator(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.divider_color_silverlight_1dp
                                        )
                                    )
                                )
                            }
                        }

                        LayoutType.GROUP_4.layout,
                        LayoutType.GROUP_6.layout -> {
                            rvBet?.apply {
                                addItemDecoration(
                                    DividerItemDecorator(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.divider_color_white4_2dp
                                        )
                                    )
                                )
                            }
                        }

                        LayoutType.CS.layout -> {
                            rvHome?.apply {
                                addItemDecoration(
                                    CustomForOddDetailVerticalDivider(
                                        context,
                                        R.dimen.recyclerview_item_dec_spec_odds_detail_odds
                                    )
                                )
                            }
                            rvDraw?.apply {
                                addItemDecoration(
                                    CustomForOddDetailVerticalDivider(
                                        context,
                                        R.dimen.recyclerview_item_dec_spec_odds_detail_odds
                                    )
                                )
                            }
                            rvAway?.apply {
                                addItemDecoration(
                                    CustomForOddDetailVerticalDivider(
                                        context,
                                        R.dimen.recyclerview_item_dec_spec_odds_detail_odds
                                    )
                                )
                            }
                        }

                        LayoutType.ONE_LIST.layout,
                        LayoutType.FG_LG.layout -> {
                            rvBet?.apply {
                                addItemDecoration(
                                    CustomForOddDetailVerticalDivider(
                                        context,
                                        R.dimen.recyclerview_item_dec_spec_odds_detail_odds
                                    )
                                )
                            }
                        }

                        LayoutType.SINGLE.layout,
                        LayoutType.SINGLE_2_ITEM.layout,
                        LayoutType.SINGLE_2_CS.layout -> {
                            rvBet?.apply {
                                addItemDecoration(
                                    GridItemDecoration(
                                        context.resources.getDimensionPixelOffset(R.dimen.recyclerview_item_dec_spec_odds_detail_odds),
                                        context.resources.getDimensionPixelOffset(R.dimen.recyclerview_item_dec_spec_odds_detail_odds),
                                        ContextCompat.getColor(context, R.color.colorWhite),
                                        false
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

    }


    override fun getItemCount(): Int = if (oddsDetailDataList.isEmpty()) {
        1
    } else {
        oddsDetailDataList.size + 1
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                holder.bindModel(oddsDetailDataList[position])
            }
        }
    }

    fun notifyDataSetChangedByCode(code: String) {
        this.code = code
        notifyDataSetChanged()
    }


    @Suppress("UNCHECKED_CAST")
    inner class ViewHolder(itemView: View, var viewType: Int) : RecyclerView.ViewHolder(itemView) {

        private fun setVisibility(visible: Boolean) {
            val param = itemView.layoutParams as RecyclerView.LayoutParams
            if (visible) {
                param.height = LinearLayout.LayoutParams.WRAP_CONTENT
                param.width = LinearLayout.LayoutParams.MATCH_PARENT
                itemView.visibility = View.VISIBLE
            } else {
                param.height = 0
                param.width = 0
                itemView.visibility = View.GONE
            }
            itemView.layoutParams = param
        }

        private fun controlExpandBottom(expand: Boolean) {
            val param = itemView.layoutParams as RecyclerView.LayoutParams
            param.bottomMargin = if (expand) 0.dp else 8.dp
            itemView.layoutParams = param
        }

        private val tvGameName: TextView? = itemView.findViewById(R.id.tv_game_name)
        private val oddsDetailPin: ImageView? = itemView.findViewById(R.id.odd_detail_pin)
        private val clItem: ConstraintLayout? = itemView.findViewById(R.id.cl_item)

        val rvBet: RecyclerView? = itemView.findViewById(R.id.rv_bet)

        //cs
        val rvHome: RecyclerView? = itemView.findViewById(R.id.rv_home)
        val rvDraw: RecyclerView? = itemView.findViewById(R.id.rv_draw)
        val rvAway: RecyclerView? = itemView.findViewById(R.id.rv_away)

        //FGLG
        private val tvFg: TextView? = itemView.findViewById(R.id.tv_fg)
        private val tvLg: TextView? = itemView.findViewById(R.id.tv_lg)

        //SCO, CS
        private val tvHomeName: TextView? = itemView.findViewById(R.id.tv_home_name)
        private val tvAwayName: TextView? = itemView.findViewById(R.id.tv_away_name)

        fun bindModel(oddsDetail: OddsDetailListData) {
            /**
             * tvGameName比賽狀態顯示細體的規則：
             * 籃球：玩法有 -SEG("第N盤") -1ST("上半場") -2ST("下半場")
             * 足球：玩法有 -1ST("上半場") -2ST("下半場")，-SEG會是時間
             * 網球：玩法有 -SEG("第N盤") -1ST("上半場") -2ST("下半場")，如果有 -SEG要判斷不能有CHAMP,CS-SEG1(第1盘正确比分)也是例外
             * 排球：玩法有 -SEG("第N盤")
             * */

            when (sportCode) {
                GameType.BK -> {
                    tvGameName?.text = when {
                        oddsDetail.gameType.contains("-SEG") || oddsDetail.gameType.contains("-1ST") || oddsDetail.gameType.contains(
                            "-2ST"
                        ) -> tvGameName?.context?.let { getTitle(it, oddsDetail) }
                        else -> tvGameName?.context?.let { getTitleNormal(oddsDetail) }
                    }
                }
                GameType.FT -> {
                    tvGameName?.text = when {
                        oddsDetail.gameType.contains("-1ST") || oddsDetail.gameType.contains("-2ST")
                        -> tvGameName?.context?.let { getTitle(it, oddsDetail) }
                        else -> tvGameName?.context?.let { getTitleNormal(oddsDetail) }
                    }
                }
                GameType.TN -> {
                    tvGameName?.text = when {
                        (oddsDetail.gameType.contains("-SEG") && !oddsDetail.gameType.contains("CHAMP") && !oddsDetail.gameType.contains(
                            "CS-SEG"
                        )) || oddsDetail.gameType.contains(
                            "-1ST"
                        ) || oddsDetail.gameType.contains(
                            "-2ST"
                        ) -> tvGameName?.context?.let { getTitle(it, oddsDetail) }
                        else -> tvGameName?.context?.let { getTitleNormal(oddsDetail) }
                    }
                }
                GameType.VB -> {
                    tvGameName?.text = when {
                        oddsDetail.gameType.contains("-SEG") -> tvGameName?.context?.let {
                            getTitle(
                                it,
                                oddsDetail
                            )
                        }
                        else -> tvGameName?.context?.let { getTitleNormal(oddsDetail) }
                    }
                }
                else -> {
                    tvGameName?.text = getTitleNormal(oddsDetail)
                }
            }



            oddsDetailPin?.apply {
                isActivated = oddsDetail.isPin

                setOnClickListener {
                    oddsDetailListener?.onClickFavorite(oddsDetail.gameType)
                }
            }

            controlExpandBottom(oddsDetail.isExpand)

            clItem?.setOnClickListener {
                oddsDetail.isExpand = !oddsDetail.isExpand
                notifyItemChanged(adapterPosition)
            }

            rvBet?.visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE

            when (sportCode) {
                GameType.FT -> {
                    when (viewType) {
                        PlayCate.TG_OU.ordinal, PlayCate.HDP.ordinal, PlayCate.HDP_1ST.ordinal, PlayCate.OU.ordinal, PlayCate.OU_1ST.ordinal, PlayCate.TG.ordinal, PlayCate.TG_1ST.ordinal, PlayCate.BTS.ordinal,
                        PlayCate.BTS_1ST.ordinal, PlayCate.BTS_2ST.ordinal, PlayCate.OE.ordinal, PlayCate.OE_1ST.ordinal, PlayCate.CLSH.ordinal, PlayCate.TWTN.ordinal, PlayCate.RTG2.ordinal, PlayCate.RTG3.ordinal, PlayCate.HWMG.ordinal,
                        PlayCate.SBH.ordinal, PlayCate.OWN_GOAL.ordinal, PlayCate.TWFB.ordinal, PlayCate.WEH.ordinal, PlayCate.WBH.ordinal, PlayCate.CORNER_FIRST.ordinal, PlayCate.CORNER_LAST.ordinal, PlayCate.PENALTY_FIRST.ordinal,
                        PlayCate.PENALTY_LAST.ordinal, PlayCate.KICK_OFF.ordinal, PlayCate.PENALTY_AWARDED.ordinal, PlayCate.FREE_KICK_FIRST.ordinal, PlayCate.FREE_KICK_LAST.ordinal, PlayCate.GOAL_KICK_FIRST.ordinal, PlayCate.GOAL_KICK_LAST.ordinal,
                        PlayCate.FOUL_BALL_FIRST.ordinal, PlayCate.FOUL_BALL_LAST.ordinal, PlayCate.OFFSIDE_FIRST.ordinal, PlayCate.OFFSIDE_LAST.ordinal, PlayCate.SUBSTITUTION_FIRST.ordinal, PlayCate.SUBSTITUTION_LAST.ordinal,
                        PlayCate.EXTRA_TIME.ordinal, PlayCate.RED_CARD_PLAYER.ordinal, PlayCate.CORNER_HDP.ordinal, PlayCate.CORNER_1ST_HDP.ordinal, PlayCate.CORNER_OU.ordinal, PlayCate.CORNER_1ST_OU.ordinal, PlayCate.CORNER_OE.ordinal, PlayCate.CORNER_1ST_OE.ordinal,
                        PlayCate.PENALTY_HDP.ordinal, PlayCate.PENALTY_1ST_HDP.ordinal, PlayCate.PENALTY_OU.ordinal, PlayCate.PENALTY_1ST_OU.ordinal, PlayCate.PENALTY_OE.ordinal, PlayCate.PENALTY_1ST_OE.ordinal,
                        PlayCate.HDP_OT.ordinal, PlayCate.HDP_1ST_OT.ordinal, PlayCate.OU_OT.ordinal, PlayCate.OU_1ST_OT.ordinal, PlayCate.TG_OT.ordinal, PlayCate.OE_OT.ordinal, PlayCate.BTS_OT.ordinal, PlayCate.PK.ordinal, PlayCate.ADVANCE.ordinal,
                        PlayCate.TG_OU_H.ordinal, PlayCate.TG_OU_H_1ST.ordinal, PlayCate.TG_OE_H.ordinal, PlayCate.TG_OE_H_1ST.ordinal, PlayCate.TG_OU_C.ordinal, PlayCate.TG_OU_C_1ST.ordinal, PlayCate.TG_OE_C.ordinal, PlayCate.TG_OE_C_1ST.ordinal,
                        PlayCate.P_HDP.ordinal, PlayCate.P_HDP_1ST.ordinal, PlayCate.P_OU.ordinal, PlayCate.P_OU_1ST.ordinal, PlayCate.P_OE.ordinal, PlayCate.P_OE_1ST.ordinal, PlayCate.HDP_SEG1.ordinal, PlayCate.OU_SEG1.ordinal, PlayCate.HDP_SEG2.ordinal,
                        PlayCate.OU_SEG2.ordinal, PlayCate.HDP_SEG3.ordinal, PlayCate.OU_SEG3.ordinal, PlayCate.HDP_SEG4.ordinal, PlayCate.OU_SEG4.ordinal, PlayCate.HDP_SEG5.ordinal, PlayCate.OU_SEG5.ordinal, PlayCate.HDP_SEG6.ordinal, PlayCate.OU_SEG6.ordinal,
                        PlayCate.CORNER_HDP_SEG1.ordinal, PlayCate.CORNER_OU_SEG1.ordinal, PlayCate.CORNER_HDP_SEG2.ordinal, PlayCate.CORNER_OU_SEG2.ordinal, PlayCate.CORNER_HDP_SEG3.ordinal, PlayCate.CORNER_OU_SEG3.ordinal,
                        PlayCate.CORNER_HDP_SEG4.ordinal, PlayCate.CORNER_OU_SEG4.ordinal, PlayCate.CORNER_HDP_SEG5.ordinal, PlayCate.CORNER_OU_SEG5.ordinal, PlayCate.CORNER_HDP_SEG6.ordinal, PlayCate.CORNER_OU_SEG6.ordinal,
                        PlayCate.PENALTY_HDP_SEG1.ordinal, PlayCate.PENALTY_OU_SEG1.ordinal, PlayCate.PENALTY_HDP_SEG2.ordinal, PlayCate.PENALTY_OU_SEG2.ordinal, PlayCate.PENALTY_HDP_SEG3.ordinal, PlayCate.PENALTY_OU_SEG3.ordinal,
                        PlayCate.PENALTY_HDP_SEG4.ordinal, PlayCate.PENALTY_OU_SEG4.ordinal, PlayCate.PENALTY_HDP_SEG5.ordinal, PlayCate.PENALTY_OU_SEG5.ordinal, PlayCate.PENALTY_HDP_SEG6.ordinal, PlayCate.PENALTY_OU_SEG6.ordinal,
                        PlayCate.TG_OU_OT_H.ordinal, PlayCate.TG_OU_OT_C.ordinal
                        -> forSingle(oddsDetail, 2)

                        PlayCate.SINGLE.ordinal, PlayCate.SINGLE_1ST.ordinal, PlayCate.HWMG_SINGLE.ordinal, PlayCate.CORNER_SINGLE.ordinal, PlayCate.CORNER_1ST_SINGLE.ordinal, PlayCate.PENALTY_SINGLE.ordinal, PlayCate.PENALTY_1ST_SINGLE.ordinal,
                        PlayCate.SINGLE_OT.ordinal, PlayCate.SINGLE_1ST_OT.ordinal, PlayCate.P_SINGLE.ordinal, PlayCate.P_SINGLE_1ST.ordinal, PlayCate.SINGLE_SEG1.ordinal, PlayCate.SINGLE_SEG2.ordinal, PlayCate.SINGLE_SEG3.ordinal,
                        PlayCate.SINGLE_SEG4.ordinal, PlayCate.SINGLE_SEG5.ordinal, PlayCate.SINGLE_SEG6.ordinal, PlayCate.CORNER_SINGLE_SEG1.ordinal, PlayCate.CORNER_SINGLE_SEG2.ordinal, PlayCate.CORNER_SINGLE_SEG3.ordinal,
                        PlayCate.CORNER_SINGLE_SEG4.ordinal, PlayCate.CORNER_SINGLE_SEG5.ordinal, PlayCate.CORNER_SINGLE_SEG6.ordinal, PlayCate.PENALTY_SINGLE_SEG1.ordinal, PlayCate.PENALTY_SINGLE_SEG2.ordinal, PlayCate.PENALTY_SINGLE_SEG3.ordinal,
                        PlayCate.PENALTY_SINGLE_SEG4.ordinal, PlayCate.PENALTY_SINGLE_SEG5.ordinal, PlayCate.PENALTY_SINGLE_SEG6.ordinal
                        -> forSingle(oddsDetail, 3)

                        PlayCate.CS.ordinal, PlayCate.CS_OT.ordinal, PlayCate.CS_1ST_SD.ordinal
                        -> forCS(oddsDetail)

                        PlayCate.LCS.ordinal -> forLCS(oddsDetail)

                        PlayCate.FGLG.ordinal
                        -> forFGLG(oddsDetail)

                        PlayCate.SCO.ordinal
                        -> forSCO(oddsDetail, adapterPosition)

                        PlayCate.SINGLE_OU.ordinal, PlayCate.SINGLE_BTS.ordinal, PlayCate.SINGLE_FLG.ordinal
                        -> group6Item(oddsDetail)

                        PlayCate.DC_OU.ordinal, PlayCate.DC_BTS.ordinal, PlayCate.DC_FLG.ordinal
                        -> group6ItemForDC(oddsDetail)

                        PlayCate.OU_BTS.ordinal
                        -> group4ItemForOuBts(oddsDetail)

                        PlayCate.OU_OE.ordinal, PlayCate.OU_TTS1ST.ordinal
                        -> group4ItemForOuTag(oddsDetail)

                        PlayCate.EPS.ordinal
                        -> forEPS(oddsDetail)

                        else -> oneList(oddsDetail)
                    }
                }

                GameType.BK -> {
                    when (viewType) {
                        PlayCate.HDP_INCL_OT.ordinal, PlayCate.HDP_1ST.ordinal, PlayCate.HDP_2ST_INCL_OT.ordinal, PlayCate.HDP_SEG1.ordinal, PlayCate.HDP_SEG2.ordinal, PlayCate.HDP_SEG3.ordinal, PlayCate.HDP_SEG4.ordinal,
                        PlayCate.OU_INCL_OT.ordinal, PlayCate.OU_1ST.ordinal, PlayCate.OU_2ST_INCL_OT.ordinal, PlayCate.OU_SEG1.ordinal, PlayCate.OU_SEG2.ordinal, PlayCate.OU_SEG3.ordinal, PlayCate.OU_SEG4.ordinal,
                        PlayCate.SINGLE_INCL_OT.ordinal, PlayCate.SINGLE_1ST_ND.ordinal, PlayCate.SINGLE_2ST_INCL_OT.ordinal, PlayCate.SINGLE_SEG1_ND.ordinal, PlayCate.SINGLE_SEG2_ND.ordinal, PlayCate.SINGLE_SEG3_ND.ordinal, PlayCate.SINGLE_SEG4_ND.ordinal,
                        PlayCate.OE_INCL_OT.ordinal, PlayCate.OE_1ST.ordinal, PlayCate.OE_2ST_INCL_OT.ordinal, PlayCate.OE_SEG1.ordinal, PlayCate.OE_SEG2.ordinal, PlayCate.OE_SEG3.ordinal, PlayCate.OU_SEG4.ordinal,
                        PlayCate.SEG_POINT.ordinal, PlayCate.LS_SEG1.ordinal, PlayCate.LS_SEG2.ordinal, PlayCate.LS_SEG3.ordinal, PlayCate.LS_SEG4.ordinal, PlayCate.RTP10.ordinal, PlayCate.RTP20.ordinal, PlayCate.P_SCO_OU.ordinal,
                        PlayCate.P_REBOUND_OU.ordinal, PlayCate.P_ASSIST_OU.ordinal, PlayCate.P_THREE_OU.ordinal, PlayCate.P_BLOCK_OU.ordinal, PlayCate.P_STEAL_OU.ordinal, PlayCate.TG_OU_H_1ST.ordinal,
                        PlayCate.TG_OU_H_INCL_OT.ordinal, PlayCate.TG_OU_H_2ST_INCL_OT.ordinal, PlayCate.TG_OU_H_SEG1.ordinal, PlayCate.TG_OU_H_SEG2.ordinal, PlayCate.TG_OU_H_SEG3.ordinal, PlayCate.TG_OU_H_SEG4.ordinal,
                        PlayCate.TG_OU_C_INCL_OT.ordinal, PlayCate.TG_OU_C_1ST.ordinal, PlayCate.TG_OU_C_2ST_INCL_OT.ordinal, PlayCate.TG_OU_C_SEG1.ordinal, PlayCate.TG_OU_C_SEG2.ordinal, PlayCate.TG_OU_C_SEG3.ordinal, PlayCate.TG_OU_C_SEG4.ordinal,
                        PlayCate.OE_SEG4.ordinal
                        -> forSingle(oddsDetail, 2)

                        PlayCate.EPS.ordinal
                        -> forEPS(oddsDetail)

                        else -> oneList(oddsDetail)
                    }
                }

                GameType.TN -> {
                    when (viewType) {
                        PlayCate.SINGLE_SEG1_GAMES_1.ordinal, PlayCate.SINGLE_SEG1_GAMES_2.ordinal, PlayCate.SINGLE_SEG1_GAMES_3.ordinal, PlayCate.SINGLE_SEG1_GAMES_4.ordinal, PlayCate.SINGLE_SEG1_GAMES_5.ordinal,
                        PlayCate.SINGLE_SEG2_GAMES_1.ordinal, PlayCate.SINGLE_SEG2_GAMES_2.ordinal, PlayCate.SINGLE_SEG2_GAMES_3.ordinal, PlayCate.SINGLE_SEG2_GAMES_4.ordinal, PlayCate.SINGLE_SEG2_GAMES_5.ordinal,
                        PlayCate.SINGLE_SEG3_GAMES_1.ordinal, PlayCate.SINGLE_SEG3_GAMES_2.ordinal, PlayCate.SINGLE_SEG3_GAMES_3.ordinal, PlayCate.SINGLE_SEG3_GAMES_4.ordinal, PlayCate.SINGLE_SEG3_GAMES_5.ordinal,
                        PlayCate.SINGLE_SEG4_GAMES_1.ordinal, PlayCate.SINGLE_SEG4_GAMES_2.ordinal, PlayCate.SINGLE_SEG4_GAMES_3.ordinal, PlayCate.SINGLE_SEG4_GAMES_4.ordinal, PlayCate.SINGLE_SEG4_GAMES_5.ordinal,
                        PlayCate.SINGLE_SEG5_GAMES_1.ordinal, PlayCate.SINGLE_SEG5_GAMES_2.ordinal, PlayCate.SINGLE_SEG5_GAMES_3.ordinal, PlayCate.SINGLE_SEG5_GAMES_4.ordinal, PlayCate.SINGLE_SEG5_GAMES_5.ordinal,
                        PlayCate.SINGLE.ordinal, PlayCate.SINGLE_SEG1.ordinal, PlayCate.SINGLE_SEG2.ordinal, PlayCate.SINGLE_SEG3.ordinal, PlayCate.SINGLE_SEG4.ordinal, PlayCate.SINGLE_SEG5.ordinal,
                        PlayCate.SET_HDP.ordinal, PlayCate.HDP.ordinal, PlayCate.HDP_SEG1.ordinal, PlayCate.HDP_SEG2.ordinal, PlayCate.HDP_SEG3.ordinal, PlayCate.HDP_SEG4.ordinal, PlayCate.HDP_SEG5.ordinal,
                        PlayCate.OU.ordinal, PlayCate.OU_SEG1.ordinal, PlayCate.OU_SEG2.ordinal, PlayCate.OU_SEG3.ordinal, PlayCate.OU_SEG4.ordinal, PlayCate.OU_SEG5.ordinal,
                        PlayCate.WIN_SEG1_CHAMP.ordinal, PlayCate.LOSE_SEG1_CHAMP.ordinal, PlayCate.TIE_BREAK.ordinal
                        -> forSingle(oddsDetail, 2)

                        PlayCate.CS.ordinal, PlayCate.CS_SEG1.ordinal, PlayCate.LCS.ordinal -> forSingleCS(oddsDetail, 2)

                        PlayCate.EPS.ordinal
                        -> forEPS(oddsDetail)

                        else -> oneList(oddsDetail)
                    }
                }

                GameType.VB -> {
                    when (viewType) {
                        PlayCate.SINGLE.ordinal, PlayCate.SET_HDP.ordinal, PlayCate.HDP.ordinal, PlayCate.HDP_SEG1.ordinal, PlayCate.HDP_SEG2.ordinal, PlayCate.HDP_SEG3.ordinal, PlayCate.HDP_SEG4.ordinal, PlayCate.HDP_SEG5.ordinal,
                        PlayCate.OU.ordinal, PlayCate.OU_SEG1.ordinal, PlayCate.OU_SEG2.ordinal, PlayCate.OU_SEG3.ordinal, PlayCate.OU_SEG4.ordinal, PlayCate.OU_SEG5.ordinal,
                        PlayCate.SINGLE_SEG1.ordinal, PlayCate.SINGLE_SEG2.ordinal, PlayCate.SINGLE_SEG3.ordinal, PlayCate.SINGLE_SEG4.ordinal, PlayCate.SINGLE_SEG5.ordinal,
                        PlayCate.OE.ordinal, PlayCate.OE_SEG1.ordinal, PlayCate.OE_SEG2.ordinal, PlayCate.OE_SEG3.ordinal, PlayCate.OE_SEG4.ordinal, PlayCate.OE_SEG5.ordinal
                        -> forSingle(oddsDetail, 2)

                        PlayCate.EPS.ordinal
                        -> forEPS(oddsDetail)

                        else -> oneList(oddsDetail)
                    }
                }


                GameType.BM -> {
                    when (viewType) {
                        PlayCate.SINGLE.ordinal, PlayCate.SINGLE_SEG1.ordinal, PlayCate.SINGLE_SEG2.ordinal, PlayCate.SINGLE_SEG3.ordinal,
                        PlayCate.HDP.ordinal, PlayCate.HDP_SEG1.ordinal, PlayCate.HDP_SEG2.ordinal, PlayCate.HDP_SEG3.ordinal,
                        PlayCate.OU.ordinal, PlayCate.OU_SEG1.ordinal, PlayCate.OU_SEG2.ordinal, PlayCate.OU_SEG3.ordinal, PlayCate.OE_SEG1.ordinal, PlayCate.OE_SEG2.ordinal, PlayCate.OE_SEG3.ordinal, PlayCate.OE.ordinal, PlayCate.SET_HDP.ordinal
                        -> forSingle(oddsDetail, 2)

                        PlayCate.EPS.ordinal
                        -> forEPS(oddsDetail)

                        else -> oneList(oddsDetail)
                    }
                }

                GameType.AFT -> {
                    when (viewType) {
                        PlayCate.SINGLE.ordinal, PlayCate.SINGLE_SEG1.ordinal, PlayCate.SINGLE_SEG2.ordinal, PlayCate.SINGLE_SEG3.ordinal, PlayCate.SINGLE_SEG4.ordinal, PlayCate.SINGLE_1ST.ordinal, PlayCate.SINGLE_2ST.ordinal,
                        PlayCate.HDP.ordinal, PlayCate.HDP_SEG1.ordinal, PlayCate.HDP_SEG2.ordinal, PlayCate.HDP_SEG3.ordinal, PlayCate.HDP_SEG4.ordinal, PlayCate.HDP_1ST.ordinal, PlayCate.HDP_2ST.ordinal,
                        PlayCate.OU.ordinal, PlayCate.OU_SEG1.ordinal, PlayCate.OU_SEG2.ordinal, PlayCate.OU_SEG3.ordinal, PlayCate.OU_SEG4.ordinal, PlayCate.OU_1ST.ordinal, PlayCate.OU_2ST.ordinal
                        -> forSingle(oddsDetail, 2)

                        PlayCate.EPS.ordinal
                        -> forEPS(oddsDetail)

                        else -> oneList(oddsDetail)
                    }
                }
                GameType.BB -> {
                    when (viewType) {
                        PlayCate.SINGLE.ordinal, PlayCate.HDP.ordinal, PlayCate.HDP_SEG1.ordinal, PlayCate.OU.ordinal, PlayCate.OU_1ST.ordinal,
                        PlayCate.EXTRA_TIME.ordinal, PlayCate.SINGLE_1ST.ordinal, PlayCate.TG_OU_H.ordinal, PlayCate.TG_OU_C.ordinal
                        -> forSingle(oddsDetail, 2)

                        PlayCate.SINGLE_SEG1.ordinal, PlayCate.SINGLE_1ST.ordinal
                        -> forSingle(oddsDetail, 3)

                        PlayCate.EPS.ordinal
                        -> forEPS(oddsDetail)

                        else -> oneList(oddsDetail)
                    }
                }

                GameType.CB -> {
                    when (viewType) {
                        PlayCate.SINGLE.ordinal, PlayCate.SINGLE_SEG1.ordinal, PlayCate.SINGLE_SEG2.ordinal, PlayCate.SINGLE_SEG3.ordinal, PlayCate.SINGLE_SEG4.ordinal,
                        PlayCate.SINGLE_SEG5.ordinal, PlayCate.SINGLE_SEG6.ordinal, PlayCate.SINGLE_SEG7.ordinal, PlayCate.SINGLE_SEG8.ordinal, PlayCate.SINGLE_1ST.ordinal,
                        PlayCate.HDP.ordinal, PlayCate.OU.ordinal
                        -> forSingle(oddsDetail, 2)

                        PlayCate.EPS.ordinal
                        -> forEPS(oddsDetail)

                        else -> oneList(oddsDetail)
                    }
                }
                GameType.IH -> {
                    when (viewType) {
                        PlayCate.HDP.ordinal, PlayCate.OU.ordinal, PlayCate.OE.ordinal
                        -> forSingle(oddsDetail, 2)

                        PlayCate.SINGLE.ordinal
                        -> forSingle(oddsDetail, 3)

                        PlayCate.EPS.ordinal
                        -> forEPS(oddsDetail)

                        else -> oneList(oddsDetail)
                    }
                }
                GameType.RB -> {
                    when (viewType) {
                        PlayCate.SINGLE_ND.ordinal,
                        PlayCate.HDP.ordinal, PlayCate.HDP_1ST.ordinal, PlayCate.HDP_2ST.ordinal,
                        PlayCate.OU.ordinal, PlayCate.OU_1ST.ordinal, PlayCate.OU_2ST.ordinal,
                        PlayCate.WM.ordinal
                        -> forSingle(oddsDetail, 2)

                        PlayCate.SINGLE.ordinal, PlayCate.SINGLE_1ST.ordinal, PlayCate.SINGLE_2ST.ordinal
                        -> forSingle(oddsDetail, 3)

                        PlayCate.EPS.ordinal
                        -> forEPS(oddsDetail)

                        else -> oneList(oddsDetail)
                    }
                }

                GameType.TT -> {
                    when (viewType) {
                        PlayCate.SINGLE.ordinal, PlayCate.SINGLE_SEG1.ordinal, PlayCate.SINGLE_SEG2.ordinal, PlayCate.SINGLE_SEG3.ordinal, PlayCate.SINGLE_SEG4.ordinal,
                        PlayCate.HDP.ordinal, PlayCate.HDP_SEG1.ordinal, PlayCate.HDP_SEG2.ordinal, PlayCate.HDP_SEG3.ordinal, PlayCate.HDP_SEG4.ordinal,
                        PlayCate.OU.ordinal, PlayCate.OU_SEG1.ordinal, PlayCate.OU_SEG2.ordinal, PlayCate.OU_SEG3.ordinal, PlayCate.OU_SEG4.ordinal,
                        PlayCate.SET_HDP.ordinal
                        -> forSingle(oddsDetail, 2)

                        PlayCate.EPS.ordinal
                        -> forEPS(oddsDetail)

                        else -> oneList(oddsDetail)
                    }
                }

                GameType.BX -> {
                    when (viewType) {
                        PlayCate.SINGLE.ordinal, PlayCate.OU.ordinal, PlayCate.GTD.ordinal, PlayCate.MOV.ordinal, PlayCate.MOV_UFC.ordinal, PlayCate.ROUND.ordinal, PlayCate.ROUND_UFC.ordinal
                        -> forSingle(oddsDetail, 2)

                        PlayCate.EPS.ordinal
                        -> forEPS(oddsDetail)

                        else -> oneList(oddsDetail)
                    }
                }

                GameType.CK -> {
                    when (viewType) {
                        PlayCate.SINGLE.ordinal, PlayCate.TO_WIN_THE_TOSS.ordinal, PlayCate.TOP_TEAM_BATSMAN_H.ordinal, PlayCate.TOP_TEAM_BATSMAN_C.ordinal, PlayCate.TOP_TEAM_BOWLER_H.ordinal, PlayCate.TOP_TEAM_BOWLER_C.ordinal, PlayCate.MOST_MATCH_FOURS.ordinal, PlayCate.MOST_MATCH_SIXES.ordinal,
                        PlayCate.HIGHEST_OPENING_PARTNERSHIP.ordinal, PlayCate.RUN_AT_FALL_OF_1ST_WICKET_H.ordinal, PlayCate.RUN_AT_FALL_OF_1ST_WICKET_C.ordinal, PlayCate.WICKET_METHOD_1ST.ordinal, PlayCate.WICKET_METHOD_H_1ST.ordinal, PlayCate.WICKET_METHOD_C_1ST.ordinal,
                        PlayCate.OVER_RUNS_2_WAY_H_1ST.ordinal, PlayCate.OVER_RUNS_2_WAY_C_1ST.ordinal
                        -> forSingle(oddsDetail, 2)

                        PlayCate.MOST_FOUR.ordinal, PlayCate.MOST_SIX.ordinal,
                        PlayCate.HOP.ordinal, PlayCate.FIL.ordinal
                        -> forSingle(oddsDetail, 3)

                        PlayCate.EPS.ordinal
                        -> forEPS(oddsDetail)

                        else -> oneList(oddsDetail)
                    }
                }

                else -> oneList(oddsDetail)
            }

            for (element in oddsDetail.typeCodes) {
                //有特優賠率時常駐顯示
                if (viewType == PlayCate.EPS.ordinal) {
                    setVisibility(true)
                } else {
                    try {
                        if (element == code) {
                            setVisibility(true)
                            break
                        } else {
                            setVisibility(false)
                        }
                    } catch (e: Exception) {
                        setVisibility(true)
                    }
                }
            }

            if (oddsDetail.isPin) {
                setVisibility(true)
            }
        }

        private fun getTitle(
            context: Context,
            oddsDetail: OddsDetailListData
        ): SpannableStringBuilder {
            val textColor = ContextCompat.getColor(context, R.color.colorGray)
            val gameTitleContentBuilder = SpannableStringBuilder()
            val statusWord =
                oddsDetail.nameMap?.get(LanguageManager.getSelectLanguage(itemView.context).key)
                    ?.split("-")
            val playName =
                oddsDetail.nameMap?.get(LanguageManager.getSelectLanguage(itemView.context).key)
                    ?.replace("-${statusWord?.last() ?: ""}", "")
            val stWordSpan = SpannableString(statusWord?.last() ?: "")
            statusWord?.last()?.length?.let {
                stWordSpan.setSpan(
                    StyleSpan(Typeface.NORMAL),
                    0,
                    it,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                stWordSpan.setSpan(
                    ForegroundColorSpan(textColor),
                    0,
                    it,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                stWordSpan.setSpan(
                    AbsoluteSizeSpan(14, true),
                    0,
                    it,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            val playNameSpan = SpannableString("$playName ")
            playName?.length?.let {
                playNameSpan.setSpan(
                    StyleSpan(Typeface.BOLD), 0,
                    it, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            gameTitleContentBuilder.append(playNameSpan).append(stWordSpan)

            return gameTitleContentBuilder
        }

        private fun getTitleNormal(oddsDetail: OddsDetailListData): SpannableStringBuilder {
            val gameTitleContentBuilder = SpannableStringBuilder()
            val title =
                oddsDetail.nameMap?.get(LanguageManager.getSelectLanguage(itemView.context).key)
            val playNameSpan = SpannableString(title)
            title?.length?.let {
                playNameSpan.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    it,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            gameTitleContentBuilder.append(playNameSpan)

            return gameTitleContentBuilder
        }

        private val epsAdapter by lazy { TypeEPSAdapter() }

        private fun forEPS(oddsDetail: OddsDetailListData) {
            val vpEps = itemView.findViewById<ViewPager2>(R.id.vp_eps)

            vpEps?.apply {
                adapter = epsAdapter
                epsAdapter.setData(
                    oddsDetail,
                    onOddClickListener,
                    oddsType
                )
                getChildAt(0)?.overScrollMode = View.OVER_SCROLL_NEVER //移除漣漪效果
                OverScrollDecoratorHelper.setUpOverScroll(
                    getChildAt(0) as RecyclerView,
                    OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL
                )
                setCurrentItem(oddsDetail.oddArrayList.indexOf(onOddClickListener.clickOdd), false)
            }

            itemView.findViewById<IndicatorView>(R.id.idv_eps).setupWithViewPager2(vpEps)
        }

        private fun oneList(oddsDetail: OddsDetailListData) {
            rvBet?.apply {
                adapter = TypeOneListAdapter(
                    oddsDetail,
                    onOddClickListener,
                    oddsType
                )
                layoutManager = LinearLayoutManager(itemView.context)
            }
        }

        private fun forCS(oddsDetail: OddsDetailListData) {

            tvHomeName?.text = homeName
            tvAwayName?.text = awayName

            itemView.findViewById<LinearLayout>(R.id.ll_content).visibility =
                if (oddsDetail.isExpand) View.VISIBLE else View.GONE

            val homeList: MutableList<Odd> = mutableListOf()
            val drawList: MutableList<Odd> = mutableListOf()
            val awayList: MutableList<Odd> = mutableListOf()

            for (odd in oddsDetail.oddArrayList) {
                if (odd?.name?.contains(" - ") == true) {
                    val stringArray: List<String> = odd.name.split(" - ")
                    if (stringArray[0].toInt() > stringArray[1].toInt()) {
                        homeList.add(odd)
                    }
                    if (stringArray[0].toInt() == stringArray[1].toInt()) {
                        drawList.add(odd)
                    }
                    if (stringArray[0].toInt() < stringArray[1].toInt()) {
                        awayList.add(odd)
                    }
                } else {
                    val list: MutableList<Odd?> = mutableListOf()
                    list.add(odd)
                    val od = OddsDetailListData(
                        oddsDetail.gameType,
                        oddsDetail.typeCodes,
                        oddsDetail.name,
                        list,
                        oddsDetail.nameMap,
                        oddsDetail.rowSort
                    )

                    rvBet?.apply {
                        adapter = TypeOneListAdapter(
                            od,
                            onOddClickListener,
                            oddsType
                        )
                        layoutManager = LinearLayoutManager(itemView.context)
                    }
                }
            }

            homeList.sortBy {
                it.name?.split(" - ")?.get(1)?.toInt()
            }
            homeList.sortBy {
                it.name?.split(" - ")?.get(0)?.toInt()
            }

            awayList.sortBy {
                it.name?.split(" - ")?.get(0)?.toInt()
            }
            awayList.sortBy {
                it.name?.split(" - ")?.get(1)?.toInt()
            }

            rvHome?.apply {
                adapter =
                    TypeCSAdapter(oddsDetail, homeList, onOddClickListener, oddsType)
                layoutManager = LinearLayoutManager(itemView.context)
            }

            rvDraw?.apply {
                adapter =
                    TypeCSAdapter(oddsDetail, drawList, onOddClickListener, oddsType)
                layoutManager = LinearLayoutManager(itemView.context)
            }

            rvAway?.apply {
                adapter =
                    TypeCSAdapter(oddsDetail, awayList, onOddClickListener, oddsType)
                layoutManager = LinearLayoutManager(itemView.context)
            }

            if (drawList.size == 0) {
                rvDraw?.visibility = View.GONE
                itemView.findViewById<TextView>(R.id.tv_draw).visibility = View.GONE
            }
        }

        private fun forLCS(oddsDetail: OddsDetailListData) {

            tvHomeName?.text = homeName
            tvAwayName?.text = awayName

            itemView.findViewById<LinearLayout>(R.id.ll_content).visibility =
                if (oddsDetail.isExpand) View.VISIBLE else View.GONE

            val homeList: MutableList<Odd> = mutableListOf()
            val drawList: MutableList<Odd> = mutableListOf()
            val awayList: MutableList<Odd> = mutableListOf()

            for (odd in oddsDetail.oddArrayList) {
                if (odd?.name?.contains(" - ") == true) {
                    val stringArray: List<String> = odd.name.split(" - ")
                    if (stringArray[0].toInt() > stringArray[1].toInt()) {
                        homeList.add(odd)
                    }
                    if (stringArray[0].toInt() == stringArray[1].toInt()) {
                        drawList.add(odd)
                    }
                    if (stringArray[0].toInt() < stringArray[1].toInt()) {
                        awayList.add(odd)
                    }
                } else {
                    val list: MutableList<Odd?> = mutableListOf()
                    list.add(odd)
                    val od = OddsDetailListData(
                        oddsDetail.gameType,
                        oddsDetail.typeCodes,
                        oddsDetail.name,
                        list,
                        oddsDetail.nameMap,
                        oddsDetail.rowSort
                    )

                    rvBet?.apply {
                        adapter = TypeOneListAdapter(
                            od,
                            onOddClickListener,
                            oddsType,
                            isOddPercentage = true
                        )
                        layoutManager = LinearLayoutManager(itemView.context)
                    }
                }
            }

            homeList.sortBy {
                it.name?.split(" - ")?.get(1)?.toInt()
            }
            homeList.sortBy {
                it.name?.split(" - ")?.get(0)?.toInt()
            }

            awayList.sortBy {
                it.name?.split(" - ")?.get(0)?.toInt()
            }
            awayList.sortBy {
                it.name?.split(" - ")?.get(1)?.toInt()
            }

            rvHome?.apply {
                adapter =
                    TypeCSAdapter(oddsDetail, homeList, onOddClickListener, oddsType, isOddPercentage = true)
                layoutManager = LinearLayoutManager(itemView.context)
            }

            rvDraw?.apply {
                adapter =
                    TypeCSAdapter(oddsDetail, drawList, onOddClickListener, oddsType, isOddPercentage = true)
                layoutManager = LinearLayoutManager(itemView.context)
            }

            rvAway?.apply {
                adapter =
                    TypeCSAdapter(oddsDetail, awayList, onOddClickListener, oddsType, isOddPercentage = true)
                layoutManager = LinearLayoutManager(itemView.context)
            }

            if (drawList.size == 0) {
                rvDraw?.visibility = View.GONE
                itemView.findViewById<TextView>(R.id.tv_draw).visibility = View.GONE
            }
        }

        private fun forSingle(oddsDetail: OddsDetailListData, spanCount: Int) {
            rvBet?.apply {
                adapter = TypeSingleAdapter(oddsDetail, onOddClickListener, oddsType)
                layoutManager = GridLayoutManager(itemView.context, spanCount)
            }
        }

        private fun forSingleCS(oddsDetail: OddsDetailListData, spanCount: Int) {
            tvHomeName?.text = homeName
            tvAwayName?.text = awayName

            tvHomeName?.isVisible = oddsDetail.isExpand
            tvAwayName?.isVisible = oddsDetail.isExpand

            val homeList: MutableList<Odd> = mutableListOf()
            val awayList: MutableList<Odd> = mutableListOf()



            oddsDetail.oddArrayList.forEach {
                it?.let { odd ->
                    val stringArray: List<String>? =
                        odd.name?.replace("\\s".toRegex(), "")?.split("-")
                    stringArray?.let { stringArrayNotNull ->

                        if ((stringArrayNotNull.getOrNull(0)?.toInt()
                                ?: 0) > (stringArrayNotNull.getOrNull(1)?.toInt()
                                ?: 0)
                        ) {
                            homeList.add(odd)
                        } else {
                            awayList.add(odd)
                        }
                    }
                }
            }

            val formattedOddArray = mutableListOf<Odd?>()

            homeList.sortBy { it.rowSort }
            awayList.sortBy { it.rowSort }

            for (i in 0 until homeList.size.coerceAtLeast(awayList.size)) {
                homeList.getOrNull(i)?.let {
                    formattedOddArray.add(it)
                }
                awayList.getOrNull(i)?.let {
                    formattedOddArray.add(it)
                }
            }

            oddsDetail.oddArrayList = formattedOddArray

            rvBet?.apply {
                adapter = TypeSingleAdapter(oddsDetail, onOddClickListener, oddsType)
                layoutManager = GridLayoutManager(itemView.context, spanCount)
            }
        }

        private fun forFGLG(oddsDetail: OddsDetailListData) {
            itemView.findViewById<ConstraintLayout>(R.id.cl_tab).visibility =
                if (oddsDetail.isExpand) View.VISIBLE else View.GONE
            rvBet?.apply {
                adapter = TypeOneListAdapter(
                    selectFGLG(oddsDetail),
                    onOddClickListener,
                    oddsType
                )
                layoutManager = LinearLayoutManager(itemView.context)
            }

            tvFg?.apply {
                isSelected = oddsDetail.gameTypeFgLgSelect == FGLGType.FG
                setOnClickListener {
                    (rvBet?.adapter as TypeOneListAdapter).mOddsDetail =
                        selectFGLG(oddsDetail.apply {
                            gameTypeFgLgSelect = FGLGType.FG
                        })
                }
            }

            tvLg?.apply {
                isSelected = oddsDetail.gameTypeFgLgSelect == FGLGType.LG
                setOnClickListener {
                    (rvBet?.adapter as TypeOneListAdapter).mOddsDetail =
                        selectFGLG(oddsDetail.apply {
                            gameTypeFgLgSelect = FGLGType.LG
                        })
                }
            }
        }

        private fun selectFGLG(oddsDetail: OddsDetailListData): OddsDetailListData {
            val oddArrayList: MutableList<Odd?> = mutableListOf()

            //回傳順序固定為首个进球主队,首个进球客队,无进球,最后进球主队,最后进球客队
            when (oddsDetail.gameTypeFgLgSelect) {
                FGLGType.FG -> {
                    tvFg?.isSelected = true
                    tvLg?.isSelected = false
                    oddArrayList.apply {
                        add(oddsDetail.oddArrayList[0])
                        add(oddsDetail.oddArrayList[1])
                        add(oddsDetail.oddArrayList[2])
                    }
                }
                else -> {
                    tvFg?.isSelected = false
                    tvLg?.isSelected = true
                    oddArrayList.apply {
                        add(oddsDetail.oddArrayList[3])
                        add(oddsDetail.oddArrayList[4])
                        add(oddsDetail.oddArrayList[2])
                    }
                }
            }
            return OddsDetailListData(
                oddsDetail.gameType,
                oddsDetail.typeCodes,
                oddsDetail.name,
                oddArrayList,
                oddsDetail.nameMap,
                oddsDetail.rowSort
            ).apply {
                isExpand = oddsDetail.isExpand
                isMoreExpand = oddsDetail.isMoreExpand
                gameTypeFgLgSelect = oddsDetail.gameTypeFgLgSelect
            }
        }

        private fun forSCO(oddsDetail: OddsDetailListData, position: Int) {

            val teamNameList = setupSCOTeamName(oddsDetail)

            itemView.findViewById<ConstraintLayout>(R.id.cl_tab).visibility =
                if (oddsDetail.isExpand) View.VISIBLE else View.GONE

            rvBet?.apply {
                adapter = TypeSCOAdapter(
                    selectSCO(
                        context,
                        oddsDetail,
                        oddsDetail.gameTypeSCOSelect ?: teamNameList[0],
                        teamNameList
                    ),
                    onOddClickListener,
                    oddsType,
                    object : TypeSCOAdapter.OnMoreClickListener {
                        override fun click() {
                            oddsDetail.isMoreExpand = !oddsDetail.isMoreExpand
                            this@OddsDetailListAdapter.notifyItemChanged(position)
                        }
                    }
                )
                layoutManager = LinearLayoutManager(itemView.context)
            }

            tvHomeName?.apply {
                isSelected = oddsDetail.gameTypeSCOSelect == teamNameList[0]
                setOnClickListener {
                    (rvBet?.adapter as TypeSCOAdapter).mOddsDetail = selectSCO(
                        context = context,
                        oddsDetail = oddsDetail.apply {
                            gameTypeSCOSelect = teamNameList[0]
                            isMoreExpand = false
                        },
                        teamName = oddsDetail.gameTypeSCOSelect ?: teamNameList[0],
                        teamNameList = teamNameList
                    )
                    this@OddsDetailListAdapter.notifyItemChanged(position)
                }
            }

            tvAwayName?.apply {
                isSelected = oddsDetail.gameTypeSCOSelect == teamNameList[1]
                setOnClickListener {
                    (rvBet?.adapter as TypeSCOAdapter).mOddsDetail = selectSCO(
                        context = context,
                        oddsDetail = oddsDetail.apply {
                            gameTypeSCOSelect = teamNameList[1]
                            isMoreExpand = false
                        },
                        teamName = oddsDetail.gameTypeSCOSelect ?: teamNameList[1],
                        teamNameList = teamNameList
                    )
                    this@OddsDetailListAdapter.notifyItemChanged(position)
                }
            }
        }

        private fun setupSCOTeamName(oddsDetail: OddsDetailListData): MutableList<String> {

            val groupTeamName = oddsDetail.oddArrayList.groupBy {
                it?.extInfoMap?.get(LanguageManager.getSelectLanguage(tvHomeName?.context).key)
            }.filterNot {
                it.key.isNullOrBlank()
            }
            return mutableListOf<String>().apply {
                groupTeamName.forEach {
                    it.key?.let { key ->
                        add(
                            key
                        )
                    }
                }
            }.apply {
                tvHomeName?.text = this.firstOrNull()
                tvAwayName?.text = this.getOrNull(1)
            }
        }

        private fun selectSCO(
            context: Context,
            oddsDetail: OddsDetailListData,
            teamName: String,
            teamNameList: MutableList<String>
        ): OddsDetailListData {
            oddsDetail.gameTypeSCOSelect = teamName

            tvHomeName?.isSelected = teamName == teamNameList[0]
            tvAwayName?.isSelected = teamName != teamNameList[0]

            //建立球員列表(一個球員三個賠率)
            var map: HashMap<String, List<Odd?>> = HashMap()

            //過濾掉 其他:(第一、任何、最後), 无進球
            //依隊名分開
            oddsDetail.oddArrayList.filterNot { odd ->
                odd?.playCode == OddSpreadForSCO.SCORE_1ST_O.playCode ||
                        odd?.playCode == OddSpreadForSCO.SCORE_ANT_O.playCode ||
                        odd?.playCode == OddSpreadForSCO.SCORE_LAST_O.playCode ||
                        odd?.playCode == OddSpreadForSCO.SCORE_N.playCode
            }.groupBy {
                it?.extInfoMap?.get(LanguageManager.getSelectLanguage(context).key)
            }.forEach {
                if (it.key == teamName) {
                    map = it.value.groupBy { odd -> odd?.name } as HashMap<String, List<Odd?>>
                }
            }

            //保留 其他:(第一、任何、最後), 无進球
            //依球員名稱分開
            //倒序排列 多的在前(無進球只有一種賠率 放最後面)
            //添加至球員列表內
            oddsDetail.oddArrayList.filter { odd ->
                odd?.playCode == OddSpreadForSCO.SCORE_1ST_O.playCode ||
                        odd?.playCode == OddSpreadForSCO.SCORE_ANT_O.playCode ||
                        odd?.playCode == OddSpreadForSCO.SCORE_LAST_O.playCode ||
                        odd?.playCode == OddSpreadForSCO.SCORE_N.playCode
            }.groupBy {
                it?.name
            }.entries.sortedByDescending {
                it.value.size
            }.associateBy(
                { it.key }, { it.value }
            ).forEach {
                map[it.key ?: ""] = it.value
            }

            return OddsDetailListData(
                oddsDetail.gameType,
                oddsDetail.typeCodes,
                oddsDetail.name,
                oddsDetail.oddArrayList,
                oddsDetail.nameMap,
                oddsDetail.rowSort
            ).apply {
                isExpand = oddsDetail.isExpand
                isMoreExpand = oddsDetail.isMoreExpand
                gameTypeSCOSelect = oddsDetail.gameTypeSCOSelect
                scoItem = map
            }
        }

        private fun group6Item(oddsDetail: OddsDetailListData) {
            rvBet?.apply {
                adapter = group6AdapterSetup(oddsDetail).apply {
                    leftName = homeName
                    centerName = itemView.context.getString(R.string.draw)
                    rightName = awayName
                }
                layoutManager = LinearLayoutManager(itemView.context)
            }
        }

        private fun group6ItemForDC(oddsDetail: OddsDetailListData) {
            rvBet?.apply {
                adapter = group6AdapterSetup(oddsDetail).apply {
                    leftName = context.getString(R.string.odds_detail_play_type_dc_1X)
                    centerName = context.getString(R.string.odds_detail_play_type_dc_2X)
                    rightName = context.getString(R.string.odds_detail_play_type_dc_12)
                }
                layoutManager = LinearLayoutManager(itemView.context)
            }
        }

        private fun group4ItemForOuBts(oddsDetail: OddsDetailListData) {
            rvBet?.apply {
                adapter = group4AdapterSetup(oddsDetail).apply {
                    leftName = context.getString(R.string.odds_detail_play_type_bts_y)
                    rightName = context.getString(R.string.odds_detail_play_type_bts_n)
                }
                layoutManager = LinearLayoutManager(itemView.context)
            }
        }

        private fun group4ItemForOuTag(oddsDetail: OddsDetailListData) {
            rvBet?.apply {
                adapter = group4AdapterSetup(oddsDetail).apply {
                    leftName = context.getString(R.string.odds_detail_play_type_ou_o)
                    rightName = context.getString(R.string.odds_detail_play_type_ou_u)
                    isShowSpreadWithName = true
                }
                layoutManager = LinearLayoutManager(itemView.context)
            }
        }

        private fun group6AdapterSetup(oddsDetail: OddsDetailListData): Type6GroupAdapter {
            return Type6GroupAdapter(
                oddsDetail,
                onOddClickListener,
                oddsType
            )
        }

        private fun group4AdapterSetup(oddsDetail: OddsDetailListData): Type4GroupAdapter =
            Type4GroupAdapter(
                oddsDetail,
                onOddClickListener,
                oddsType
            )

    }

    class BottomNavigationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}

class OddsDetailListener(
    val clickListenerFavorite: (playCate: String) -> Unit
) {
    fun onClickFavorite(playCate: String) = clickListenerFavorite(playCate)
}