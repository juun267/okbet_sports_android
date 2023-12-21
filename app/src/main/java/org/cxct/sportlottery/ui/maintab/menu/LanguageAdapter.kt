package org.cxct.sportlottery.ui.maintab.menu

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemLanguageBinding
import org.cxct.sportlottery.util.LanguageManager

class LanguageAdapter(data: List<LanguageManager.Language>) : BindingAdapter<LanguageManager.Language, ItemLanguageBinding>() {

    init {
        setList(data)
    }

    private val selectedLanguage by lazy { LanguageManager.getSelectLanguage(context) }

    override fun onBinding(
        position: Int,
        binding: ItemLanguageBinding,
        item: LanguageManager.Language,
    ) =binding.run {
        binding.root.isSelected = selectedLanguage == item
        when (item) {
            LanguageManager.Language.ZH -> {
                ivLogo.setImageResource(R.drawable.ic_flag_cn)
                tvName.setText(R.string.language_cn)
            }
            LanguageManager.Language.VI -> {
                ivLogo.setImageResource(R.drawable.ic_flag_vi)
                tvName.setText(R.string.language_vi)
            }
            LanguageManager.Language.TH -> {
                ivLogo.setImageResource(R.drawable.ic_flag_th)
                tvName.setText(R.string.language_th)
            }
            LanguageManager.Language.PHI ->{
                ivLogo.setImageResource(R.drawable.ic_flag_phi)
                tvName.setText(R.string.language_phi)
            }
            else -> {
                ivLogo.setImageResource(R.drawable.ic_flag_en)
                tvName.setText(R.string.language_en)
            }

        }
    }
}