package org.cxct.sportlottery.ui.profileCenter

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_profile.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sLoginData
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity
import org.cxct.sportlottery.ui.profileCenter.nickname.ChangeNicknameActivity

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initButton()
        initView()
    }

    private fun initButton() {
        btn_back.setOnClickListener {
            finish()
        }

        btn_nickname.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, ChangeNicknameActivity::class.java))
        }

        btn_pwd_setting.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, SettingPasswordActivity::class.java))
        }
    }

    private fun initView() {
        //TODO 使用 userInfo API 來刷新畫面
        updateAvatar()
        tv_nickname.text = sLoginData?.nickName
        tv_member_account.text = sLoginData?.userName
        tv_id.text = sLoginData?.userId?.toString()
        tv_real_name.text = sLoginData?.fullName
    }

    private fun updateAvatar() {
        Glide.with(this)
            .load(sLoginData?.iconUrl)
            .apply(RequestOptions().placeholder(R.drawable.ic_head))
            .into(iv_head) //載入頭像
    }
}