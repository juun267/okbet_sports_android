package org.cxct.sportlottery.util

import android.content.Context
import android.content.res.AssetManager
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

object LocalJsonReaderUtil {

     fun loadJSONFromAsset(context : Context, assetsPath : String): String {
        val stringBuilder = StringBuilder()
        try {
            //获取assets资源管理器
            val assetManager: AssetManager = context.assets

            //通过管理器打开文件并读取
            val bf = BufferedReader(InputStreamReader(assetManager.open(assetsPath)))
            var line = bf.readLine()
            while (line != null) {
                stringBuilder.append(line)
                line = bf.readLine()
            }
        } catch (e : IOException) {
            e.printStackTrace()
        }
        return stringBuilder.toString()
    }



}