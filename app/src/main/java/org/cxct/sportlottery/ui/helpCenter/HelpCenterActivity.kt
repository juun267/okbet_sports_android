package org.cxct.sportlottery.ui.helpCenter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseToolBarActivity

class HelpCenterActivity : BaseToolBarActivity<HelpCenterViewModel>(HelpCenterViewModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun setContentView(): Int {
        return R.layout.activity_help_center
    }

    override fun setToolBarName(): String {
        return getString(R.string.help_center)
    }
}