package org.cxct.sportlottery.ui.base

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.MyFavoriteNotifyType
import org.cxct.sportlottery.ui.bet.list.receipt.BetInfoCarReceiptDialog
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.betList.receipt.BetReceiptFragment
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.menu.OddsType
import kotlin.reflect.KClass


abstract class BaseBottomNavActivity<T : BaseBottomNavViewModel>(clazz: KClass<T>) :
    BaseSocketActivity<T>(clazz) {

    abstract fun initToolBar()

    abstract fun initMenu()

    abstract fun clickMenuEvent()

    abstract fun initBottomNavigation()

    abstract fun showBetListPage()

    abstract fun getBetListPageVisible(): Boolean

    abstract fun updateUiWithLogin(isLogin: Boolean)

    abstract fun updateOddsType(oddsType: OddsType)

    abstract fun updateBetListCount(num: Int)

    abstract fun showLoginNotify()

    abstract fun showMyFavoriteNotify(myFavoriteNotifyType: Int)

    abstract fun navOneSportPage(thirdGameCategory: ThirdGameCategory?)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.isLogin.observe(this) {
            updateUiWithLogin(it)
        }

        viewModel.oddsType.observe(this) {
            updateOddsType(it)
        }

        viewModel.thirdGameCategory.observe(this) {
            navOneSportPage(it.getContentIfNotHandled())
        }

        viewModel.intentClass.observe(this) {
            it.getContentIfNotHandled()?.let { clazz ->
                if (clazz == GameActivity::class.java) {
                    GameActivity.reStart(this)
                } else {
                    startActivity(Intent(this, clazz))
                }
            }
        }

        viewModel.showShoppingCart.observe(this) {
            it.getContentIfNotHandled()?.let {
                showBetListPage()
            }
        }

        viewModel.betInfoList.observe(this) {
            updateBetListCount(it.peekContent().size)
        }

        viewModel.notifyLogin.observe(this) {
            showLoginNotify()
        }

        //TODO 不是通用的不應該放在這邊, 只要在需要顯示的頁面實作就好了
        viewModel.notifyMyFavorite.observe(this) {
            it.getContentIfNotHandled()?.let { result ->
                when (result.type) {
                    FavoriteType.LEAGUE -> {
                        when (result.isFavorite) {
                            true -> showMyFavoriteNotify(MyFavoriteNotifyType.LEAGUE_ADD.ordinal)
                            false -> showMyFavoriteNotify(MyFavoriteNotifyType.LEAGUE_REMOVE.ordinal)
                        }
                    }
                    FavoriteType.MATCH -> {
                        when (result.isFavorite) {
                            true -> showMyFavoriteNotify(MyFavoriteNotifyType.MATCH_ADD.ordinal)
                            false -> showMyFavoriteNotify(MyFavoriteNotifyType.MATCH_REMOVE.ordinal)
                        }
                    }
                    FavoriteType.PLAY_CATE -> {
                        when (result.isFavorite) {
                            true -> showMyFavoriteNotify(MyFavoriteNotifyType.DETAIL_ADD.ordinal)
                            false -> showMyFavoriteNotify(MyFavoriteNotifyType.DETAIL_REMOVE.ordinal)
                        }
                    }
                }
            }
        }

    }

    protected fun showBetReceiptDialog(
        betResultData: Receipt?,
        betParlayList: List<ParlayOdd>,
        isMultiBet: Boolean,
        containerId: Int,
    ) {
        if (isMultiBet) {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.push_right_to_left_enter,
                    R.anim.pop_bottom_to_top_exit,
                    R.anim.push_right_to_left_enter,
                    R.anim.pop_bottom_to_top_exit
                )
                .replace(
                    containerId,
                    BetReceiptFragment.newInstance(betResultData, betParlayList)
                )
                .addToBackStack(BetReceiptFragment::class.java.simpleName)
                .commit()
        } else {
            BetInfoCarReceiptDialog(betResultData).show(
                supportFragmentManager,
                BetInfoCarReceiptDialog::class.java.simpleName
            )
        }
    }

    fun View.slideVisibility(isHide: Boolean, duration: Long = 200) {
        if (isHide) {
            if (translationY == height.toFloat()) return
            translationY = 0f
            animate()
                .translationY(height.toFloat())
                .setDuration(duration).setListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                        translationY = height.toFloat()
                    }
                })
        } else {
            if (translationY == 0f) return
            translationY = height.toFloat()
            animate()
                .translationY(0f)
                .setDuration(duration).setListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                        translationY = 0f
                    }
                })
        }
    }
}