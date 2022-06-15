package org.cxct.sportlottery.ui.game.home

import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.ScaleXSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.home_menu_block_v2.view.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.sport.SportMenu
import org.cxct.sportlottery.network.sport.coupon.SportCouponMenuData
import timber.log.Timber

class GameMenuV2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var mOnClickMenuListener: OnClickMenuListener? = null

    fun setListener(onClickMenuListener: OnClickMenuListener?) {
        mOnClickMenuListener = onClickMenuListener
    }

    fun bind(data: HomeListAdapter.MenuItemData) {
        //TODO 即將開賽、第三方... 尚未確定先隱藏
        with(itemView) {
            card_game_soon.visibility = View.GONE
            card_lottery.visibility = View.GONE
            card_live.visibility = View.GONE
            card_poker.visibility = View.GONE
            card_slot.visibility = View.GONE
            card_fishing.visibility = View.GONE
            card_game_result.visibility = View.GONE
            card_update.visibility = View.GONE
        }
//        setupView(data)
//        updateThirdGameCard(data)
        setupViewClick()
        setupSportMenu(data.sportMenuList)
//        setupComingSoonCard()
//        setupSportCouponMenu(data.sportCouponMenuList)
    }

    private fun setupView(data: HomeListAdapter.MenuItemData) {
        itemView.card_game_soon.setCount(data.atStartCount)
    }

    private fun setupViewClick() {
        with(itemView) {
            card_game_soon.setOnClickListener {
                mOnClickMenuListener?.onGameSoon()
            }

            card_lottery.setOnClickListener {
                mOnClickMenuListener?.onLottery()
            }

            card_live.setOnClickListener {
                mOnClickMenuListener?.onLive()
            }

            card_poker.setOnClickListener {
                mOnClickMenuListener?.onPoker()
            }

            card_slot.setOnClickListener {
                mOnClickMenuListener?.onSlot()
            }

            card_fishing.setOnClickListener {
                mOnClickMenuListener?.onFishing()
            }

            card_game_result.setOnClickListener {
                mOnClickMenuListener?.onGameResult()
            }

            card_update.setOnClickListener {
                mOnClickMenuListener?.onUpdate()
            }
        }
    }

    private fun setupSportMenu(sportMenuList: List<SportMenu>) {
        with(itemView) {

            val filteredSportMenuList = sportMenuList.filter {
                it.gameCount > 0 || it.gameType.key == "BB_COMING_SOON" ||
                        it.gameType.key == "ES_COMING_SOON"
            }

            if (block_game.size != filteredSportMenuList.size) {
                block_game.removeAllViews()

                filteredSportMenuList.forEachIndexed { index, sportMenu ->
                    when (index) {
                        else -> {
                            if (sportMenu.gameCount > 0 || sportMenu.gameType.key == "BB_COMING_SOON" ||
                                sportMenu.gameType.key == "ES_COMING_SOON"
                            ) {
                                block_game.addView(
                                    HomeGameCardV2(
                                        itemView.context
                                    ).apply {
                                        setupHomeCard(this, sportMenu)
                                    })
                            }
                        }
                    }
                }
            } else {
                filteredSportMenuList.forEachIndexed { index, sportMenu ->
                    when (index) {
                        else -> {
                            if (sportMenu.gameCount > 0 || sportMenu.gameType.key == "BB_COMING_SOON" ||
                                sportMenu.gameType.key == "ES_COMING_SOON"
                            ) {
                                val homeGameCardV2 = (block_game.getChildAt(index) as HomeGameCardV2)
                                setupHomeCard(
                                    homeGameCardV2,
                                    sportMenu
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupSportCouponMenu(sportCouponMenuList: List<SportCouponMenuData>) {
        with(itemView) {
            if (special_block_game.size != sportCouponMenuList.size) {
                special_block_game.removeAllViews()
                sportCouponMenuList.forEach { sportCouponMenuData ->
                    special_block_game.addView(HomeGameCardV2(itemView.context).apply {
                        setupCouponCard(this, sportCouponMenuData)
                    })
                }
            } else {
                sportCouponMenuList.forEachIndexed { index, sportCouponMenuData ->
                    setupCouponCard((special_block_game.getChildAt(index) as HomeGameCardV2), sportCouponMenuData)
                }
            }
        }
    }

    private fun setupHomeCard(homeGameCardV2: HomeGameCardV2, sportMenu: SportMenu) {
        homeGameCardV2.apply {
            val title = GameType.getGameTypeString(context, sportMenu.gameType.key)
            val iconGameType = GameType.getGameTypeMenuIcon(sportMenu.gameType)
            setTitle(if (title.isEmpty()) sportMenu.sportName else title)
            sportMenu.icon?.let { setIcon(if (iconGameType == 0) sportMenu.icon else iconGameType) }
            setCount(sportMenu.gameCount)

            setOnClickListener {
                if (sportMenu.gameType.key != "BB_COMING_SOON" && sportMenu.gameType.key != "ES_COMING_SOON")
                    mOnClickMenuListener?.onHomeCard(sportMenu)
            }
        }
    }

    private fun setupCouponCard(couponCard: HomeGameCardV2, sportCouponMenuData: SportCouponMenuData) {
        with(couponCard) {
            setTitle(sportCouponMenuData.couponName)
            setIcon(R.drawable.ic_game_champ)
            setOnClickListener {
                mOnClickMenuListener?.onCouponCard(sportCouponMenuData)
            }
        }
    }

    private fun updateThirdGameCard(data: HomeListAdapter.MenuItemData) {
        with(itemView) {
            card_lottery.visibility = if (data.lotteryVisible) View.VISIBLE else View.GONE
            card_live.visibility = if (data.liveVisible) View.VISIBLE else View.GONE
            card_poker.visibility = if (data.pokerVisible) View.VISIBLE else View.GONE
            card_slot.visibility = if (data.slotVisible) View.VISIBLE else View.GONE
            card_fishing.visibility = if (data.fishingVisible) View.VISIBLE else View.GONE
        }
    }

//    private fun setupComingSoonCard() {
//        itemView.card_baseball_coming_soon.setComingSoonGameCard(itemView.context.getString(R.string.baseball))
//        itemView.card_esports_coming_soon.setComingSoonGameCard(itemView.context.getString(R.string.esports))
//        //最後一項不需要divider
//        itemView.card_esports_coming_soon.setDividerVisibility(false)
//    }

//    private fun HomeGameCardV2.setComingSoonGameCard(titleString: String) {
//        val spannableStringBuilder = SpannableStringBuilder()
//        val titleSpannableString = SpannableString(titleString)
//        titleSpannableString.setSpan(
//            ForegroundColorSpan(
//                ContextCompat.getColor(
//                    itemView.context,
//                    R.color.color_BBBBBB_333333
//                ),
//            ), 0, titleString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//        )
//        val comingSoonString = itemView.context.getString(R.string.coming_soon)
//        val comingSoonSpannableString = SpannableString(comingSoonString)
//        comingSoonSpannableString.setSpan(
//            ForegroundColorSpan(
//                ContextCompat.getColor(
//                    itemView.context,
//                    R.color.color_b73a20
//                ),
//            ), 0, comingSoonString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
//        )
//        spannableStringBuilder.append(titleSpannableString).append(" ").append(comingSoonSpannableString)
//        this.setTitle(spannableStringBuilder)
//    }
}