package org.cxct.sportlottery.ui.permission

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_google_permission.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.main.MainActivity

class GooglePermissionActivity : AppCompatActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                if (sConfigData?.thirdOpen == FLAG_OPEN)
                    MainActivity.reStart(this)
                else
                    GameActivity.reStart(this)
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_permission)

        checkPermissionGiven()
        initBtn()
    }

    override fun onBackPressed() {
        //do not go back to SplashActivity
    }

    private fun checkPermissionGiven() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            //permission granted
            finish()
        }
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
    }

    private fun initBtn() {
        btn_allow_permission.setOnClickListener {
            requestLocationPermission()
        }
    }
}