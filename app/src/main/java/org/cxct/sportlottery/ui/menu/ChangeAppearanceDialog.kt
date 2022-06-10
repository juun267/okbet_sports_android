package org.cxct.sportlottery.ui.menu

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_change_appearance.view.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.game.menu.LeftMenuFragment
import org.cxct.sportlottery.ui.main.MainViewModel

/**
 * @app_destination 外觀(日間/夜間)切換
 */
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
            if(MultiLanguagesApplication.isNightMode){
                rb_night_mode.isChecked = true
                rb_day_mode.isChecked = false
                tv_title.text =  "${getString(R.string.appearance)}${"："}${getString(R.string.night_mode)}"

            }else{
                rb_night_mode.isChecked = false
                rb_day_mode.isChecked = true
                tv_title.text =  "${getString(R.string.appearance)}${"："}${getString(R.string.day_mode)}"

            }

            img_back?.setOnClickListener {
                parentFragmentManager.popBackStack()
            }

            img_close?.setOnClickListener {
                parentFragmentManager.findFragmentByTag(LeftMenuFragment::class.java.simpleName)?.let {
                    (it as DialogFragment).dismiss()
                }
                parentFragmentManager.popBackStack()
            }



            rb_day_mode?.setOnClickListener {
                MultiLanguagesApplication.saveNightMode(false)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                dismiss()
            }

            rb_night_mode?.setOnClickListener {
                MultiLanguagesApplication.saveNightMode(true)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                dismiss()
            }
        }
    }

    private fun initObserver(){
    }



}