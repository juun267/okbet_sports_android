package org.cxct.sportlottery.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.os.Build
import android.util.LruCache
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import com.caverock.androidsvg.SVG
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.jetbrains.annotations.NotNull
import timber.log.Timber
import java.lang.ref.WeakReference

object SvgUtil {
    const val defaultIconPath = "<g fill=\"none\" fill-rule=\"evenodd\"><circle fill=\"#0089E9\" cx=\"12\" cy=\"12\" r=\"12\"/><path fill=\"#A8EF84\" d=\"M6.984 2l1.033.84 2.183.441-.364 1.325-1.091.442-1.092 2.209-1.818 1.325-2.547.442v1.325l.728.884-.001.949L5.105 11l.762 1.655h2.666l1.143 2.07H11.2l-.38 2.068-1.525 1.655v1.242l-1.524 1.24V23l-1.142-.414-.762-2.069V16.38l-1.905-.413L3.2 14.31v-1.24l.384-.418-.66-.535-1.091-.883-.633-2.31C2.29 5.77 4.372 3.277 6.984 2zm9.55 4l.74 1.284 2.222.429 2.223-.857.11-.072c2.023 4.17 1.244 9.133-1.14 13.039l-.452-.979v-1.712l-.74-1.713-.741-1.713v-1.712l-1.112-.856-1.481.428-2.593-1.285-.37-2.997L14.311 6h2.222zm.524-3A11.124 11.124 0 0120.2 5.246L19.033 6l-1.944-.377-1.167-.377-1.555.377-1.167-.377.778-1.507h1.555L17.058 3z\"/><circle cx=\"12\" cy=\"12\" r=\"11.5\" stroke=\"#FFF\"/><path fill=\"#78D2F5\" fill-rule=\"nonzero\" d=\"M17.425 12.674a19.714 19.714 0 002.753-6.283 9.423 9.423 0 00-.418-.553.314.314 0 00-.145.2 19.086 19.086 0 01-2.548 6.032 14.643 14.643 0 00-4.489-5.854 13.75 13.75 0 014.976-2.093.314.314 0 00.21-.153 9.971 9.971 0 00-.617-.403 14.356 14.356 0 00-5.105 2.253 14.356 14.356 0 00-5.105-2.253 9.971 9.971 0 00-.618.403.314.314 0 00.21.153 13.75 13.75 0 014.977 2.093A14.467 14.467 0 008.192 9.84a19.646 19.646 0 00-5.966.636c-.03.182-.057.365-.077.55.074.054.17.075.265.05a19.022 19.022 0 015.403-.62 14.441 14.441 0 00-1.714 9.731c.2.15.406.292.617.426a19.672 19.672 0 0010.18-7.19 13.86 13.86 0 01.409 6.936.314.314 0 00.059.252c.21-.134.414-.275.613-.424a14.481 14.481 0 00-.39-6.665 19.091 19.091 0 013.376 2.796c.029.03.063.054.1.07.092-.199.177-.4.257-.606a19.723 19.723 0 00-3.9-3.109zM12.042 6.6a13.826 13.826 0 013.209 3.413c.44.665.82 1.358 1.138 2.074a19.652 19.652 0 00-3.663-1.486 19.438 19.438 0 00-3.81-.71 13.815 13.815 0 013.126-3.29zM6.703 19.961a13.815 13.815 0 011.826-9.47 18.88 18.88 0 017.99 2.384 19.045 19.045 0 01-9.816 7.086z\"/><path fill=\"#F8FF20\" fill-rule=\"nonzero\" d=\"M6.907 19.51a.91.91 0 010 1.824A.91.91 0 016 20.42a.91.91 0 01.907-.912zM17.694 19c.5 0 .908.409.908.912a.91.91 0 01-.908.912.91.91 0 01-.907-.912.91.91 0 01.907-.912zm3-4c.5 0 .908.409.908.912a.91.91 0 01-.908.912.91.91 0 01-.907-.912.91.91 0 01.907-.912zm-3.63-3a.91.91 0 01.907.912.91.91 0 01-.908.912.91.91 0 01-.907-.912.91.91 0 01.907-.912zM2.906 9.487a.91.91 0 01.908.912.91.91 0 01-.908.912.91.91 0 01-.907-.912.91.91 0 01.907-.912zm5.3-.287a.91.91 0 01.908.912.91.91 0 01-.908.912.91.91 0 01-.907-.912.91.91 0 01.907-.912zm4.135-4.188a.91.91 0 01.908.911.91.91 0 01-.908.912.91.91 0 01-.907-.912.91.91 0 01.907-.911zm7.468-.047c.5 0 .907.41.907.912a.91.91 0 01-.907.912.91.91 0 01-.908-.912.91.91 0 01.908-.912zm-2.103-2.14a.91.91 0 010 1.824.91.91 0 01-.907-.913.91.91 0 01.907-.912zm-10.664 0a.91.91 0 010 1.824.91.91 0 01-.907-.913.91.91 0 01.907-.912z\"/><path fill=\"#FFF\" fill-rule=\"nonzero\" d=\"M12 0C5.373 0 0 5.373 0 12s5.373 12 12 12 12-5.373 12-12S18.627 0 12 0zm0 .96C18.097.96 23.04 5.903 23.04 12c0 6.097-4.943 11.04-11.04 11.04C5.903 23.04.96 18.097.96 12 .96 5.903 5.903.96 12 .96z\"/></g>"

    private val svgDrawableCache = LruCache<String, WeakReference<Drawable>>(70)
    private val svgVideoCache = LruCache<String, WeakReference<SVGAVideoEntity>>(20)

    fun ImageView.setSvgIcon(svgData: String, @DrawableRes errorIcon: Int) {
        val cache = svgDrawableCache[svgData]?.get()
        if (cache != null) {
            setImageDrawable(cache)
            return
        }

        val svgIcon = getSvgDrawable(context, svgData)
        if (svgIcon == null) {
            setImageResource(errorIcon)
        } else {
            setImageDrawable(svgIcon)
            svgDrawableCache.put(svgData, WeakReference(svgIcon))
        }
    }
    fun SVGAImageView.setAssetSvgIcon(assetPath: String, autoPlay: Boolean = false) {
        val cache = svgVideoCache[assetPath]?.get()
        if (cache != null) {
            setVideoItem(cache)
            stepToFrame(0, autoPlay)
            return
        }
        SVGAParser(context).decodeFromAssets(assetPath, object : SVGAParser.ParseCompletion {
            @RequiresApi(api = Build.VERSION_CODES.P)
            override fun onComplete(@NotNull videoItem: SVGAVideoEntity) {
                svgVideoCache.put(assetPath,WeakReference(videoItem))
                setVideoItem(videoItem)
                stepToFrame(0, autoPlay)
            }
            override fun onError() {
            }
        })
    }
    fun getSvgDrawable(context: Context, svgData: String): PictureDrawable? {
        return svgFormatDataDrawable(context, svgData)
    }

    private fun svgFormatDataDrawable(context: Context, svgData: String): PictureDrawable? {
        val data = String.format(context.getString(R.string.svg_format), 48, 48, 24, 24, svgData)
        val svgFile = runCatching { SVG.getFromString(data) }.getOrNull() ?: return null
        return PictureDrawable(svgFile.renderToPicture())
    }

    fun getSvgDrawable(svgData: String): PictureDrawable?  {
        val svgFile = runCatching { SVG.getFromString(svgData) }.getOrNull() ?: return null
        return PictureDrawable(svgFile.renderToPicture())
    }
}