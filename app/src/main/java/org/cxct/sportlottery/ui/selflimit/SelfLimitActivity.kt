package org.cxct.sportlottery.ui.selflimit

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.loading
import org.cxct.sportlottery.databinding.ActivitySelfLimitBinding
import org.cxct.sportlottery.ui.base.BaseSocketActivity

/**
 * @app_destination 自我禁制
 */
class SelfLimitActivity : BaseSocketActivity<SelfLimitViewModel,ActivitySelfLimitBinding>(SelfLimitViewModel::class) {
    override fun pageName() = "自我禁制页面"
    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF,true)
        initToolbar()
        initView()
        initLiveData()
    }

    private fun initToolbar()=binding.toolBar.run {
        titleText = getString(R.string.self_limit)
        setOnBackPressListener{
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
        binding.customTabLayout.binding.tabLayoutCustom.setupWithViewPager(binding.vpSelfLimit)
    }

    private fun initLiveData() {
        viewModel.isLoading.observe(this) {
            if (it) loading()
            else hideLoading()
        }

        viewModel.isShowToolbar.observe(this) {
            binding.customTabLayout.visibility = it
        }

        viewModel.toolbarName.observe(this) {
            binding.toolBar.titleText = it
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