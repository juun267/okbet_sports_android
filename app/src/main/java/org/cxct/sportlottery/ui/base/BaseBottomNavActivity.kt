package org.cxct.sportlottery.ui.base

import android.os.Bundle
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.MyFavoriteNotifyType
import org.cxct.sportlottery.ui.betList.receipt.BetReceiptFragment
import kotlin.reflect.KClass


abstract class BaseBottomNavActivity<T : BaseBottomNavViewModel>(clazz: KClass<T>) :
    BaseSocketActivity<T>(clazz) {

    abstract fun showBetListPage()

    abstract fun getBetListPageVisible(): Boolean

    abstract fun updateBetListCount(num: Int)

    abstract fun showLoginNotify()

    abstract fun showMyFavoriteNotify(myFavoriteNotifyType: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                            true -> {
                                showMyFavoriteNotify(MyFavoriteNotifyType.MATCH_ADD.ordinal)
                            }
                            false -> {
                                showMyFavoriteNotify(MyFavoriteNotifyType.MATCH_REMOVE.ordinal)
                            }
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
//        if (isMultiBet) {
        supportFragmentManager.beginTransaction()
//                .setCustomAnimations(
//                    R.anim.push_right_to_left_enter,
//                    R.anim.pop_bottom_to_top_exit,
//                    R.anim.push_right_to_left_enter,
//                    R.anim.pop_bottom_to_top_exit
//                )
            .replace(
                containerId, BetReceiptFragment.newInstance(betResultData, betParlayList)
            ).addToBackStack(BetReceiptFragment::class.java.simpleName).commit()
//        } else {
//            BetInfoCarReceiptDialog(betResultData).show(
//                supportFragmentManager,
//                BetInfoCarReceiptDialog::class.java.simpleName
//            )
//        }
    }

}