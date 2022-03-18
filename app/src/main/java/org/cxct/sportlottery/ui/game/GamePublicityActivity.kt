package org.cxct.sportlottery.ui.game

import android.content.Intent
import android.os.Bundle
import android.view.View
import org.cxct.sportlottery.databinding.ActivityGamePublicityBinding
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity

class GamePublicityActivity : BaseSocketActivity<GameViewModel>(GameViewModel::class), View.OnClickListener {
    private lateinit var binding: ActivityGamePublicityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGamePublicityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initObservers()
    }

    private fun initViews() {
        initOnClickListener()
    }

    private fun initOnClickListener() {
        binding.tvRegister.setOnClickListener(this)
        binding.tvLogin.setOnClickListener(this)
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
                    startActivity(Intent(this@GamePublicityActivity, RegisterActivity::class.java))
                }
                tvLogin -> {
                    startActivity(Intent(this@GamePublicityActivity, LoginActivity::class.java))
                }
            }
        }
    }
}