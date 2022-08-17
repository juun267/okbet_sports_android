package org.cxct.sportlottery.ui.maintab

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.LanguageManager

class LanguageAdapter(data: List<LanguageManager.Language>?) :
    BaseQuickAdapter<LanguageManager.Language, BaseViewHolder>(R.layout.item_language, data) {
    override fun convert(helper: BaseViewHolder, item: LanguageManager.Language) {
        when (item) {
            LanguageManager.Language.ZH -> {
                helper.setImageResource(R.id.iv_logo, R.drawable.ic_flag_cn)
                helper.setText(R.id.tv_name, R.string.language_cn)
            }
            LanguageManager.Language.VI -> {
                helper.setImageResource(R.id.iv_logo, R.drawable.ic_flag_vi)
                helper.setText(R.id.tv_name, R.string.language_vi)
            }
            else -> {
                helper.setImageResource(R.id.iv_logo, R.drawable.ic_flag_en)
                helper.setText(R.id.tv_name, R.string.language_en)
            }
        }

    }
}