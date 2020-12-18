package org.cxct.sportlottery.ui.menu.results

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.activity_results_settlement.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityResultsSettlementBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class ResultsSettlementActivity : AppCompatActivity() {
    private lateinit var settlementBinding: ActivityResultsSettlementBinding
    private val settlementViewModel: SettlementViewModel by viewModel()
    private val settlementRvAdapter by lazy {
        SettlementRvAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settlementBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_results_settlement)
        settlementBinding.apply {
            settlementViewModel = this@ResultsSettlementActivity.settlementViewModel
            lifecycleOwner = this@ResultsSettlementActivity
            rv_results.adapter = settlementRvAdapter
        }

        settlementViewModel.settlementData.observe(this, {
            settlementRvAdapter.mDataList = it

            val spinnerGameTypeItem = it.map { settlementItem ->
                getString(GameType.valueOf(settlementItem.gameType).string)
            }.toMutableList()
            setupSpinnerGameType(spinnerGameTypeItem)
        })
        settlementViewModel.getSettlementData()
    }

    private fun setupSpinnerGameType(spinnerGameTypeItem: MutableList<String>) {
        spinner_game_type.let {

            it.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                spinnerGameTypeItem
            )
            it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    settlementViewModel.settlementFilter.value?.gameType =
                        spinnerGameTypeItem[position]
                }
            }
        }
    }

    private fun setupSpinnerGameZone(spinnerLeagueItem: MutableList<String>) {
        spinner_game_zone.let {

            it.adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerLeagueItem)
            it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    settlementViewModel.settlementFilter.value?.gameZone =
                        spinnerLeagueItem[position]
                    settlementViewModel.setGameTypeFilter(spinnerLeagueItem[position])
                }
            }
        }
    }
}
