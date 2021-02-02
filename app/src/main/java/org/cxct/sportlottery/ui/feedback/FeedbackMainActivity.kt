package org.cxct.sportlottery.ui.feedback

import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseToolBarActivity

class FeedbackMainActivity : BaseToolBarActivity<FeedbackViewModel>(FeedbackViewModel::class){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isOpenMenu(false) //關掉menu
    }

    override fun setContentView(): Int {
        return R.layout.activity_feedback_main
    }

    override fun setToolBarName(): String {
        return getString(R.string.feedback_toolbar_title)
    }

}