package org.cxct.sportlottery.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_main.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityMainBinding
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.ui.splash.SplashViewModel
import org.cxct.sportlottery.util.MetricsUtil
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseOddButtonActivity<MainViewModel>(MainViewModel::class) {

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
        initObserve()

        //若啟動頁是使用 local host 進入，到首頁要再 getHost() 一次，背景替換使用最快線路
        if (mSplashViewModel.isNeedGetHost())
            mSplashViewModel.getHost()
    }

    private fun initToolBar() {
        iv_logo.setImageResource(R.drawable.ic_logo)
        iv_logo.setOnClickListener {
            //TODO simon test 首頁跳轉
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
                    true
                }
                R.id.promotion_page -> {
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

    private fun initObserve() {
        viewModel.errorResultToken.observe(this, Observer {
            viewModel.logout()
        })

        viewModel.userInfo.observe(this, Observer {
            updateAvatar(it?.iconUrl)
        })

        receiver.userNotice.observe(this, Observer {
            //TODO simon test review UserNotice 彈窗，需要顯示在最上層，目前如果開啟多個 activity，現行架構只會顯示在 MainActivity 裡面
            it?.userNoticeList?.let { list ->
                if (list.isNotEmpty())
                    UserNoticeDialog(this).setNoticeList(list).show()
            }
        })

        receiver.notice.observe(this, Observer {
            hideLoading()
            if (it != null) {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
        })

        receiver.sysMaintenance.observe(this) {
            startActivity(Intent(this, MaintenanceActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
        }
    }

    //載入頭像
    private fun updateAvatar(iconUrl: String?) {
        Glide.with(this)
            .load(iconUrl)
            .apply(RequestOptions().placeholder(R.drawable.ic_head))
            .into(iv_head)
    }

}
