package org.cxct.sportlottery.ui.favorite

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_my_favorite.*
import kotlinx.android.synthetic.main.fragment_game_v3.*
import kotlinx.android.synthetic.main.view_bottom_navigation_sport.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.ui.base.BaseFavoriteActivity
import org.cxct.sportlottery.ui.bet.list.BetInfoCarDialog
import org.cxct.sportlottery.ui.common.StatusSheetAdapter
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.game.common.LeagueAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeListener
import org.cxct.sportlottery.ui.game.hall.adapter.PlayCategoryAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.PlayCategoryListener
import org.cxct.sportlottery.ui.main.accountHistory.AccountHistoryActivity
import org.cxct.sportlottery.ui.transactionStatus.TransactionStatusActivity
import org.cxct.sportlottery.util.SpaceItemDecoration

class MyFavoriteActivity : BaseFavoriteActivity<MyFavoriteViewModel>(MyFavoriteViewModel::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_my_favorite)

        setupBottomNavigation()

        initObserver()
    }

    private fun setupBottomNavigation() {
        sport_bottom_navigation.setNavigationItemClickListener {
            when (it) {
                R.id.navigation_sport -> {
                    //TODO navigate sport home
                    true
                }
                R.id.navigation_game -> {
                    //TODO navigate sport game
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
}