package org.cxct.sportlottery.ui.favorite

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_my_favorite.*
import kotlinx.android.synthetic.main.bottom_navigation_item.view.*
import kotlinx.android.synthetic.main.sport_bottom_navigation.*
import kotlinx.android.synthetic.main.view_bottom_navigation_sport.*
import kotlinx.android.synthetic.main.view_nav_right.*
import kotlinx.android.synthetic.main.view_toolbar_main.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.ui.base.BaseBottomNavActivity
import org.cxct.sportlottery.ui.bet.list.BetInfoCarDialog
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.betList.BetListFragment
import org.cxct.sportlottery.ui.game.betList.receipt.BetReceiptFragment
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.menu.ChangeLanguageDialog
import org.cxct.sportlottery.ui.menu.ChangeOddsTypeDialog
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.MetricsUtil

class MyFavoriteActivity : BaseBottomNavActivity<MyFavoriteViewModel>(MyFavoriteViewModel::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_my_favorite)

        initToolBar()

        initBottomNavigation()

        initMenu()

        initObserver()
    }

    override fun initToolBar() {
        iv_logo.setImageResource(R.drawable.ic_logo)
        iv_logo.setOnClickListener {
            viewModel.navMainPage(ThirdGameCategory.MAIN)
        }

        iv_language.setImageResource(LanguageManager.getLanguageFlag(this))

        //頭像 當 側邊欄 開/關
        iv_head.setOnClickListener {
            if (drawer_layout.isDrawerOpen(nav_right)) drawer_layout.closeDrawers()
            else {
                drawer_layout.openDrawer(nav_right)
                viewModel.getMoney()
            }
        }

        btn_login.setOnClickListener {
            startActivity(Intent(this@MyFavoriteActivity, LoginActivity::class.java))
        }

        btn_register.setOnClickListener {
            startActivity(Intent(this@MyFavoriteActivity, RegisterActivity::class.java))
        }

        tv_odds_type.setOnClickListener {
            ChangeOddsTypeDialog().show(supportFragmentManager, null)
        }

        iv_language.setOnClickListener {
            ChangeLanguageDialog().show(supportFragmentManager, null)
        }
    }

    override fun initMenu() {
        try {
            //關閉側邊欄滑動行為
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            //選單選擇結束要收起選單
            val menuFrag =
                supportFragmentManager.findFragmentById(R.id.fragment_menu) as MenuFragment
            menuFrag.setDownMenuListener { drawer_layout.closeDrawers() }
            nav_right.layoutParams.width = MetricsUtil.getMenuWidth() //動態調整側邊欄寬

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun initBottomNavigation() {
        sport_bottom_navigation.apply {
            setNavigationItemClickListener {
                when (it) {
                    R.id.navigation_sport -> {
                        viewModel.navGame()
                        finish()
                        false
                    }
                    R.id.navigation_game -> {
                        true
                    }
                    R.id.item_bet_list -> {
                        viewModel.navShoppingCart()
                        false
                    }
                    R.id.navigation_account_history -> {
                        viewModel.navAccountHistory()
                        finish()
                        false
                    }
                    R.id.navigation_transaction_status -> {
                        viewModel.navTranStatus()
                        finish()
                        false
                    }
                    else -> false
                }
            }

            setSelected(R.id.navigation_game)
        }
    }

    override fun showBetListPage() {
        val betListFragment =
            BetListFragment.newInstance(object : BetListFragment.BetResultListener {
                override fun onBetResult(betResultData: Receipt?, betParlayList: List<ParlayOdd>) {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.push_right_to_left_enter,
                            R.anim.pop_bottom_to_top_exit,
                            R.anim.push_right_to_left_enter,
                            R.anim.pop_bottom_to_top_exit
                        )
                        .replace(
                            R.id.fl_bet_list,
                            BetReceiptFragment.newInstance(betResultData, betParlayList)
                        )
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

    override fun updateBetListCount(num: Int) {
        sport_bottom_navigation.setBetCount(num)
    }

    override fun showLoginNotify() {
        snackBarLoginNotify.apply {
            setAnchorView(R.id.my_favorite_bottom_navigation)
            show()
        }
    }

    override fun showMyFavoriteNotify(myFavoriteNotifyType: Int) {
        setSnackBarMyFavoriteNotify(myFavoriteNotifyType)
        snackBarMyFavoriteNotify?.apply {
            setAnchorView(R.id.my_favorite_bottom_navigation)
            show()
        }
    }

    private fun initObserver() {
        viewModel.showBetUpperLimit.observe(this, {
            if (it.getContentIfNotHandled() == true)
                snackBarBetUpperLimitNotify.apply {
                    setAnchorView(R.id.my_favorite_bottom_navigation)
                    show()
                }
        })

        viewModel.userInfo.observe(this, {
            updateAvatar(it?.iconUrl)
        })

        viewModel.showBetInfoSingle.observe(this, {
            it?.getContentIfNotHandled()?.let {
                BetInfoCarDialog().show(
                    supportFragmentManager,
                    BetInfoCarDialog::class.java.simpleName
                )
            }
        })

        viewModel.nowTransNum.observe(this, {
            navigation_transaction_status.trans_number.text = it.toString()
        })
    }

    private fun updateAvatar(iconUrl: String?) {
        Glide.with(this).load(iconUrl)
            .apply(RequestOptions().placeholder(R.drawable.img_avatar_default)).into(
                iv_head
            ) //載入頭像
    }

    override fun updateUiWithLogin(isLogin: Boolean) {
        if (isLogin) {
            btn_login.visibility = View.GONE
            btn_register.visibility = View.GONE
            toolbar_divider.visibility = View.GONE
            iv_head.visibility = View.VISIBLE
            tv_odds_type.visibility = View.VISIBLE
        } else {
            btn_login.visibility = View.VISIBLE
            btn_register.visibility = View.VISIBLE
            toolbar_divider.visibility = View.VISIBLE
            iv_head.visibility = View.GONE
            tv_odds_type.visibility = View.GONE
        }
    }

    override fun updateOddsType(oddsType: OddsType) {
        tv_odds_type.text = getString(oddsType.res)
    }

    override fun navOneSportPage(thirdGameCategory: ThirdGameCategory?) {
        if (thirdGameCategory != null) {
            val intent = Intent(this, MainActivity::class.java)
                .putExtra(MainActivity.ARGS_THIRD_GAME_CATE, thirdGameCategory)
            startActivity(intent)

            return
        }

        startActivity(Intent(this, GameActivity::class.java))
    }
}