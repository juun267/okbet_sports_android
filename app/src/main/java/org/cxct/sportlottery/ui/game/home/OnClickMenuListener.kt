package org.cxct.sportlottery.ui.game.home

import org.cxct.sportlottery.network.sport.SportMenu
import org.cxct.sportlottery.network.sport.coupon.SportCouponMenuData

class OnClickMenuListener(
    private var onGameSoon: () -> Unit,
    private var onLottery: () -> Unit,
    private var onLive: () -> Unit,
    private var onPoker: () -> Unit,
    private var onSlot: () -> Unit,
    private var onFishing: () -> Unit,
    private var onGameResult: () -> Unit,
    private var onUpdate: () -> Unit,
    private var onFirstGame: (sportMenu: SportMenu) -> Unit,
    private var onSecondGame: (sportMenu: SportMenu) -> Unit,
    private var onHomeCard: (sportMenu: SportMenu) -> Unit,
    private var onCouponCard: (sportCouponMenu: SportCouponMenuData) -> Unit
) {
    fun onGameSoon() = onGameSoon.invoke()
    fun onLottery() = onLottery.invoke()
    fun onLive() = onLive.invoke()
    fun onPoker() = onPoker.invoke()
    fun onSlot() = onSlot.invoke()
    fun onFishing() = onFishing.invoke()
    fun onGameResult() = onGameResult.invoke()
    fun onUpdate() = onUpdate.invoke()
    fun onFirstGame(sportMenu: SportMenu) = onFirstGame.invoke(sportMenu)
    fun onSecondGame(sportMenu: SportMenu) = onSecondGame.invoke(sportMenu)
    fun onHomeCard(sportMenu: SportMenu) = onHomeCard.invoke(sportMenu)
    fun onCouponCard(sportCouponMenu: SportCouponMenuData) = onCouponCard.invoke(sportCouponMenu)
}