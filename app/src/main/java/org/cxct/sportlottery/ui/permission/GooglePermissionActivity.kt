package org.cxct.sportlottery.ui.permission

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_google_permission.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.setTextWithStrokeWidth

class GooglePermissionActivity : BaseActivity<GooglePermissionViewModel>(GooglePermissionViewModel::class) {

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) finish()
            else {
                /**
                 * Android 11 (API 30) 以上版本，只會跳出權限彈窗兩次，
                 * 若要獲取權限須由使用者去設定自行開啟。
                 */
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    showPromptDialog(getString(R.string.prompt), getString(R.string.grant_location_permission_on_setting)) {
                        openPermissionSettings()
                    }
                } else {
                    //do nothing for android sdk version under 30
                }
            }
        }

    override fun onResume() {
        super.onResume()
        checkPermissionGiven()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_permission)

        initBtn()
    }

    override fun onBackPressed() {
        //do not go back to SplashActivity
    }

    override fun shouldShowRequestPermissionRationale(permission: String): Boolean {
        return true
    }

    private fun openPermissionSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + this.packageName)
        )
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        this.startActivity(intent)
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
        btn_allow_permission.setTextWithStrokeWidth(getString(R.string.allow_location_permission), 0.7f)
    }
}