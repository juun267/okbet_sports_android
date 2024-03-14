package org.cxct.sportlottery.ui.splash

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.annotation.RequiresApi
import com.xuexiang.xupdate.utils.ApkInstallUtils
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.databinding.DialogAppDownloadBinding
import org.cxct.sportlottery.network.appUpdate.CheckAppVersionResult
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.*
import java.io.File


class AppDownloadDialog: BaseDialog<BaseViewModel,DialogAppDownloadBinding>() {

    companion object{
        fun newInstance(mIsForce: Boolean,mLastVersion: String,checkAppVersionResult: CheckAppVersionResult?,apkFilePath: String)= AppDownloadDialog().apply{
            arguments = Bundle().apply {
                putBoolean("mIsForce",mIsForce)
                putString("mLastVersion",mLastVersion)
                putParcelable("checkAppVersionResult",checkAppVersionResult)
                putString("apkPath",apkFilePath)
            }
        }
    }
    private val mIsForce by lazy { requireArguments().getBoolean("mIsForce") }
    private val mLastVersion by lazy { requireArguments().getString("mLastVersion")!! }
    private val checkAppVersionResult by lazy { requireArguments().getParcelable<CheckAppVersionResult>("checkAppVersionResult") }
    private val apkPath by lazy { arguments?.getString("apkPath") }

    interface OnJumpToNextListener{
        fun onJump(clazz: Class<out Activity>)
    }

    override fun onInitView()=binding.run {
        isCancelable = false
        tvTitle.text = MultiLanguagesApplication.stringOf(R.string.find_new_version)
        btnCancel.text = MultiLanguagesApplication.stringOf(R.string.btn_pass)
        btnCancel.visibility = if (mIsForce) View.GONE else View.VISIBLE
        btnCancel.setOnClickListener {
            dismiss()
            val clazz = requireArguments().getSerializable("activity") as? Class<out Activity>
            clazz?.let {
                (requireActivity() as? OnJumpToNextListener)?.onJump(clazz)
            }
        }
            if (apkPath.isNullOrEmpty())
                btnDownload.text = MultiLanguagesApplication.stringOf(R.string.update)
            else
                btnDownload.text = MultiLanguagesApplication.stringOf(R.string.install)
        btnDownload.setOnClickListener {
            if (BuildConfig.FLAVOR != "google") {
                installApk()
            } else {
                checkAppVersionResult?.let {
                    try {
                        jumpMarketApp(requireContext(), it.storeURL ?: "")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        JumpUtil.toExternalWeb(requireContext(), it.storeURL1 ?: "")
                    }
                }
            }
        }

        blockProgressBar.visibility = View.GONE
        labelNewVersion.text = String.format(requireContext().getString(R.string.version_name), mLastVersion)
        tvCurrentVersion.text = "v${BuildConfig.VERSION_NAME}"
        tvNewVersion.text = "v${mLastVersion.split("_")[1]}"
        btnDownload.setTitleLetterSpacing()
        btnCancel.setTitleLetterSpacing()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun startInstallPermissionSettingActivity() {
        val packageURI: Uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
        //注意这个是8.0新API
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI)
        startActivity(intent)
    }

    //安裝更新
    private fun installApk() {
        ApkInstallUtils.install(requireContext(), File(apkPath))//填写文件所在的路径
    }

    fun jumpMarketApp(context: Context, url: String) {
        when (BuildConfig.FLAVOR) {
            "google" -> {
                runWithCatch {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(url)
                        setPackage("com.android.vending")
                    }
                    context.startActivity(intent)
                }
            }
            "huawei" -> {
                val intent = Intent("com.huawei.appmarket.intent.action.AppDetail")
                intent.setPackage("com.huawei.appmarket")
                intent.putExtra("APP_PACKAGENAME", context.packageName)
                context.startActivity(intent)
            }
            else -> {
                runWithCatch {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(url)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }
}