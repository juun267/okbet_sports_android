package org.cxct.sportlottery.ui.news

import android.os.Bundle
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityNewsBinding
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.game.GameViewModel

/**
 * @app_destination 最新消息
 */
class NewsActivity : BaseSocketActivity<GameViewModel>(GameViewModel::class) {
    private lateinit var binding: ActivityNewsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        initToolbar()
    }

    private fun initToolbar() {
        with(binding.toolBar) {
            tvToolbarTitle.text = getString(R.string.news)
        }
        btn_toolbar_back.setOnClickListener {
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