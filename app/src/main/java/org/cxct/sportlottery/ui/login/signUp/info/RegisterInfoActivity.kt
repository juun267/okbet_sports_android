package org.cxct.sportlottery.ui.login.signUp.info

import android.os.Bundle
import org.cxct.sportlottery.databinding.ActivityRegisterInfoBinding
import org.cxct.sportlottery.ui.base.BaseActivity

/**
 * 注册补充用户信息
 */
class RegisterInfoActivity:BaseActivity<RegisterInfoViewModel>(RegisterInfoViewModel::class) {

    private lateinit var binding: ActivityRegisterInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityRegisterInfoBinding.inflate(layoutInflater)
        initData()
    }

    private  fun initData(){
        val temp=intent.getSerializableExtra("data")
        val a=""
    }
}