package org.cxct.sportlottery.ui.selflimit

import android.graphics.Typeface
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
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
        val viewFirst = this.layoutInflater.inflate(R.layout.tab_layout_text_view, null)
        val textView = viewFirst.findViewById<TextView>(R.id.tv_tab_item)
        textView.text = getString(R.string.selfLimit_froze)

        val viewSecond = this.layoutInflater.inflate(R.layout.tab_layout_text_view, null)
        val textViewSecond = viewSecond.findViewById<TextView>(R.id.tv_tab_item)
        textViewSecond.text = getString(R.string.selfLimit_per_bet_limit)

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

                val tabTextView: TextView? = tab.customView?.findViewById(R.id.tv_tab_item)
                tabTextView?.setTextColor(resources.getColor(R.color.colorBlue))
                tabTextView?.setTypeface(null, Typeface.BOLD)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val tabTextView: TextView? = tab.customView?.findViewById(R.id.tv_tab_item)
                tabTextView?.setTextColor(resources.getColor(R.color.colorWhite))
                tabTextView?.setTypeface(null, Typeface.NORMAL)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        if (!::adapter.isInitialized){
            adapter = ViewPagerAdapter(supportFragmentManager)
            adapter.addFragment(SelfLimitFrozeFragment())
            adapter.addFragment(SelfLimitBetFragment())
        }
        binding.tabLayout.setupWithViewPager(binding.vpSelfLimit)
        binding.vpSelfLimit.adapter = adapter

        binding.tabLayout.getTabAt(0)?.customView = viewFirst
        binding.tabLayout.getTabAt(1)?.customView = viewSecond

        //todo 暫時用點擊事件解決tab初始化問題 from Jeff
        binding.tabLayout.getTabAt(1)?.select()
        binding.tabLayout.getTabAt(0)?.select()
    }

    private fun initLiveData() {
        viewModel.isLoading.observe(this) {
            if (it) loading()
            else hideLoading()
        }

        viewModel.isShowToolbar.observe(this) {
            ll_tab.visibility = it
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
                0 -> getString(R.string.selfLimit_froze)
                1 -> getString(R.string.selfLimit_per_bet_limit)
                else -> null
            }
        }
    }
}