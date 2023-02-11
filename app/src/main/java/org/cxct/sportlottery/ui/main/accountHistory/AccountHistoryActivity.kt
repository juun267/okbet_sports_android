package org.cxct.sportlottery.ui.main.accountHistory

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_account_history.*
import kotlinx.android.synthetic.main.bottom_navigation_item.view.*
import kotlinx.android.synthetic.main.content_bet_info_item_v3.view.*
import kotlinx.android.synthetic.main.sport_bottom_navigation.*
import kotlinx.android.synthetic.main.view_bottom_navigation_sport.*
import kotlinx.android.synthetic.main.view_message.*
import kotlinx.android.synthetic.main.view_nav_right.*
import kotlinx.android.synthetic.main.view_toolbar_main.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.ui.MarqueeAdapter
import org.cxct.sportlottery.ui.base.BaseBottomNavActivity
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.game.betList.BetListFragment
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.menu.ChangeLanguageDialog
import org.cxct.sportlottery.ui.menu.ChangeOddsTypeDialog
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.news.NewsActivity
import org.cxct.sportlottery.util.*


class AccountHistoryActivity :
    BaseBottomNavActivity<AccountHistoryViewModel>(AccountHistoryViewModel::class) {

    private val navController by lazy { findNavController(R.id.account_history_container) }
    private val mMarqueeAdapter by lazy { MarqueeAdapter() }

    private var betListFragment = BetListFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_history)

        initBottomNavigation()
        initToolBar()
        initRvMarquee()
        initMenu()
        setupNoticeButton(iv_notice)
        initObserve()
