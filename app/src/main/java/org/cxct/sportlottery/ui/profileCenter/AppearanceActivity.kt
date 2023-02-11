package org.cxct.sportlottery.ui.profileCenter

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.android.synthetic.main.activity_appearance.*
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.main.MainViewModel
import org.cxct.sportlottery.util.setTitleLetterSpacing

/**
 * @app_destination 外觀(日間/夜間)切換
 */
class AppearanceActivity : BaseActivity<MainViewModel>(MainViewModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appearance)
        initToolbar()
        initEvent()
    }

    private fun initToolbar() {
        tv_toolbar_title.setTitleLetterSpacing()
        tv_toolbar_title.text = getString(R.string.appearance)
        btn_toolbar_back.setOnClickListener {
            finish()
        }
    }

    private fun initEvent() {
            if(MultiLanguagesApplication.isNightMode){
                rb_night_mode.isChecked = true
                rb_day_mode.isChecked = false

            }else{
                rb_night_mode.isChecked = false
                rb_day_mode.isChecked = true
            }


            rb_day_mode?.setOnClickListener {
                MultiLanguagesApplication.saveNightMode(false)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            rb_night_mode?.setOnClickListener {
                MultiLanguagesApplication.saveNightMode(true)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
    }



}