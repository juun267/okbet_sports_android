package org.cxct.sportlottery.ui.bet_record

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_tool_bar.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityToolBarBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.util.MetricsUtil

class BetRecordActivity : BaseActivity<BetRecordViewModel>(BetRecordViewModel::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityToolBarBinding>(this, R.layout.activity_tool_bar)

        binding.apply {
            betRecordViewModel = this@BetRecordActivity.viewModel
            lifecycleOwner = this@BetRecordActivity
        }

        setToolbar()
        initMenu()
    }

    private fun setToolbar() {
        tool_bar.setTitle(getString(R.string.bet_record))
    }

    private fun initMenu() {
        try {
            //選單選擇結束要收起選單
            val menuFrag = supportFragmentManager.findFragmentById(R.id.fragment_menu) as MenuFragment
            menuFrag.setDownMenuListener { drawer_layout.closeDrawers() }

            nav_right.layoutParams.width = MetricsUtil.getMenuWidth() //動態調整側邊欄寬
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun transFragment(frag: Fragment?, isAddToBackStack: Boolean, tag: String?) {
        try {
            if (frag == null) {
                return
            }
            val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()

            //setting animation
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE) //set scene animation //another can use "ft.setCustomAnimations()"
            fragmentTransaction.replace(R.id.container, frag, tag) //replace fragment in the container layout.
            if (isAddToBackStack) {
                fragmentTransaction.addToBackStack(tag)
            }
            fragmentTransaction.commitAllowingStateLoss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}