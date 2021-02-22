package org.cxct.sportlottery.ui.main

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
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
class PopImageDialog(context: Context, val imageDataList: List<ImageData>) : AlertDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_pop_image)
        window?.setBackgroundDrawableResource(android.R.color.transparent)

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
                        JumpUtil.toExternalWeb(context, imageDataList[0].imageLink)
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

                Glide.with(context)
                    .load(url)
                    .apply(requestOptions)
                    .into(image)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}