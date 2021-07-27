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

                sportCode?.let {
                    val spanCount = PlayCateUtils.getPlayCateSpanCount(data.playTypeCode, sportCode)

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
                        setupOddButton(odd_btn_draw, data.oddList[2])
                    } else {
                        odd_btn_draw.visibility = View.GONE
                    }
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

    }
}