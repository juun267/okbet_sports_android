package org.cxct.sportlottery.ui.game.publicity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.databinding.ActivityGamePublicityBinding
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.language.SwitchLanguageActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.util.LanguageManager

class GamePublicityActivity : BaseSocketActivity<GamePublicityViewModel>(GamePublicityViewModel::class),
    View.OnClickListener {
    private lateinit var binding: ActivityGamePublicityBinding

    companion object {
        fun reStart(context: Context) {
            val intent = Intent(context, GamePublicityActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    private val mPublicityAdapter = GamePublicityAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGamePublicityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initObservers()
        queryData()
    }

    private fun initViews() {
        initToolBar()
        initOnClickListener()
        initRecommendView()
        initTitle()
        initBottomView()
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

    private fun initRecommendView() {
        with(binding.rvPublicity) {
            layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = mPublicityAdapter
            itemAnimator = null
        }
    }

    private fun initTitle() {
        with(mPublicityAdapter) {
            addTitle()
            addSubTitle()
        }
    }

    private fun initBottomView() {
        mPublicityAdapter.addBottomView()
    }

    private fun initObservers() {
        viewModel.isLogin.observe(this) {
            if (it) {
                startActivity(Intent(this, GameActivity::class.java))
                finish()
            }
        }

        viewModel.publicityRecommend.observe(this, { event ->
            event?.getContentIfNotHandled()?.let { result ->
                mPublicityAdapter.addRecommend(result.recommendList)
            }
        })
    }

    private fun queryData() {
        viewModel.getRecommend()
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