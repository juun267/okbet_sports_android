package org.cxct.sportlottery.ui.news

import androidx.navigation.findNavController
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityNewsBinding
import org.cxct.sportlottery.ui.base.BaseActivity

/**
 * @app_destination 最新消息
 */
class NewsActivity : BaseActivity<NewsViewModel, ActivityNewsBinding>() {

    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        initViews()
    }

    private fun initViews() {
        initToolbar()
    }

    private fun initToolbar() {
        with(binding.toolBar) {
            tvToolbarTitle.text = getString(R.string.news)
        }
        binding.toolBar.btnToolbarBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        val navController = findNavController(binding.bankContainer.id)
        if (navController.previousBackStackEntry != null)
            findNavController(binding.bankContainer.id).popBackStack()
        else
            finish()
    }
}