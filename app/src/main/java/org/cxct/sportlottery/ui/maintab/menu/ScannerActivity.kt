package org.cxct.sportlottery.ui.maintab.menu

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Interpolator
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat.RepeatMode
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.gyf.immersionbar.ImmersionBar
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ActivityScannerBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp

class ScannerActivity : BaseActivity<MainViewModel>(MainViewModel::class) {

    private lateinit var codeScanner: CodeScanner
    private lateinit var animator: ValueAnimator
    private val binding by lazy {
        ActivityScannerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val scannerView = binding.scannerView

        ImmersionBar.with(this).statusBarDarkFont(false).transparentStatusBar()
            .fitsSystemWindows(false).init()

        codeScanner = CodeScanner(this, scannerView)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = true // Whether to enable flash or not

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_LONG).show()
            }
        }

        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(
                    this, "Camera initialization error: ${it.message}", Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.ivClose.setOnClickListener {
            finish()
        }


    }

    private fun startScanAnim() {
        binding.ivScan.visible()
        animator = ObjectAnimator.ofFloat(
            binding.ivScan, "translationY", -140.dp.toFloat(), 140.dp.toFloat()
        )
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.repeatMode = ValueAnimator.REVERSE
        animator.duration = 2000
        animator.repeatCount = ValueAnimator.INFINITE
        animator.start()

    }

    override fun onResume() {
        super.onResume()

        codeScanner.startPreview()
        startScanAnim()
    }


    override fun onPause() {
        super.onPause()
        codeScanner.releaseResources()
    }
}