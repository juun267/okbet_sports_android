package org.cxct.sportlottery.ui.menu


import android.os.Bundle
import android.view.*
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
            img_close?.setOnClickListener {
                dismiss()
            }

            rb_eu?.setOnClickListener {
                selectOddsType(OddsType.EU)
            }

            rb_hk?.setOnClickListener {
                selectOddsType(OddsType.HK)
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


    private fun setOddsType(oddsType: OddsType) {
        context?.let {
            when (oddsType) {
                OddsType.EU -> {
                    rb_eu.isChecked = true
                }
                OddsType.HK -> {
                    rb_hk.isChecked = true
                }
            }
        }
    }


    private fun selectOddsType(oddsType: OddsType) {
        viewModel.saveOddsType(oddsType)
        dismiss()
    }


}