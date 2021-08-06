package org.cxct.sportlottery.ui.feedback

import android.os.Bundle
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.activity_feedback_main.*
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity

class FeedbackMainActivity : BaseOddButtonActivity<FeedbackViewModel>(FeedbackViewModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback_main)

        viewModel.getUserInfo()

        initButton()
        initToolbar()
        initLiveData()
    }

    private fun initButton() {
        val navController = findNavController(R.id.myNavHostFragment)
        btn_sugession.setOnClickListener {
            when (navController.currentDestination?.id) {
                R.id.feedbackRecordListFragment -> {
                    navController.navigate(R.id.action_feedbackRecordListFragment_to_feedbackSuggestFragment)
                }
            }
        }
        btn_record.setOnClickListener {
            when (navController.currentDestination?.id) {
                R.id.feedbackSuggestFragment -> {
                    navController.navigate(R.id.action_feedbackSuggestFragment_to_feedbackRecordListFragment)
                }
                R.id.feedbackSubmitFragment -> {
                    navController.navigate(R.id.action_feedbackSubmitFragment_to_feedbackRecordListFragment)
                }
            }

        }
    }

    private fun initToolbar() {
        tv_toolbar_title.text = getString(R.string.feedback_toolbar_title)
        btn_toolbar_back.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initLiveData() {
        viewModel.isLoading.observe(this,  {
            if (it) loading()
            else hideLoading()
        })

        viewModel.isShowToolbar.observe(this, {
            ll_tab.visibility = it
        })

        viewModel.toolbarName.observe(this, {
            tv_toolbar_title.text = it
        })
    }

}