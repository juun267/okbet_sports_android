package org.cxct.sportlottery.ui.menu


import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_change_odd_type_full_screen.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.game.menu.LeftMenuFragment
import org.cxct.sportlottery.ui.main.MainViewModel

class ChangeOddsTypeFullScreenDialog : BaseDialog<MainViewModel>(MainViewModel::class) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_change_odd_type_full_screen, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setWindowAnimations(R.style.LeftMenu)
        initEvent(view)
        initObserver()
        getOddsType()
    }


    private fun initEvent(rootView: View?) {
        rootView?.apply {

            img_back?.setOnClickListener {
                dismiss()
            }

            img_close?.setOnClickListener {
                parentFragmentManager.findFragmentByTag(LeftMenuFragment::class.java.simpleName)?.let {
                    (it as DialogFragment).dismiss()
                }
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
    }


}