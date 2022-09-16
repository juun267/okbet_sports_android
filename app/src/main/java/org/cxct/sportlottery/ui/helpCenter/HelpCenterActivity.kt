package org.cxct.sportlottery.ui.helpCenter

import android.os.Bundle
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.activity_help_center.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketActivity

class HelpCenterActivity : BaseSocketActivity<HelpCenterViewModel>(HelpCenterViewModel::class) {

    private val mNavController by lazy {
        findNavController(R.id.help_center_container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_232C4F_FFFFFF,true)
        setContentView(R.layout.activity_help_center)
        setupEvent()
    }

    override fun onBackPressed() {
        backEvent()
    }

    private fun setupEvent() {
        custom_tool_bar.setOnBackPressListener {
            finish()
        }
    }

    private fun backEvent() {
        if (mNavController.previousBackStackEntry == null) {
            finish()
        } else {
            mNavController.popBackStack()
        }
    }
}