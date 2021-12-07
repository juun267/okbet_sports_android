package org.cxct.sportlottery.ui.game.menu

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_left_menu_item.view.*
import kotlinx.android.synthetic.main.content_left_menu_item_footer.view.*
import kotlinx.android.synthetic.main.content_left_menu_item_header.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MyFavoriteNotifyType

class LeftMenuItemNewAdapter(
    private val itemSelectedListener: ItemSelectedListener,
    private val sportClickListener: SportClickListener,
    private val inPlayClickListener: InPlayClickListener,
    private val premiumOddsClickListener: PremiumOddsClickListener,
    private val gameRuleClickListener: GameRuleClickListener,
    private val oddTypeClickListener: OddTypeClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType {
        HEADER, ITEM, FOOTER
    }

    var dataList: List<MenuItemData> = listOf()
    var selectedNumber = 0
    var isLogin = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun addFooterAndSubmitList(newDataList: MutableList<MenuItemData>) {
        newDataList.add(0, MenuItemData(0, "", "", 1).apply {
            isHeaderOrFooter = true
        }) //add header

        newDataList.add(MenuItemData(0, "", "", 0).apply {
            isHeaderOrFooter = true
        }) //add footer

        selectedNumber = newDataList.count {
            it.isSelected == 1
        }

        this.dataList = newDataList
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> ItemType.HEADER.ordinal
            dataList.size - 1 -> ItemType.FOOTER.ordinal
            else -> ItemType.ITEM.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.HEADER.ordinal -> HeaderViewHolder.from(parent)
            ItemType.FOOTER.ordinal -> FooterViewHolder.from(parent)
            else -> ItemViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                holder.bind(inPlayClickListener, premiumOddsClickListener)
            }

            is FooterViewHolder -> {
                holder.bind(gameRuleClickListener, oddTypeClickListener)
            }

            is ItemViewHolder -> {
                val item = dataList[position]

                holder.itemView.apply {
                    img_price.setImageResource(item.imgId)
                    txv_price.text = item.title

                    divider.isVisible = position == selectedNumber - 1

                    cl_content.setOnClickListener {
                        sportClickListener.onClickSport(item.gameType)
                    }

                    when (item.isSelected) {
                        0 -> {
                            btn_select.setImageResource(R.drawable.ic_pin_v4)
                            item.isSelected = 1

                            btn_select.setOnClickListener {
                                itemSelectedListener.onSelect(
                                    item.gameType,
                                    MyFavoriteNotifyType.SPORT_ADD.code
                                )
                                if (isLogin) notifyItemChanged(position)
                            }

                        }
                        1 -> {
                            btn_select.setImageResource(R.drawable.ic_pin_selected_v4)
                            item.isSelected = 0

                            btn_select.setOnClickListener {
                                itemSelectedListener.onSelect(
                                    item.gameType,
                                    MyFavoriteNotifyType.SPORT_REMOVE.code
                                )
                                notifyItemChanged(position)
                            }

                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        companion object {
            fun from(parent: ViewGroup): ItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.content_left_menu_item, parent, false)
                return ItemViewHolder(view)
            }
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.content_left_menu_item_header, parent, false)
                return HeaderViewHolder(view)
            }
        }

        fun bind(
            inPlayClickListener: InPlayClickListener,
            premiumOddsClickListener: PremiumOddsClickListener
        ) {
            itemView.apply {
                ct_inplay.setOnClickListener {
                    inPlayClickListener.onClick()
                }
                ct_premium_odds.setOnClickListener {
                    premiumOddsClickListener.onClick()
                }
            }
        }
    }


    class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        companion object {
            fun from(parent: ViewGroup): FooterViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.content_left_menu_item_footer, parent, false)
                return FooterViewHolder(view)
            }
        }

        fun bind(
            gameRuleClickListener: GameRuleClickListener,
            oddTypeClickListener: OddTypeClickListener
        ) {
            itemView.apply {
                //遊戲規則
                ct_game_rule.setOnClickListener {
                    gameRuleClickListener.onClick()
                }
                //盤口設定
                tv_odds_type.setOnClickListener {
                    oddTypeClickListener.onClick()
                }
            }
        }
    }

    class ItemSelectedListener(private val itemSelectedListener: (string: String, type: Int) -> Unit) {
        fun onSelect(string: String, type: Int) = itemSelectedListener(string, type)
    }

    class SportClickListener(val clickSportListener: (gameType: String) -> Unit) {
        fun onClickSport(gameType: String) = clickSportListener(gameType)
    }

    class PremiumOddsClickListener(private val clickListener: () -> Unit) {
        fun onClick() = clickListener()
    }

    class InPlayClickListener(private val clickListener: () -> Unit) {
        fun onClick() = clickListener()
    }

    class GameRuleClickListener(private val clickListener: () -> Unit) {
        fun onClick() = clickListener()
    }

    class OddTypeClickListener(private val clickListener: () -> Unit) {
        fun onClick() = clickListener()
    }

}
