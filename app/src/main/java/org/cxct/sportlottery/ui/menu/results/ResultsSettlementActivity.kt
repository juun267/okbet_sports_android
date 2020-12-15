package org.cxct.sportlottery.ui.menu.results

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_results_settlement.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityResultsSettlementBinding

class ResultsSettlementActivity : AppCompatActivity() {

    private lateinit var settlementRvAdapter: SettlementRvAdapter
    private lateinit var settlementViewModel: SettlementViewModel
    private lateinit var settlementBinding: ActivityResultsSettlementBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results_settlement)

        settlementViewModel = ViewModelProvider(this, SettlementViewModel.Factory(application)).get(SettlementViewModel::class.java)
        settlementBinding = DataBindingUtil.setContentView(this, R.layout.activity_results_settlement)
        settlementRvAdapter = SettlementRvAdapter(settlementViewModel)

        settlementBinding.apply {
            settlementViewModel = this@ResultsSettlementActivity.settlementViewModel
            lifecycleOwner = this@ResultsSettlementActivity
            rv_results.adapter = settlementRvAdapter
        }
    }
}
