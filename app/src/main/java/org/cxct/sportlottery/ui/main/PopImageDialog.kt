package org.cxct.sportlottery.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.dialog_pop_image.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.JumpUtil

/**
 * 顯示彈窗圖片資料
 */
//20210414 紀錄：體育暫不使用首頁彈窗圖功能
class PopImageDialog(val imageDataList: List<ImageData>) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.dialog_pop_image, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClose()
        initImage()
    }

    private fun setupClose() {
        btn_close.setOnClickListener {
            dismiss()
        }
    }

    private fun initImage() {
        try {
            if (imageDataList.isNotEmpty()) {
                //如果有 連結url, 點擊跳轉畫面
                image.setOnClickListener {
                    if (!imageDataList[0].imageLink.isNullOrEmpty()) {
                        JumpUtil.toExternalWeb(context ?: requireContext(), imageDataList[0].imageLink)
                    }
                }

                val requestOptions = RequestOptions()
                    .fitCenter()
                    .placeholder(R.drawable.ic_image_load)
                    .error(R.drawable.ic_image_broken)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .dontTransform()

                //加載圖片
                val url = sConfigData?.resServerHost + imageDataList[0].imageName1

                Glide.with(context ?: requireContext())
                    .load(url)
                    .apply(requestOptions)
                    .into(image)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}