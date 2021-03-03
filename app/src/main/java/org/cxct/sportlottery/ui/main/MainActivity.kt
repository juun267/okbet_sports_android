package org.cxct.sportlottery.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toast_top_bet_result.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityMainBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.repository.TestFlag
import org.cxct.sportlottery.ui.base.BaseNoticeActivity
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.ui.splash.SplashViewModel
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.MetricsUtil
import org.cxct.sportlottery.util.ToastUtil
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseNoticeActivity<MainViewModel>(MainViewModel::class) {

    companion object {
        //切換語系，activity 要重啟才會生效
        fun reStart(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    private val mSplashViewModel: SplashViewModel by viewModel()

    private lateinit var mainBinding: ActivityMainBinding

    private val navController by lazy { findNavController(R.id.main_container) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        mainBinding.apply {
            mainViewModel = this@MainActivity.viewModel
            lifecycleOwner = this@MainActivity
        }

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

            Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show()

            when (it.itemId) {
                R.id.home_page -> {
                    true
                }
                R.id.game_page -> {
                    startActivity(Intent(this, GameActivity::class.java))
                    true
                }
                R.id.promotion_page -> {
                    when (viewModel.userInfo.value?.testFlag) {
                        TestFlag.NORMAL.index -> {
                            JumpUtil.toInternalWeb(this, Constants.getPromotionUrl(viewModel.token), getString(R.string.promotion))
                        }
                        null -> { //尚未登入
                            startActivity(Intent(this, LoginActivity::class.java))
                        }
                        else -> { //遊客
                            ToastUtil.showToastInCenter(this, getString(R.string.message_guest_no_permission))
                        }
                    }
                    true
                }
                R.id.chat_page -> {
                    true
                }
                R.id.my_account_page -> {
                    true
                }
                else -> false
            }
        }
    }

    private fun getPopImage() {
        viewModel.getPopImage()
    }

    private fun initObserve() {
        viewModel.errorResultToken.observe(this, Observer {
            viewModel.logout()
        })

        viewModel.userInfo.observe(this, Observer {
            updateAvatar(it?.iconUrl)
        })

        //彈窗圖
        viewModel.popImageList.observe(this, Observer {
            setPopImage(it)
        })
    }

    //彈窗圖
    private fun setPopImage(popImageList: List<ImageData>) {
        PopImageDialog(this, popImageList).show()
    }

    //載入頭像
    private fun updateAvatar(iconUrl: String?) {
        Glide.with(this)
            .load(iconUrl)
            .apply(RequestOptions().placeholder(R.drawable.img_avatar_default))
            .into(iv_head)
    }

}
