package org.cxct.sportlottery.ui.game.home.recommend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.button_odd_v4.view.*
import kotlinx.android.synthetic.main.home_recommend_vp.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.game.PlayTypeUtils
import org.cxct.sportlottery.ui.game.common.OddStateViewHolder
import org.cxct.sportlottery.ui.game.home.OnClickOddListener
import org.cxct.sportlottery.ui.game.widget.OddButtonV4
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.odds.OddsDetailListAdapter

class VpRecommendAdapter(
    val sportCode: String?,
    val dataList: List<OddBean>,
    val oddsType: OddsType,
    val matchOdd: MatchOdd
) : RecyclerView.Adapter<VpRecommendAdapter.ViewHolderHdpOu>() {

    var onClickOddListener: OnClickOddListener? = null

    private val mOddStateRefreshListener by lazy {
        object : OddStateViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
                dataList.forEachIndexed { index, oddBean ->
                    if (oddBean.oddList.find { it.id == odd.id } != null)
                        notifyItemChanged(index)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderHdpOu {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_recommend_vp, parent, false)
        return ViewHolderHdpOu(view)
    }

    override fun onBindViewHolder(holder: ViewHolderHdpOu, position: Int) {
        try {
            val data = dataList[position]
            holder.bind(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int = dataList.size

    inner class ViewHolderHdpOu(
        itemView: View,
        override val oddStateChangeListener: OddStateChangeListener = mOddStateRefreshListener
    ) : OddStateViewHolder(itemView) {

        fun bind(data: OddBean) {
            itemView.apply {
                tv_play_type.text =
                    PlayTypeUtils.getPlayTypeTitleResId(data.playTypeCode, sportCode)?.let {
                        itemView.context.getString(it)
                    } ?: ""

                val spanCount = getSpanCount(data.playTypeCode)

                if (spanCount > 0 && data.oddList.isNotEmpty()) {
                    odd_btn_home.visibility = View.VISIBLE
                    setupOddButton(odd_btn_home, data.oddList[0])
                } else {
                    odd_btn_home.visibility = View.GONE
                }

                if (spanCount > 1 && data.oddList.size > 1) {
                    odd_btn_away.visibility = View.VISIBLE
                    setupOddButton(odd_btn_away, data.oddList[1])
                } else {
                    odd_btn_away.visibility = View.GONE
                }
                if (spanCount > 2 && data.oddList.size > 2) {
                    odd_btn_draw.visibility = View.VISIBLE
                    setupOddButton(odd_btn_draw, data.oddList[1])
                } else {
                    odd_btn_draw.visibility = View.GONE
                }
            }
        }

        private fun setupOddButton(oddButton: OddButtonV4, odd: Odd) {
            oddButton.apply homeButtonSettings@{
                odd_type_text.text = odd.name
                odd_top_text.text = odd.spread

                odd_bottom_text.text = when (oddsType) {
                    OddsType.EU -> odd.odds.toString()
                    OddsType.HK -> odd.hkOdds.toString()
                }

                isSelected = odd.isSelected ?: false

                setOnClickListener {
                    val playCateName = itemView.tv_play_type.text.toString()
                    val playName = odd.name?: ""
                    onClickOddListener?.onClickBet(matchOdd, odd, playCateName, playName)
                }
            }
        }

        //TODO simon test review 賠率按鈕 依照不同盤口 顯示個數(目前假定至多不會超過三個)
        fun getSpanCount(playTypeCode: String): Int {
            return when (playTypeCode) {
                OddsDetailListAdapter.GameType.TWTN.value,
                OddsDetailListAdapter.GameType.CLSH.value,
                OddsDetailListAdapter.GameType.OU.value,
                OddsDetailListAdapter.GameType.OU_1ST.value,
                OddsDetailListAdapter.GameType.OU_2ST.value,
                OddsDetailListAdapter.GameType.OE.value,
                OddsDetailListAdapter.GameType.TG.value,
                OddsDetailListAdapter.GameType.TG_.value,
                OddsDetailListAdapter.GameType.TG_OU.value,
                OddsDetailListAdapter.GameType.C_OU.value,
                OddsDetailListAdapter.GameType.C_OE.value,
                OddsDetailListAdapter.GameType.OU_I_OT.value,
                OddsDetailListAdapter.GameType.OU_SEG.value,
                OddsDetailListAdapter.GameType.BTS.value,
                OddsDetailListAdapter.GameType.HDP.value -> 2

                OddsDetailListAdapter.GameType.CS.value -> 1

                OddsDetailListAdapter.GameType.SINGLE_OT.value,
                OddsDetailListAdapter.GameType.SINGLE_SEG.value,
                OddsDetailListAdapter.GameType.SINGLE_1ST.value,
                OddsDetailListAdapter.GameType.SINGLE_2ST.value,
                OddsDetailListAdapter.GameType.SINGLE.value -> 3

                OddsDetailListAdapter.GameType.SINGLE_OT_2.value,
                OddsDetailListAdapter.GameType.SINGLE_SEG_2.value,
                OddsDetailListAdapter.GameType.SINGLE_1ST_2.value,
                OddsDetailListAdapter.GameType.SINGLE_2ST_2.value,
                OddsDetailListAdapter.GameType.SINGLE_2.value -> 2

                OddsDetailListAdapter.GameType.FGLG.value -> 3

                OddsDetailListAdapter.GameType.HWMG_SINGLE.value,
                OddsDetailListAdapter.GameType.WBH.value,
                OddsDetailListAdapter.GameType.WEH.value,
                OddsDetailListAdapter.GameType.SBH.value,
                OddsDetailListAdapter.GameType.NGOAL_1.value,
                OddsDetailListAdapter.GameType.HDP_ONE_LIST.value,
                OddsDetailListAdapter.GameType.SCO.value,
                OddsDetailListAdapter.GameType.W3.value,
                OddsDetailListAdapter.GameType.DC.value,
                OddsDetailListAdapter.GameType.GT1ST.value,
                OddsDetailListAdapter.GameType.WM.value,
                OddsDetailListAdapter.GameType.HTFT.value -> 2

                OddsDetailListAdapter.GameType.DC_OU.value,
                OddsDetailListAdapter.GameType.DC_BTS.value,
                OddsDetailListAdapter.GameType.DC_FLG.value -> 3

                OddsDetailListAdapter.GameType.SINGLE_FLG.value,
                OddsDetailListAdapter.GameType.SINGLE_BTS.value,
                OddsDetailListAdapter.GameType.SINGLE_OU.value -> 3

                OddsDetailListAdapter.GameType.OU_BTS.value -> 2

                OddsDetailListAdapter.GameType.OU_TTS1ST.value,
                OddsDetailListAdapter.GameType.OU_OE.value -> 2

                //臨時新增或尚未確定的排版 以單行列表作為排版
                else -> 1
            }
        }

    }

}