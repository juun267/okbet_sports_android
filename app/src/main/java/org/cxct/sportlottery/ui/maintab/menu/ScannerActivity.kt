package org.cxct.sportlottery.ui.maintab.menu

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.gyf.immersionbar.ImmersionBar
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ActivityScannerBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.view.dialog.ScanErrorDialog

class ScannerActivity : BaseActivity<MainViewModel,ActivityScannerBinding>(MainViewModel::class) {

    private lateinit var codeScanner: CodeScanner
    private var animator: ValueAnimator? = null

    private val scannerView by lazy {
        binding.scannerView
    }
    private val ivScanFrame by lazy {
        binding.ivScan
    }

    override fun onInitView() {
        ImmersionBar.with(this).statusBarDarkFont(false).transparentStatusBar()
            .fitsSystemWindows(false).init()
        initCodeScanner()
        binding.ivClose.setOnClickListener {
            finish()
        }
    }

    private fun initCodeScanner() {
        codeScanner = CodeScanner(this, scannerView).apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.TWO_DIMENSIONAL_FORMATS
            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isFlashEnabled = false
            decodeCallback = DecodeCallback {
                runOnUiThread {
                    stopScanAnim()
                    val newUrl =
                        Constants.getPrintReceiptScan(it.text.toString())
                    if (newUrl.isNotEmpty()) {
                        JumpUtil.toInternalWeb(this@ScannerActivity, href = newUrl,getString(R.string.N890))
                        finish()
                    } else {
                        val errorDialog = ScanErrorDialog(this@ScannerActivity)
                        errorDialog.onIvCloseClickListener = {
                            startPreview()
                            startScanAnim()
                        }
                        errorDialog.show()
                    }
                }
            }
            errorCallback = ErrorCallback {
                runOnUiThread {
//                    Toast.makeText(
//                        this@ScannerActivity,
//                        "Camera initialization error: ${it.message}",
//                        Toast.LENGTH_LONG
//                    ).show()
                }
            }
        }
    }

    private fun startScanAnim() {
        ivScanFrame.visible()
        ivScanFrame.post {
            //frameSize取值范围为0.0 - 1.0 ， 占用父控件的大小比例
            val height = (scannerView.frameSize * scannerView.width) / 2
//            Timber.d(
//                "scannerView.frameSize:${scannerView.frameSize} " + "scannerView.width:${scannerView.width} height:${height}"
//            )
            animator = ObjectAnimator.ofFloat(
                ivScanFrame, "translationY", -height, height
            ).apply {
                interpolator = AccelerateDecelerateInterpolator()
                repeatMode = ValueAnimator.REVERSE
                duration = 2000
                repeatCount = ValueAnimator.INFINITE
                start()
            }

        }
    }

    private fun stopScanAnim() {
        ivScanFrame.gone()
        animator?.cancel()
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
        ivScanFrame.postDelayed({
            startScanAnim()
        }, 500)
    }


    override fun onPause() {
        super.onPause()
        codeScanner.releaseResources()
        stopScanAnim()
    }
}