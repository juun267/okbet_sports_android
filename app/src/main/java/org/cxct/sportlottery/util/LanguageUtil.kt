package org.cxct.sportlottery.util

import com.luck.picture.lib.language.LanguageConfig
import org.cxct.sportlottery.application.MultiLanguagesApplication

object LanguageUtil {

     fun getLanguage(): Int {
        return when (LanguageManager.getSelectLanguage(MultiLanguagesApplication.appContext)) {
            LanguageManager.Language.ZH -> LanguageConfig.CHINESE
            LanguageManager.Language.ZHT -> LanguageConfig.TRADITIONAL_CHINESE
            LanguageManager.Language.EN -> LanguageConfig.ENGLISH
            LanguageManager.Language.VI -> LanguageConfig.VIETNAM
            LanguageManager.Language.TH -> LanguageConfig.ENGLISH // 套件無支援
            else ->{
                LanguageConfig.ENGLISH
            }

        }
    }
}