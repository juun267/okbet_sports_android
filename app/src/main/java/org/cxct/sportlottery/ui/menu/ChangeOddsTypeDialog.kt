package org.cxct.sportlottery.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.dialog_change_odd_type.*
import kotlinx.android.synthetic.main.dialog_change_odd_type.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseBottomSheetFragment
import org.cxct.sportlottery.ui.main.MainViewModel

class ChangeOddsTypeDialog : BaseBottomSheetFragment<MainViewModel>(MainViewModel::class) {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_change_odd_type, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initEvent(view)
        initObserver()
        getOddsType()
    }


    private fun initEvent(rootView: View?) {
        rootView?.apply {
            tv_close?.setOnClickListener {
                dismiss()
            }
            tv_odd_type_hk?.setOnClickListener {
                selectOddsType(OddsType.HK.value)
            }
            tv_odd_type_eu?.setOnClickListener {
                selectOddsType(OddsType.EU.value)
            }
        }
    }

    private fun initObserver(){
        viewModel.oddsType.observe(viewLifecycleOwner, {
            setOddsType(it)
        })
    }

    private fun getOddsType(){
        viewModel.getOddsType()
    }

    private fun setOddsType(oddsType: String) {
        context?.let { context ->
            when (oddsType) {
                OddsType.EU.value -> {
                    tv_odd_type_hk.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))
                    tv_odd_type_eu.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite6))
                }
                OddsType.HK.value -> {
                    tv_odd_type_hk.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite6))
                    tv_odd_type_eu.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))
                }
            }
        }
    }


    private fun selectOddsType(oddsType: String) {
        viewModel.saveOddsType(oddsType)
        dismiss()
    }


}