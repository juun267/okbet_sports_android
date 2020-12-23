package org.cxct.sportlottery.ui.base

import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.activity_tool_bar.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityToolBarBinding
import org.cxct.sportlottery.ui.bet_record.search.BetRecordSearchFragment

class ToolBarActivity : BaseActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_tool_bar)
        val binding = DataBindingUtil.setContentView<ActivityToolBarBinding>(this, R.layout.activity_tool_bar)
//        binding.lifecycleOwner = this
        binding.activity = this

        drawerLayout = binding.drawerLayout
        bindNavHost()
    }

    private fun bindNavHost() {

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