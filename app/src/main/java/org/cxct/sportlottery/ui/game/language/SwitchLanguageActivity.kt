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
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.login.signIn.LoginViewModel
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.util.*


class SwitchLanguageActivity : BaseActivity<LoginViewModel>(LoginViewModel::class), View.OnClickListener {

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
                if (sConfigData?.thirdOpen == FLAG_OPEN){
                    MainActivity.reStart(this)
                }
                else{
                    GameActivity.reStart(this)
//                    viewModel.goGameHome()
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySwitchLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        initObservers()
    }

    private fun initView(){
        binding.ivBack.setOnClickListener(this)
        binding.llEnglish.setOnClickListener(this)
        binding.llChina.setOnClickListener(this)
        binding.llVietnam.setOnClickListener(this)
        binding.ivLogo.setOnClickListener(this)
        when (SPUtil.getInstance(this).getSelectLanguage()) {
            LanguageManager.Language.ZH.key -> {
                binding.tvChina.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorBlue))
            }
            LanguageManager.Language.EN.key -> {
                binding.tvEnglish.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorBlue))
            }
            LanguageManager.Language.VI.key -> {
                binding.tvVietnam.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorBlue))
            }
        }
    }

    private fun initObservers() {
        /*viewModel.goGameHome.observe(this, {
            if (it?.getContentIfNotHandled() == true) {
                GameActivity.reStart(this)
            } else {
                GamePublicityActivity.reStart(this)
            }
        })*/
    }

    private fun selectLanguage(select: LanguageManager.Language) {
        if(SPUtil.getInstance(applicationContext).getSelectLanguage() != select.key){
            this?.run {
                LanguageManager.saveSelectLanguage(this, select)
                if (sConfigData?.thirdOpen == FLAG_OPEN){
                    MainActivity.reStart(this)
                }
                else {
                    GameActivity.reStart(this)
//                    viewModel.goGameHome()
                }
            }
        }
    }

}
