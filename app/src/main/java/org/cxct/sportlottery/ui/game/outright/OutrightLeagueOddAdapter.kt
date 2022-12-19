package org.cxct.sportlottery.ui.game.outright

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.button_odd_detail.view.*
import kotlinx.android.synthetic.main.itemview_outright_odd_subtitle_v4.view.*
import kotlinx.android.synthetic.main.itemview_outright_odd_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemviewOutrightOddMoreV4Binding
import org.cxct.sportlottery.databinding.ItemviewOutrightTitleV4Binding
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.outright.odds.MatchOdd
import org.cxct.sportlottery.network.outright.odds.OutrightShowMoreItem
import org.cxct.sportlottery.network.outright.odds.OutrightSubTitleItem
import org.cxct.sportlottery.ui.base.BaseGameAdapter
import org.cxct.sportlottery.ui.game.common.OddStateViewHolder
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.DisplayUtil.dp

@SuppressLint("NotifyDataSetChanged")
class OutrightLeagueOddAdapter : BaseGameAdapter() {

    enum class OutrightViewType { TITLE, SUB_TITLE, ODD, MORE, NULL }

    var data: List<Any?> = listOf()
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
                notifyItemChanged(data.indexOf(data.firstOrNull { data ->
                    if (data is Odd)
                        data == odd
                    else false
                }))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            OutrightViewType.TITLE.ordinal -> {
                OutrightTitleViewHolder.from(parent)
            }
            OutrightViewType.SUB_TITLE.ordinal -> {
                SubTitleViewHolder.from(parent)
            }
            OutrightViewType.ODD.ordinal -> {
                OddViewHolder.from(parent, oddStateRefreshListener)
            }
            OutrightViewType.NULL.ordinal -> {
                NullViewHolder.from(parent)
            }
            OutrightViewType.MORE.ordinal -> {
                MoreViewHolder.from(parent)
            }
            else -> initBaseViewHolders(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is OutrightTitleViewHolder -> {
                holder.bind(data[position] as MatchOdd, outrightOddListener)
            }
            is SubTitleViewHolder -> {
                val item = data[position] as OutrightSubTitleItem
                holder.bind(item.belongMatchOdd, item, outrightOddListener)
            }
            is OddViewHolder -> {
                val item = data[position] as Odd
                item.belongMatchOdd?.matchInfo?.leagueName =
                    item.belongMatchOdd?.matchInfo?.name //注單會使用leagueName這欄位, 但後端沒傳
                holder.bind(item.belongMatchOdd, item, outrightOddListener, oddsType)
            }
            is MoreViewHolder -> {
                val item = data[position] as OutrightShowMoreItem
                holder.bind(item, outrightOddListener)
            }
        }
    }

    override fun getItemCount(): Int = if (data.isEmpty()) {
        2
    } else {
        data.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (isPreload) {
            return BaseItemType.PRELOAD_ITEM.type
        }

        return when {
            data.isEmpty() -> BaseItemType.NO_DATA.type
            else -> {
                when (val item = data[position]) {
                    is MatchOdd -> OutrightViewType.TITLE.ordinal
                    is OutrightSubTitleItem -> {
                        /**
                         * item是否需要顯示, (聯賽收合)
                         */
                        val itemVisibility = item.leagueExpanded

                        if (itemVisibility) {
                            OutrightViewType.SUB_TITLE.ordinal
                        } else {
                            OutrightViewType.NULL.ordinal
                        }
                    }
                    is Odd -> {
                        /**
                         * item是否需要顯示, (聯賽收合, 玩法收合, 是否顯示更多)
                         */
                        val show = item.leagueExpanded && item.playCateExpand && item.isExpand

                        if (show) {
                            OutrightViewType.ODD.ordinal
                        } else {
                            OutrightViewType.NULL.ordinal
                        }
                    }
                    is OutrightShowMoreItem -> {
                        /**
                         * item是否需要顯示, (聯賽收合, 玩法收合)
                         */
                        val itemVisibility = item.leagueExpanded && item.playCateExpand

                        if (itemVisibility) {
                            OutrightViewType.MORE.ordinal
                        } else {
                            OutrightViewType.NULL.ordinal
                        }
                    }
                    else -> OutrightViewType.NULL.ordinal
                }
            }
        }
    }

    class OutrightTitleViewHolder private constructor(private val viewBinding: ItemviewOutrightTitleV4Binding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        private val firstItemTopMargin = 10.dp //第一個Title與球類標題的間隔
        private val endDivider = 8.dp //若該聯賽隱藏與下一個聯賽的間隔

        @SuppressLint("SetTextI18n")
        fun bind(
            matchOdd: MatchOdd?,
            outrightOddListener: OutrightOddListener?
        ) {
            val lp = itemView.layoutParams as RecyclerView.LayoutParams

            //第一個Title與球類標題的間隔
            val topMargin = if (bindingAdapterPosition == 0) {
                firstItemTopMargin
            } else {
                0
            }

            //若該聯賽隱藏與下一個聯賽的間隔
            val bottomMargin = if (matchOdd?.isExpanded == true) {
                0
            } else {
                endDivider
            }

            lp.setMargins(0, topMargin, 0, bottomMargin)

            viewBinding.root.setOnClickListener {
                outrightOddListener?.onClickMatch(matchOdd)
            }

            viewBinding.outrightLeagueName.text = matchOdd?.matchInfo?.name
            viewBinding.outrightLeagueDate.text = "${matchOdd?.startDate ?: ""} ${matchOdd?.startTime ?: ""}"

        }

        companion object {
            fun from(parent: ViewGroup): OutrightTitleViewHolder {

                val layoutInflater = LayoutInflater.from(parent.context)

                return OutrightTitleViewHolder(ItemviewOutrightTitleV4Binding.inflate(layoutInflater, parent, false))
            }
        }
    }

    class SubTitleViewHolder private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(matchOdd: MatchOdd?, item: OutrightSubTitleItem, outrightOddListener: OutrightOddListener?) {
            itemView.outright_odd_subtitle.text = item.subTitle

            itemView.outright_odd_subtitle.setOnClickListener {
                outrightOddListener?.onClickExpand(matchOdd, item.playCateCode)
            }
        }

        companion object {
            fun from(parent: ViewGroup): SubTitleViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_outright_odd_subtitle_v4, parent, false)

                return SubTitleViewHolder(view)
            }
        }
    }

    class OddViewHolder private constructor(
        itemView: View,
        private val refreshListener: OddStateChangeListener
    ) : OddStateViewHolder(itemView) {

        fun bind(
            matchOdd: MatchOdd?,
            item: Odd,
            outrightOddListener: OutrightOddListener?,
            oddsType: OddsType
        ) {
            itemView.outright_odd_btn.apply {
                setupOdd(item, oddsType)
                tv_spread.text = ""
                this@OddViewHolder.setupOddState(this, item)
                setOnClickListener {
                    outrightOddListener?.onClickBet(matchOdd, item, item.outrightCateKey ?: "")
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup, refreshListener: OddStateChangeListener): OddViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_outright_odd_v4, parent, false)

                return OddViewHolder(view, refreshListener)
            }
        }

        override val oddStateChangeListener: OddStateChangeListener
            get() = refreshListener
    }

    class MoreViewHolder(private val viewBinding: ItemviewOutrightOddMoreV4Binding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(itemData: OutrightShowMoreItem, outrightOddListener: OutrightOddListener?) {
            viewBinding.root.setOnClickListener {
                outrightOddListener?.onClickMore(itemData.playCateCode, itemData.matchOdd)
                itemData.isExpanded = !(itemData.isExpanded)
                setupMoreIcon(itemData)
            }
            setupMoreIcon(itemData)
        }

        private fun setupMoreIcon(itemData: OutrightShowMoreItem) {
            when (itemData.isExpanded) {
                false -> {
                    viewBinding.tvMore.text = itemView.context.getString(R.string.display_more)
                    viewBinding.ivMoreIcon.animate().rotation(0f).setDuration(100).start()
                }
                true -> {
                    viewBinding.tvMore.text = itemView.context.getString(R.string.odds_detail_less)
                    viewBinding.ivMoreIcon.animate().rotation(180f).setDuration(100).start()
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): MoreViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                return MoreViewHolder(ItemviewOutrightOddMoreV4Binding.inflate(layoutInflater, parent, false))
            }
        }
    }

    class NullViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(parent: ViewGroup) =
                NullViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.itemview_null, parent, false)
                )
        }
    }

}