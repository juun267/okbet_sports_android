package org.cxct.sportlottery.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_toolbar_main.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.TestFlag
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseNoticeActivity
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.main.more.MainMoreFragmentArgs
import org.cxct.sportlottery.ui.main.news.NewsDialog
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterActivity
import org.cxct.sportlottery.ui.splash.SplashViewModel
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.MetricsUtil
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseNoticeActivity<MainViewModel>(MainViewModel::class) {

    companion object {
        //切換語系，activity 要重啟才會生效
        fun reStart(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        private var isPopImageDialog = true //第一次進 APP 才要跳 彈窗圖 dialog
    }

    private val mSplashViewModel: SplashViewModel by viewModel()

    private val navController by lazy { findNavController(R.id.main_container) }

    private var mNewsDialog: NewsDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNoticeButton(btn_notice)
        initToolBar()
        initMenu()
        initBottomNav()
        getPopImage()
        initObserve()

        //若啟動頁是使用 local host 進入，到首頁要再 getHost() 一次，背景替換使用最快線路
        if (mSplashViewModel.isNeedGetHost())
            mSplashViewModel.getHost()
    }

    override fun onBackPressed() {
        if (navController.currentDestination?.id != R.id.mainFragment) {
            iv_logo.performClick()
            return
        }

        super.onBackPressed()
    }

    private fun initToolBar() {
        iv_logo.setImageResource(R.drawable.ic_logo)

        //點擊 logo 回到首頁
        iv_logo.setOnClickListener {
            navController.popBackStack(R.id.mainFragment, false)
        }

        //頭像 當 側邊欄 開/關
        iv_head.setOnClickListener {
            if (drawer_layout.isDrawerOpen(nav_right)) drawer_layout.closeDrawers()
            else {
                drawer_layout.openDrawer(nav_right)
            }
        }

        btn_login.setOnClickListener {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        }

        btn_register.setOnClickListener {
            startActivity(Intent(this@MainActivity, RegisterActivity::class.java))
        }
    }

    private fun initMenu() {
        try {
            //關閉側邊欄滑動行為
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            //選單選擇結束要收起選單
            val menuFrag =
                supportFragmentManager.findFragmentById(R.id.fragment_menu) as MenuFragment
            menuFrag.setDownMenuListener(View.OnClickListener { drawer_layout.closeDrawers() })

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
                        Constants.getPromotionUrl(viewModel.token),
                        getString(R.string.promotion)
                    )
                    false
                }
                R.id.chat_page -> {
                    false
                }
                R.id.my_account_page -> {
                    when (viewModel.userInfo.value?.testFlag) {
                        TestFlag.NORMAL.index -> {
                            startActivity(Intent(this, ProfileCenterActivity::class.java))
                        }
                        else -> { //遊客 //尚未登入
                            startActivity(Intent(this, RegisterActivity::class.java))
                        }
                    }
                    false
                }
                else -> false
            }
        }

        //聊天室按鈕 啟用判斷
        bottom_nav_view.menu.findItem(R.id.chat_page).isVisible = sConfigData?.chatOpen == FLAG_OPEN
    }

    private fun getPopImage() {
        viewModel.getPopImage()
    }

    private fun getMsgDialog() {
        viewModel.getMsgDialog()
    }

    private fun initObserve() {
        viewModel.isLogin.observe(this, Observer {
            getMsgDialog() //登入/登出刷新彈窗公告
            updateUiWithLogin(it)
        })

        viewModel.errorResultToken.observe(this, Observer {
            viewModel.logout()
        })

        viewModel.userInfo.observe(this, Observer {
            updateAvatar(it?.iconUrl)
        })

        //彈窗圖
        viewModel.popImageList.observe(this, Observer {
            setPopImage(it?: listOf())
        })

        //公告彈窗
        viewModel.messageDialogResult.observe(this, Observer {
            setNewsDialog(it)
        })

        viewModel.goToThirdGamePage.observe(this, Observer {
            it.getContentIfNotHandled().let { cate ->
                when {
                    cate == ThirdGameCategory.MAIN -> iv_logo.performClick() //跳轉到首頁
                    cate != null -> goToMainMoreFragment(cate.name)
                    else -> {
                    }
                }
            }
        })
    }

    private fun updateUiWithLogin(isLogin: Boolean) {
        if (isLogin) {
            btn_login.visibility = View.GONE
            btn_register.visibility = View.GONE
            toolbar_divider.visibility = View.GONE
            iv_head.visibility = View.VISIBLE
        } else {
            btn_login.visibility = View.VISIBLE
            btn_register.visibility = View.VISIBLE
            toolbar_divider.visibility = View.VISIBLE
            iv_head.visibility = View.GONE
        }
    }

    //彈窗圖
    private fun setPopImage(popImageList: List<ImageData>) {
        if (isPopImageDialog) {
            isPopImageDialog = false
            PopImageDialog(this, popImageList).show()
        }
    }

    //用戶登入公告訊息彈窗
    private fun setNewsDialog(messageListResult: MessageListResult) {
        //未登入、遊客登入都要顯示彈窗
        if (!messageListResult.rows.isNullOrEmpty() &&
            mNewsDialog?.isVisible != true
        ) {
            mNewsDialog = NewsDialog(this, messageListResult.rows)
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

    private fun goToMainMoreFragment(cateCode: String?, firmCode: String? = null) {
        val bundle = MainMoreFragmentArgs(cateCode ?: "", firmCode ?: "").toBundle()
        navController.navigate(R.id.mainMoreFragment, bundle)
    }
}
