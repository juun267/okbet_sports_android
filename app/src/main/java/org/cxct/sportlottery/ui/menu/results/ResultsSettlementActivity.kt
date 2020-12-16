package org.cxct.sportlottery.ui.menu.results

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.activity_results_settlement.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityResultsSettlementBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class ResultsSettlementActivity : AppCompatActivity() {

    private lateinit var settlementRvAdapter: SettlementRvAdapter
    private val settlementViewModel: SettlementViewModel by viewModel()
    private lateinit var settlementBinding: ActivityResultsSettlementBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settlementBinding = DataBindingUtil.setContentView(this, R.layout.activity_results_settlement)
        settlementRvAdapter = SettlementRvAdapter(settlementViewModel)

        settlementBinding.apply {
            settlementViewModel = this@ResultsSettlementActivity.settlementViewModel
            lifecycleOwner = this@ResultsSettlementActivity
            rv_results.adapter = settlementRvAdapter
        }
    }
}
