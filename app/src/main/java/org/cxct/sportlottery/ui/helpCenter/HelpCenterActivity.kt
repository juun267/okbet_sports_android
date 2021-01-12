package org.cxct.sportlottery.ui.helpCenter

import android.os.Bundle
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.activity_base_tool_bar.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseToolBarActivity

class HelpCenterActivity : BaseToolBarActivity<HelpCenterViewModel>(HelpCenterViewModel::class) {

    private val mNavController by lazy {
        findNavController(R.id.help_center_container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupEvent()
    }

    override fun setContentView(): Int {
        return R.layout.activity_help_center
    }

    override fun setToolBarName(): String {
        return getString(R.string.help_center)
    }


    override fun onBackPressed() {
        backEvent()
    }

    private fun setupEvent() {
        btn_toolbar_back.setOnClickListener {
            backEvent()
        }
    }

    private fun backEvent() {
        when (mNavController.currentDestination?.id) {
            R.id.helpCenterFragment -> {
                drawer_layout.closeDrawers()
                super.onBackPressed()
            }
            R.id.gameRuleFragment -> {
                val action = GameRuleFragmentDirections.actionGameRuleFragmentToHelpCenterFragment()
                mNavController.navigate(action)
            }
        }
    }
}