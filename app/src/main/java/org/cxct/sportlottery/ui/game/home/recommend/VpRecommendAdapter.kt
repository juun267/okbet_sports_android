package org.cxct.sportlottery.ui.game.home.recommend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.button_odd_v4.view.*
import kotlinx.android.synthetic.main.home_recommend_vp.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.Odd
import org.cxct.sportlottery.ui.game.PlayCateUtils
import org.cxct.sportlottery.ui.game.common.OddStateViewHolder
import org.cxct.sportlottery.ui.game.home.OnClickOddListener
import org.cxct.sportlottery.ui.game.widget.OddButtonV4
import org.cxct.sportlottery.ui.menu.OddsType


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
                    PlayCateUtils.getPlayCateTitleResId(data.playTypeCode, sportCode)?.let {
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
                    val playName = odd.name ?: ""
                    onClickOddListener?.onClickBet(matchOdd, odd, playCateName, playName)
                }
            }
        }

        //TODO simon test review 賠率按鈕 依照不同盤口 顯示個數(目前假定至多不會超過三個)
        fun getSpanCount(playTypeCode: String): Int {
            return when (playTypeCode) {
                PlayCate.TWTN.value,
                PlayCate.CLSH.value,
                PlayCate.OU.value,
                PlayCate.OU_1ST.value,
                PlayCate.OU_2ST.value,
                PlayCate.OE.value,
                PlayCate.TG.value,
                PlayCate.TG_.value,
                PlayCate.TG_OU.value,
                PlayCate.C_OU.value,
                PlayCate.C_OE.value,
                PlayCate.OU_I_OT.value,
                PlayCate.OU_SEG.value,
                PlayCate.BTS.value,
                PlayCate.HDP.value -> 2

                PlayCate.CS.value -> 1

                PlayCate.SINGLE_OT.value,
                PlayCate.SINGLE_SEG.value,
                PlayCate.SINGLE_1ST.value,
                PlayCate.SINGLE_2ST.value,
                PlayCate.SINGLE.value -> 3

                PlayCate.SINGLE_OT_2.value,
                PlayCate.SINGLE_SEG_2.value,
                PlayCate.SINGLE_1ST_2.value,
                PlayCate.SINGLE_2ST_2.value,
                PlayCate.SINGLE_2.value -> 2

                PlayCate.FGLG.value -> 3

                PlayCate.HWMG_SINGLE.value,
                PlayCate.WBH.value,
                PlayCate.WEH.value,
                PlayCate.SBH.value,
                PlayCate.NGOAL_1.value,
                PlayCate.HDP_ONE_LIST.value,
                PlayCate.SCO.value,
                PlayCate.W3.value,
                PlayCate.DC.value,
                PlayCate.GT1ST.value,
                PlayCate.WM.value,
                PlayCate.HTFT.value -> 2

                PlayCate.DC_OU.value,
                PlayCate.DC_BTS.value,
                PlayCate.DC_FLG.value -> 3

                PlayCate.SINGLE_FLG.value,
                PlayCate.SINGLE_BTS.value,
                PlayCate.SINGLE_OU.value -> 3

                PlayCate.OU_BTS.value -> 2

                PlayCate.OU_TTS1ST.value,
                PlayCate.OU_OE.value -> 2

                //臨時新增或尚未確定的排版 以單行列表作為排版
                else -> 1
            }
        }
    }
}