package org.cxct.sportlottery.ui.profileCenter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_profile_center.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity

class ProfileCenterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_center)

        btn_profile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}