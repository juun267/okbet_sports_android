package org.cxct.sportlottery.ui.game.language

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
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


class SwitchLanguageActivity : BaseActivity<LoginViewModel>(LoginViewModel::class), View.OnClickListener {

    companion object {
        const val FROM_ACTIVITY = "fromActivity"
    }

    private lateinit var binding: ActivitySwitchLanguageBinding

    override fun onClick(v: View?) {
        when (v) {
            binding.ivBack -> {
                onBackPressed()
            }
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
            binding.ivLogo ->{
                if (sConfigData?.thirdOpen == FLAG_OPEN)
                    MainActivity.reStart(this)
                else
                    goGamePublicityPage()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySwitchLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView(){
        binding.ivBack.setOnClickListener(this)
        binding.llEnglish.setOnClickListener(this)
        binding.llChina.setOnClickListener(this)
        binding.llVietnam.setOnClickListener(this)
        binding.ivLogo.setOnClickListener(this)
        when (LanguageManager.getSelectLanguage(applicationContext)) {
            LanguageManager.Language.ZH -> {
                binding.tvChina.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorBlue))
            }
            LanguageManager.Language.EN -> {
                binding.tvEnglish.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorBlue))
            }
            LanguageManager.Language.VI -> {
                binding.tvVietnam.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorBlue))
            }
        }
    }

    private fun selectLanguage(select: LanguageManager.Language) {
        if(SPUtil.getInstance(applicationContext).getSelectLanguage() != select.key){
            this?.run {
                LanguageManager.saveSelectLanguage(this, select)
                if (sConfigData?.thirdOpen == FLAG_OPEN)
                    MainActivity.reStart(this)
                else {
                    when (intent.getSerializableExtra(FROM_ACTIVITY)) {
                        Page.PUBLICITY -> goGamePublicityPage()
                        else -> GamePublicityActivity.reStart(this)
                    }
                }
            }
        }
    }

    private fun goGamePublicityPage() {
        GamePublicityActivity.reStart(this)
    }

}
