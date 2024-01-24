package org.cxct.sportlottery.ui.feedback

import androidx.navigation.findNavController
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.loading
import org.cxct.sportlottery.databinding.ActivityFeedbackMainBinding
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog

/**
 * @app_destination 建议反馈
 */
class FeedbackMainActivity : BindingActivity<FeedbackViewModel,ActivityFeedbackMainBinding>() {
    private val navController by lazy { findNavController(R.id.myNavHostFragment) }

    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF,true)
        viewModel.getUserInfo()
        initButton()
        initToolbar()
        initLiveData()
    }

    private fun initButton()=binding.run {
        customTabLayout.setCustomTabSelectedListener { position ->
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
        binding.customToolBar.setOnBackPressListener {
            onBackPressed()
        }
    }

    private fun initLiveData() {
        viewModel.isLoading.observe(this) {
            if (it) loading()
            else hideLoading()
        }

        viewModel.isShowToolbar.observe(this) {
            binding.customToolBar.visibility = it
        }

        viewModel.toolbarName.observe(this) {
            binding.customToolBar.titleText = it
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