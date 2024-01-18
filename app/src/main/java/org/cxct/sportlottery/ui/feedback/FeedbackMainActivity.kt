package org.cxct.sportlottery.ui.feedback

import android.os.Bundle
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.activity_feedback_main.*
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog

/**
 * @app_destination 建议反馈
 */
class FeedbackMainActivity : BaseSocketActivity<FeedbackViewModel>(FeedbackViewModel::class) {
    private val navController by lazy { findNavController(R.id.myNavHostFragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //沉浸式颜色修改
        setStatusbar(R.color.color_232C4F_FFFFFF,true)
        setContentView(R.layout.activity_feedback_main)

        viewModel.getUserInfo()

        initButton()
        initToolbar()
        initLiveData()
    }

    private fun initButton() {
        val navController = findNavController(R.id.myNavHostFragment)
        custom_tab_layout.setCustomTabSelectedListener { position ->
            when(position) {
                0 -> {
                    when (navController.currentDestination?.id) {
                        R.id.feedbackRecordListFragment -> {
                            navController.navigate(R.id.action_feedbackRecordListFragment_to_feedbackSuggestFragment)
                        }
                    }
                }
                1 -> {
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
        }
    }

    private fun initToolbar() {
        custom_tool_bar.setOnBackPressListener {
            onBackPressed()
        }
    }

    private fun initLiveData() {
        viewModel.isLoading.observe(this) {
            if (it) loading()
            else hideLoading()
        }

        viewModel.isShowToolbar.observe(this) {
            custom_tab_layout.visibility = it
        }

        viewModel.toolbarName.observe(this) {
            tv_toolbar_title.text = it
        }
        viewModel.feedBackBaseResult.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (navController.currentDestination?.id) {
                    R.id.feedbackSubmitFragment -> {
                        navController.navigate(R.id.action_feedbackSubmitFragment_to_feedbackSuggestFragment)
                    }
                }

                val msgContent =
                    if (result.success) getString(R.string.feedback_submit_succeed) else result.msg
                val dialog = CustomAlertDialog(this).apply {
                    setTitle(this@FeedbackMainActivity.getString(R.string.prompt))
                    setMessage(msgContent)
                    setNegativeButtonText(null)
                }
                dialog.show(supportFragmentManager, null)
            }
        }
    }

}