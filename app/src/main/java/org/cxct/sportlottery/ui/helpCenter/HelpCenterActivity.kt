package org.cxct.sportlottery.ui.helpCenter

import android.os.Bundle
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.activity_help_center.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity

class HelpCenterActivity : BaseOddButtonActivity<HelpCenterViewModel>(HelpCenterViewModel::class) {

    private val mNavController by lazy {
        findNavController(R.id.help_center_container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_center)
        setupEvent()
    }

    override fun onBackPressed() {
        backEvent()
    }

    private fun setupEvent() {
        btn_back.setOnClickListener {
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