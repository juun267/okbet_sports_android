package org.cxct.sportlottery.ui.aboutMe

import android.os.Bundle
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.activity_help_center.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.helpCenter.HelpCenterViewModel

class AboutMeActivity : BaseSocketActivity<HelpCenterViewModel>(HelpCenterViewModel::class) {

    private val mNavController by lazy {
        findNavController(R.id.about_us_container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_232C4F_FFFFFF,true)
        setContentView(R.layout.activity_about_me)
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