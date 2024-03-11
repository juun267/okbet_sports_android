package org.cxct.sportlottery.ui.sport.detail.adapter2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.network.common.ComparePlayCate
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.sport.detail.OddsDetailListData
import org.cxct.sportlottery.ui.sport.detail.OnOddClickListener
import org.cxct.sportlottery.view.DividerItemDecorator

class OddsDetailListAdapter(val onOddClickListener: OnOddClickListener)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val CS = R.layout.content_odds_detail_list_cs
    val SINGLE_2_CS = R.layout.content_odds_detail_list_single_2_cs_item
    val ONE_LIST = R.layout.content_odds_detail_list_one
    val SINGLE = R.layout.content_odds_detail_list_single
    val SINGLE_2_ITEM = R.layout.content_odds_detail_list_single_2_item
    val FG_LG = R.layout.content_odds_detail_list_fg_lg
    val GROUP_6 = R.layout.content_odds_detail_list_group_6_item
    val GROUP_4 = R.layout.content_odds_detail_list_group_4_item
    val SCO = R.layout.content_odds_detail_list_sco
    val EPS = R.layout.content_odds_detail_list_eps
    val ENDSCORE = R.layout.content_odds_detail_list_endscore

    var isFirstRefresh = false

    @set:Synchronized
    var betInfoList: MutableList<BetInfoListData> = mutableListOf()
        set(value) {
            field = value
            oddsDetailDataList.forEachIndexed { index, data ->
                data.oddArrayList.forEach { odd ->
                    val oddSelected = betInfoList.any { it.matchOdd.oddsId == odd?.id }
                    if (odd?.isSelected != oddSelected) {
                        odd?.isSelected = oddSelected
                        val realIndex = itemList.indexOf(data)
                        notifyItemChanged(realIndex, odd?.id)
                    }
                }
            }

        }


    var oddsDetailDataList = ArrayList<OddsDetailListData>()
        set(value) {
            field = value
            resetListData()
        }

    private var itemList = ArrayList<OddsDetailListData>()
    var homeName: String? = null
    var awayName: String? = null
    var sportCode: GameType? = null

    //隊伍角球數量
    var homeCornerKicks: Int? = null
    var awayCornerKicks: Int? = null

    private var code: String? = null

    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var oddsDetailListener: ((playCate: String) -> Unit)? = null

    fun setPreloadItem() {
        itemList.clear()
        notifyDataSetChanged()
    }

    private fun resetListData(){
        itemList.clear()
        oddsDetailDataList.forEach {
            when{
                it.typeCodes.contains(code)-> itemList.add(it)
                //当玩法为末位比分的时候不需要显示置顶
                it.isPin && code != MatchType.END_SCORE.postValue-> itemList.add(it)
            }
        }
        notifyDataSetChanged()
    }


    override fun getItemViewType(position: Int): Int {
        val item = itemList[position]
        if (item.itemLayout != 0) {
            return item.itemLayout
        }

        val playCateCode = item.gameType
        val playCate = item.playCate
        val viewType =  if (playCate == PlayCate.UNCHECK) {
            ComparePlayCate.comparePlayCateCode(sportCode, playCateCode).ordinal
        } else {
            playCate.ordinal
        }

        item.itemLayout = when (sportCode) {
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
                    PlayCate.TG_OU_OT_H.ordinal, PlayCate.TG_OU_OT_C.ordinal,
                    PlayCate.PK_1ST_C.ordinal,
                    PlayCate.PK_1ST_H.ordinal,
                    PlayCate.PK_2ND_C.ordinal,
                    PlayCate.PK_2ND_H.ordinal,
                    PlayCate.PK_3RD_C.ordinal,
                    PlayCate.PK_3RD_H.ordinal,
                    PlayCate.PK_4TH_C.ordinal,
                    PlayCate.PK_4TH_H.ordinal,
                    PlayCate.PK_5TH_C.ordinal,
                    PlayCate.PK_5TH_H.ordinal,
                    PlayCate.PK_FINISH.ordinal,
                    PlayCate.OU_PK.ordinal,
                    PlayCate.PK_HDP.ordinal,
                    -> SINGLE_2_ITEM

                    PlayCate.SINGLE.ordinal, PlayCate.SINGLE_1ST.ordinal, PlayCate.HWMG_SINGLE.ordinal, PlayCate.CORNER_SINGLE.ordinal, PlayCate.CORNER_1ST_SINGLE.ordinal, PlayCate.PENALTY_SINGLE.ordinal, PlayCate.PENALTY_1ST_SINGLE.ordinal,
                    PlayCate.SINGLE_OT.ordinal, PlayCate.SINGLE_1ST_OT.ordinal, PlayCate.P_SINGLE.ordinal, PlayCate.P_SINGLE_1ST.ordinal, PlayCate.SINGLE_SEG1.ordinal, PlayCate.SINGLE_SEG2.ordinal, PlayCate.SINGLE_SEG3.ordinal,
                    PlayCate.SINGLE_SEG4.ordinal, PlayCate.SINGLE_SEG5.ordinal, PlayCate.SINGLE_SEG6.ordinal, PlayCate.CORNER_SINGLE_SEG1.ordinal, PlayCate.CORNER_SINGLE_SEG2.ordinal, PlayCate.CORNER_SINGLE_SEG3.ordinal,
                    PlayCate.CORNER_SINGLE_SEG4.ordinal, PlayCate.CORNER_SINGLE_SEG5.ordinal, PlayCate.CORNER_SINGLE_SEG6.ordinal, PlayCate.PENALTY_SINGLE_SEG1.ordinal, PlayCate.PENALTY_SINGLE_SEG2.ordinal, PlayCate.PENALTY_SINGLE_SEG3.ordinal,
                    PlayCate.PENALTY_SINGLE_SEG4.ordinal, PlayCate.PENALTY_SINGLE_SEG5.ordinal, PlayCate.PENALTY_SINGLE_SEG6.ordinal,
                    PlayCate.SINGLE_PK.ordinal,
                    PlayCate.PK_ROUND1.ordinal,
                    PlayCate.PK_ROUND2.ordinal,
                    PlayCate.PK_ROUND3.ordinal,
                    PlayCate.PK_ROUND4.ordinal,
                    PlayCate.PK_ROUND5.ordinal,
                    -> SINGLE

                    PlayCate.CS.ordinal, PlayCate.CS_OT.ordinal, PlayCate.CS_1ST_SD.ordinal, PlayCate.LCS.ordinal,
                    -> CS

                    PlayCate.FGLG.ordinal,
                    -> FG_LG

                    PlayCate.SCO.ordinal,
                    -> SCO

                    PlayCate.DC_OU.ordinal, PlayCate.SINGLE_OU.ordinal, PlayCate.SINGLE_BTS.ordinal, PlayCate.SINGLE_FLG.ordinal, PlayCate.DC_BTS.ordinal, PlayCate.DC_FLG.ordinal,
                    -> GROUP_6

                    PlayCate.OU_BTS.ordinal, PlayCate.OU_OE.ordinal, PlayCate.OU_TTS1ST.ordinal,
                    -> GROUP_4

                    PlayCate.EPS.ordinal,
                    -> EPS

                    else -> ONE_LIST
                }
            }

            GameType.BK -> {
                when (viewType) {
                    PlayCate.HDP_INCL_OT.ordinal, PlayCate.HDP_1ST.ordinal, PlayCate.HDP_2ST_INCL_OT.ordinal, PlayCate.HDP_SEG1.ordinal, PlayCate.HDP_SEG2.ordinal, PlayCate.HDP_SEG3.ordinal, PlayCate.HDP_SEG4.ordinal,
                    PlayCate.OU_INCL_OT.ordinal, PlayCate.OU_1ST.ordinal, PlayCate.OU_2ST_INCL_OT.ordinal, PlayCate.OU_SEG1.ordinal, PlayCate.OU_SEG2.ordinal, PlayCate.OU_SEG3.ordinal, PlayCate.OU_SEG4.ordinal,
                    PlayCate.SINGLE_INCL_OT.ordinal, PlayCate.SINGLE_1ST_ND.ordinal, PlayCate.SINGLE_2ST_INCL_OT.ordinal, PlayCate.SINGLE_SEG1_ND.ordinal, PlayCate.SINGLE_SEG2_ND.ordinal, PlayCate.SINGLE_SEG3_ND.ordinal, PlayCate.SINGLE_SEG4_ND.ordinal,
                    PlayCate.OE_INCL_OT.ordinal, PlayCate.OE_1ST.ordinal, PlayCate.OE_2ST_INCL_OT.ordinal, PlayCate.OE_SEG1.ordinal, PlayCate.OE_SEG2.ordinal, PlayCate.OE_SEG3.ordinal,
                    PlayCate.SEG_POINT.ordinal, PlayCate.LS_SEG1.ordinal, PlayCate.LS_SEG2.ordinal, PlayCate.LS_SEG3.ordinal, PlayCate.LS_SEG4.ordinal, PlayCate.RTP10.ordinal, PlayCate.RTP20.ordinal, PlayCate.P_SCO_OU.ordinal,
                    PlayCate.P_REBOUND_OU.ordinal, PlayCate.P_ASSIST_OU.ordinal, PlayCate.P_THREE_OU.ordinal, PlayCate.P_BLOCK_OU.ordinal, PlayCate.P_STEAL_OU.ordinal, PlayCate.TG_OU_H_1ST.ordinal,
                    PlayCate.TG_OU_H_INCL_OT.ordinal, PlayCate.TG_OU_H_2ST_INCL_OT.ordinal, PlayCate.TG_OU_H_SEG1.ordinal, PlayCate.TG_OU_H_SEG2.ordinal, PlayCate.TG_OU_H_SEG3.ordinal, PlayCate.TG_OU_H_SEG4.ordinal,
                    PlayCate.TG_OU_C_INCL_OT.ordinal, PlayCate.TG_OU_C_1ST.ordinal, PlayCate.TG_OU_C_2ST_INCL_OT.ordinal, PlayCate.TG_OU_C_SEG1.ordinal, PlayCate.TG_OU_C_SEG2.ordinal, PlayCate.TG_OU_C_SEG3.ordinal, PlayCate.TG_OU_C_SEG4.ordinal,
                    PlayCate.OE_SEG4.ordinal
                    -> SINGLE_2_ITEM
                    PlayCate.EPS.ordinal,
                    -> EPS
                    PlayCate.FS_LD_CS.ordinal,PlayCate.FS_LD_CS_SEG1.ordinal,PlayCate.FS_LD_CS_SEG2.ordinal,PlayCate.FS_LD_CS_SEG3.ordinal,PlayCate.FS_LD_CS_SEG4.ordinal,
                    -> ENDSCORE
                    else -> ONE_LIST
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
                    PlayCate.WIN_SEG1_CHAMP.ordinal, PlayCate.LOSE_SEG1_CHAMP.ordinal, PlayCate.TIE_BREAK.ordinal,
                    -> SINGLE_2_ITEM

                    PlayCate.CS.ordinal, PlayCate.CS_SEG1.ordinal, PlayCate.LCS.ordinal -> SINGLE_2_CS

                    PlayCate.EPS.ordinal,
                    -> EPS

                    else -> ONE_LIST
                }
            }

            GameType.VB -> {
                when (viewType) {
                    PlayCate.SINGLE.ordinal, PlayCate.SET_HDP.ordinal, PlayCate.HDP.ordinal, PlayCate.HDP_SEG1.ordinal, PlayCate.HDP_SEG2.ordinal, PlayCate.HDP_SEG3.ordinal, PlayCate.HDP_SEG4.ordinal, PlayCate.HDP_SEG5.ordinal,
                    PlayCate.OU.ordinal, PlayCate.OU_SEG1.ordinal, PlayCate.OU_SEG2.ordinal, PlayCate.OU_SEG3.ordinal, PlayCate.OU_SEG4.ordinal, PlayCate.OU_SEG5.ordinal,
                    PlayCate.SINGLE_SEG1.ordinal, PlayCate.SINGLE_SEG2.ordinal, PlayCate.SINGLE_SEG3.ordinal, PlayCate.SINGLE_SEG4.ordinal, PlayCate.SINGLE_SEG5.ordinal,
                    PlayCate.OE.ordinal, PlayCate.OE_SEG1.ordinal, PlayCate.OE_SEG2.ordinal, PlayCate.OE_SEG3.ordinal, PlayCate.OE_SEG4.ordinal, PlayCate.OE_SEG5.ordinal,
                    -> SINGLE_2_ITEM

                    PlayCate.EPS.ordinal,
                    -> EPS

                    else -> ONE_LIST
                }
            }

            GameType.BM -> {
                when (viewType) {
                    PlayCate.SINGLE.ordinal, PlayCate.SINGLE_SEG1.ordinal, PlayCate.SINGLE_SEG2.ordinal, PlayCate.SINGLE_SEG3.ordinal,
                    PlayCate.HDP.ordinal, PlayCate.HDP_SEG1.ordinal, PlayCate.HDP_SEG2.ordinal, PlayCate.HDP_SEG3.ordinal,
                    PlayCate.OU.ordinal, PlayCate.OU_SEG1.ordinal, PlayCate.OU_SEG2.ordinal, PlayCate.OU_SEG3.ordinal, PlayCate.OE_SEG1.ordinal, PlayCate.OE_SEG2.ordinal, PlayCate.OE_SEG3.ordinal, PlayCate.OE.ordinal, PlayCate.SET_HDP.ordinal,
                    -> SINGLE_2_ITEM

                    PlayCate.EPS.ordinal,
                    -> EPS

                    else -> ONE_LIST
                }
            }

            GameType.AFT -> {
                when (viewType) {
                    PlayCate.SINGLE.ordinal, PlayCate.SINGLE_SEG1.ordinal, PlayCate.SINGLE_SEG2.ordinal, PlayCate.SINGLE_SEG3.ordinal, PlayCate.SINGLE_SEG4.ordinal, PlayCate.SINGLE_1ST.ordinal, PlayCate.SINGLE_2ST.ordinal,
                    PlayCate.HDP.ordinal, PlayCate.HDP_SEG1.ordinal, PlayCate.HDP_SEG2.ordinal, PlayCate.HDP_SEG3.ordinal, PlayCate.HDP_SEG4.ordinal, PlayCate.HDP_1ST.ordinal, PlayCate.HDP_2ST.ordinal,
                    PlayCate.OU.ordinal, PlayCate.OU_SEG1.ordinal, PlayCate.OU_SEG2.ordinal, PlayCate.OU_SEG3.ordinal, PlayCate.OU_SEG4.ordinal, PlayCate.OU_1ST.ordinal, PlayCate.OU_2ST.ordinal,
                    -> SINGLE_2_ITEM

                    PlayCate.EPS.ordinal,
                    -> EPS

                    else -> ONE_LIST
                }
            }

            GameType.BB -> {
                when (viewType) {
                    PlayCate.SINGLE.ordinal, PlayCate.HDP.ordinal, PlayCate.HDP_SEG1.ordinal, PlayCate.OU.ordinal, PlayCate.OU_1ST.ordinal,
                    PlayCate.EXTRA_TIME.ordinal, PlayCate.SINGLE_1ST.ordinal, PlayCate.TG_OU_H.ordinal, PlayCate.TG_OU_C.ordinal,
                    -> SINGLE_2_ITEM

                    PlayCate.SINGLE_SEG1.ordinal,
                    -> SINGLE

                    PlayCate.EPS.ordinal,
                    -> EPS

                    PlayCate.WM.ordinal, PlayCate.WM_1ST.ordinal,
                    -> ONE_LIST

                    else -> ONE_LIST
                }
            }

            GameType.ES -> {
                when (viewType) {
                    PlayCate.CS_5_MAP.ordinal -> SINGLE_2_CS
                    else -> SINGLE_2_ITEM
                }
            }

            GameType.CB -> {
                when (viewType) {
                    PlayCate.SINGLE.ordinal, PlayCate.SINGLE_SEG1.ordinal, PlayCate.SINGLE_SEG2.ordinal, PlayCate.SINGLE_SEG3.ordinal, PlayCate.SINGLE_SEG4.ordinal,
                    PlayCate.SINGLE_SEG5.ordinal, PlayCate.SINGLE_SEG6.ordinal, PlayCate.SINGLE_SEG7.ordinal, PlayCate.SINGLE_SEG8.ordinal, PlayCate.SINGLE_1ST.ordinal,
                    PlayCate.SINGLE_2ST.ordinal,
                    PlayCate.HDP.ordinal, PlayCate.OU.ordinal,
                    PlayCate.HDP_1ST.ordinal,PlayCate.HDP_2ST.ordinal,
                    -> SINGLE_2_ITEM

                    PlayCate.EPS.ordinal,
                    -> EPS

                    else -> ONE_LIST
                }
            }

            GameType.IH -> {
                when (viewType) {
                    PlayCate.HDP.ordinal, PlayCate.OU.ordinal, PlayCate.OE.ordinal, PlayCate.SINGLE_ND.ordinal,
                    -> SINGLE_2_ITEM

                    PlayCate.SINGLE.ordinal,
                    -> SINGLE

                    PlayCate.EPS.ordinal,
                    -> EPS

                    else -> ONE_LIST
                }
            }

            GameType.RB -> {
                when (viewType) {
                    PlayCate.SINGLE_ND.ordinal,
                    PlayCate.HDP.ordinal, PlayCate.HDP_1ST.ordinal, PlayCate.HDP_2ST.ordinal,
                    PlayCate.OU.ordinal, PlayCate.OU_1ST.ordinal, PlayCate.OU_2ST.ordinal,
                    -> SINGLE_2_ITEM

                    PlayCate.SINGLE.ordinal, PlayCate.SINGLE_1ST.ordinal, PlayCate.SINGLE_2ST.ordinal,
                    -> SINGLE

                    PlayCate.EPS.ordinal,
                    -> EPS

                    PlayCate.WM.ordinal,
                    -> ONE_LIST

                    else -> ONE_LIST
                }
            }

            GameType.TT -> {
                when (viewType) {
                    PlayCate.SINGLE.ordinal, PlayCate.SINGLE_SEG1.ordinal, PlayCate.SINGLE_SEG2.ordinal,
                    PlayCate.SINGLE_SEG3.ordinal, PlayCate.SINGLE_SEG4.ordinal, PlayCate.HDP.ordinal,
                    PlayCate.HDP_SEG1.ordinal, PlayCate.HDP_SEG2.ordinal, PlayCate.HDP_SEG3.ordinal,
                    PlayCate.HDP_SEG4.ordinal, PlayCate.OU.ordinal, PlayCate.OU_SEG1.ordinal,
                    PlayCate.OU_SEG2.ordinal, PlayCate.OU_SEG3.ordinal, PlayCate.OU_SEG4.ordinal,
                    PlayCate.SET_HDP.ordinal,
                    -> SINGLE_2_ITEM

                    PlayCate.EPS.ordinal,
                    -> EPS

                    else -> ONE_LIST
                }
            }

            GameType.BX -> {
                when (viewType) {
                    PlayCate.SINGLE.ordinal, PlayCate.OU.ordinal, PlayCate.GTD.ordinal,
                    PlayCate.MOV.ordinal, PlayCate.MOV_UFC.ordinal,
                    PlayCate.ROUND.ordinal, PlayCate.ROUND_UFC.ordinal,
                    -> SINGLE_2_ITEM

                    PlayCate.EPS.ordinal,
                    -> EPS

                    else -> ONE_LIST
                }
            }

            GameType.CK -> {
                when (viewType) {
                    PlayCate.TO_WIN_THE_TOSS.ordinal, PlayCate.TOP_TEAM_BATSMAN_H.ordinal, PlayCate.TOP_TEAM_BATSMAN_C.ordinal, PlayCate.TOP_TEAM_BOWLER_H.ordinal, PlayCate.TOP_TEAM_BOWLER_C.ordinal, PlayCate.MOST_MATCH_FOURS.ordinal, PlayCate.MOST_MATCH_SIXES.ordinal,
                    PlayCate.HIGHEST_OPENING_PARTNERSHIP.ordinal, PlayCate.RUN_AT_FALL_OF_1ST_WICKET_H.ordinal, PlayCate.RUN_AT_FALL_OF_1ST_WICKET_C.ordinal, PlayCate.WICKET_METHOD_1ST.ordinal, PlayCate.WICKET_METHOD_H_1ST.ordinal, PlayCate.WICKET_METHOD_C_1ST.ordinal,
                    PlayCate.OVER_RUNS_2_WAY_H_1ST.ordinal, PlayCate.OVER_RUNS_2_WAY_C_1ST.ordinal,
                    PlayCate.SINGLE_ND.ordinal, PlayCate.TWTT.ordinal,
                    PlayCate.W_METHOD_1ST.ordinal,
                    PlayCate.NMO_1ST_H.ordinal, PlayCate.NMO_1ST_C.ordinal, PlayCate.NMO_2ND_H.ordinal, PlayCate.NMO_2ND_C.ordinal, PlayCate.MODW_1ST_H.ordinal,
                    PlayCate.MODW_1ST_C.ordinal, PlayCate.MODW_2ND_H.ordinal,
                    PlayCate.MODW_2ND_C.ordinal,
                    PlayCate.S_RAFO_2ND_W_H.ordinal, PlayCate.S_RAFO_2ND_W_C.ordinal,
                    PlayCate.OU_2_WAY_1ST_C.ordinal, PlayCate.OU_2_WAY_1ST_H.ordinal,
                    -> SINGLE_2_ITEM

                    PlayCate.SINGLE.ordinal, PlayCate.MOST_FOUR.ordinal, PlayCate.MOST_SIX.ordinal,
                    PlayCate.HOP.ordinal, PlayCate.FIL.ordinal,
                    -> SINGLE

                    PlayCate.EPS.ordinal,
                    -> EPS

                    else -> ONE_LIST
                }
            }

            else -> ONE_LIST

        }

        return item.itemLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val vh = OddsDetailVH(this, viewType, LayoutInflater.from(parent.context).inflate(viewType, parent, false))
        if (viewType == SCO && vh.rvBet != null && vh.rvBet.itemDecorationCount == 0) {
            vh.rvBet.addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(vh.rvBet.context, R.drawable.divider_color_silverlight_1dp)))
        }
        return vh
    }

    override fun getItemCount() = itemList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is OddsDetailVH && itemList.isNotEmpty()) {
            holder.bindModel(itemList[position])
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if(holder is OddsDetailVH && itemList.isNotEmpty()) {
            holder.bindModel(itemList[position], payloads)
        }
    }

    fun notifyDataSetChangedByCode(code: String) {
        this.code = code
        isFirstRefresh = true
        resetListData()
    }

}
