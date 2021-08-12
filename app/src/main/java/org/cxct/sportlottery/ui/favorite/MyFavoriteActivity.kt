package org.cxct.sportlottery.ui.favorite

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_my_favorite.*
import kotlinx.android.synthetic.main.fragment_game_v3.*
import kotlinx.android.synthetic.main.view_bottom_navigation_sport.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFavoriteActivity
import org.cxct.sportlottery.ui.bet.list.BetInfoCarDialog
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.main.accountHistory.AccountHistoryActivity
import org.cxct.sportlottery.ui.transactionStatus.TransactionStatusActivity

class MyFavoriteActivity : BaseFavoriteActivity<MyFavoriteViewModel>(MyFavoriteViewModel::class) {

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
                    showBetListDialog()
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
    }

    private fun initNavigationView() {
        sport_bottom_navigation.setSelected(R.id.navigation_game)
    }
}