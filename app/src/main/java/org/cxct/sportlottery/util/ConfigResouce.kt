package org.cxct.sportlottery.util

import android.content.Context
import com.bumptech.glide.Glide
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.network.index.config.ConfigData
import org.cxct.sportlottery.network.index.config.ImageData

object ConfigResource {

    private val BANNER_SPLASH = 9           // 启动页轮播图
    private val HOME_AD = 7                 // 首页弹窗
    private val SPORT_AD = 14               // 体育弹窗
    private val OKGAME_AD = 16              // okgame 弹窗
    private val PROMOTIONS_AD = 5           // 优惠活动
    private val IMAGE_TYPE_SORT = arrayOf(BANNER_SPLASH, HOME_AD, SPORT_AD, OKGAME_AD, PROMOTIONS_AD) // 图片缓存下载顺序

    // 预下载图片资源
    fun preloadResource(config: ConfigData) {
        val host = config.resServerHost ?: return
        val imageList = config.imageList ?: return
        val context = MultiLanguagesApplication.appContext
        val language = LanguageManager.getSelectLanguage(context).key
        val tasks = mutableMapOf<Int, MutableList<String>>()
        imageList.forEach {
            if (!it.isHidden) {
                takeUrl(it, host, language)?.let { img->
                    val urlList = tasks[img.first] ?: mutableListOf<String>().apply { tasks[img.first] = this }
                    urlList.add(img.second)
                }
            }
        }

        if (tasks.isEmpty()) {
            return
        }

        IMAGE_TYPE_SORT.forEach {
            tasks[it]?.forEach { url-> preDownloadImg(context, url) }
        }
    }

    private fun takeUrl(imgData: ImageData, host: String, lang: String): Pair<Int, String>? {

        if (imgData.imageType == BANNER_SPLASH
            || imgData.imageType == HOME_AD
            || imgData.imageType == SPORT_AD
            || imgData.imageType == OKGAME_AD) {

            return if (imgData.lang != lang || imgData.imageName1.isNullOrEmpty()) {
                null
            } else  {
                Pair(imgData.imageType, host + imgData.imageName1)
            }
        }

        if (imgData.imageType == PROMOTIONS_AD) {
            return if(imgData.imageName3.isNullOrEmpty()) {
                null
            } else {
                Pair(PROMOTIONS_AD, host + imgData.imageName3)
            }
        }

        return null
    }

    private fun preDownloadImg(context: Context, url: String) {
        Glide.with(context).load(url).downloadOnly(Integer.MAX_VALUE, Integer.MAX_VALUE)
    }

}