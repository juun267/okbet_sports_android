package org.cxct.sportlottery.ui.game.menu

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder
import kotlinx.android.synthetic.main.content_left_menu_item.view.*
import kotlinx.android.synthetic.main.content_left_menu_item_footer.view.*
import kotlinx.android.synthetic.main.content_left_menu_item_header.view.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MyFavoriteNotifyType
import org.cxct.sportlottery.util.SvgUtil
import org.cxct.sportlottery.util.listener.OnClickListener

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
    var specialList: List<MenuItemData> = listOf()
    var listener: OnClickListener? = null
    var selectedNumber = 0
    var isLogin = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun addFooterAndSubmitList(newDataList: MutableList<MenuItemData>) {
        newDataList.add(0, MenuItemData(0, "", "", 1, 0, false).apply {
            isHeaderOrFooter = true
        }) //add header

        newDataList.add(MenuItemData(0, "", "", 0, 0, false).apply {
            isHeaderOrFooter = true
        }) //add footer

        selectedNumber = newDataList.count {
            it.isSelected == 1
        }

        this.dataList = newDataList
        notifyDataSetChanged()
    }

    fun addSpecialEvent(newDataList: MutableList<MenuItemData>, listener: OnClickListener) {
        this.specialList = newDataList
        this.listener = listener
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
                holder.bind(
                    isShowMemberLevel,
                    isLogin,
                    headerSelectedListener,
                    specialList,
                    listener
                ) // UninitializedPropertyAccessException: lateinit property listener has not been initialized
            }

            is FooterViewHolder -> {
                holder.bind(isLogin, footerSelectedListener)
            }

            is ItemViewHolder -> {
                val item = dataList[position]

                holder.itemView.apply {
                    img_price.setImageResource(item.imgId)
                    txv_price.text = item.title

                    tv_count.text = item.gameCount.toString()

                    divider.isVisible = position == selectedNumber - 1

                    if (item.isCurrentSportType) {
                        txv_price.setTypeface(txv_price.typeface, Typeface.BOLD)
                        tv_count.setTypeface(tv_count.typeface, Typeface.BOLD)
                        txv_price.isSelected = true
                        tv_count.isSelected = true
                    } else {
                        val typeface: Typeface? =
                            ResourcesCompat.getFont(MultiLanguagesApplication.appContext, R.font.helvetica_normal)
                        txv_price.setTypeface(typeface, Typeface.NORMAL)
                        tv_count.setTypeface(typeface, Typeface.NORMAL)
                        txv_price.isSelected = false
                        tv_count.isSelected = false
                    }

                    cl_content.setOnClickListener {
                        itemSelectedListener.onSportClick(item.gameType)
                    }

                    when (item.isSelected) {
                        0 -> {
                            btn_select.setImageResource(R.drawable.ic_pin_v4)

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
        lateinit var adapter: CommonAdapter<MenuItemData>

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
            headerSelectedListener: HeaderSelectedListener,
            specialList: List<MenuItemData>,
            listener: OnClickListener?
        ) {
            itemView.apply {
                tv_recharge.isVisible = isLogin
                tv_withdraw.isVisible = isLogin
                tv_member_level.isVisible = isLogin && isShowMemberLevel

                block_home.setOnClickListener {
                    headerSelectedListener.backMainPageSelected()
                }
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
                rv_left_special.layoutManager =
                    LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                rv_left_special.isNestedScrollingEnabled = false
                if (specialList.isNotEmpty()) {
                    adapter = object : CommonAdapter<MenuItemData>(
                        context,
                        R.layout.item_left_special_item,
                        specialList
                    ) {

                        override fun convert(
                            holder: ViewHolder,
                            t: MenuItemData,
                            position: Int
                        ) {
                            holder.setText(R.id.tvSpecialEvent, t.title)
                            try {//後端有機會給錯格式導致無法解析
                                val countryIcon = SvgUtil.getSvgDrawable(itemView.context, t.couponIcon)
                                holder.setImageDrawable(R.id.img_ic, countryIcon)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                    }
                    rv_left_special.adapter = adapter

                    adapter.setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {
                        override fun onItemClick(
                            view: View,
                            holder: RecyclerView.ViewHolder,
                            position: Int
                        ) {
                            listener?.onItemClick(position)
                        }

                        override fun onItemLongClick(
                            view: View,
                            holder: RecyclerView.ViewHolder,
                            position: Int
                        ): Boolean {
                            return false
                        }
                    })
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

                //TODO for test
                tv_appearance.visibility = if (BuildConfig.DEBUG) View.VISIBLE else View.GONE

//                tv_appearance.isVisible = isLogin
                // tv_appearance.isVisible = false //暫時隱藏
                if (MultiLanguagesApplication.isNightMode) {
                    tv_appearance.text = context.getString(R.string.appearance) + ": " + context.getString(R.string.night_mode)
                } else {
                    tv_appearance.text = context.getString(R.string.appearance) + ": " + context.getString(R.string.day_mode)
                }
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

                ct_news.setOnClickListener {
                    footerSelectedListener.newsSelected()
                }
            }
        }
    }

    class HeaderSelectedListener(
        private val backMainPageSelectedListener: () -> Unit,
        private val rechargeSelectedListener: () -> Unit,
        private val withdrawSelectedListener: () -> Unit,
        private val memberLevelSelectedListener: () -> Unit,
        private val promotionSelectedListener: () -> Unit,
        private val inPlaySelectedListener: () -> Unit,
        private val premiumOddsSelectedListener: () -> Unit,
    ) {
        fun backMainPageSelected() = backMainPageSelectedListener()
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
        private val gameRuleSelectedListener: () -> Unit,
        private val newsSelectedListener: () -> Unit
    ) {
        fun oddTypeSelected() = oddTypeSelectedListener()
        fun appearanceSelected() = appearanceSelectedListener()
        fun gameRuleSelected() = gameRuleSelectedListener()
        fun newsSelected() = newsSelectedListener()
    }
}
