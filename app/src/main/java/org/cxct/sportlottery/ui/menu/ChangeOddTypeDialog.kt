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

class ChangeOddTypeDialog : BaseBottomSheetFragment<MainViewModel>(MainViewModel::class) {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_change_odd_type, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initEvent(view)
        initObserver()
        getOddType()
    }


    private fun initEvent(rootView: View?) {
        rootView?.apply {
            tv_close?.setOnClickListener {
                dismiss()
            }
            tv_odd_type_hk?.setOnClickListener {
                selectOddType(OddType.HK.value)
            }
            tv_odd_type_eu?.setOnClickListener {
                selectOddType(OddType.EU.value)
            }
        }
    }

    private fun initObserver(){
        viewModel.oddType.observe(viewLifecycleOwner, {
            setOddType(it)
        })
    }

    private fun getOddType(){
        viewModel.getOddType()
    }

    private fun setOddType(oddType: String) {
        context?.let { context ->
            when (oddType) {
                OddType.EU.value -> {
                    tv_odd_type_hk.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))
                    tv_odd_type_eu.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite6))
                }
                OddType.HK.value -> {
                    tv_odd_type_hk.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite6))
                    tv_odd_type_eu.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))
                }
            }
        }
    }


    private fun selectOddType(oddType: String) {
        viewModel.saveOddType(oddType)
        dismiss()
    }


}