package org.cxct.sportlottery.ui.profileCenter.identity.liveness

import aai.liveness.CameraType
import aai.liveness.GuardianLivenessDetectionSDK
import aai.liveness.LivenessResult
import aai.liveness.activity.LivenessActivity
import aai.liveness.configs.AuditImageConfig.AuditImageConfigBuilder
import ai.advance.liveness.lib.Market
import android.content.Intent
import android.graphics.Paint
import androidx.activity.result.contract.ActivityResultContracts
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.loading
import org.cxct.sportlottery.common.extentions.showErrorPromptDialog
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.databinding.ActivityLivenessStartBinding
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityActivity
import org.cxct.sportlottery.ui.profileCenter.identity.handheld.VerifyHandheldActivity
import org.cxct.sportlottery.util.LogUtil


class LivenessStartActivity: BaseActivity<ProfileCenterViewModel, ActivityLivenessStartBinding>() {

    override fun pageName() = "KYC活体识别"

    override fun onInitView() {
        setStatusbar(darkFont = true)
        binding.toolBar.setOnBackPressListener { finish() }
        viewModel.getLivenessLicense(BuildConfig.APPLICATION_ID)
        GuardianLivenessDetectionSDK.letSDKHandleCameraPermission()
        binding.btnStart.setOnClickListener {
            val intent = Intent(this, LivenessActivity::class.java)
            startForResult.launch(intent)
        }
        binding.tvhandheldId.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.tvhandheldId.setOnClickListener {
            startActivity<VerifyHandheldActivity>()
        }
        viewModel.licenseResult.observe(this){
            if (it.succeeded()){
                it.getData()?.let {
                        it1 -> initSDK(it1)
                }
            }else{
                showErrorPromptDialog(it.msg){}
            }

        }
        viewModel.liveKycVerifyResult.observe(this){ result->
            hideLoading()
            if (result.success){
//                2-人脸识别失败,重新扫脸
//                4-人脸&证件比对失败,直接回到上传证件那一步
                when(result.getData()?.resultCode?.toInt()){
                    2 -> {// 仅面部识别失败
                        startActivity(LivenessResultActivity::class.java)
                    }
                    3 -> {// 识别成功

                        UserInfoRepository.loadUserInfo()
                        startActivity(Intent(this,VerifyIdentityActivity::class.java).apply {
                            putExtra("backToMainPage",true)
                        })
                    }
                    4 -> {// 人脸&证件比对失败
                        startActivity(LivenessResultActivity::class.java){
                            it.putExtra("tryAgain",false)
                        }
                    }
                    else -> {// 第一步就失败或者全部失败
                        startActivity(LivenessResultActivity::class.java)
                    }
                }
            }else{
              showErrorPromptDialog(result.msg){}
            }
        }
    }
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // 请务必保存 eventId,以便于我们进行问题排查
        val eventId = LivenessResult.getEventId();
        LogUtil.d("eventId=$eventId")
        if (LivenessResult.isSuccess()) {// 成功
            val livenessId = LivenessResult.getLivenessId();// livenessId
            LogUtil.d("livenessId=$livenessId")
            loading()
            viewModel.liveKycVerify(livenessId);
        } else {// 失败
              //取消认证，不提示错误
            val errorCode = LivenessResult.getErrorCode();// 错误码
            if (errorCode!="USER_GIVE_UP"){
                val errorMsg = LivenessResult.getErrorMsg();// 错误信息
                showErrorPromptDialog(if (errorMsg.isNullOrEmpty()) "$errorCode" else "$errorMsg"){}
                LogUtil.e("errorCode=$errorCode errorMsg=$errorMsg")
            }
        }
    }
    private fun initSDK(license: String){
        GuardianLivenessDetectionSDK.init(MultiLanguagesApplication.mInstance, Market.Philippines, false)
        GuardianLivenessDetectionSDK.letSDKHandleCameraPermission()
        GuardianLivenessDetectionSDK.setRecordVideoSwitch(true)
        GuardianLivenessDetectionSDK.isDetectOcclusion(true)
        GuardianLivenessDetectionSDK.setResultPictureSize(600)
        GuardianLivenessDetectionSDK.bindUser(UserInfoRepository.userInfo.value?.userId.toString())
        GuardianLivenessDetectionSDK.setCameraType(CameraType.FRONT)

        val checkResult = GuardianLivenessDetectionSDK.setLicenseAndCheck(license)
        LogUtil.d(license)
        LogUtil.d("checkResult=$checkResult")
        if ("SUCCESS" == checkResult) {
            GuardianLivenessDetectionSDK.setAuditImageConfig(
                AuditImageConfigBuilder()
                    .setEnableCollectSwitch(true) // 开启收集开关,默认值为false
                    .setImageCaptureInterval(400) // 捕获图像的最小间隔时间,默认间隔为400毫秒
                    .setImageMaxNumber(10) // 最大捕获图像数量,默认数量为10
                    .setImageWidth(400) // 图像宽度,默认为400像素
                    .setImageQuality(30) // 图像压缩质量,必须在[30,100]范围内,默认值为30
                    .build()
            )
            binding.btnStart.isEnabled = true
        } else {
            binding.btnStart.isEnabled = false
            // license 错误, 过期/格式错误/appId 不在授权范围
            showErrorPromptDialog(checkResult){}
        }
    }

}