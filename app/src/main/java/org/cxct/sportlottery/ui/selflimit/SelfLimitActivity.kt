package org.cxct.sportlottery.ui.selflimit

import android.os.Bundle
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.findNavController
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_feedback_main.*
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivitySelfLimitBinding
import org.cxct.sportlottery.ui.base.BaseSocketActivity

class SelfLimitActivity : BaseSocketActivity<SelfLimitViewModel>(SelfLimitViewModel::class) {

    private lateinit var binding: ActivitySelfLimitBinding
    private var indicatorWidth = 0
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
        tv_toolbar_title.text = getString(R.string.selfLimit)
        btn_toolbar_back.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initView(){
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.selfLimit_froze))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.selfLimit_per_bet_limit))
        binding.tabLayout.post {
            indicatorWidth = binding.tabLayout.width / 2
            val indicatorParams =
                binding.indicator.layoutParams as FrameLayout.LayoutParams
            indicatorParams.width = indicatorWidth
            binding.indicator.layoutParams = indicatorParams
        }
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val params =
                    binding.indicator.layoutParams as FrameLayout.LayoutParams
                if (tab.position == 0) {
                    params.leftMargin = 0
                    binding.indicator.layoutParams = params
                } else if (tab.position == 1) {
                    params.leftMargin = binding.tabLayout.width / 2
                    binding.indicator.layoutParams = params
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        if (!::adapter.isInitialized){
            adapter = ViewPagerAdapter(supportFragmentManager)
            adapter.addFragment(SelfLimitFrozeFragment())
            adapter.addFragment(SelfLimitBetFragment())
        }
        binding.tabLayout.setupWithViewPager(binding.vpSelfLimit)
        binding.vpSelfLimit.adapter = adapter
    }

    private fun initLiveData() {
        viewModel.isLoading.observe(this,  {
            if (it) loading()
            else hideLoading()
        })

        viewModel.isShowToolbar.observe(this, {
            ll_tab.visibility = it
        })

        viewModel.toolbarName.observe(this, {
            tv_toolbar_title.text = it
        })
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
                0 -> getString(R.string.selfLimit_froze)
                1 -> getString(R.string.selfLimit_per_bet_limit)
                else -> null
            }
        }
    }
}