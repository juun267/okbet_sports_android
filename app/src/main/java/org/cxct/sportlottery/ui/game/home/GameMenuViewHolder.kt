package org.cxct.sportlottery.ui.game.home

import android.view.View
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.home_menu_block.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.sport.SportMenu
import org.cxct.sportlottery.network.sport.coupon.SportCouponMenuData

class GameMenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var mOnClickMenuListener: OnClickMenuListener? = null

    fun setListener(onClickMenuListener: OnClickMenuListener?) {
        mOnClickMenuListener = onClickMenuListener
    }

    fun bind(data: HomeListAdapter.MenuItemData) {
        setupView(data)
        updateThirdGameCard(data)
        setupViewClick()
        setupSportMenu(data.sportMenuList)
        setupSportCouponMenu(data.sportCouponMenuList)
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
            if (block_game.size != sportMenuList.size) {
                block_game.removeAllViews()

                sportMenuList.forEachIndexed { index, sportMenu ->
                    when (index) {
                        0 -> setupFirstGame(sportMenu)
                        1 -> setupSecondGame(sportMenu)
                        else -> {
                            if (sportMenu.gameCount > 0) {
                                block_game.addView(
                                    HomeGameCard(
                                        itemView.context
                                    ).apply {
                                        setupHomeCard(this, sportMenu)
                                    })
                            }
                        }
                    }
                }
            } else {
                sportMenuList.forEachIndexed { index, sportMenu ->
                    when (index) {
                        0 -> setupFirstGame(sportMenu)
                        1 -> setupSecondGame(sportMenu)
                        else -> {
                            if (sportMenu.gameCount > 0) {
                                setupHomeCard(
                                    (block_game.getChildAt(index) as HomeGameCard),
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
                    special_block_game.addView(HomeGameCard(itemView.context).apply {
                        setupCouponCard(this, sportCouponMenuData)
                    })
                }
            } else {
                sportCouponMenuList.forEachIndexed { index, sportCouponMenuData ->
                    setupCouponCard((special_block_game.getChildAt(index) as HomeGameCard), sportCouponMenuData)
                }
            }
        }
    }

    private fun setupFirstGame(sportMenu: SportMenu) {
        with(itemView) {
            label_en_first_game.text = context?.getString(R.string.goal_buster)
            label_first_game.text = sportMenu.sportName
            sportMenu.icon?.let { iv_first_game.setImageResource(sportMenu.icon) }
            tv_first_game_count.text = sportMenu.gameCount.toString()

            card_first_game.setOnClickListener {
                mOnClickMenuListener?.onFirstGame(sportMenu)

            }
        }
    }

    private fun setupSecondGame(sportMenu: SportMenu) {
        with(itemView) {
            label_en_second_game.text = context?.getString(R.string.top_games)
            label_second_game.text = sportMenu.sportName
            sportMenu.icon?.let { iv_second_game.setImageResource(sportMenu.icon) }
            tv_second_game_count.text = sportMenu.gameCount.toString()

            card_second_game.setOnClickListener {
                mOnClickMenuListener?.onSecondGame(sportMenu)
            }
        }
    }

    private fun setupHomeCard(homeGameCard: HomeGameCard, sportMenu: SportMenu) {
        homeGameCard.apply {
            val title = GameType.getGameTypeString(context, sportMenu.gameType.key)
            setTitle(if (title.isEmpty()) sportMenu.sportName else title)
            sportMenu.icon?.let { setIcon(sportMenu.icon) }
            setCount(sportMenu.gameCount)

            setOnClickListener {
                mOnClickMenuListener?.onHomeCard(sportMenu)
            }
        }
    }

    private fun setupCouponCard(couponCard: HomeGameCard, sportCouponMenuData: SportCouponMenuData) {
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
}