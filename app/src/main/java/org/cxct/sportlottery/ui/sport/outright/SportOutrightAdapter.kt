package org.cxct.sportlottery.ui.sport

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import kotlinx.android.synthetic.main.button_odd_outright.view.*
import kotlinx.android.synthetic.main.item_outright_league.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.outright.odds.OutrightItem
import org.cxct.sportlottery.ui.base.BaseGameAdapter
import org.cxct.sportlottery.ui.game.common.OddStateViewHolder
import org.cxct.sportlottery.ui.game.widget.OddsOutrightButton
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.GridItemDecoration
import org.cxct.sportlottery.util.SvgUtil

@SuppressLint("NotifyDataSetChanged")
class SportOutrightAdapter : BaseGameAdapter() {

    enum class OutrightViewType { ITEM }

    var data: List<OutrightItem> = listOf()
        set(value) {
            field = value
            isPreload = false
            notifyDataSetChanged()
        }

    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var outrightOddListener: OutrightOddListener? = null

    fun setPreloadItem() {
        data.toMutableList().clear()
        isPreload = true
        notifyDataSetChanged()
    }

    //TODO need review
    private val oddStateRefreshListener by lazy {
        object : OddStateViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
                updateOdds(odd)
            }
        }
    }

    fun updateOdds(odd: Odd) {
        data.forEachIndexed { index, outrightItem ->
            outrightItem.oddsList.find {
                var newOdd = it.find { index ->
                    index.id == odd.id
                }
                newOdd = odd
                notifyItemChanged(index)
                return
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            OutrightViewType.ITEM.ordinal -> {
                OutrightLeagueViewHolder.from(parent, oddStateRefreshListener)
            }
            else -> initBaseViewHolders(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is OutrightLeagueViewHolder -> {
                holder.bind(data[position], oddsType, outrightOddListener)
            }
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int {
        if (isPreload) {
            return BaseItemType.PRELOAD_ITEM.type
        }

        return OutrightViewType.ITEM.ordinal
    }

    class OutrightLeagueViewHolder private constructor(
        val itemView: View,
        val refreshListener: OddStateViewHolder.OddStateChangeListener,
    ) : OddStateViewHolder(itemView) {
        companion object {
            fun from(
                parent: ViewGroup,
                refreshListener: OddStateChangeListener,
            ): OutrightLeagueViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.item_outright_league, parent, false)
                return OutrightLeagueViewHolder(view, refreshListener)
            }
        }

        override val oddStateChangeListener: OddStateChangeListener
            get() = refreshListener

        @SuppressLint("SetTextI18n")
        fun bind(
            outrightItem: OutrightItem,
            oddsType: OddsType,
            outrightOddListener: OutrightOddListener?,
        ) {
            itemView.outright_info_block.setOnClickListener {
                outrightOddListener?.onClickMatch(outrightItem)
            }
            outrightItem.matchOdd.matchInfo?.let {
                itemView.tv_league_name.text = it.name
                val logo = if (it.img.isNullOrEmpty()) {
                    SvgUtil.defaultIconPath
                } else {
                    it.img!!
                }
                val countryIcon = SvgUtil.getSvgDrawable(itemView.context, logo)
                itemView.iv_league_logo.setImageDrawable(countryIcon)
            }
            itemView.iv_league_arrow.isSelected = outrightItem.leagueExpanded
            itemView.rv_catagory.visibility =
                if (outrightItem.leagueExpanded) View.VISIBLE else View.GONE
            itemView.iv_league_arrow.setOnClickListener {
                outrightOddListener?.onClickExpand(outrightItem)
            }
            itemView.rv_catagory.apply {
                if (adapter == null) {
                    layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    adapter = OddsOutrightCatagoryAdapter(outrightItem,
                        oddsType,
                        this@OutrightLeagueViewHolder,
                        outrightOddListener).apply {
                    }
                } else {
                    (adapter as OddsOutrightCatagoryAdapter).update(outrightItem, oddsType)
                }
            }

        }
    }


    class OddsOutrightCatagoryAdapter(
        var outrightItem: OutrightItem,
        var oddsType: OddsType,
        val oddStateViewHolder: OddStateViewHolder,
        val outrightOddListener: OutrightOddListener?,
    ) : BaseQuickAdapter<List<Odd>?, BaseViewHolder?>(R.layout.item_outright_catagory,
        outrightItem.oddsList) {
        override fun convert(helper: BaseViewHolder?, item: List<Odd>?) {
            helper?.setText(R.id.tv_match_name, outrightItem.subTitleList[helper.layoutPosition])
            helper?.setText(R.id.tv_time,
                "${outrightItem.matchOdd?.startDate ?: ""} ${outrightItem.matchOdd?.startTime ?: ""}")
            helper?.getView<RecyclerView>(R.id.rv_odds)?.apply {
                if (adapter == null) {
                    layoutManager = GridLayoutManager(context, 2)
                    addItemDecoration(GridItemDecoration(
                        context.resources.getDimensionPixelOffset(R.dimen.recyclerview_item_dec_spec_odds_detail_odds),
                        context.resources.getDimensionPixelOffset(R.dimen.recyclerview_item_dec_spec_odds_detail_odds),
                        Color.TRANSPARENT, true))
                    adapter = OddsOutrightOddsAdapter(item, oddsType, oddStateViewHolder).apply {
                        setOnItemClickListener(object : BaseQuickAdapter.OnItemClickListener {
                            override fun onItemClick(
                                adapter: BaseQuickAdapter<*, *>?,
                                view: View?,
                                position: Int,
                            ) {
                                data[position]?.let {
                                    outrightOddListener?.onClickBet(outrightItem,
                                        it,
                                        it.outrightCateKey ?: "")
                                }
                            }
                        })
                    }
                } else {
                    (adapter as OddsOutrightOddsAdapter).update(item, oddsType)
                }

            }
        }

        fun update(outrightItem: OutrightItem, oddsType: OddsType) {
            this.oddsType = oddsType
            this.outrightItem = outrightItem
            setNewData(outrightItem.oddsList)
        }
    }

    class OddsOutrightOddsAdapter(
        data: List<Odd?>?,
        var oddsType: OddsType,
        var oddStateViewHolder: OddStateViewHolder,
    ) : BaseQuickAdapter<Odd?, BaseViewHolder?>(R.layout.item_outright_odd, data) {
        override fun convert(helper: BaseViewHolder?, item: Odd?) {
            helper?.getView<OddsOutrightButton>(R.id.outright_odd_btn)?.apply {
                setupOdd(item, oddsType)
                tv_spread.text = ""
                oddStateViewHolder.setupOddState(this, item)
            }
        }

        fun update(data: List<Odd?>?, oddsType: OddsType) {
            this.oddsType = oddsType
            setNewData(data)
        }
    }
}

class OutrightOddListener(
    val clickListenerBet: (outrightItem: OutrightItem, odd: Odd, playCateCode: String) -> Unit,
    val clickExpand: (outrightItem: OutrightItem) -> Unit,
    private val onClickMatch: (outrightItem: OutrightItem) -> Unit,
) {
    fun onClickBet(outrightItem: OutrightItem, odd: Odd, playCateCode: String) =
        clickListenerBet(outrightItem, odd, playCateCode)

    fun onClickExpand(outrightItem: OutrightItem) = clickExpand(outrightItem)
    fun onClickMatch(outrightItem: OutrightItem) = onClickMatch.invoke(outrightItem)
}
