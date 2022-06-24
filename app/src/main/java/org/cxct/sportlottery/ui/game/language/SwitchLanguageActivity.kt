package org.cxct.sportlottery.ui.game.language

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_switch_language.view.*
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivitySwitchLanguageBinding
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.Page
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.login.signIn.LoginViewModel
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.SPUtil
import org.cxct.sportlottery.util.setTitleLetterSpacing
import java.security.AccessController.getContext


class SwitchLanguageActivity : BaseActivity<LoginViewModel>(LoginViewModel::class), View.OnClickListener {

    companion object {
        const val FROM_ACTIVITY = "fromActivity"
    }

    private lateinit var binding: ActivitySwitchLanguageBinding

    override fun onClick(v: View?) {
        when (v) {
            binding.llChina -> {
                viewModel.betInfoRepository.clear()
                selectLanguage(LanguageManager.Language.ZH)
            }
            binding.llEnglish -> {
                viewModel.betInfoRepository.clear()
                selectLanguage(LanguageManager.Language.EN)
            }
            binding.llVietnam -> {
                viewModel.betInfoRepository.clear()
                selectLanguage(LanguageManager.Language.VI)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySwitchLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar();
        initView()
    }
    private fun initToolbar() {
        tv_toolbar_title.setTitleLetterSpacing()
        tv_toolbar_title.text = getString(R.string.language_setting)
        btn_toolbar_back.setOnClickListener {
           finish()
        }
    }
    private fun initView(){
        val lngeList = sConfigData?.supportLanguage?.split(",")

        binding.llLanguageList.removeAllViews()

        lngeList?.forEachIndexed { index, value ->
            when (value) {
                LanguageManager.Language.EN.key -> binding.llLanguageList.addView(binding.llEnglish)
                LanguageManager.Language.VI.key -> binding.llLanguageList.addView(binding.llVietnam)
                else -> binding.llLanguageList.addView(binding.llChina)
            }
            when (index) {
                1 -> binding.llLanguageList.addView(binding.line2)
                2 -> binding.llLanguageList.addView(binding.line3)
                else -> binding.llLanguageList.addView(binding.line1)
            }
        }

        binding.llEnglish.setOnClickListener(this)
        binding.llChina.setOnClickListener(this)
        binding.llVietnam.setOnClickListener(this)
        when (LanguageManager.getSelectLanguage(applicationContext)) {
            LanguageManager.Language.ZH -> {
                binding.tvChina.setTextColor(ContextCompat.getColor(applicationContext, R.color.color_317FFF_0760D4))
            }
            LanguageManager.Language.EN -> {
                binding.tvEnglish.setTextColor(ContextCompat.getColor(applicationContext, R.color.color_317FFF_0760D4))
            }
            LanguageManager.Language.VI -> {
                binding.tvVietnam.setTextColor(ContextCompat.getColor(applicationContext, R.color.color_317FFF_0760D4))
            }
        }
    }

    private fun selectLanguage(select: LanguageManager.Language) {
        if(SPUtil.getInstance(applicationContext).getSelectLanguage() != select.key){
            this?.run {
                LanguageManager.saveSelectLanguage(this, select)
//                if (sConfigData?.thirdOpen == FLAG_OPEN)
//                    MainActivity.reStart(this)
//                else {
                    when (intent.getSerializableExtra(FROM_ACTIVITY)) {
                        Page.PUBLICITY -> goGamePublicityPage()
                        else -> GamePublicityActivity.reStart(this)
                    }
//                }
            }
        }
    }

    private fun goGamePublicityPage() {
        GamePublicityActivity.reStart(this)
    }

}
