package org.cxct.sportlottery.ui.vip

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_vip.*
import org.cxct.sportlottery.R

class VipActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vip)

        initView()
    }

    private fun initView() {
        bubble_level_one.isSelected = true
    }
}