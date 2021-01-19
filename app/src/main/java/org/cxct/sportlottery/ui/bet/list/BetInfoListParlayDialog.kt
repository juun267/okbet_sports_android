package org.cxct.sportlottery.ui.bet.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.dialog_bet_info_list.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBetInfoListBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.home.MainViewModel

class BetInfoListParlayDialog : BaseDialog<MainViewModel>(MainViewModel::class), BetInfoListAdapter.OnItemClickListener {

    companion object {
        val TAG = BetInfoListParlayDialog::class.java.simpleName
    }

    init {
        setStyle(R.style.Common)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding: DialogBetInfoListBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_bet_info_list, container, false)
        binding.apply {
            mainViewModel = this@BetInfoListParlayDialog.viewModel
            lifecycleOwner = this@BetInfoListParlayDialog
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        observeData()
    }

    private fun initUI(){
        iv_close.setOnClickListener {
            dismiss()
        }
    }

    private fun observeData(){

    }

    override fun onDeleteClick(position: Int) {

    }

    override fun onBetClick() {

    }

    override fun onAddMoreClick() {
        dismiss()
    }

}