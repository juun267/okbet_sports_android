package org.cxct.sportlottery.ui.favorite

import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.addFragment
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.MyFavoriteNotifyType
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.betList.BetListFragment
import org.cxct.sportlottery.ui.betList.receipt.BetReceiptFragment
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.ui.sport.favorite.FavoriteFragment
import splitties.fragments.addToBackStack
import splitties.views.dsl.core.setContentView
import timber.log.Timber

class FavoriteActivity : BaseSocketActivity<MainViewModel>(MainViewModel::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FavoriteUi(this))
        addFragment(R.id.frameLayout, FavoriteFragment())
        initObserver()
    }

    private fun initObserver() {

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


        viewModel.showBetInfoSingle.observe(this) {
            it.getContentIfNotHandled()?.let {
                showBetListPage()
            }
        }
    }

    private fun showMyFavoriteNotify(myFavoriteNotifyType: Int) {
        setSnackBarMyFavoriteNotify(myFavoriteNotifyType)
        snackBarMyFavoriteNotify?.apply {
            show()
        }
    }


    fun setupBetData(fastBetDataBean: FastBetDataBean) {
        viewModel.updateMatchBetListData(fastBetDataBean)
    }

    fun showBetListPage() {
        Timber.d("showBetListPage: =====")
        val ft = supportFragmentManager.beginTransaction()
        val betListFragment =
            BetListFragment.newInstance(object : BetListFragment.BetResultListener {
                override fun onBetResult(
                    betResultData: Receipt?, betParlayList: List<ParlayOdd>, isMultiBet: Boolean
                ) {
                    showBetReceiptDialog(betResultData, betParlayList, isMultiBet, R.id.fl_bet_list)
                }
            })
        ft.add(R.id.fl_bet_list, betListFragment).addToBackStack().commit()
    }

    fun showBetReceiptDialog(
        betResultData: Receipt?,
        betParlayList: List<ParlayOdd>,
        isMultiBet: Boolean,
        containerId: Int,
    ) {
        supportFragmentManager.beginTransaction().replace(
            containerId, BetReceiptFragment.newInstance(betResultData, betParlayList)
        ).addToBackStack(BetReceiptFragment::class.java.simpleName).commit()
    }
}