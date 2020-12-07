package org.cxct.sportlottery.ui.normal

import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseActivity

class MainActivity : BaseActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
