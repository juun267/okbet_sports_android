package org.cxct.sportlottery.ui.chat

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.gyf.immersionbar.ImmersionBar
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentChatPhotoBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.view.PinchImageView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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
                val viewTarget =
                    object : CustomViewTarget<PinchImageView, Bitmap>(binding.ivPhoto) {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?,
                        ) {
                            startPostponedEnterTransition()
                            binding.ivPhoto.setImageBitmap(resource)
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {}

                        override fun onResourceCleared(placeholder: Drawable?) {}
                    }
                Glide.with(requireContext()).asBitmap().load(photoUrl).into(viewTarget)
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
        Thread {
            try {
                val bitmap = Glide.with(requireContext())
                    .asBitmap()
                    .load(photoUrl)
                    .submit()
                    .get()

                var file =
                    File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "Sport")
                if (!file.exists() && !file.mkdir()) {
//                    Log.d(TAG, " DIRECTORY_PICTURES mkdir() fail")
                    file =
                        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                            "Camera")
                    if (!file.exists() && !file.mkdir()) {
//                        Log.d(TAG, " DIRECTORY_DCIM  mkdir() fail")
                    }
                }

                val filename =
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date()) + ".jpg"

                val mediaFile = File(file.path + File.separator + filename)
                val fOut = FileOutputStream(mediaFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.flush()
                fOut.close()

                //保存图片后发送广播通知更新数据库
                MediaScannerConnection.scanFile(context, arrayOf(mediaFile.absolutePath), null,
                    object : MediaScannerConnection.MediaScannerConnectionClient {
                        override fun onScanCompleted(path: String?, uri: Uri?) {
//                            Log.d(TAG, "onMediaScannerConnected")
                        }

                        override fun onMediaScannerConnected() {
//                            Log.d(TAG, "onMediaScannerConnected")
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
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as ChatActivity).initToolbar()
    }

}