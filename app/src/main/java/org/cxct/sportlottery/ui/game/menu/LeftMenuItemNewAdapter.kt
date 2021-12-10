package org.cxct.sportlottery.ui.game.menu

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
    private val isShowMemberLevel: Boolean,
    private val headerSelectedListener: HeaderSelectedListener,
    private val itemSelectedListener: ItemSelectedListener,
    private val footerSelectedListener: FooterSelectedListener
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
                holder.bind(isShowMemberLevel, isLogin, headerSelectedListener)
            }

            is FooterViewHolder -> {
                holder.bind(isLogin, footerSelectedListener)
            }

            is ItemViewHolder -> {
                val item = dataList[position]

                holder.itemView.apply {
                    img_price.setImageResource(item.imgId)
                    txv_price.text = item.title

                    divider.isVisible = position == selectedNumber - 1

                    cl_content.setOnClickListener {
                        itemSelectedListener.onSportClick(item.gameType)
                    }

                    when (item.isSelected) {
                        0 -> {
                            btn_select.setImageResource(R.drawable.ic_pin_v4)
                            item.isSelected = 1

                            btn_select.setOnClickListener {
                                itemSelectedListener.onSportSelect(
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
                                itemSelectedListener.onSportSelect(
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
            isShowMemberLevel: Boolean,
            isLogin: Boolean,
            headerSelectedListener: HeaderSelectedListener
        ) {
            itemView.apply {
                tv_recharge.isVisible = isLogin
                tv_withdraw.isVisible = isLogin
                tv_member_level.isVisible = isLogin && isShowMemberLevel
                tv_promotion.isVisible = isLogin
                divider_login.isVisible = isLogin

                tv_recharge.setOnClickListener {
                    headerSelectedListener.rechargeSelected()
                }
                tv_withdraw.setOnClickListener {
                    headerSelectedListener.withdrawSelected()
                }
                tv_member_level.setOnClickListener {
                    headerSelectedListener.memberLevelSelected()
                }
                tv_promotion.setOnClickListener {
                    headerSelectedListener.promotionSelected()
                }

                ct_inplay.setOnClickListener {
                    headerSelectedListener.inPlaySelected()
                }
                ct_premium_odds.setOnClickListener {
                    headerSelectedListener.premiumOddsSelected()
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
            isLogin: Boolean,
            footerSelectedListener: FooterSelectedListener
        ) {
            itemView.apply {

                tv_appearance.isVisible = isLogin

                //盤口設定
                tv_odds_type.setOnClickListener {
                    footerSelectedListener.oddTypeSelected()
                }

                tv_appearance.setOnClickListener {
                    footerSelectedListener.appearanceSelected()
                }

                //遊戲規則
                ct_game_rule.setOnClickListener {
                    footerSelectedListener.gameRuleSelected()
                }
            }
        }
    }

    class HeaderSelectedListener(
        private val rechargeSelectedListener: () -> Unit,
        private val withdrawSelectedListener: () -> Unit,
        private val memberLevelSelectedListener: () -> Unit,
        private val promotionSelectedListener: () -> Unit,
        private val inPlaySelectedListener: () -> Unit,
        private val premiumOddsSelectedListener: () -> Unit,
    ) {
        fun rechargeSelected() = rechargeSelectedListener()
        fun withdrawSelected() = withdrawSelectedListener()
        fun memberLevelSelected() = memberLevelSelectedListener()
        fun promotionSelected() = promotionSelectedListener()
        fun inPlaySelected() = inPlaySelectedListener()
        fun premiumOddsSelected() = premiumOddsSelectedListener()
    }

    class ItemSelectedListener(
        private val sportClickListener: (gameType: String) -> Unit,
        private val sportSelectedListener: (string: String, type: Int) -> Unit
    ) {
        fun onSportClick(gameType: String) = sportClickListener(gameType)
        fun onSportSelect(string: String, type: Int) = sportSelectedListener(string, type)
    }

    class FooterSelectedListener(
        private val oddTypeSelectedListener: () -> Unit,
        private val appearanceSelectedListener: () -> Unit,
        private val gameRuleSelectedListener: () -> Unit
    ) {
        fun oddTypeSelected() = oddTypeSelectedListener()
        fun appearanceSelected() = appearanceSelectedListener()
        fun gameRuleSelected() = gameRuleSelectedListener()
    }
}