//        initServiceButton()
    }

    override fun initBottomNavigation() {
        sport_bottom_navigation.apply {
            setNavigationItemClickListener {
                when (it) {
                    R.id.navigation_home -> {
                        viewModel.navHome()
                        finish()
                        false
                    }
                    R.id.navigation_sport -> {
                        viewModel.navGame()
                        finish()
                        false
                    }
                    R.id.navigation_account_history -> {
                        true
                    }
                    R.id.navigation_transaction_status -> {
                        viewModel.navTranStatus()
                        finish()
                        false
                    }
                    R.id.navigation_my -> {
                        viewModel.navMy()
                        finish()
                        false
                    }
                    else -> false
                }
            }

            setSelected(R.id.navigation_account_history)
        }
    }

    override fun showBetListPage() {
        betListFragment =
            BetListFragment.newInstance(object : BetListFragment.BetResultListener {
                override fun onBetResult(
                    betResultData: Receipt?,
                    betParlayList: List<ParlayOdd>,
                    isMultiBet: Boolean
                ) {
                    showBetReceiptDialog(betResultData, betParlayList, isMultiBet, R.id.fl_bet_list)
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

    override fun getBetListPageVisible(): Boolean {
        return betListFragment.isVisible
    }

    override fun updateBetListCount(num: Int) {
        sport_bottom_navigation.setBetCount(num)
    }
    override fun updateBetListOdds(list: MutableList<BetInfoListData>) {
        val multipleOdds = getMultipleOdds(list)
        cl_bet_list_bar.tvOdds.text = multipleOdds
    }

    override fun showLoginNotify() {
        snackBarLoginNotify.apply {
            setAnchorView(R.id.sport_bottom_navigation)
            show()
        }
    }

    override fun showMyFavoriteNotify(myFavoriteNotifyType: Int) {
        setSnackBarMyFavoriteNotify(myFavoriteNotifyType)
        snackBarMyFavoriteNotify?.apply {
            setAnchorView(R.id.game_bottom_navigation)
            show()
        }
    }

    override fun onResume() {
        super.onResume()
        rv_marquee.startAuto()
    }

    override fun onPause() {
        super.onPause()
        rv_marquee.stopAuto()
    }

    override fun onBackPressed() {
        //返回鍵優先關閉投注單fragment
        if (supportFragmentManager.backStackEntryCount != 0) {
            for (i in 0 until supportFragmentManager.backStackEntryCount) {
                supportFragmentManager.popBackStack()
            }
            return
        }

        when (navController.currentDestination?.id) {
            R.id.accountHistoryNextFragment -> {
                navController.navigateUp()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    private fun initObserve() {

        viewModel.userInfo.observe(this) {
            updateAvatar(it?.iconUrl)
        }

        viewModel.messageListResult.observe(this) {
            updateUiWithResult(it)
        }

        viewModel.settlementNotificationMsg.observe(this) {
            val message = it.getContentIfNotHandled()
            message?.let { messageNotnull -> view_notification.addNotification(messageNotnull) }
        }

        viewModel.userInfo.observe(this) {
            updateAvatar(it?.iconUrl)
        }

        viewModel.isLogin.observe(this) {
            getAnnouncement()
        }

        viewModel.nowTransNum.observe(this) {
            navigation_transaction_status.trans_number.text = it.toString()
        }

        viewModel.navPublicityPage.observe(this) {
            GamePublicityActivity.reStart(this)
        }
    }

    /*private fun initServiceButton(){
        btn_floating_service.setView(this)
    }*/

    private fun updateUiWithResult(messageListResult: MessageListResult?) {
        val titleList: MutableList<String> = mutableListOf()
        messageListResult?.let {
            it.rows?.forEach { data -> titleList.add(data.title + " - " + data.message) }

            mMarqueeAdapter.setData(titleList)

            if (messageListResult.success && titleList.size > 0) {
                rv_marquee.startAuto(false) //啟動跑馬燈
            } else {
                rv_marquee.stopAuto(true) //停止跑馬燈
            }
        }
    }

    private fun initRvMarquee() {
        account_history_message.setOnClickListener {
            startActivity(Intent(this, NewsActivity::class.java))
        }
        rv_marquee.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv_marquee.adapter = mMarqueeAdapter
    }

    private fun getAnnouncement() {
        viewModel.getAnnouncement()
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

    override fun updateUiWithLogin(isLogin: Boolean) {
        if (isLogin) {
            btn_login.visibility = View.GONE
            iv_menu.visibility =View.VISIBLE
            iv_notice.visibility = View.VISIBLE
            btn_register.isVisible = !isLogin && !isUAT()
            toolbar_divider.visibility = View.GONE
            iv_head.visibility = View.GONE
            tv_odds_type.visibility = View.GONE
        } else {
            btn_login.visibility = View.VISIBLE
            btn_register.isVisible = !isLogin && !isUAT()
            toolbar_divider.visibility = View.VISIBLE
            iv_head.visibility = View.GONE
            tv_odds_type.visibility = View.GONE
            iv_menu.visibility =View.GONE
            iv_notice.visibility =View.GONE
        }
    }

    private fun updateAvatar(iconUrl: String?) {
        Glide.with(this).load(iconUrl)
            .apply(RequestOptions().placeholder(R.drawable.img_avatar_default)).into(
                iv_head
            ) //載入頭像
    }

    override fun initToolBar() {
        iv_logo.setImageResource(R.drawable.ic_logo)

        iv_language.setImageResource(LanguageManager.getLanguageFlag(this))

        //點擊 logo 回到首頁
        iv_logo.setOnClickListener {
            viewModel.navMainPage(ThirdGameCategory.MAIN)
        }

        //頭像 當 側邊欄 開/關
        iv_menu.setOnClickListener {
            clickMenuEvent()
        }

        btn_login.setOnClickListener {
            startLogin()
        }

        btn_register.setOnClickListener {
            startRegister()
        }

        tv_odds_type.setOnClickListener {
            ChangeOddsTypeDialog().show(supportFragmentManager, null)
        }

        iv_language.setOnClickListener {
            ChangeLanguageDialog(ChangeLanguageDialog.ClearBetListListener{
                viewModel.betInfoRepository.clear()
            }).show(supportFragmentManager, null)
        }
    }

    override fun clickMenuEvent() {
        if (drawer_layout.isDrawerOpen(nav_right)) drawer_layout.closeDrawers()
        else {
            drawer_layout.openDrawer(nav_right)
            viewModel.getMoney()
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

        startActivity(Intent(this, GamePublicityActivity::class.java))
    }
}