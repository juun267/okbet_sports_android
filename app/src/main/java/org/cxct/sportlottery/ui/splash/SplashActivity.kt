package org.cxct.sportlottery.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_splash.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.home.MainActivity

class SplashActivity : BaseActivity<SplashViewModel>(SplashViewModel::class) {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        setupVersion()

        //TODO simon test review 之後流程會是 getHost() checkAppUpdate() getConfig()
        Handler().postDelayed({
            getAppConfig()
        }, 4000)

        //TODO simon test 之後放正式圖片就可以刪掉了
        iv_bg.scaleType = ImageView.ScaleType.FIT_CENTER
        Glide.with(this).asGif().load(R.mipmap.bg_test).into(iv_bg)
    }

    private fun setupVersion() {
        val version = BuildConfig.VERSION_CODE.toString() + "_" + BuildConfig.VERSION_NAME
        tv_version_info.text = version
    }

    private fun getAppConfig() {
        viewModel.configResult.observe(this, Observer {
            if (it?.success == true)
                goHomePage()
            else
                showErrorRetryDialog(getString(R.string.error_config))
        })

        viewModel.getConfig()
    }

    private fun goHomePage() {
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()
    }

    //過程中任一流程請求失敗，點擊確定按鈕重試
    private fun showErrorRetryDialog(message: String) {
        val dialog = CustomAlertDialog(this)
        dialog.setMessage(message)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.setNegativeButtonText(null)
        dialog.setPositiveButtonText(getString(R.string.btn_retry))
        dialog.setPositiveClickListener(View.OnClickListener {
            getAppConfig() //TODO simon test review getHost 流程
            dialog.dismiss()
        })
        dialog.show()
    }

}
