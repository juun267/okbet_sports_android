package org.cxct.sportlottery.ui.menu


import android.os.Bundle
import android.view.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogChangeOddTypeBinding
import org.cxct.sportlottery.ui.base.BaseBottomSheetFragment
import org.cxct.sportlottery.ui.main.MainViewModel


class ChangeOddsTypeDialog : BaseBottomSheetFragment<MainViewModel>(MainViewModel::class) {

    private lateinit var viewBinding: DialogChangeOddTypeBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = DialogChangeOddTypeBinding.inflate(inflater, container, false)
        return viewBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initEvent(view)
        initObserver()
        getOddsType()
    }


    private fun initEvent(rootView: View?) {
        rootView?.apply {
            viewBinding.imgClose.setOnClickListener {
                dismiss()
            }

            viewBinding.rbEu.setOnClickListener {
                selectOddsType(OddsType.EU)
            }

            viewBinding.rbHk.setOnClickListener {
                selectOddsType(OddsType.HK)
            }

            viewBinding.rbMys.setOnClickListener {
                selectOddsType(OddsType.MYS)
            }

            viewBinding.rbIdn.setOnClickListener {
                selectOddsType(OddsType.IDN)
            }
        }
    }

    private fun initObserver(){
        viewModel.oddsType.observe(viewLifecycleOwner, {
            setOddsType(it)
        })
    }


    private fun getOddsType(){
        MultiLanguagesApplication.mInstance.getOddsType()
    }


    private fun setOddsType(oddsType: OddsType) {
        context?.let {
            when (oddsType) {
                OddsType.EU -> {
                    viewBinding.rbEu.isChecked = true
                }
                OddsType.HK -> {
                    viewBinding.rbHk.isChecked = true
                }
                OddsType.MYS -> {
                    viewBinding.rbMys.isChecked = true
                }
                OddsType.IDN -> {
                    viewBinding.rbIdn.isChecked = true
                }
            }
        }
    }


    private fun selectOddsType(oddsType: OddsType) {
        viewModel.saveOddsType(oddsType)
        dismiss()
    }


}