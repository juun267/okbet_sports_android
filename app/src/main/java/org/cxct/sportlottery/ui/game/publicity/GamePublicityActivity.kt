package org.cxct.sportlottery.ui.game.publicity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import org.cxct.sportlottery.databinding.ActivityGamePublicityBinding
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.language.SwitchLanguageActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.util.LanguageManager

class GamePublicityActivity : BaseSocketActivity<GameViewModel>(GameViewModel::class), View.OnClickListener {
    private lateinit var binding: ActivityGamePublicityBinding

    companion object {
        fun reStart(context: Context) {
            val intent = Intent(context, GamePublicityActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGamePublicityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initObservers()
    }

    private fun initViews() {
        initToolBar()
        initOnClickListener()
    }

    private fun initToolBar() {
        with(binding) {
            publicityToolbar.ivLanguage.setImageResource(LanguageManager.getLanguageFlag(this@GamePublicityActivity))
            publicityToolbar.tvLanguage.text = LanguageManager.getLanguageStringResource(this@GamePublicityActivity)
        }
    }

    private fun initOnClickListener() {
        binding.tvRegister.setOnClickListener(this)
        binding.tvLogin.setOnClickListener(this)
        binding.publicityToolbar.blockLanguage.setOnClickListener(this)
    }

    private fun initObservers() {
        viewModel.isLogin.observe(this) {
            if (it) {
                startActivity(Intent(this, GameActivity::class.java))
                finish()
            }
        }
    }

    override fun onClick(v: View?) {
        avoidFastDoubleClick()
        with(binding) {
            when (v) {
                tvRegister -> {
                    goRegisterPage()
                }
                tvLogin -> {
                    goLoginPage()
                }
                publicityToolbar.blockLanguage -> {
                    goSwitchLanguagePage()
                }
            }
        }
    }

    private fun goRegisterPage() {
        startActivity(Intent(this@GamePublicityActivity, RegisterActivity::class.java))
    }

    private fun goLoginPage() {
        startActivity(Intent(this@GamePublicityActivity, LoginActivity::class.java))
    }

    private fun goSwitchLanguagePage() {
        startActivity(Intent(this@GamePublicityActivity, SwitchLanguageActivity::class.java))
    }
}