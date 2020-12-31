package org.cxct.sportlottery.ui.menu

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_change_language.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.home.MainActivity
import org.cxct.sportlottery.util.LanguageManager

class ChangeLanguageDialog : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_change_language, null)
        dialog.setContentView(view)

        initEvent(view)
        return dialog
    }

    private fun initEvent(rootView: View?) {
        rootView?.apply {
            btn_chinese?.setOnClickListener {
                selectLanguage(LanguageManager.Language.ZH)
            }

            btn_english?.setOnClickListener {
                selectLanguage(LanguageManager.Language.EN)
            }
        }
    }

    private fun selectLanguage(select: LanguageManager.Language) {
        activity?.run {
            LanguageManager.saveSelectLanguage(this, select)
            MainActivity.reStart(this)
        }
    }

}