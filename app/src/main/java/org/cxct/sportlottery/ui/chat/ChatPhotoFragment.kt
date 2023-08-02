package org.cxct.sportlottery.ui.chat

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.gyf.immersionbar.ImmersionBar
import com.tbruyelle.rxpermissions2.RxPermissions
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.FragmentChatPhotoBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.menu.ScannerActivity
import org.cxct.sportlottery.util.DownloadUtil
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.ToastUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*


/**
 * @author kevin
 * @create 2023/3/30
 * @description
 */
class ChatPhotoFragment : BaseFragment<ChatViewModel>(ChatViewModel::class) {

    companion object {
        const val PHOTO_URL = "photo_url"

        @JvmStatic
        fun newInstance(url: String?) = ChatPhotoFragment().apply {
            arguments = Bundle().apply {
                putString(PHOTO_URL, url)
            }
        }
    }

    private lateinit var binding: FragmentChatPhotoBinding

    private lateinit var photoUrl: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentChatPhotoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoUrl = arguments?.getString(PHOTO_URL) ?: ""
        initBlur()
        initPhoto()
        initToolbar()
    }

    private fun initBlur() {
        binding.bvBlock.setupWith(activity?.window?.decorView?.rootView as ViewGroup)
            .setFrameClearDrawable(activity?.window?.decorView?.background)
            .setBlurRadius(4f)
    }

    private fun initPhoto() {
        try {
            if (photoUrl.isNotEmpty()) {
                binding.ivPhoto.load(photoUrl)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initToolbar() {
        ImmersionBar.with(requireActivity())
            .statusBarColor(R.color.transparent)
            .transparentStatusBar()
            .statusBarDarkFont(false)
            .fitsSystemWindows(false)
            .init()

        binding.ivDownload.setOnClickListener {
            saveToPictureFolder()
        }

        binding.ivClose.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    /**
     * 儲存到圖片庫
     */
    private fun saveToPictureFolder() {
        RxPermissions(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe { onNext ->
            if (onNext) {
                Thread {
                    try {
                        var file =
                            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                                "Sport")
                        if (!file.exists() && !file.mkdir()) {
                            file =
                                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                                    "Camera")
                            if (!file.exists() && !file.mkdir()) {
                            }
                        }


                        DownloadUtil.get().download(photoUrl,file.path ,object : DownloadUtil.OnDownloadListener{
                            override fun onDownloadSuccess(filePath:String) {
                                //保存图片后发送广播通知更新数据库
                                MediaScannerConnection.scanFile(context, arrayOf(filePath), null,
                                    object : MediaScannerConnection.MediaScannerConnectionClient {
                                        override fun onScanCompleted(path: String?, uri: Uri?) {
                                        }

                                        override fun onMediaScannerConnected() {
                                        }
                                    }
                                )

                                activity?.runOnUiThread {
                                    showPromptDialog(
                                        title = null,
                                        message = requireContext().getString(R.string.chat_photo_download_done),
                                        buttonText = null,
                                        isShowDivider = false
                                    ) {}
                                }
                            }

                            override fun onDownloading(progress: Int) {
//                        hideLoading()
                            }

                            override fun onDownloadFailed() {
//                        hideLoading()
                            }
                        })

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }.start()
            } else {
//                ToastUtil.showToast(
//                    requireContext(),
//                    LocalUtils.getString(R.string.N980)
//                )
            }
        }.isDisposed


    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as ChatActivity).initToolbar()
    }

}