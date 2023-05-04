package org.cxct.sportlottery.ui.maintab.menu

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import org.cxct.sportlottery.databinding.FragmentMainLeft2Binding
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.*

class MainLeftFragment2 : BindingFragment<MainViewModel, FragmentMainLeft2Binding>() {

    private inline fun getMainTabActivity() = activity as MainTabActivity
    private lateinit var languageAdapter: LanguageAdapter

    var fromPage = 0

    override fun onInitView(view: View) {
        initView()
    }

    override fun onBindViewStatus(view: View) {
        initLanguageView()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun initView() = binding.run {

        ivClose.setOnClickListener { getMainTabActivity().closeDrawerLayout() }
        linLanguage.setOnClickListener {
            var isSelected = !linLanguage.isSelected
            linLanguage.isSelected = isSelected
            rvLanguage.isVisible = isSelected
        }

    }

    private fun initLanguageView() {
        languageAdapter = LanguageAdapter(LanguageManager.makeUseLanguage())
        languageAdapter.setOnItemClickListener { adapter, _, position ->
            viewModel.betInfoRepository.clear()
            selectLanguage(adapter.getItem(position) as LanguageManager.Language)
        }
        binding.rvLanguage.layoutManager = GridLayoutManager(context, 2)
        binding.rvLanguage.adapter = languageAdapter
    }

    private fun selectLanguage(select: LanguageManager.Language) {
        if (LanguageManager.getSelectLanguageName() != select.key) {
            context?.let {
                LanguageManager.saveSelectLanguage(it, select)
                MainTabActivity.reStart(it)
            }
        }
    }




}