package org.cxct.sportlottery.ui.profileCenter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.iv_head
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sLoginData

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
    }

    private fun initView() {
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