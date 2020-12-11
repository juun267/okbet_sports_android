package org.cxct.sportlottery.ui.menu

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.dialog_change_language.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.MainActivity
import org.cxct.sportlottery.util.LanguageManager

/**
 * TODO 語言切換 sample，之後再調整
 */
class ChangeLanguageDialog(context: Context) : AlertDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_change_language)

        initView()
        initEvent()
    }

    private fun initView() {
        when(LanguageManager.getSelectLanguage(context)) {
            LanguageManager.Language.AUTO -> rBtn_auto.isChecked = true
            LanguageManager.Language.CN -> rBtn_chinese.isChecked = true
            LanguageManager.Language.EN -> rBtn_english.isChecked = true
        }
    }

    private fun initEvent() {
        rBtn_auto.setOnClickListener {
            selectLanguage(LanguageManager.Language.AUTO)
        }

        rBtn_chinese.setOnClickListener {
            selectLanguage(LanguageManager.Language.CN)
        }

        rBtn_english.setOnClickListener {
            selectLanguage(LanguageManager.Language.EN)
        }
    }


    private fun selectLanguage(select: LanguageManager.Language) {
        LanguageManager.saveSelectLanguage(context, select)
        MainActivity.reStart(context)
    }



}