package org.cxct.sportlottery.ui.favorite

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.ViewModel
import kotlinx.android.synthetic.main.activity_my_favorite.*
import kotlinx.android.synthetic.main.fragment_game_v3.*
import kotlinx.android.synthetic.main.view_bottom_navigation_sport.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.bet.list.BetInfoCarDialog
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.betList.BetListFragment
import org.cxct.sportlottery.ui.game.betList.receipt.BetReceiptFragment
import org.cxct.sportlottery.ui.main.accountHistory.AccountHistoryActivity
import org.cxct.sportlottery.ui.transactionStatus.TransactionStatusActivity

class MyFavoriteActivity : BaseSocketActivity<MyFavoriteViewModel>(MyFavoriteViewModel::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_my_favorite)

        setupBottomNavigation()

        initObserver()

        initNavigationView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setupBottomNavigation() {
        sport_bottom_navigation.setNavigationItemClickListener {
            when (it) {
                R.id.navigation_sport -> {
                    finish()
                    startActivity(Intent(this@MyFavoriteActivity, GameActivity::class.java))
                    false
                }
                R.id.navigation_game -> {
                    true
                }
                R.id.item_bet_list -> {
                    showBetListPage()
                    false
                }
                R.id.navigation_account_history -> {
                    startActivity(
                        Intent(
                            this@MyFavoriteActivity,
                            AccountHistoryActivity::class.java
                        )
                    )
                    false
                }
                R.id.navigation_transaction_status -> {
                    startActivity(
                        Intent(
                            this@MyFavoriteActivity,
                            TransactionStatusActivity::class.java
                        )
                    )
                    false
                }
                else -> false
            }
        }
    }

    private fun showBetListPage() {
        val betListFragment = BetListFragment.newInstance(object : BetListFragment.BetResultListener {
            override fun onBetResult(betResultData: Receipt?, betParlayList: List<ParlayOdd>) {
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.push_right_to_left_enter, R.anim.pop_bottom_to_top_exit, R.anim.push_right_to_left_enter, R.anim.pop_bottom_to_top_exit)
                    .replace(R.id.fl_bet_list, BetReceiptFragment.newInstance(betResultData, betParlayList))
                    .addToBackStack(BetReceiptFragment::class.java.simpleName)
                    .commit()
            }

        })
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.push_bottom_to_top_enter,
                R.anim.pop_bottom_to_top_exit,
                R.anim.push_bottom_to_top_enter,
                R.anim.pop_bottom_to_top_exit
            )
            .add(R.id.fl_bet_list, betListFragment)
            .addToBackStack(BetListFragment::class.java.simpleName)
            .commit()
    }

    private fun initObserver() {
        viewModel.showBetInfoSingle.observe(this, {
            it?.getContentIfNotHandled()?.let {
                BetInfoCarDialog().show(
                    supportFragmentManager,
                    BetInfoCarDialog::class.java.simpleName
                )
            }
        })

        viewModel.betInfoRepository.betInfoList.observe(this, {
            sport_bottom_navigation.setBetCount(it.peekContent().size)
        })

        viewModel.notifyLogin.observe(this, {
            snackBarLoginNotify.apply {
                setAnchorView(R.id.game_bottom_navigation)
                show()
            }
        })
    }

    private fun initNavigationView() {
        sport_bottom_navigation.setSelected(R.id.navigation_game)
    }
}