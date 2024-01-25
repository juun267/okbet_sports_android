package org.cxct.sportlottery.ui.selflimit

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import kotlinx.android.synthetic.main.activity_self_limit.*
import kotlinx.android.synthetic.main.custom_tab_layout.view.*
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.loading
import org.cxct.sportlottery.databinding.ActivitySelfLimitBinding
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.util.setTitleLetterSpacing

/**
 * @app_destination 自我禁制
 */
class SelfLimitActivity : BaseSocketActivity<SelfLimitViewModel>(SelfLimitViewModel::class) {

    private lateinit var binding: ActivitySelfLimitBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelfLimitBinding.inflate(layoutInflater)
        setStatusbar(R.color.color_232C4F_FFFFFF,true)
        setContentView(binding.root)

        initToolbar()
        initView()
        initLiveData()
    }

    private fun initToolbar() {
        tv_toolbar_title.setTitleLetterSpacing()
        tv_toolbar_title.text = getString(R.string.self_limit)
        btn_toolbar_back.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initView() {
        binding.vpSelfLimit.apply {
            adapter = ViewPagerAdapter(supportFragmentManager).apply {
                addFragment(SelfLimitFrozeFragment())
                addFragment(SelfLimitBetFragment())
            }
        }
        binding.customTabLayout.tab_layout_custom.setupWithViewPager(binding.vpSelfLimit)
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