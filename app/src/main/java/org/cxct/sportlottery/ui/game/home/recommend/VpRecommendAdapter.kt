package org.cxct.sportlottery.ui.game.home.recommend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.button_odd_detail.view.*
import kotlinx.android.synthetic.main.home_recommend_champion.view.*
import kotlinx.android.synthetic.main.home_recommend_vp.view.*
import kotlinx.android.synthetic.main.home_recommend_vp.view.tv_play_type
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.game.PlayCateUtils
import org.cxct.sportlottery.ui.game.common.OddStateViewHolder
import org.cxct.sportlottery.ui.game.home.OnClickMoreListener
import org.cxct.sportlottery.ui.game.home.OnClickOddListener
import org.cxct.sportlottery.ui.game.widget.OddsButton
import org.cxct.sportlottery.ui.menu.OddsType


class VpRecommendAdapter(
    val sportCode: String?,
    val dataList: List<OddBean>,
    private val isOutright: Int?,
    val oddsType: OddsType,
    val matchOdd: MatchOdd
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType {
        RECOMMEND_OUTRIGHT, RECOMMEND
    }

    var onClickOddListener: OnClickOddListener? = null
    var onClickOutrightOddListener: OnClickOddListener? = null
    var onClickMoreListener: OnClickMoreListener? = null

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

    override fun getItemViewType(position: Int): Int {
        return when (isOutright) {
            0 -> {
                ItemType.RECOMMEND.ordinal
            }
            else -> {
                ItemType.RECOMMEND_OUTRIGHT.ordinal
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.RECOMMEND.ordinal -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_recommend_vp, parent, false)
                ViewHolderHdpOu(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_recommend_champion, parent, false)
                ViewHolderRecOutright(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        try {
            when (holder) {
                is ViewHolderHdpOu -> {
                    val data = dataList[position]
                    holder.bind(data)
                }

                is ViewHolderRecOutright -> {
                    val data = dataList[0]
                    holder.bind(data)
                }
            }
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
                //TODO simon test review playTypeCode = "EPS",更優賠率 盤口 顯示處理
                tv_play_type.text =
                    PlayCateUtils.getPlayCateTitleResId(data.playTypeCode, sportCode)?.let {
                        itemView.context.getString(it)
                    } ?: ""

                sportCode?.let {
                    val spanCount = PlayCateUtils.getPlayCateSpanCount(data.playTypeCode, sportCode)

                    if (spanCount > 0 && data.oddList.isNotEmpty()) {
                        odd_btn_home.visibility = View.VISIBLE
                        setupOddsButton(odd_btn_home, data.oddList[0])
                    } else {
                        odd_btn_home.visibility = View.GONE
                    }

                    if (spanCount > 1 && data.oddList.size > 1) {
                        odd_btn_away.visibility = View.VISIBLE
                        setupOddsButton(odd_btn_away, data.oddList[1])
                    } else {
                        odd_btn_away.visibility = View.GONE
                    }
                    if (spanCount > 2 && data.oddList.size > 2) {
                        odd_btn_draw.visibility = View.VISIBLE
                        setupOddsButton(odd_btn_draw, data.oddList[2])
                    } else {
                        odd_btn_draw.visibility = View.GONE
                    }
                }
            }
        }

        private fun setupOddsButton(oddsButton: OddsButton, odd: Odd) {
            oddsButton.apply homeButtonSettings@{
                setupOdd(odd, oddsType)

                isSelected = odd.isSelected ?: false

                setOnClickListener {
                    val playCateName = itemView.tv_play_type.text.toString()
                    val playName = odd.name ?: ""

                    onClickOddListener?.onClickBet(matchOdd.apply {
                        this.matchInfo?.gameType = sportCode
                    }, odd, playCateName, playName)
                }
            }
        }

    }

    inner class ViewHolderRecOutright(
        itemView: View,
        override val oddStateChangeListener: OddStateChangeListener = mOddStateRefreshListener
    ) : OddStateViewHolder(itemView) {

        fun bind(data: OddBean) {
            itemView.apply {
                tv_play_type.text =
                    PlayCateUtils.getPlayCateTitleResId(data.playTypeCode, sportCode)?.let {
                        itemView.context.getString(it)
                    } ?: ""

                rec_champ_btn_pre1.apply {
                    if (data.oddList.isEmpty()) {
                        visibility = View.GONE
                        return
                    }

                    setupOdd(data.oddList.getOrNull(0), oddsType)

                    tv_name.apply {
                        text = data.oddList.getOrNull(0)?.spread
                        visibility = View.VISIBLE
                    }

                    tv_spread.text = ""

                    this@ViewHolderRecOutright.setupOddState(this, data.oddList.getOrNull(0))

                    isSelected = data.oddList.getOrNull(0)?.isSelected ?: false

                    setOnClickListener {
                        data.oddList.getOrNull(0)?.let { odd ->
                            onClickOutrightOddListener?.onClickBet(matchOdd.apply {
                                this.matchInfo?.gameType = sportCode
                            }, odd)
                        }
                    }
                }

                rec_champ_btn_pre2.apply {
                    if (data.oddList.size < 2) {
                        visibility = View.GONE
                        return
                    }

                    setupOdd(data.oddList.getOrNull(1), oddsType)

                    tv_name.apply {
                        text = data.oddList.getOrNull(1)?.spread
                        visibility = View.VISIBLE
                    }

                    tv_spread.text = ""

                    this@ViewHolderRecOutright.setupOddState(this, data.oddList.getOrNull(1))

                    isSelected = data.oddList.getOrNull(1)?.isSelected ?: false

                    setOnClickListener {
                        data.oddList.getOrNull(1)?.let { odd ->
                            onClickOutrightOddListener?.onClickBet(matchOdd.apply {
                                this.matchInfo?.gameType = sportCode
                            }, odd)
                        }
                    }
                }

                rec_champ_btn_pre3.apply {
                    if (data.oddList.size < 3) {
                        visibility = View.GONE
                        return
                    }

                    setupOdd(data.oddList.getOrNull(2), oddsType)

                    tv_name.apply {
                        text = data.oddList.getOrNull(2)?.spread
                        visibility = View.VISIBLE
                    }

                    tv_spread.text = ""

                    this@ViewHolderRecOutright.setupOddState(this, data.oddList.getOrNull(2))

                    isSelected = data.oddList.getOrNull(2)?.isSelected ?: false

                    setOnClickListener {
                        data.oddList.getOrNull(2)?.let { odd ->
                            onClickOutrightOddListener?.onClickBet(matchOdd.apply {
                                this.matchInfo?.gameType = sportCode
                            }, odd)
                        }
                    }
                }

                rec_champ_btn_pre4.apply {
                    if (data.oddList.size < 4) {
                        visibility = View.GONE
                        return
                    }

                    setupOdd(data.oddList.getOrNull(3), oddsType)

                    tv_name.apply {
                        text = data.oddList.getOrNull(3)?.spread
                        visibility = View.VISIBLE
                    }

                    tv_spread.text = ""

                    this@ViewHolderRecOutright.setupOddState(this, data.oddList.getOrNull(3))

                    isSelected = data.oddList.getOrNull(3)?.isSelected ?: false

                    setOnClickListener {
                        data.oddList.getOrNull(3)?.let { odd ->
                            onClickOutrightOddListener?.onClickBet(matchOdd.apply {
                                this.matchInfo?.gameType = sportCode
                            }, odd)
                        }
                    }
                }

                rec_champ_more.apply {
                    setOnClickListener {
                        onClickMoreListener?.onClickMore(matchOdd)
                    }
                }
            }
        }
    }
}