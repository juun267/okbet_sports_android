package org.cxct.sportlottery.ui.menu


import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_change_appearance.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.game.menu.LeftMenuFragment
import org.cxct.sportlottery.ui.main.MainViewModel

class ChangeAppearanceDialog : BaseDialog<MainViewModel>(MainViewModel::class) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_change_appearance, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setWindowAnimations(R.style.LeftMenu)
        initEvent(view)
        initObserver()
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

            rb_day_mode?.setOnClickListener {
                //TODO Cheryl:添加日間模式
            }

            rb_night_mode?.setOnClickListener {
                //TODO Cheryl:添加夜間模式
            }
        }
    }

    private fun initObserver(){
    }



}