package org.cxct.sportlottery.ui.profileCenter.identity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.lifecycleScope
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivityTakeidPhotoBinding
import org.cxct.sportlottery.net.user.data.OCRInfo
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.util.MD5Util
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import org.cxct.sportlottery.view.PictureSelectUtil
import org.cxct.sportlottery.view.camera.BitmapUtil
import org.cxct.sportlottery.view.isVisible
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.ArrayList
import kotlin.math.abs

class TakeIDPhotoActivity: BindingActivity<ProfileCenterViewModel, ActivityTakeidPhotoBinding>()
    , SensorEventListener {

    companion object {

        fun start(context: Context, idType: Int, idTypeName: String) {
            val intent = Intent(context, TakeIDPhotoActivity::class.java)
            intent.putExtra("idType", idType)
            intent.putExtra("idTypeName", idTypeName)
            context.startActivity(intent)
        }
    }

    private val maxSize = 200
    private var mSensorManager: SensorManager? = null
    private var mDefaultSensor: Sensor? = null
    private var mSensorRotation = 0
    private val cropDrawable = DrawableCreatorUtils.getCommonBackgroundStyle(16, R.color.transparent, R.color.white)
    private var photoFile: File? = null
    private val idType by lazy { intent.getIntExtra("idType", 0) }
    private val idTypeName by lazy { intent.getStringExtra("idTypeName")!! }
    private var ocrInfo: OCRInfo? = null

    override fun onInitView() = binding.run {
        setStatusbar()
        initSensor()
        initObserver()

        toolBar.tvToolbarTitle.setText(R.string.P256)
        ivCameraCrop.setImageDrawable(cropDrawable)
        ivCameraTake.setOnClickListener { takePhoto() }
        btnUpload.setOnClickListener { uploadPhoto() }
        toolBar.btnToolbarBack.setOnClickListener {
            if (binding.frLoading.isVisible() || !btnUpload.isVisible()) {
                finish()
            } else {
                enableCameraPreview()
            }
        }
        ivChooseImage.setOnClickListener {
            PictureSelectUtil.pictureSelect(this@TakeIDPhotoActivity, object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>?) {
                    result?.first()?.compressPath?.let {
                        photoFile = File(it)
                        uploadPhoto()
                    }
                }

                override fun onCancel() {

                }
            })
        }

    }

    private fun initObserver() {
        viewModel.ocrResult.observe(this) {
            hideLoading()
            val url = it.second
            ocrInfo = it.third

            if (url.isEmptyStr()) {
                toast(getString(R.string.chat_connection_error))
                return@observe
            }

            if (idType != 1) {
                toEditInfo(url)
                return@observe
            }

            if (it.first && it.third != null) {
                toEditInfo(url)
            } else {
                OCRFailedDialog(this).show()
            }
        }
    }

    private fun toEditInfo(imgUrl: String) {
        KYCFormActivity.start(this, idType, idTypeName, imgUrl, ocrInfo)
        finish()
    }

    private fun initSensor() {
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mDefaultSensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onResume() {
        super.onResume()
        if (mDefaultSensor != null) {
            mSensorManager?.registerListener(this, mDefaultSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
//        Log.e("For Test", "======>>> TakeIDPhotoActivity 1111")
//        if (binding.cameraPreview.tag != null && binding.cameraPreview.isVisible()) {
//            Log.e("For Test", "======>>> TakeIDPhotoActivity 2222")
//            binding.cameraPreview.startPreview()
//        }
//        binding.cameraPreview.tag = Any()
    }

    override fun onPause() {
        super.onPause()
        mSensorManager?.unregisterListener(this)
    }

    private fun takePhoto() {
        binding.frLoading.visible()
        binding.cameraPreview.takePhoto{ bytes, camera ->
            val size = camera.parameters.previewSize //获取预览大小
            lifecycleScope.launch(Dispatchers.IO) {
                val w = size.width
                val h = size.height
                val rightRotationBitmap: Bitmap = BitmapUtil.setRightRotationBitmap(this@TakeIDPhotoActivity, mSensorRotation, bytes, w, h)
                val cropBitmap: Bitmap = cropImageForVertical(rightRotationBitmap)
                /*保存自动裁剪的图片后，直接返回*/
                photoFile = saveToLocal(cropBitmap)
                if (photoFile == null) {
                    launch(Dispatchers.Main) {
                        binding.frLoading.gone()
                        toast(getString(R.string.unknown_error))
                    }
                    return@launch
                }

                if (photoFile!!.length() < 1024 * maxSize) {
                    launch(Dispatchers.Main) { enablePhotoPreview(cropBitmap) }
                    return@launch
                }

                compressFileSize(photoFile!!, cropBitmap)
            }
        }
    }

    private fun enableCameraPreview() = binding.run {
        ivCameraCrop.setImageDrawable(cropDrawable)
        cameraPreview.visible()
        ivChooseImage.visible()
        ivCameraTake.visible()
        btnUpload.gone()
        binding.frLoading.gone()
        cameraPreview.startPreview()
    }

    private fun enablePhotoPreview(photo: Bitmap) = binding.run {
        ivCameraCrop.setImageBitmap(photo)
        cameraPreview.isEnabled = false
        cameraPreview.gone()
        ivChooseImage.gone()
        ivCameraTake.gone()
        btnUpload.visible()
        binding.frLoading.gone()
    }

    private fun uploadPhoto() {
        if (photoFile == null) {
            enableCameraPreview()
            toast(getString(R.string.unknown_error))
            return
        }

        loading()
        viewModel.startOCR(photoFile!!, idType)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { mSensorRotation = calculateSensorRotation(it.values[0], it.values[1]) }
    }

    private fun calculateSensorRotation(x: Float, y: Float): Int {
        //x是values[0]的值，X轴方向加速度，从左侧向右侧移动，values[0]为负值；从右向左移动，values[0]为正值
        //y是values[1]的值，Y轴方向加速度，从上到下移动，values[1]为负值；从下往上移动，values[1]为正值
        //不考虑Z轴上的数据，
        if (abs(x) > 6 && abs(y) < 4) {
            return if (x > 6) { 270 } else { 90 }
        } else if (abs(y) > 6 && abs(x) < 4) {
            return if (y > 6) { 0 } else { 180 }
        }
        return -1
    }

    private fun cropImageForVertical(bitmap: Bitmap): Bitmap {
        /*计算裁剪区域的的坐标点 相对屏幕*/
        //暂时考虑padding和margin
        val left: Float = binding.ivCameraCrop.left.toFloat()
        val top: Float = binding.ivCameraCrop.top.toFloat()
        val right: Float = binding.ivCameraCrop.right.toFloat()
        //底部坐标=top+自身高度
        val bottom: Float = binding.ivCameraCrop.height + top

        /*计算裁剪框坐标点占原图坐标点的比例*/
        val previewWidth: Int = binding.cameraPreview.width
        val previewHeight: Int = binding.cameraPreview.height
        val leftProportion = left / previewWidth
        val topProportion = top / previewHeight
        val rightProportion = right / previewWidth
        val bottomProportion = bottom / previewHeight

        /*自动裁剪*/
        return Bitmap.createBitmap(
            bitmap,
            (leftProportion * bitmap.width.toFloat()).toInt(),
            (topProportion * bitmap.height.toFloat()).toInt(),
            ((rightProportion - leftProportion) * bitmap.width.toFloat()).toInt(),
            ((bottomProportion - topProportion) * bitmap.height.toFloat()).toInt()
        )
    }

    private fun saveToLocal(bitmap: Bitmap): File? {
        val file = File(externalCacheDir!!.absolutePath + File.separator + MD5Util.MD5Encode("${System.currentTimeMillis()}"))
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        var os: OutputStream? = null
        var ret = false
        try {
            os = BufferedOutputStream(FileOutputStream(file))
            ret = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            os?.safeClose()
        }
        return if (ret) file else null
    }

    private fun compressFileSize(file: File, bitmap: Bitmap) {
        Luban.with(this)
            .ignoreBy(maxSize)
            .load(file)
            .setCompressListener(object : OnNewCompressListener {
                override fun onStart() {

                }

                override fun onSuccess(source: String, compressFile: File) {
                    photoFile = compressFile
                    runWithCatch { File(source).delete() }
                    enablePhotoPreview(bitmap)
                }

                override fun onError(source: String?, e: Throwable?) {
                    binding.frLoading.gone()
                    toast(getString(R.string.unknown_error))
                }

            }).launch()

    }

}