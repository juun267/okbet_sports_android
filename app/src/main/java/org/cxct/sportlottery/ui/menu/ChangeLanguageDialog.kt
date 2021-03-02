package org.cxct.sportlottery.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_change_language.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.util.LanguageManager

class ChangeLanguageDialog : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_change_language, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initEvent(view)
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