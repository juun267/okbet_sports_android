package org.cxct.sportlottery.ui.feedback

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_feedback_main.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity

class FeedbackMainActivity : BaseOddButtonActivity<FeedbackViewModel>(FeedbackViewModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_feedback_main)

        initToolbar()
        initLiveData()
        initData()
    }

    private fun initToolbar() {
        btn_toolbar_back.setOnClickListener {
            finish()
        }
    }

    private fun initLiveData() {
        viewModel.isLoading.observe(this@FeedbackMainActivity,{
            if (it)
                loading()
            else
                hideLoading()
        })
    }
    private fun initData(){
        viewModel.getUserInfo()
    }
}