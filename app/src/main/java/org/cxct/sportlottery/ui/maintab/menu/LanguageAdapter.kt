package org.cxct.sportlottery.ui.maintab.menu

import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.LanguageManager

class LanguageAdapter(data: List<LanguageManager.Language>) :
    BaseQuickAdapter<LanguageManager.Language, BaseViewHolder>(R.layout.item_language, data.toMutableList()) {

    private val selectedLanguage by lazy { LanguageManager.getSelectLanguage(context) }

    override fun convert(helper: BaseViewHolder, item: LanguageManager.Language) {
        val isSelected = selectedLanguage == item
        helper.getView<ImageView>(R.id.iv_select).isSelected = isSelected
        if (isSelected) {
            helper.setTextColorRes(R.id.tv_name, R.color.color_025BE8)
        } else {
            helper.setTextColorRes(R.id.tv_name, R.color.color_FFFFFF_414655)
        }

        when (item) {
            LanguageManager.Language.ZH -> {
                helper.setImageResource(R.id.iv_logo, R.drawable.ic_flag_cn)
                helper.setText(R.id.tv_name, R.string.language_cn)
            }
            LanguageManager.Language.VI -> {
                helper.setImageResource(R.id.iv_logo, R.drawable.ic_flag_vi)
                helper.setText(R.id.tv_name, R.string.language_vi)
            }
            LanguageManager.Language.TH -> {
                helper.setImageResource(R.id.iv_logo, R.drawable.ic_flag_th)
                helper.setText(R.id.tv_name, R.string.language_th)
            }
            LanguageManager.Language.PHI ->{
                helper.setImageResource(R.id.iv_logo,R.drawable.ic_flag_phi)
                    .setText(R.id.tv_name,R.string.language_phi)
            }
            else -> {
                helper.setImageResource(R.id.iv_logo, R.drawable.ic_flag_en)
                helper.setText(R.id.tv_name, R.string.language_en)
            }

        }

    }
}