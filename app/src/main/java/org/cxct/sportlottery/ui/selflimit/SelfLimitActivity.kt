package org.cxct.sportlottery.ui.selflimit

import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import kotlinx.android.synthetic.main.activity_self_limit.*
import kotlinx.android.synthetic.main.custom_tab_layout.view.*
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivitySelfLimitBinding
import org.cxct.sportlottery.ui.base.BaseSocketActivity

class SelfLimitActivity : BaseSocketActivity<SelfLimitViewModel>(SelfLimitViewModel::class) {

    private lateinit var binding: ActivitySelfLimitBinding
    private lateinit var adapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelfLimitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initView()
        initLiveData()
    }


    private fun initToolbar() {
        tv_toolbar_title.text = getString(R.string.self_limit)
        btn_toolbar_back.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initView(){
        val viewFirst = this.layoutInflater.inflate(R.layout.tab_layout_text_view, null)
        val textView = viewFirst.findViewById<TextView>(R.id.tv_tab_item)
        textView.text = getString(R.string.self_limit_account_login_limit)

        val viewSecond = this.layoutInflater.inflate(R.layout.tab_layout_text_view, null)
        val textViewSecond = viewSecond.findViewById<TextView>(R.id.tv_tab_item)
        textViewSecond.text = getString(R.string.self_limit_per_bet_limit)

        if (!::adapter.isInitialized){
            adapter = ViewPagerAdapter(supportFragmentManager)
            adapter.addFragment(SelfLimitFrozeFragment())
            adapter.addFragment(SelfLimitBetFragment())
        }
        binding.customTabLayout.tab_layout_custom.setupWithViewPager(binding.vpSelfLimit)
        binding.vpSelfLimit.adapter = adapter

        binding.customTabLayout.tab_layout_custom.getTabAt(0)?.customView = viewFirst
        binding.customTabLayout.tab_layout_custom.getTabAt(1)?.customView = viewSecond

//        //todo 暫時用點擊事件解決tab初始化問題 from Jeff
//        binding.tabLayout.getTabAt(1)?.select()
//        binding.tabLayout.getTabAt(0)?.select()
    }

    private fun initLiveData() {
        viewModel.isLoading.observe(this) {
            if (it) loading()
            else hideLoading()
        }

        viewModel.isShowToolbar.observe(this) {
            custom_tab_layout.visibility = it
        }

        viewModel.toolbarName.observe(this) {
            tv_toolbar_title.text = it
        }
    }


    internal inner class ViewPagerAdapter(manager: FragmentManager) :
        FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val mFragmentList = ArrayList<Fragment>()
//        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }


        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment) {
            mFragmentList.add(fragment)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> getString(R.string.self_limit_account_login_limit)
                1 -> getString(R.string.self_limit_per_bet_limit)
                else -> null
            }
        }
    }
}