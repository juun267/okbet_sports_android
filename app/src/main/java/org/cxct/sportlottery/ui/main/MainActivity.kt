package org.cxct.sportlottery.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_toolbar_main.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.main.more.MainMoreFragmentArgs
import org.cxct.sportlottery.ui.main.news.NewsDialog
import org.cxct.sportlottery.ui.menu.ChangeLanguageDialog
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterActivity
import org.cxct.sportlottery.ui.splash.SplashViewModel
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.MetricsUtil
import org.cxct.sportlottery.util.phoneNumCheckDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.*

class MainActivity : BaseSocketActivity<MainViewModel>(MainViewModel::class) {

    companion object {
        //切換語系，activity 要重啟才會生效
        fun reStart(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        const val ARGS_THIRD_GAME_CATE = "key-thirdGameCategory"
    }

    private val mSplashViewModel: SplashViewModel by viewModel()

    private val navController by lazy { findNavController(R.id.main_container) }

    private var mNewsDialog: NewsDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNoticeButton(iv_notice)
        initToolBar()
        initMenu()
        initBottomNav()
//        initServiceButton()
        initObserve()
        setFontTheme()

        //若啟動頁是使用 local host 進入，到首頁要再 getHost() 一次，背景替換使用最快線路
        //20210414修改邏輯, 若local host可以使用, 就直接使用, 若無法使用才getHost取得可以使用之域名
        /*if (mSplashViewModel.isNeedGetHost())
            mSplashViewModel.getHost()*/
    }

    private fun setFontTheme() {
        when (LanguageManager.getSelectLanguage(this)) {
            LanguageManager.Language.ZH, LanguageManager.Language.ZHT -> {
                setTheme(R.style.ChineseTheme)
            }
            LanguageManager.Language.VI -> {
                setTheme(R.style.VietnamTheme)
            }
            else -> {
                setTheme(R.style.EnglishTheme)
            }
        }
    }

    override fun onBackPressed() {
        if (navController.currentDestination?.id != R.id.mainFragment) {
            iv_logo.performClick()
            return
        }

        super.onBackPressed()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        jumpScreen()
    }

    private fun jumpScreen() {
        try {
            val cate = intent.getSerializableExtra(ARGS_THIRD_GAME_CATE) as ThirdGameCategory
            Timber.d("Jump screen: ${cate.name}")
            when (cate) {
                ThirdGameCategory.MAIN -> iv_logo.performClick() //跳轉到首頁
                ThirdGameCategory.CGCP -> goToMainFragment()
                else -> goToMainMoreFragment(cate.name)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initToolBar() {
        iv_logo.setImageResource(R.drawable.ic_logo)

        iv_language.setImageResource(LanguageManager.getLanguageFlag(this))

        //點擊 logo 回到首頁
        iv_logo.setOnClickListener {
            navController.popBackStack(R.id.mainFragment, false)
        }

        //頭像 當 側邊欄 開/關
        iv_menu.setOnClickListener {
            if (drawer_layout.isDrawerOpen(nav_right)) drawer_layout.closeDrawers()
            else {
                drawer_layout.openDrawer(nav_right)
                viewModel.getMoney()
            }
        }

        btn_login.setOnClickListener {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        }

        btn_register.setOnClickListener {
            startActivity(Intent(this@MainActivity, RegisterActivity::class.java))
        }

        iv_language.setOnClickListener {
            ChangeLanguageDialog(ChangeLanguageDialog.ClearBetListListener {
                viewModel.betInfoRepository.clear()
            }).show(supportFragmentManager, null)
        }
    }

    private fun initMenu() {
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

    private fun initBottomNav() {
        bottom_nav_view.setOnNavigationItemSelectedListener {
            //20200303 紀錄：跳轉其他 Activity 頁面，不需要切換 BottomNav 選取狀態
            when (it.itemId) {
                R.id.home_page -> {
                    iv_logo.performClick()
                    true
                }
                R.id.game_page -> {
                    startActivity(Intent(this, GameActivity::class.java))
                    false
                }
                R.id.promotion_page -> {
                    JumpUtil.toInternalWeb(
                        this,
                        Constants.getPromotionUrl(
                            viewModel.token,
                            LanguageManager.getSelectLanguage(this@MainActivity)
                        ),
                        getString(R.string.promotion)
                    )
                    false
                }
                R.id.chat_page -> {
                    false
                }
                R.id.my_account_page -> {
                    viewModel.navActivity(ProfileCenterActivity::class.java)
                    false
                }
                else -> false
            }
        }

        //聊天室按鈕 啟用判斷
        bottom_nav_view.menu.findItem(R.id.chat_page).isVisible = sConfigData?.chatOpen == FLAG_OPEN
    }

    /*private fun initServiceButton() {
        btn_floating_service.setView(this)
    }*/

    private fun getMsgDialog() {
        viewModel.getAnnouncement()
    }

    private fun initObserve() {
        viewModel.isLogin.observe(this) {
            getMsgDialog() //登入/登出刷新彈窗公告
            updateUiWithLogin(it)
        }

        viewModel.navActivity.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                startActivity(Intent(this, it))
            }
        }

        viewModel.userInfo.observe(this) {
            updateAvatar(it?.iconUrl)
        }

        //公告彈窗
        viewModel.promoteNoticeResult.observe(this) {
            it.getContentIfNotHandled()?.let { result ->
                //Task 1901 在新版公告介面出來之前先隱藏
//                setNewsDialog(result)
            }
        }

        //使用者沒有電話號碼
        viewModel.showPhoneNumberMessageDialog.observe(this) {
            it.getContentIfNotHandled()?.let { b ->
                if (!b) phoneNumCheckDialog(this, supportFragmentManager)
            }
        }
    }

    private fun updateUiWithLogin(isLogin: Boolean) {
        if (isLogin) {
            btn_login.visibility = View.GONE
            iv_menu.visibility = View.VISIBLE
            iv_notice.visibility = View.VISIBLE
            btn_register.visibility = View.GONE
            toolbar_divider.visibility = View.GONE
            iv_head.visibility = View.GONE
        } else {
            btn_login.visibility = View.VISIBLE
            btn_register.visibility = View.VISIBLE
            toolbar_divider.visibility = View.VISIBLE
            iv_head.visibility = View.GONE
            iv_menu.visibility = View.GONE
            iv_notice.visibility = View.GONE
        }
    }

    //用戶登入公告訊息彈窗
    private fun setNewsDialog(messageListResult: MessageListResult) {
        //未登入、遊客登入都要顯示彈窗
        if (!messageListResult.rows.isNullOrEmpty()) {
            mNewsDialog?.dismiss()
            mNewsDialog = NewsDialog(messageListResult.rows)
            mNewsDialog?.show(supportFragmentManager, null)
        }
    }

    //載入頭像
    private fun updateAvatar(iconUrl: String?) {
        Glide.with(this)
            .load(iconUrl)
            .apply(RequestOptions().placeholder(R.drawable.img_avatar_default))
            .into(iv_head)
    }

    private fun goToMainFragment() {
        viewModel.goToLottery()
    }

    private fun goToMainMoreFragment(cateCode: String?, firmCode: String? = null) {
        val bundle = MainMoreFragmentArgs(cateCode ?: "", firmCode ?: "").toBundle()
        navController.navigate(R.id.mainMoreFragment, bundle)
    }

}
