package org.cxct.sportlottery.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.home_cate_tab.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityMainBinding
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.ui.MarqueeAdapter
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.login.LoginActivity
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.util.MetricsUtil
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity<BaseViewModel>() {

    companion object {
        private const val TAG = "MainActivity"

        //切換語系，activity 要重啟才會生效
        fun reStart(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    override val viewModel by inject<MainViewModel>()
    private lateinit var mainBinding: ActivityMainBinding

    private val mMarqueeAdapter = MarqueeAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initToolBar()
        initMenu()
        initRvMarquee()
        initTabLayout()
        refreshView()
    }

    override fun onResume() {
        super.onResume()
        rv_marquee.startAuto()
    }

    override fun onPause() {
        super.onPause()
        rv_marquee.stopAuto()
    }

    private fun initToolBar() {
        iv_logo.setImageResource(R.drawable.ic_logo)

        //側邊欄 開/關
        btn_menu.setOnClickListener {
            if (drawer_layout.isDrawerOpen(nav_right))
                drawer_layout.closeDrawers()
            else {
                drawer_layout.openDrawer(nav_right)
            }
        }

        btn_login.setOnClickListener {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        }

        btn_logout.setOnClickListener {
            viewModel.logout()
            getAnnouncement()
        }
    }

    private fun initMenu() {
        try {
            //選單選擇結束要收起選單
            val menuFrag =
                supportFragmentManager.findFragmentById(R.id.fragment_menu) as MenuFragment
            menuFrag.setDownMenuListener(View.OnClickListener { drawer_layout.closeDrawers() })

            nav_right.layoutParams.width = MetricsUtil.getMenuWidth() //動態調整側邊欄寬
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //公告
    private fun initRvMarquee() {
        rv_marquee.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv_marquee.adapter = mMarqueeAdapter
    }

    private fun initTabLayout() {
        try {
            val tabAll = tabLayout.getTabAt(0)?.customView
            tabAll?.tv_title?.setText(R.string.home_tab_all)
            tabAll?.tv_number?.text = "0"

            val tabInPlay = tabLayout.getTabAt(1)?.customView
            tabInPlay?.tv_title?.setText(R.string.home_tab_in_play)
            tabInPlay?.tv_number?.text = "0"

            val tabToday = tabLayout.getTabAt(2)?.customView
            tabToday?.tv_title?.setText(R.string.home_tab_today)
            tabToday?.tv_number?.text = "0"

            val tabEarly = tabLayout.getTabAt(3)?.customView
            tabEarly?.tv_title?.setText(R.string.home_tab_early)
            tabEarly?.tv_number?.text = "0"

            val tabParlay = tabLayout.getTabAt(4)?.customView
            tabParlay?.tv_title?.setText(R.string.home_tab_parlay)
            tabParlay?.tv_number?.text = "0"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun refreshView() {
        //登入資料
        viewModel.token.observe(this) {
            Log.e("simon test", "token: $it")
            if (it.isNullOrEmpty()) {
                btn_login.visibility = View.VISIBLE
                btn_logout.visibility = View.GONE
            } else {
                btn_login.visibility = View.GONE
                btn_logout.visibility = View.VISIBLE
            }

            queryData()
        }


        viewModel.baseResult.observe(this, Observer {
            hideLoading()

            when (it) {
                is MessageListResult? -> {
                    updateUiWithResult(it)
                }
                is SportMenuResult? -> {
                    updateUiWithResult(it)
                }
            }
        })
    }

    private fun updateUiWithResult(messageListResult: MessageListResult?) {
        val titleList: MutableList<String> = mutableListOf()
        messageListResult?.rows?.forEach { data -> titleList.add(data.title + " - " + data.content) }

        if (messageListResult?.success == true && titleList.size > 0) {
            rv_marquee.startAuto() //啟動跑馬燈
        } else {
            rv_marquee.stopAuto() //停止跑馬燈
        }

        mMarqueeAdapter.setData(titleList)
    }

    private fun updateUiWithResult(sportMenuResult: SportMenuResult?) {
        if (sportMenuResult?.success == true) {
            //TODO simon test 刷新 運動彩票頁籤
            Toast.makeText(this, "獲取體育菜單成功", Toast.LENGTH_SHORT).show()
        } else {
            //獲取體育菜單失敗，就跳轉到登入頁 (可能是 token 過期)
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun queryData() {
        getAnnouncement()
        getSportMenu()
    }

    private fun getAnnouncement() {
        viewModel.getAnnouncement()
    }

    private fun getSportMenu() {
        loading()
        viewModel.getSportMenu()
    }
}
