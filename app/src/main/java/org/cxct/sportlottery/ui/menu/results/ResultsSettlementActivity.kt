package org.cxct.sportlottery.ui.menu.results

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
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
        settlementRvAdapter = SettlementRvAdapter()

        settlementBinding.apply {
            settlementViewModel = this@ResultsSettlementActivity.settlementViewModel
            lifecycleOwner = this@ResultsSettlementActivity
            rv_results.adapter = settlementRvAdapter
        }
        val spinnerGameTypeItem = mutableListOf<String>()
        settlementViewModel.settlementData.value?.let {list ->
            settlementRvAdapter.setData(list)
            list.forEach { spinnerGameTypeItem.add(it.gameType) }
        }
        spinner_game_type.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerGameTypeItem)
        spinner_game_type.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                settlementViewModel.settlementFilter.value?.gameType = spinnerGameTypeItem[position]
            }
        }
    }
}
