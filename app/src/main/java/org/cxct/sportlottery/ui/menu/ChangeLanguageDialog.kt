package org.cxct.sportlottery.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_change_language.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.util.LanguageManager

class ChangeLanguageDialog(private val clearBetListListener: ClearBetListListener) : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_change_language, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initEvent(view)
    }

    private fun initEvent(rootView: View?) {
        rootView?.apply {
            btn_close?.setOnClickListener {
                dismiss()
            }

            btn_chinese?.setOnClickListener {
                clearBetListListener.onClick()
                selectLanguage(LanguageManager.Language.ZH)
            }

            btn_english?.setOnClickListener {
                clearBetListListener.onClick()
                selectLanguage(LanguageManager.Language.EN)
            }

            btn_vietnamese?.setOnClickListener {
                clearBetListListener.onClick()
                selectLanguage(LanguageManager.Language.VI)
            }
        }
    }

    private fun selectLanguage(select: LanguageManager.Language) {
        activity?.run {
            LanguageManager.saveSelectLanguage(this, select)
//            if (sConfigData?.thirdOpen == FLAG_OPEN)
//                MainActivity.reStart(this)
//            else
                GamePublicityActivity.reStart(this)
        }
    }

    class ClearBetListListener(private val clearBetListListener: () -> Unit) {
        fun onClick() = clearBetListListener()
    }

}